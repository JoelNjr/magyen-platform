package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.ApplyQuotationDiscountCommand;
import com.magyen.platform.commercial.application.dto.ApplyQuotationDiscountResult;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.domain.exception.QuotationDomainException;
import com.magyen.platform.shared.domain.Money;

import java.util.Objects;

/**
 * Aplica un descuento sobre el subtotal de una cotización DRAFT.
 * <p>
 * No altera precios unitarios. El total se recalcula en el dominio.
 */
public class ApplyQuotationDiscountUseCase {

    private final QuotationRepository quotationRepository;

    public ApplyQuotationDiscountUseCase(QuotationRepository quotationRepository) {
        this.quotationRepository = Objects.requireNonNull(
                quotationRepository,
                "Quotation repository must not be null"
        );
    }

    public ApplyQuotationDiscountResult execute(ApplyQuotationDiscountCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.quotationId(), "Quotation id must not be null");
        if (command.discountAmount() == null) {
            throw new QuotationDomainException("Discount amount must not be null");
        }

        Quotation quotation = quotationRepository.findById(command.quotationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quotation not found: " + command.quotationId()
                ));

        quotation.applyDiscount(Money.of(command.discountAmount()));
        Quotation saved = quotationRepository.save(quotation);

        return new ApplyQuotationDiscountResult(
                saved.getId(),
                saved.getSubtotal().getAmount(),
                saved.getDiscount().getAmount(),
                saved.getTotal().getAmount()
        );
    }
}
