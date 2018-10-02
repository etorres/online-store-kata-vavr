package es.eriktorr.samples.resilient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.eriktorr.samples.resilient.orders.domain.model.Order;
import es.eriktorr.samples.resilient.orders.domain.model.OrderId;
import es.eriktorr.samples.resilient.orders.domain.model.Orders;
import es.eriktorr.samples.resilient.orders.domain.model.StoreId;
import es.eriktorr.samples.resilient.orders.domain.services.OrderProcessor;
import io.vavr.collection.HashSet;
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResilientSpringApplication.class)
public class OrderProcessorTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderProcessor orderProcessor;

    @Before
    public void setUp() throws JsonProcessingException {
        val ordersJsonPayload = objectMapper.writeValueAsString(new Orders(HashSet.of(
                new Order(new OrderId("o1")),
                new Order(new OrderId("o2"))
        )));
        val mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
        mockRestServiceServer.expect(requestTo("/store1/orders"))
                .andRespond(withSuccess(ordersJsonPayload, MediaType.APPLICATION_JSON));
    }

    @Test
    public void
    process_orders_from_store() {
        orderProcessor.processOrdersFrom(new StoreId("store1"));
    }

}