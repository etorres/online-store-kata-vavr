package es.eriktorr.samples.resilient;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.eriktorr.samples.resilient.orders.domain.model.Order;
import es.eriktorr.samples.resilient.orders.domain.model.OrderId;
import es.eriktorr.samples.resilient.orders.domain.model.StoreId;
import es.eriktorr.samples.resilient.orders.domain.services.OrderProcessor;
import es.eriktorr.samples.resilient.orders.infrastructure.filesystem.OrderPathCreator;
import es.eriktorr.samples.resilient.orders.infrastructure.ws.ClientType;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;

import static es.eriktorr.samples.resilient.orders.infrastructure.ws.OrdersServiceClient.ORDERS_SERVICE_CLIENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.util.FileSystemUtils.deleteRecursively;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResilientSpringApplication.class, properties = {
        "orders.storage.path=${java.io.tmpdir}/resilient/${random.value}",
        "order1.uuid=${random.uuid}",
        "order2.uuid=${random.uuid}"
})
public class OrderProcessorTest {

    private static final String OK_STORE_ID = "store_ok";
    private static final String ERROR_STORE_ID = "store_error";

    private static final Order ORDER_1 = new Order(new OrderId("o1"), "Purchase includes a discount");
    private static final Order ORDER_2 = new Order(new OrderId("o2"), "The payment is pending");

    @Autowired @ClientType(ORDERS_SERVICE_CLIENT)
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderPathCreator orderPathCreator;

    @Autowired
    private OrderProcessor orderProcessor;

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
        val ordersJsonPayload = objectMapper.writeValueAsString(new HashSet<>(Arrays.asList(
                ORDER_1, ORDER_2
        )));
        givenGetOrdersFrom(OK_STORE_ID).andRespond(withSuccess(ordersJsonPayload, MediaType.APPLICATION_JSON));

        orderProcessor.processOrdersFrom(new StoreId(OK_STORE_ID));

        // TODO

        assertThatAFileWasCreatedFor(ORDER_1);
        assertThatAFileWasCreatedFor(ORDER_2);
    }

    @Test public void
    fail_to_fetch_orders_from_external_web_service() {
        givenGetOrdersFrom(ERROR_STORE_ID).andRespond(withServerError());

        orderProcessor.processOrdersFrom(new StoreId(ERROR_STORE_ID));

        // TODO
        assertThatFilesDoesNotExist(ORDER_1);
        assertThatFilesDoesNotExist(ORDER_2);
    }

    // TODO : fail to save orders to file-system

    private ResponseActions givenGetOrdersFrom(String storeId) {
        val url = String.format("%s/%s/orders", ordersServiceUrl, storeId);
        return mockRestServiceServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET));
    }

    private void assertThatAFileWasCreatedFor(Order order) throws IOException {
        val path = orderPathCreator.pathFrom(order);
        assertThat(Files.readAllLines(path).get(0)).isEqualTo(order.toString());
    }

    private void assertThatFilesDoesNotExist(Order order) {
        val path = orderPathCreator.pathFrom(order);
        assertThat(Files.exists(path)).isFalse();
    }

    /*
    * Deduplicate
    * Summary Log on termination
    * RetryProperties
    */

}