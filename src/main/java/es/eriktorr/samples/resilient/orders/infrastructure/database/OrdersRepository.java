package es.eriktorr.samples.resilient.orders.infrastructure.database;

import es.eriktorr.samples.resilient.orders.domain.model.Order;
import es.eriktorr.samples.resilient.orders.domain.model.OrderId;
import io.vavr.control.Try;
import lombok.val;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OrdersRepository {

    private static final String INSERT_ORDER_SQL = "INSERT INTO orders (description) VALUES (?)";

    private final JdbcTemplate jdbcTemplate;

    public OrdersRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Try<Order> save(Order order) {
        return Try.of(() -> {
            val generatedKey = insertIntoDatabase(order);
            return new Order(
                    new OrderId(Long.toString(generatedKey)),
                    order.getDescription()
            );
        });
    }

    private long insertIntoDatabase(Order order) {
        val generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            val preparedStatement = connection.prepareStatement(INSERT_ORDER_SQL);
            preparedStatement.setString(1, order.getDescription());
            return preparedStatement;
        }, generatedKeyHolder);
        return (long) generatedKeyHolder.getKey();
    }

}