package com.magyen.platform.administration.domain.exception;

/**
 * Ya existe una entrada activa o inactiva con el mismo nombre en ese catálogo.
 */
public class CatalogNameAlreadyExistsException extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "Catalog name already exists.";

    public CatalogNameAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }
}
