package com.magyen.platform.inventory.infrastructure.persistence.sequence;

import com.magyen.platform.inventory.domain.PaperRollNumberGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Genera números de rollo Plotter usando la secuencia PostgreSQL {@code paper_roll_number_seq}.
 */
@Component
public class JdbcPaperRollNumberGenerator implements PaperRollNumberGenerator {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPaperRollNumberGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "JdbcTemplate must not be null");
    }

    @Override
    public String nextPaperRollNumber() {
        Long nextValue = jdbcTemplate.queryForObject("SELECT nextval('paper_roll_number_seq')", Long.class);
        if (nextValue == null) {
            throw new IllegalStateException("paper_roll_number_seq returned null");
        }
        return String.format("RP-%03d", nextValue);
    }
}
