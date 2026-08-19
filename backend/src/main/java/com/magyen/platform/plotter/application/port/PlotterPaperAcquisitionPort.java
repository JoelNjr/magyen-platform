package com.magyen.platform.plotter.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Lectura de adquisiciones físicas de papel. No crea asientos Finance.
 */
public interface PlotterPaperAcquisitionPort {

    List<PaperAcquisitionSnapshot> findPaperAcquisitions(LocalDate fromDate, LocalDate toDate);

    record PaperAcquisitionSnapshot(
            LocalDate purchaseDate,
            BigDecimal totalCost
    ) {
    }
}
