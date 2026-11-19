package org.ikigaidigital.integration;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.ikigaidigital.infrastructure.adapter.in.web.generated.model.TimeDepositResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestPropertySource(properties = {
    "spring.flyway.locations=classpath:db/migration",
    "spring.jpa.properties.hibernate.generate_statistics=true"
})
class TimeDepositApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE withdrawals, \"timeDeposits\" RESTART IDENTITY CASCADE");
    }

    @Test
    @DisplayName("GET /api/v1/time-deposits returns empty JSON array when no deposits exist")
    void returnsEmptyListWhenDatabaseIsEmpty() {
        ResponseEntity<List<TimeDepositResponse>> response = restTemplate.exchange(
            "/api/v1/time-deposits",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("GET /api/v1/time-deposits returns all deposits with exact schema and nested withdrawals")
    void returnsAllDepositsWithWithdrawals() {
        jdbcTemplate.update(
            "INSERT INTO \"timeDeposits\" (id, \"planType\", days, balance) VALUES (?, ?, ?, ?)",
            1, "basic", 45, new BigDecimal("1234567.00")
        );
        jdbcTemplate.update(
            "INSERT INTO \"timeDeposits\" (id, \"planType\", days, balance) VALUES (?, ?, ?, ?)",
            2, "premium", 60, new BigDecimal("50000.00")
        );
        jdbcTemplate.update(
            "INSERT INTO withdrawals (id, \"timeDepositId\", amount, date) VALUES (?, ?, ?, ?)",
            10, 1, new BigDecimal("500.00"), LocalDate.of(2026, 1, 15)
        );
        jdbcTemplate.update(
            "INSERT INTO withdrawals (id, \"timeDepositId\", amount, date) VALUES (?, ?, ?, ?)",
            11, 1, new BigDecimal("250.50"), LocalDate.of(2026, 2, 1)
        );

        ResponseEntity<List<TimeDepositResponse>> response = restTemplate.exchange(
            "/api/v1/time-deposits",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<TimeDepositResponse> body = response.getBody();
        assertThat(body).hasSize(2);

        TimeDepositResponse basic = body.get(0);
        assertThat(basic.getId()).isEqualTo(1);
        assertThat(basic.getPlanType()).isEqualTo("basic");
        assertThat(basic.getDays()).isEqualTo(45);
        assertThat(basic.getBalance()).isEqualByComparingTo("1234567.00");
        assertThat(basic.getWithdrawals()).hasSize(2);
        assertThat(basic.getWithdrawals().get(0).getId()).isEqualTo(10);
        assertThat(basic.getWithdrawals().get(0).getAmount()).isEqualByComparingTo("500.00");
        assertThat(basic.getWithdrawals().get(0).getDate()).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(basic.getWithdrawals().get(1).getId()).isEqualTo(11);
        assertThat(basic.getWithdrawals().get(1).getAmount()).isEqualByComparingTo("250.50");

        TimeDepositResponse premium = body.get(1);
        assertThat(premium.getId()).isEqualTo(2);
        assertThat(premium.getPlanType()).isEqualTo("premium");
        assertThat(premium.getWithdrawals()).isEmpty();
    }

    @Test
    @DisplayName("POST /api/v1/time-deposits/balances/update updates balances in PostgreSQL and returns 204 No Content")
    void updatesBalancesTransactionallyAndReturns204() {
        jdbcTemplate.update(
            "INSERT INTO \"timeDeposits\" (id, \"planType\", days, balance) VALUES "
                + "(1, 'basic', 30, 1200.00), "
                + "(2, 'basic', 45, 1234567.00), "
                + "(3, 'student', 365, 1200.00), "
                + "(4, 'student', 366, 1200.00), "
                + "(5, 'premium', 45, 1200.00), "
                + "(6, 'premium', 46, 1200.00)"
        );
        jdbcTemplate.update(
            "INSERT INTO withdrawals (id, \"timeDepositId\", amount, date) VALUES (?, ?, ?, ?)",
            1, 2, new BigDecimal("100.00"), LocalDate.of(2026, 1, 10)
        );

        ResponseEntity<Void> firstUpdate = restTemplate.postForEntity(
            "/api/v1/time-deposits/balances/update",
            null,
            Void.class
        );
        assertThat(firstUpdate.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(firstUpdate.getBody()).isNull();

        Map<Integer, TimeDepositResponse> afterFirst = fetchAllById();
        assertThat(afterFirst.get(1).getBalance()).isEqualByComparingTo("1200.00");
        assertThat(afterFirst.get(2).getBalance()).isEqualByComparingTo("1235595.81");
        assertThat(afterFirst.get(2).getWithdrawals()).hasSize(1);
        assertThat(afterFirst.get(3).getBalance()).isEqualByComparingTo("1203.00");
        assertThat(afterFirst.get(4).getBalance()).isEqualByComparingTo("1200.00");
        assertThat(afterFirst.get(5).getBalance()).isEqualByComparingTo("1200.00");
        assertThat(afterFirst.get(6).getBalance()).isEqualByComparingTo("1205.00");

        // Second POST verifies documented non-idempotent monthly interest accrual
        ResponseEntity<Void> secondUpdate = restTemplate.postForEntity(
            "/api/v1/time-deposits/balances/update",
            null,
            Void.class
        );
        assertThat(secondUpdate.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Map<Integer, TimeDepositResponse> afterSecond = fetchAllById();
        // 1205.00 * 0.05 / 12 = 5.0208... -> 5.02 -> 1210.02
        assertThat(afterSecond.get(6).getBalance()).isEqualByComparingTo("1210.02");
        assertThat(afterSecond.get(1).getBalance()).isEqualByComparingTo("1200.00");
    }

    @Test
    @DisplayName("POST /api/v1/time-deposits/balances/update preserves NUMERIC(19,2) precision without losing cents")
    void preservesNumericPrecisionInDatabaseDuringBalanceUpdate() {
        jdbcTemplate.update(
            "INSERT INTO \"timeDeposits\" (id, \"planType\", days, balance) VALUES (?, ?, ?, ?), (?, ?, ?, ?)",
            1, "basic", 20, new BigDecimal("1000000000000000.01"),
            2, "basic", 45, new BigDecimal("1000000000000000.01")
        );

        ResponseEntity<Void> response = restTemplate.postForEntity(
            "/api/v1/time-deposits/balances/update",
            null,
            Void.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Map<Integer, TimeDepositResponse> updated = fetchAllById();
        assertThat(updated.get(1).getBalance()).isEqualByComparingTo("1000000000000000.01");
        assertThat(updated.get(2).getBalance()).isEqualByComparingTo("1000833333333333.34");
    }

    @Test
    @DisplayName("GET /api/v1/time-deposits loads deposits and withdrawals in a single SQL query (no N+1)")
    void loadsDepositsAndWithdrawalsInSingleQuery() {
        for (int i = 1; i <= 5; i++) {
            jdbcTemplate.update(
                "INSERT INTO \"timeDeposits\" (id, \"planType\", days, balance) VALUES (?, ?, ?, ?)",
                i, "basic", 45, new BigDecimal("1000.00")
            );
            jdbcTemplate.update(
                "INSERT INTO withdrawals (\"timeDepositId\", amount, date) VALUES (?, ?, ?), (?, ?, ?)",
                i, new BigDecimal("50.00"), LocalDate.of(2026, 1, 1),
                i, new BigDecimal("75.00"), LocalDate.of(2026, 2, 1)
            );
        }

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        List<TimeDepositResponse> deposits = restTemplate.exchange(
            "/api/v1/time-deposits",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<TimeDepositResponse>>() {}
        ).getBody();

        assertThat(deposits).hasSize(5);
        assertThat(deposits).allSatisfy(td -> assertThat(td.getWithdrawals()).hasSize(2));
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Serves contract-first OpenAPI YAML and Swagger UI")
    void servesOpenApiContractAndSwaggerUi() {
        ResponseEntity<String> openApiSpec = restTemplate.getForEntity(
            "/openapi/time-deposit-api.yaml",
            String.class
        );
        assertThat(openApiSpec.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(openApiSpec.getBody())
            .contains("/api/v1/time-deposits:")
            .contains("/api/v1/time-deposits/balances/update:");

        ResponseEntity<String> swaggerUi = restTemplate.getForEntity("/swagger-ui/index.html", String.class);
        assertThat(swaggerUi.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> swaggerConfig = restTemplate.getForEntity("/v3/api-docs/swagger-config", String.class);
        assertThat(swaggerConfig.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(swaggerConfig.getBody()).contains("/openapi/time-deposit-api.yaml");
    }

    private Map<Integer, TimeDepositResponse> fetchAllById() {
        List<TimeDepositResponse> deposits = restTemplate.exchange(
            "/api/v1/time-deposits",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<TimeDepositResponse>>() {}
        ).getBody();
        assertThat(deposits).isNotNull();
        return deposits.stream().collect(Collectors.toMap(TimeDepositResponse::getId, Function.identity()));
    }
}
