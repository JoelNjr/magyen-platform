package com.magyen.platform.commercial.application.port;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Gastos manuales de Finance ya atribuidos a una Orden.
 * <p>
 * Es el mismo movimiento del ledger. No crea un costo de producción paralelo.
 */
public interface OrderAttributedExpensePort {

    BigDecimal sumManualExpenses(UUID orderId);
}
