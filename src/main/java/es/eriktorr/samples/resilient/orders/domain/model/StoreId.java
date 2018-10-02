package es.eriktorr.samples.resilient.orders.domain.model;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
public class StoreId {

    String value;

}