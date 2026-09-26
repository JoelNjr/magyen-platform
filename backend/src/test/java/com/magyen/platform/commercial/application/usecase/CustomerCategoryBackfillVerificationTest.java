package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.domain.Customer;
import com.magyen.platform.commercial.domain.CustomerCategory;
import com.magyen.platform.commercial.domain.CustomerRepository;
import com.magyen.platform.plotter.application.usecase.GetPlotterPendingBalancesUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica el backfill y los saldos del clon local. No inserta filas.
 */
@SpringBootTest
@Transactional
class CustomerCategoryBackfillVerificationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private GetPlotterPendingBalancesUseCase getPlotterPendingBalancesUseCase;

    @Test
    void classifiesExistingCustomersWithoutMergingOrDeleting() {
        List<Customer> customers = customerRepository.findAll();
        assertEquals(44, customers.size());
        assertEquals(31, count(customers, CustomerCategory.MAGYEN));
        assertEquals(11, count(customers, CustomerCategory.PLOTTER));
        assertEquals(2, count(customers, CustomerCategory.UNCLASSIFIED));

        List<Customer> jorge = customers.stream()
                .filter(customer -> "Jorge Alomias".equals(customer.getName()))
                .toList();
        assertEquals(2, jorge.size());
        assertEquals(1, jorge.stream().filter(customer -> customer.getCategory().isMagyen()).count());
        assertEquals(1, jorge.stream().filter(customer -> customer.getCategory() == CustomerCategory.UNCLASSIFIED).count());

        Customer sandra = customers.stream()
                .filter(customer -> "Sandra".equals(customer.getName()))
                .findFirst()
                .orElseThrow();
        assertEquals(CustomerCategory.UNCLASSIFIED, sandra.getCategory());
        assertTrue(customers.stream().anyMatch(customer -> "Sandra Martinez".equals(customer.getName())
                && customer.getCategory().isPlotter()));
    }

    @Test
    void pendingBalancesMatchExternalPlotterBook() {
        var balances = getPlotterPendingBalancesUseCase.execute();
        assertEquals(52, balances.openJobCount());
        assertEquals(11, balances.customerCount());
        assertEquals(new BigDecimal("6523000.00"), balances.externalBilledAmount());
        assertEquals(new BigDecimal("2826500.00"), balances.externalPaidAmount());
        assertEquals(new BigDecimal("3696500.00"), balances.outstandingAmount());
        assertTrue(balances.negativeBalances().isEmpty());
        assertEquals(11, balances.customers().size());
    }

    private static long count(List<Customer> customers, CustomerCategory category) {
        return customers.stream().filter(customer -> customer.getCategory() == category).count();
    }
}
