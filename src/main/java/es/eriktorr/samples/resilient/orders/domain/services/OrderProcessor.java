package es.eriktorr.samples.resilient.orders.domain.services;

import es.eriktorr.samples.resilient.infrastructure.ws.OrdersServiceClient;
import es.eriktorr.samples.resilient.orders.domain.model.StoreId;
import lombok.val;

public class OrderProcessor {

    private final OrdersServiceClient ordersServiceClient;

    public OrderProcessor(OrdersServiceClient ordersServiceClient) {
        this.ordersServiceClient = ordersServiceClient;
    }

    public void processOrdersFrom(StoreId storeId) {
        val orders = ordersServiceClient.ordersFrom(storeId);

        System.err.println("\n\n >> HERE: " + orders + "\n");

        // fetchOrdersFromWebService

        // saveOrderInFileSystem

        // auditOrderInDatabase

        // writeMessageToLoggingSystem


        // TODO

        throw new IllegalStateException("feature under development");
    }

}