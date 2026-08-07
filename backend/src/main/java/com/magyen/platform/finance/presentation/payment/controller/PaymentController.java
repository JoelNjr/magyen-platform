package com.magyen.platform.finance.presentation.payment.controller;

import com.magyen.platform.finance.application.dto.GetPaymentQuery;
import com.magyen.platform.finance.application.dto.GetPaymentResult;
import com.magyen.platform.finance.application.dto.GetPaymentsByOrderQuery;
import com.magyen.platform.finance.application.dto.GetPaymentsByOrderResult;
import com.magyen.platform.finance.application.dto.RegisterPaymentCommand;
import com.magyen.platform.finance.application.dto.RegisterPaymentResult;
import com.magyen.platform.finance.application.usecase.GetPaymentUseCase;
import com.magyen.platform.finance.application.usecase.GetPaymentsByOrderUseCase;
import com.magyen.platform.finance.application.usecase.RegisterPaymentUseCase;
import com.magyen.platform.finance.presentation.payment.mapper.PaymentPresentationMapper;
import com.magyen.platform.finance.presentation.payment.request.RegisterPaymentRequest;
import com.magyen.platform.finance.presentation.payment.response.GetPaymentResponse;
import com.magyen.platform.finance.presentation.payment.response.GetPaymentsByOrderResponse;
import com.magyen.platform.finance.presentation.payment.response.RegisterPaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Expone la API REST de pagos.
 * <p>
 * Coordina HTTP con Application; no contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final RegisterPaymentUseCase registerPaymentUseCase;
    private final GetPaymentUseCase getPaymentUseCase;
    private final GetPaymentsByOrderUseCase getPaymentsByOrderUseCase;
    private final PaymentPresentationMapper paymentPresentationMapper;

    public PaymentController(
            RegisterPaymentUseCase registerPaymentUseCase,
            GetPaymentUseCase getPaymentUseCase,
            GetPaymentsByOrderUseCase getPaymentsByOrderUseCase,
            PaymentPresentationMapper paymentPresentationMapper
    ) {
        this.registerPaymentUseCase = registerPaymentUseCase;
        this.getPaymentUseCase = getPaymentUseCase;
        this.getPaymentsByOrderUseCase = getPaymentsByOrderUseCase;
        this.paymentPresentationMapper = paymentPresentationMapper;
    }

    @PostMapping
    public ResponseEntity<RegisterPaymentResponse> registerPayment(
            @RequestBody RegisterPaymentRequest request
    ) {
        RegisterPaymentCommand command = paymentPresentationMapper.toCommand(request);
        RegisterPaymentResult result = registerPaymentUseCase.execute(command);
        RegisterPaymentResponse response = paymentPresentationMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<GetPaymentResponse> getPayment(
            @PathVariable UUID paymentId
    ) {
        GetPaymentQuery query = paymentPresentationMapper.toGetPaymentQuery(paymentId);
        GetPaymentResult result = getPaymentUseCase.execute(query);
        GetPaymentResponse response = paymentPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<GetPaymentsByOrderResponse> getPaymentsByOrder(
            @PathVariable UUID orderId
    ) {
        GetPaymentsByOrderQuery query = paymentPresentationMapper.toGetPaymentsByOrderQuery(orderId);
        GetPaymentsByOrderResult result = getPaymentsByOrderUseCase.execute(query);
        GetPaymentsByOrderResponse response = paymentPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }
}
