package com.magyen.platform.config.security;

import java.util.Arrays;
import java.util.List;

/**
 * Parsea orígenes CORS explícitos desde una lista separada por comas.
 * No admite comodín.
 */
public final class CorsAllowedOrigins {

    private CorsAllowedOrigins() {
    }

    public static List<String> parse(String rawOrigins) {
        if (rawOrigins == null || rawOrigins.isBlank()) {
            throw new IllegalArgumentException("CORS allowed origins must not be blank");
        }

        List<String> origins = Arrays.stream(rawOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();

        if (origins.isEmpty()) {
            throw new IllegalArgumentException("CORS allowed origins must not be blank");
        }

        for (String origin : origins) {
            if ("*".equals(origin)) {
                throw new IllegalArgumentException("CORS allowed origins must be explicit; wildcard is not allowed");
            }
            if (!origin.startsWith("http://") && !origin.startsWith("https://")) {
                throw new IllegalArgumentException("CORS allowed origin must start with http:// or https://: " + origin);
            }
        }

        return List.copyOf(origins);
    }
}
