package com.magyen.platform.finance.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia para el agregado {@link RecurringFinancialObligation}.
 */
public interface RecurringFinancialObligationRepository {

    RecurringFinancialObligation save(RecurringFinancialObligation obligation);

    /**
     * Persiste cambios de una obligación existente.
     * <p>
     * Semánticamente equivalente a {@link #save(RecurringFinancialObligation)} en esta
     * implementación de infraestructura, pero mantiene la intención de actualización
     * en el port de dominio.
     */
    RecurringFinancialObligation update(RecurringFinancialObligation obligation);

    Optional<RecurringFinancialObligation> findById(UUID id);

    /**
     * Lista todas las obligaciones ordenadas por nombre y luego por id.
     */
    List<RecurringFinancialObligation> findAll();

    /**
     * Lista obligaciones activas ordenadas por nombre y luego por id.
     */
    List<RecurringFinancialObligation> findActive();
}
