package com.magyen.platform.plotter.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia para {@link PlotterPayment}.
 */
public interface PlotterPaymentRepository {

    PlotterPayment save(PlotterPayment payment);

    Optional<PlotterPayment> findById(UUID id);

    /**
     * Lista pagos de un trabajo ordenados del más reciente al más antiguo.
     */
    List<PlotterPayment> findByPlotterJobIdNewestFirst(UUID plotterJobId);
}
