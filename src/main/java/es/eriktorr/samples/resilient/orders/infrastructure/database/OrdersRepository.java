package es.eriktorr.samples.resilient.orders.infrastructure.database;

import es.eriktorr.samples.resilient.orders.domain.model.Order;
import es.eriktorr.samples.resilient.orders.domain.model.OrderId;
import io.vavr.control.Try;
import lombok.val;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OrdersRepository {

    private static final String INSERT_ORDER_SQL = "INSERT INTO orders (description) VALUES (:description)";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public OrdersRepository(JdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(namedParameterJdbcTemplate);
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
        val namedParameters = new MapSqlParameterSource("description", order.getDescription());
        namedParameterJdbcTemplate.update(INSERT_ORDER_SQL, namedParameters, generatedKeyHolder, new String[] { "id" });
        return Optional.ofNullable(generatedKeyHolder.getKey()).orElse(-1L).longValue();
    }

}