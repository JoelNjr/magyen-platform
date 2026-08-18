package com.magyen.platform.administration.domain;

import com.magyen.platform.administration.domain.exception.AdministrationDomainException;

import java.util.Objects;
import java.util.UUID;

/**
 * Entrada configurable de un catálogo de Administración (prenda, tela, cuello o manga).
 * <p>
 * El nombre es la etiqueta de negocio que Comercial persiste por snapshot.
 * Desactivar no borra la fila ni altera registros históricos.
 */
public class AdministrationCatalogEntry {

    private static final int MAX_NAME_LENGTH = 100;

    private final UUID id;
    private final AdministrationCatalogKind kind;
    private final String name;
    private final boolean active;

    private AdministrationCatalogEntry(
            UUID id,
            AdministrationCatalogKind kind,
            String name,
            boolean active
    ) {
        this.id = Objects.requireNonNull(id, "Catalog entry id must not be null");
        this.kind = Objects.requireNonNull(kind, "Catalog kind must not be null");
        this.name = requireName(name);
        this.active = active;
    }

    public static AdministrationCatalogEntry create(AdministrationCatalogKind kind, String name) {
        return new AdministrationCatalogEntry(UUID.randomUUID(), kind, name, true);
    }

    public static AdministrationCatalogEntry reconstitute(
            UUID id,
            AdministrationCatalogKind kind,
            String name,
            boolean active
    ) {
        return new AdministrationCatalogEntry(id, kind, name, active);
    }

    public AdministrationCatalogEntry activate() {
        if (active) {
            return this;
        }
        return new AdministrationCatalogEntry(id, kind, name, true);
    }

    public AdministrationCatalogEntry deactivate() {
        if (!active) {
            return this;
        }
        return new AdministrationCatalogEntry(id, kind, name, false);
    }

    public UUID getId() {
        return id;
    }

    public AdministrationCatalogKind getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        AdministrationCatalogEntry that = (AdministrationCatalogEntry) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new AdministrationDomainException("Catalog name must not be blank");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new AdministrationDomainException(
                    "Catalog name must not exceed " + MAX_NAME_LENGTH + " characters"
            );
        }
        return trimmed;
    }
}
