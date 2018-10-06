package es.eriktorr.samples.resilient.orders.infrastructure.database;

import es.eriktorr.samples.resilient.orders.domain.model.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OrdersRepository {

    private static final String INSERT_ORDER_SQL = "INSERT INTO orders (id, store, reference, description) VALUES (?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    public OrdersRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Order order) {
        jdbcTemplate.update(INSERT_ORDER_SQL, order.getOrderId().getValue(), order.getStoreId().getValue(),
                order.getOrderReference().getValue(), order.getDescription());
    }

}