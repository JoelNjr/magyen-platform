package com.magyen.platform.commercial.infrastructure.persistence;

import com.magyen.platform.commercial.domain.QuotationNumber;
import com.magyen.platform.commercial.domain.QuotationNumberGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * PostgreSQL adapter that obtains the next commercial quotation number from
 * {@code quotation_number_seq}.
 */
@Repository
public class PostgresQuotationNumberGenerator implements QuotationNumberGenerator {

    private static final String NEXT_QUOTATION_NUMBER_SQL =
            "SELECT nextval('quotation_number_seq')";

    private final JdbcTemplate jdbcTemplate;

    public PostgresQuotationNumberGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "JdbcTemplate must not be null");
    }

    @Override
    public QuotationNumber next() {
        Long nextValue = jdbcTemplate.queryForObject(NEXT_QUOTATION_NUMBER_SQL, Long.class);
        Objects.requireNonNull(nextValue, "Sequence nextval must not return null");
        return QuotationNumber.of(nextValue);
    }
}
