package com.magyen.platform.administration.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del usuario inicial de desarrollo.
 * <p>
 * Los valores vienen de entorno. Nunca se registran passwords ni secretos.
 */
@ConfigurationProperties(prefix = "magyen.security.bootstrap")
public class AuthenticationBootstrapProperties {

    private boolean enabled = false;
    private String username = "";
    private String password = "";
    private String role = "ADMIN";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? "" : username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? "" : password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = (role == null || role.isBlank()) ? "ADMIN" : role;
    }
}
