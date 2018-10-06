package es.eriktorr.samples.resilient.orders.infrastructure.database;

import es.eriktorr.samples.resilient.orders.domain.model.Order;
import es.eriktorr.samples.resilient.orders.domain.model.OrderId;
import es.eriktorr.samples.resilient.orders.domain.model.OrderReference;
import es.eriktorr.samples.resilient.orders.domain.model.StoreId;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderRowMapper implements RowMapper<Order> {

    @Override
    public Order mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
        return new Order(
                new OrderId(resultSet.getString("id")),
                new StoreId(resultSet.getString("store")),
                new OrderReference(resultSet.getString("reference")),
                resultSet.getString("description")
        );
    }

}