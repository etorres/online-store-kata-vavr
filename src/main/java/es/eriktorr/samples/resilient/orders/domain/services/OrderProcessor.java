package es.eriktorr.samples.resilient.orders.domain.services;

import es.eriktorr.samples.resilient.orders.domain.model.StoreId;

public class OrderProcessor {

    public void processOrdersFrom(StoreId storeId) {
        throw new IllegalStateException("feature under development");
    }

}
