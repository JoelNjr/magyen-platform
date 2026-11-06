package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.util.UUID;

public record CancelPlotterJobResponse(
        UUID plotterJobId,
        boolean deleted,
        boolean alreadyCancelled,
        String status
) {
}
