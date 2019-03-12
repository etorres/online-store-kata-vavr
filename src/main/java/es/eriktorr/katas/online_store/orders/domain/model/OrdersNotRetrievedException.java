package es.eriktorr.katas.online_store.orders.domain.model;

public class OrdersNotRetrievedException extends RuntimeException {

    public OrdersNotRetrievedException(Throwable cause) {
        super(cause);
    }

}