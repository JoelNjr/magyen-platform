package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.SellerNameResolver;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.dto.GetQuotationCommand;
import com.magyen.platform.commercial.application.dto.GetQuotationResult;
import com.magyen.platform.commercial.application.port.CommercialSellerEmployeeInfo;
import com.magyen.platform.commercial.domain.Customer;
import com.magyen.platform.commercial.domain.CustomerRepository;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationNumber;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.domain.Seller;
import com.magyen.platform.commercial.domain.SellerRepository;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.DeactivatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.finance.application.usecase.DeactivatePayrollEmployeeUseCase;
import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.production.application.port.ProductionLaborOperatorInfo;
import com.magyen.platform.production.application.usecase.ListEligibleProductionLaborOperatorsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class CommercialSellerUnificationUseCaseTest {

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private DeactivatePayrollEmployeeUseCase deactivatePayrollEmployeeUseCase;

    @Autowired
    private GetSellersUseCase getSellersUseCase;

    @Autowired
    private SellerNameResolver sellerNameResolver;

    @Autowired
    private CreateQuotationUseCase createQuotationUseCase;

    @Autowired
    private GetQuotationUseCase getQuotationUseCase;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private SellerRepository leftoverSellerRepository;

    @Autowired
    private ListEligibleProductionLaborOperatorsUseCase listEligibleProductionLaborOperatorsUseCase;

    @Test
    void selectorIncludesOnlyActiveFixedEmployees() {
        CreatePayrollEmployeeResult fixed = createFixed("Vendedor-" + suffix());
        CreatePayrollEmployeeResult production = createProduction("Operario-" + suffix());
        CreatePayrollEmployeeResult inactive = createFixed("Inactivo-" + suffix());
        deactivatePayrollEmployeeUseCase.execute(new DeactivatePayrollEmployeeCommand(inactive.employeeId()));

        var sellerIds = getSellersUseCase.execute().sellers().stream()
                .map(seller -> seller.sellerId())
                .toList();

        assertTrue(sellerIds.contains(fixed.employeeId()));
        assertFalse(sellerIds.contains(production.employeeId()));
        assertFalse(sellerIds.contains(inactive.employeeId()));

        CommercialSellerEmployeeInfo eligible = sellerNameResolver.requireEligibleSeller(fixed.employeeId());
        assertEquals(fixed.displayName(), eligible.displayName());
        assertThrows(IllegalArgumentException.class, () ->
                sellerNameResolver.requireEligibleSeller(production.employeeId()));
        assertThrows(IllegalArgumentException.class, () ->
                sellerNameResolver.requireEligibleSeller(inactive.employeeId()));
    }

    @Test
    void productionSelectorStillOnlyShowsActiveProductionBasedEmployees() {
        CreatePayrollEmployeeResult production = createProduction("Prod-Sel-" + suffix());
        CreatePayrollEmployeeResult fixed = createFixed("Fijo-Sel-" + suffix());

        var operatorIds = listEligibleProductionLaborOperatorsUseCase.execute().stream()
                .map(ProductionLaborOperatorInfo::employeeId)
                .toList();

        assertTrue(operatorIds.contains(production.employeeId()));
        assertFalse(operatorIds.contains(fixed.employeeId()));
    }

    @Test
    void newQuotationStoresEmployeeIdAndResolvesNameFromFinance() {
        CreatePayrollEmployeeResult seller = createFixed("Empleado Vendedor-" + suffix());
        Customer customer = customerRepository.save(Customer.create("Cliente-Unif-" + suffix()));

        UUID quotationId = createQuotationUseCase.execute(new CreateQuotationCommand(
                customer.getId(),
                LocalDate.of(2026, 8, 20),
                seller.employeeId(),
                "unificación",
                LocalDate.of(2026, 8, 17)
        )).quotationId();

        GetQuotationResult quotation = getQuotationUseCase.execute(new GetQuotationCommand(quotationId));
        assertEquals(seller.employeeId(), quotation.sellerId());
        assertEquals(seller.displayName(), quotation.sellerName());
    }

    @Test
    void leftoverSellerIdentityRemainsReadableOnHistoricalQuotations() {
        Seller leftover = leftoverSellerRepository.save(Seller.create("Histórico Leftover-" + suffix()));
        Customer customer = customerRepository.save(Customer.create("Cliente-Hist-" + suffix()));
        Quotation historical = quotationRepository.save(Quotation.create(
                QuotationNumber.of(Math.abs(UUID.randomUUID().getLeastSignificantBits())),
                customer.getId(),
                LocalDate.of(2026, 7, 27),
                LocalDate.of(2026, 8, 6),
                leftover.getId(),
                "histórico leftover"
        ));

        assertEquals(leftover.getName(), sellerNameResolver.resolveName(leftover.getId()));
        GetQuotationResult quotation = getQuotationUseCase.execute(new GetQuotationCommand(historical.getId()));
        assertEquals(leftover.getId(), quotation.sellerId());
        assertEquals(leftover.getName(), quotation.sellerName());

        var sellerIds = getSellersUseCase.execute().sellers().stream()
                .map(seller -> seller.sellerId())
                .toList();
        assertFalse(sellerIds.contains(leftover.getId()));
    }

    private CreatePayrollEmployeeResult createFixed(String name) {
        return createPayrollEmployeeUseCase.execute(new CreatePayrollEmployeeCommand(
                name,
                PayrollCompensationType.FIXED_PAYROLL,
                new BigDecimal("1500000.00"),
                LocalDate.of(2026, 8, 1),
                null
        ));
    }

    private CreatePayrollEmployeeResult createProduction(String name) {
        return createPayrollEmployeeUseCase.execute(new CreatePayrollEmployeeCommand(
                name,
                PayrollCompensationType.PRODUCTION_BASED,
                null,
                null,
                null
        ));
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
