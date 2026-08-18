package com.magyen.platform.inventory.infrastructure.persistence.sequence;

import com.magyen.platform.inventory.domain.MaterialCode;
import com.magyen.platform.inventory.domain.MaterialCodeGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Genera códigos de material usando la secuencia PostgreSQL {@code material_code_seq}.
 */
@Component
public class JdbcMaterialCodeGenerator implements MaterialCodeGenerator {

    private final JdbcTemplate jdbcTemplate;

    public JdbcMaterialCodeGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "JdbcTemplate must not be null");
    }

    @Override
    public MaterialCode nextMaterialCode() {
        Long nextValue = jdbcTemplate.queryForObject("SELECT nextval('material_code_seq')", Long.class);
        if (nextValue == null) {
            throw new IllegalStateException("material_code_seq returned null");
        }
        return MaterialCode.of(String.format("MAT-%03d", nextValue));
    }
}
