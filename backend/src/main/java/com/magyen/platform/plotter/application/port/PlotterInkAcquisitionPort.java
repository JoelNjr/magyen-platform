package com.magyen.platform.plotter.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Lectura de adquisiciones físicas de tinta. No crea asientos Finance.
 */
public interface PlotterInkAcquisitionPort {

    List<InkAcquisitionSnapshot> findInkAcquisitions(LocalDate fromDate, LocalDate toDate);

    record InkAcquisitionSnapshot(
            LocalDate purchaseDate,
            BigDecimal totalCost
    ) {
    }
}
