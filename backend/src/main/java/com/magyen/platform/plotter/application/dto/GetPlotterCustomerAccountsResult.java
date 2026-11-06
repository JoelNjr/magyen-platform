package com.magyen.platform.plotter.application.dto;

import java.util.List;

public record GetPlotterCustomerAccountsResult(
        List<PlotterCustomerAccountSummary> customers
) {
}
