package es.eriktorr.samples.resilient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.eriktorr.samples.resilient.orders.infrastructure.ws.ClientType;
import es.eriktorr.samples.resilient.orders.domain.model.Order;
import es.eriktorr.samples.resilient.orders.domain.model.OrderId;
import es.eriktorr.samples.resilient.orders.domain.model.Orders;
import es.eriktorr.samples.resilient.orders.domain.model.StoreId;
import es.eriktorr.samples.resilient.orders.domain.services.OrderProcessor;
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
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

import java.util.Arrays;
import java.util.HashSet;

import static es.eriktorr.samples.resilient.orders.infrastructure.ws.OrdersServiceClient.ORDERS_SERVICE_CLIENT;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResilientSpringApplication.class)
public class OrderProcessorTest {

    private static final String OK_STORE_ID = "store_ok";
    private static final String ERROR_STORE_ID = "store_error";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Autowired @ClientType(ORDERS_SERVICE_CLIENT)
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderProcessor orderProcessor;

    @Value("${orders.service.url}")
    private String ordersServiceUrl;

    private MockRestServiceServer mockRestServiceServer;

    @Before
    public void setUp() throws JsonProcessingException {
        val ordersJsonPayload = objectMapper.writeValueAsString(new Orders(new HashSet<>(Arrays.asList(
                new Order(new OrderId("o1"), "Purchase includes a discount"),
                new Order(new OrderId("o2"), "The payment is pending")
        ))));
        mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
        givenGetOrdersFrom(OK_STORE_ID).andRespond(withSuccess(ordersJsonPayload, MediaType.APPLICATION_JSON));
        givenGetOrdersFrom(ERROR_STORE_ID).andRespond(withServerError());
    }

    @Test
    public void
    process_orders_from_store() {
        orderProcessor.processOrdersFrom(new StoreId(OK_STORE_ID));

        // TODO
        // check second-order effects
    }

    @Test public void
    fail_to_fetch_orders_from_external_web_service() {
        orderProcessor.processOrdersFrom(new StoreId(ERROR_STORE_ID));

        // TODO
    }

    // TODO : fail to save orders to file-system

    private ResponseActions givenGetOrdersFrom(String storeId) {
        val url = String.format("%s/%s/orders", ordersServiceUrl, storeId);
        return mockRestServiceServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET));
    }

}