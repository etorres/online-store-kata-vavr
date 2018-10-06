package es.eriktorr.samples.resilient.orders.infrastructure.database;

import es.eriktorr.samples.resilient.orders.domain.model.Order;
import es.eriktorr.samples.resilient.orders.domain.model.OrderReference;
import es.eriktorr.samples.resilient.orders.domain.model.StoreId;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;

@Repository
public class OrdersRepository {

    private static final String INSERT_ORDER_SQL = "INSERT INTO orders (id, store, reference, description) VALUES (?, ?, ?, ?)";
    private static final String FIND_ORDER_BY_STORE_REFERENCE_SQL = "SELECT id, store, reference, description FROM orders " +
            "WHERE store = ? AND reference = ?";

    private final JdbcTemplate jdbcTemplate;

    public OrdersRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Order order) {
        jdbcTemplate.update(INSERT_ORDER_SQL, order.getOrderId().getValue(), order.getStoreId().getValue(),
                order.getOrderReference().getValue(), order.getDescription());
    }

    public Order findBy(StoreId storeId, OrderReference orderReference) {
        try {
            return jdbcTemplate.queryForObject(FIND_ORDER_BY_STORE_REFERENCE_SQL,
                    new Object[]{ storeId.getValue(), orderReference.getValue() },
                    new int[]{ Types.VARCHAR, Types.VARCHAR },
                    new OrderRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return Order.INVALID;
        }
    }

}