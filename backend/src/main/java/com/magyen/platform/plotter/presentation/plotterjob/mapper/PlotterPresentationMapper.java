package com.magyen.platform.plotter.presentation.plotterjob.mapper;

import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterJobQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterJobsResult;
import com.magyen.platform.plotter.application.dto.GetPlotterPaymentsQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterPaymentsResult;
import com.magyen.platform.plotter.application.dto.RegisterPlotterPaymentCommand;
import com.magyen.platform.plotter.application.dto.RegisterPlotterPaymentResult;
import com.magyen.platform.plotter.domain.PlotterJobType;
import com.magyen.platform.plotter.presentation.plotterjob.request.CreatePlotterJobRequest;
import com.magyen.platform.plotter.presentation.plotterjob.request.RegisterPlotterPaymentRequest;
import com.magyen.platform.plotter.presentation.plotterjob.response.CreatePlotterJobResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetPlotterJobResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetPlotterJobsResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetPlotterPaymentResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetPlotterPaymentsResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.RegisterPlotterPaymentResponse;

import java.util.Objects;
import java.util.UUID;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application.
 */
public class PlotterPresentationMapper {

    public CreatePlotterJobCommand toCommand(CreatePlotterJobRequest request) {
        Objects.requireNonNull(request, "CreatePlotterJobRequest must not be null");

        PlotterJobType jobType = request.jobType() == null || request.jobType().isBlank()
                ? null
                : PlotterJobType.of(request.jobType());

        return new CreatePlotterJobCommand(
                request.customerId(),
                request.orderId(),
                request.creationDate(),
                request.paperInventoryItemId(),
                request.printedMeters(),
                request.pricePerMeter(),
                request.observations(),
                jobType,
                request.plotterJobId()
        );
    }

    public CreatePlotterJobResponse toCreateResponse(CreatePlotterJobResult result) {
        Objects.requireNonNull(result, "CreatePlotterJobResult must not be null");

        return new CreatePlotterJobResponse(
                result.plotterJobId(),
                result.jobType().name(),
                result.customerId(),
                result.customerName(),
                result.orderId(),
                result.orderNumber(),
                result.orderDescription(),
                result.creationDate(),
                result.paperInventoryItemId(),
                result.printedMeters(),
                result.pricePerMeter(),
                result.totalAmount(),
                result.status().name(),
                result.observations()
        );
    }

    public GetPlotterJobQuery toQuery(UUID plotterJobId) {
        Objects.requireNonNull(plotterJobId, "Plotter job id must not be null");
        return new GetPlotterJobQuery(plotterJobId);
    }

    public GetPlotterJobResponse toGetResponse(GetPlotterJobResult result) {
        Objects.requireNonNull(result, "GetPlotterJobResult must not be null");

        return new GetPlotterJobResponse(
                result.plotterJobId(),
                result.jobType().name(),
                result.customerId(),
                result.customerName(),
                result.orderId(),
                result.orderNumber(),
                result.orderDescription(),
                result.creationDate(),
                result.paperInventoryItemId(),
                result.printedMeters(),
                result.pricePerMeter(),
                result.totalAmount(),
                result.paidAmount(),
                result.outstandingAmount(),
                result.status().name(),
                result.observations()
        );
    }

    public GetPlotterJobsResponse toGetJobsResponse(GetPlotterJobsResult result) {
        Objects.requireNonNull(result, "GetPlotterJobsResult must not be null");

        return new GetPlotterJobsResponse(
                result.jobs().stream()
                        .map(this::toGetResponse)
                        .toList()
        );
    }

    public RegisterPlotterPaymentCommand toRegisterPaymentCommand(
            UUID plotterJobId,
            RegisterPlotterPaymentRequest request
    ) {
        Objects.requireNonNull(plotterJobId, "Plotter job id must not be null");
        Objects.requireNonNull(request, "Register plotter payment request must not be null");
        return new RegisterPlotterPaymentCommand(
                plotterJobId,
                request.amount(),
                request.paymentDate(),
                request.observations()
        );
    }

    public RegisterPlotterPaymentResponse toRegisterPaymentResponse(RegisterPlotterPaymentResult result) {
        Objects.requireNonNull(result, "Register plotter payment result must not be null");
        return new RegisterPlotterPaymentResponse(
                result.paymentId(),
                result.plotterJobId(),
                result.amount(),
                result.paymentDate(),
                result.observations(),
                result.paidAmount(),
                result.outstandingAmount()
        );
    }

    public GetPlotterPaymentsQuery toPaymentsQuery(UUID plotterJobId) {
        Objects.requireNonNull(plotterJobId, "Plotter job id must not be null");
        return new GetPlotterPaymentsQuery(plotterJobId);
    }

    public GetPlotterPaymentsResponse toPaymentsResponse(GetPlotterPaymentsResult result) {
        Objects.requireNonNull(result, "Get plotter payments result must not be null");
        return new GetPlotterPaymentsResponse(
                result.payments().stream()
                        .map(payment -> new GetPlotterPaymentResponse(
                                payment.paymentId(),
                                payment.plotterJobId(),
                                payment.amount(),
                                payment.paymentDate(),
                                payment.observations()
                        ))
                        .toList(),
                result.totalAmount(),
                result.paidAmount(),
                result.outstandingAmount()
        );
    }
}
