package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.util.List;

public record GetPlotterCustomerAccountsResponse(
        List<PlotterCustomerAccountSummaryResponse> customers
) {
}
