package com.magyen.platform.intelligence.application.usecase;

import com.magyen.platform.finance.domain.Payment;
import com.magyen.platform.finance.domain.PaymentRepository;
import com.magyen.platform.intelligence.application.dto.GetPaymentsReportResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que consolida el reporte de pagos a partir de los pagos registrados.
 * <p>
 * Solo consulta información existente; no modifica el estado del negocio.
 */
public class GetPaymentsReportUseCase {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final PaymentRepository paymentRepository;

    public GetPaymentsReportUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = Objects.requireNonNull(
                paymentRepository,
                "Payment repository must not be null"
        );
    }

    public GetPaymentsReportResult execute() {
        List<Payment> payments = paymentRepository.findAll();

        BigDecimal totalReceived = payments.stream()
                .map(payment -> payment.getAmount().getValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long paymentCount = payments.size();
        BigDecimal averagePerPayment = calculateAverage(totalReceived, paymentCount);

        return new GetPaymentsReportResult(totalReceived, paymentCount, averagePerPayment);
    }

    private BigDecimal calculateAverage(BigDecimal total, long count) {
        if (count == 0) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, ROUNDING_MODE);
        }
        return total.divide(BigDecimal.valueOf(count), MONEY_SCALE, ROUNDING_MODE);
    }
}
