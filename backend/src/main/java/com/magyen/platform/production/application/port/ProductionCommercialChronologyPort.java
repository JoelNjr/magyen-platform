package com.magyen.platform.production.application.port;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Application para leer fechas comerciales sin acoplar JPA cruzado.
 * <p>
 * Si la orden comercial no existe (p. ej. fixtures de prueba aislados),
 * la cronología comercial se omite y solo aplican reglas internas de producción.
 */
public interface ProductionCommercialChronologyPort {

    Optional<CommercialOrderChronology> findChronology(UUID commercialOrderId);
}
