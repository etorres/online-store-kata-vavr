package es.eriktorr.katas.online_store;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.eriktorr.katas.online_store.configuration.RestClientType;
import es.eriktorr.katas.online_store.orders.domain.model.*;
import es.eriktorr.katas.online_store.orders.domain.services.OrderProcessor;
import es.eriktorr.katas.online_store.orders.infrastructure.database.OrdersRepository;
import es.eriktorr.katas.online_store.orders.infrastructure.filesystem.OrderPathCreator;
import lombok.val;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import static es.eriktorr.katas.online_store.orders.infrastructure.ws.OrdersServiceClient.ORDERS_SERVICE_CLIENT;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.util.FileSystemUtils.deleteRecursively;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResilientSpringApplication.class, properties = {
        "orders.storage.path=${java.io.tmpdir}/katas/online_store/${random.value}"
})
public class OrderProcessorTest {

    private static final String STORE_ID_1 = "store1";
    private static final String NO_STORE_ID = "no_store";

    private static final String EXISTING_ORDER_ID = "00000000-0000-0000-0000-000000000000";

    @Autowired @RestClientType(ORDERS_SERVICE_CLIENT)
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderPathCreator orderPathCreator;

    @Autowired
    private OrderProcessor orderProcessor;

    @Autowired
    private OrdersRepository ordersRepository;

    @MockBean
    private OrderIdGenerator orderIdGenerator;

    @Value("${orders.service.url}")
    private String ordersServiceUrl;

    private MockRestServiceServer mockRestServiceServer;

    @Before
    public void setUp() {
        mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
    }

    @After
    public void cleanUp() {
        deleteRecursively(new File(orderPathCreator.ordersStoragePath));
    }

    @Test
    public void
    process_orders_from_store() throws IOException {
        final String uuid1 = randomUUID().toString(), uuid2 = randomUUID().toString();
        final OrderId orderId1 = new OrderId(uuid1), orderId2 = new OrderId(uuid2), orderId3 = new OrderId(EXISTING_ORDER_ID);
        final Order order1 = order1(STORE_ID_1, uuid1), order2 = order2(STORE_ID_1, uuid2), order3 = duplicateOrder();
        val ordersJsonPayload = objectMapper.writeValueAsString(new LinkedHashSet<>(Arrays.asList(
                order1, order2, order3
        )));
        givenGetOrdersFrom(STORE_ID_1).andRespond(withSuccess(ordersJsonPayload, MediaType.APPLICATION_JSON));
        given(orderIdGenerator.nextOrderId()).willReturn(orderId1, orderId2, orderId3);

        orderProcessor.processOrdersFrom(new StoreId(STORE_ID_1));

        assertThatAFileWasCreatedFor(
                Order.from(orderId1, order1),
                Order.from(orderId2, order2)
        );
        assertThatNoFileExistFor(
                Order.from(orderId3, order3)
        );
        assertThatARecordWasInserted(
                Order.from(orderId1, order1),
                Order.from(orderId2, order2)
        );
    }

    @Test public void
    fail_to_fetch_orders_from_external_web_service() {
        final Order order1 = order1(NO_STORE_ID, randomUUID().toString()),
                order2 = order2(NO_STORE_ID, randomUUID().toString());
        givenGetOrdersFrom(NO_STORE_ID).andRespond(withServerError());

        orderProcessor.processOrdersFrom(new StoreId(NO_STORE_ID));

        assertThatNoFileExistFor(order1, order2);
        assertThatNoRecordWasInserted(order1, order2);
    }

    @Test public void
    fail_to_save_orders_to_file_system() throws IOException {
        val uuid1 = randomUUID().toString();
        val orderId1 = new OrderId(uuid1);
        val order1 = order1(STORE_ID_1, uuid1);
        val ordersJsonPayload = objectMapper.writeValueAsString(new LinkedHashSet<>(Collections.singletonList(order1)));
        givenGetOrdersFrom(STORE_ID_1).andRespond(withSuccess(ordersJsonPayload, MediaType.APPLICATION_JSON));
        given(orderIdGenerator.nextOrderId()).willReturn(orderId1);

        val pathToOrder1 = orderPathCreator.pathFrom(order1);
        Files.createDirectories(pathToOrder1.getParent());
        Files.createFile(pathToOrder1);

        try (val file = new RandomAccessFile(pathToOrder1.toFile(), "rw"); val fileChannel = file.getChannel();
             val fileLock = fileChannel.tryLock()) {
            if (fileLock == null) fail("failed to acquire file lock");
            orderProcessor.processOrdersFrom(new StoreId(STORE_ID_1));
        }

        assertThat(pathToOrder1.toFile().length()).isEqualTo(0L);
        assertThatNoRecordWasInserted(order1);
    }

    private ResponseActions givenGetOrdersFrom(String storeId) {
        val url = String.format("%s/%s/orders", ordersServiceUrl, storeId);
        return mockRestServiceServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET));
    }

    private void assertThatAFileWasCreatedFor(Order... orders) {
        val softAssertions = new SoftAssertions();
        Arrays.stream(orders).forEachOrdered(order -> {
            val path = orderPathCreator.pathFrom(order);
            softAssertions.assertThat(readAllLinesFrom(path)).isEqualTo(order.toString());
        });
        softAssertions.assertAll();
    }

    private String readAllLinesFrom(Path path) {
        try {
            return Files.readAllLines(path).get(0);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void assertThatNoFileExistFor(Order... orders) {
        val softAssertions = new SoftAssertions();
        Arrays.stream(orders).forEachOrdered(order -> {
            val path = orderPathCreator.pathFrom(order);
            softAssertions.assertThat(Files.exists(path)).isFalse();
        });
        softAssertions.assertAll();
    }

    private void assertThatARecordWasInserted(Order... orders) {
        val softAssertions = new SoftAssertions();
        Arrays.stream(orders).forEachOrdered(order -> {
            val orderRecord = ordersRepository.findBy(order.getStoreId(), order.getOrderReference());
            softAssertions.assertThat(orderRecord).isEqualTo(order);
        });
        softAssertions.assertAll();
    }

    private void assertThatNoRecordWasInserted(Order... orders) {
        val softAssertions = new SoftAssertions();
        Arrays.stream(orders).forEachOrdered(order -> {
            val orderRecord = ordersRepository.findBy(order.getStoreId(), order.getOrderReference());
            softAssertions.assertThat(orderRecord).isEqualTo(Order.INVALID);
        });
        softAssertions.assertAll();
    }

    private Order order1(String storeId, String uuid) {
        return new Order(null,
                new StoreId(storeId),
                new OrderReference(uuid),
                "Purchase includes a discount");
    }

    private Order order2(String storeId, String uuid) {
        return new Order(null,
                new StoreId(storeId),
                new OrderReference(uuid),
                "The payment is pending");
    }

    private Order duplicateOrder() {
        return new Order(null,
                new StoreId(STORE_ID_1),
                new OrderReference(EXISTING_ORDER_ID),
                null);
    }

}