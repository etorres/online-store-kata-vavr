package es.eriktorr.samples.resilient.orders.domain.model;

import lombok.Value;

import java.util.Set;

@Value
public class Orders {

    private final Set<Order> orders;

}