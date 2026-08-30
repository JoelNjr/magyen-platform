package com.magyen.platform.config.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CorsAllowedOriginsTest {

    @Test
    void parsesLocalDevelopmentDefault() {
        assertEquals(List.of("http://localhost:5173"), CorsAllowedOrigins.parse("http://localhost:5173"));
    }

    @Test
    void parsesCommaSeparatedProductionOrigins() {
        assertEquals(
                List.of(
                        "https://magyen.com",
                        "https://www.magyen.com",
                        "https://magyen-platform-frontend.onrender.com"
                ),
                CorsAllowedOrigins.parse(
                        "https://magyen.com,https://www.magyen.com,https://magyen-platform-frontend.onrender.com"
                )
        );
    }

    @Test
    void trimsWhitespaceAroundOrigins() {
        assertEquals(
                List.of("https://magyen.com", "https://www.magyen.com"),
                CorsAllowedOrigins.parse(" https://magyen.com , https://www.magyen.com ")
        );
    }

    @Test
    void rejectsWildcard() {
        assertThrows(IllegalArgumentException.class, () -> CorsAllowedOrigins.parse("*"));
        assertThrows(
                IllegalArgumentException.class,
                () -> CorsAllowedOrigins.parse("https://magyen.com,*")
        );
    }

    @Test
    void rejectsBlankAndInvalidSchemes() {
        assertThrows(IllegalArgumentException.class, () -> CorsAllowedOrigins.parse(""));
        assertThrows(IllegalArgumentException.class, () -> CorsAllowedOrigins.parse("   "));
        assertThrows(IllegalArgumentException.class, () -> CorsAllowedOrigins.parse("magyen.com"));
    }
}
