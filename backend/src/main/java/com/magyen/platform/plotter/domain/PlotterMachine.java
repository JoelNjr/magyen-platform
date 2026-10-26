package com.magyen.platform.plotter.domain;

import com.magyen.platform.plotter.domain.exception.PlotterDomainException;

import java.util.Objects;
import java.util.UUID;

/**
 * Máquina física de Plotter.
 * <p>
 * El identificador es estable. El nombre solo se muestra. Una máquina inactiva
 * no recibe trabajos nuevos; los trabajos ya registrados conservan su referencia.
 */
public class PlotterMachine {

    private final UUID id;
    private final String name;
    private boolean active;
    private final boolean defaultForRegistration;

    private PlotterMachine(UUID id, String name, boolean active, boolean defaultForRegistration) {
        this.id = Objects.requireNonNull(id, "Plotter machine id must not be null");
        if (name == null || name.isBlank()) {
            throw new PlotterDomainException("Plotter machine name must not be blank");
        }
        this.name = name.trim();
        this.active = active;
        this.defaultForRegistration = defaultForRegistration;
    }

    public static PlotterMachine reconstitute(
            UUID id,
            String name,
            boolean active,
            boolean defaultForRegistration
    ) {
        return new PlotterMachine(id, name, active, defaultForRegistration);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isDefaultForRegistration() {
        return defaultForRegistration;
    }
}
