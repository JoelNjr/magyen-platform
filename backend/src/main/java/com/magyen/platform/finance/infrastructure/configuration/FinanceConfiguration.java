package com.magyen.platform.finance.infrastructure.configuration;

import com.magyen.platform.commercial.application.usecase.GetSellerCommissionPerformanceUseCase;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.finance.application.port.EmployeeProductionEarningsPort;
import com.magyen.platform.finance.application.port.EmployeeSellerCommissionsPort;
import com.magyen.platform.finance.application.usecase.ActivatePayrollEmployeeUseCase;
import com.magyen.platform.finance.application.usecase.CancelPayrollPeriodUseCase;
import com.magyen.platform.finance.application.usecase.CancelRecurringFinancialObligationOccurrenceUseCase;
import com.magyen.platform.finance.application.usecase.CancelPayrollDeductionUseCase;
import com.magyen.platform.finance.application.usecase.CreatePayrollDeductionUseCase;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.finance.application.usecase.CreateRecurringFinancialObligationOccurrenceUseCase;
import com.magyen.platform.finance.application.usecase.CreateRecurringFinancialObligationUseCase;
import com.magyen.platform.finance.application.usecase.DeactivatePayrollEmployeeUseCase;
import com.magyen.platform.finance.application.usecase.DeactivateRecurringFinancialObligationUseCase;
import com.magyen.platform.finance.application.usecase.GeneratePayrollPeriodsUseCase;
import com.magyen.platform.finance.application.usecase.GenerateRecurringFinancialObligationOccurrencesUseCase;
import com.magyen.platform.finance.application.usecase.GetFinancialPeriodSummaryUseCase;
import com.magyen.platform.finance.application.usecase.GetFinancialTransactionUseCase;
import com.magyen.platform.finance.application.usecase.GetFinancialTransactionsUseCase;
import com.magyen.platform.finance.application.usecase.GetOverdueFinancialObligationOccurrencesUseCase;
import com.magyen.platform.finance.application.usecase.GetPaymentUseCase;
import com.magyen.platform.finance.application.usecase.GetPaymentsByOrderUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollDeductionsUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollEmployeeCommissionsUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollEmployeeFinancialSummaryUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollEmployeePerformanceUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollEmployeeProductionEarningsUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollEmployeeUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollEmployeesUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollPeriodUseCase;
import com.magyen.platform.finance.application.usecase.GetPayrollPeriodsUseCase;
import com.magyen.platform.finance.application.usecase.GetPendingFinancialObligationOccurrencesUseCase;
import com.magyen.platform.finance.application.usecase.GetRecurringFinancialObligationOccurrenceUseCase;
import com.magyen.platform.finance.application.usecase.GetRecurringFinancialObligationOccurrencesUseCase;
import com.magyen.platform.finance.application.usecase.GetRecurringFinancialObligationUseCase;
import com.magyen.platform.finance.application.usecase.GetRecurringFinancialObligationsUseCase;
import com.magyen.platform.finance.application.usecase.GetUpcomingFinancialObligationOccurrencesUseCase;
import com.magyen.platform.finance.application.usecase.PayPayrollPeriodUseCase;
import com.magyen.platform.finance.application.usecase.PayRecurringFinancialObligationOccurrenceUseCase;
import com.magyen.platform.finance.application.usecase.RegisterFinancialTransactionUseCase;
import com.magyen.platform.finance.application.usecase.RegisterPaymentUseCase;
import com.magyen.platform.finance.application.usecase.RegisterPlotterPaymentIncomeUseCase;
import com.magyen.platform.finance.application.usecase.RegisterProductionLaborPaymentExpenseUseCase;
import com.magyen.platform.finance.application.usecase.EnsureInventoryPurchaseExpenseUseCase;
import com.magyen.platform.finance.application.usecase.EnsurePlotterInternalServiceLedgerUseCase;
import com.magyen.platform.finance.application.usecase.ResolveProductionLaborOperatorUseCase;
import com.magyen.platform.finance.application.usecase.SynchronizeCommercialPaymentFinancialTransactionUseCase;
import com.magyen.platform.finance.application.usecase.UpdatePayrollEmployeeCompensationUseCase;
import com.magyen.platform.finance.application.usecase.UpdateRecurringFinancialObligationUseCase;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.PaymentRepository;
import com.magyen.platform.finance.domain.PayrollDeductionRepository;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;
import com.magyen.platform.finance.domain.PayrollPeriodRepository;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrenceRepository;
import com.magyen.platform.finance.domain.RecurringFinancialObligationRepository;
import com.magyen.platform.finance.infrastructure.persistence.mapper.FinancialTransactionPersistenceMapper;
import com.magyen.platform.finance.infrastructure.persistence.mapper.PaymentPersistenceMapper;
import com.magyen.platform.finance.infrastructure.persistence.mapper.PayrollDeductionPersistenceMapper;
import com.magyen.platform.finance.infrastructure.persistence.mapper.PayrollEmployeePersistenceMapper;
import com.magyen.platform.finance.infrastructure.persistence.mapper.PayrollPeriodPersistenceMapper;
import com.magyen.platform.finance.infrastructure.production.PayrollEmployeeProductionEarningsAdapter;
import com.magyen.platform.finance.infrastructure.commercial.PayrollEmployeeSellerCommissionsAdapter;
import com.magyen.platform.finance.infrastructure.persistence.mapper.RecurringFinancialObligationOccurrencePersistenceMapper;
import com.magyen.platform.finance.infrastructure.persistence.mapper.RecurringFinancialObligationPersistenceMapper;
import com.magyen.platform.finance.presentation.obligation.mapper.RecurringFinancialObligationPresentationMapper;
import com.magyen.platform.finance.presentation.occurrence.mapper.RecurringFinancialObligationOccurrencePresentationMapper;
import com.magyen.platform.finance.presentation.payment.mapper.PaymentPresentationMapper;
import com.magyen.platform.finance.presentation.payroll.mapper.PayrollDeductionPresentationMapper;
import com.magyen.platform.finance.presentation.payroll.mapper.PayrollEmployeePresentationMapper;
import com.magyen.platform.finance.presentation.payroll.mapper.PayrollPeriodPresentationMapper;
import com.magyen.platform.finance.presentation.summary.mapper.FinancialPeriodSummaryPresentationMapper;
import com.magyen.platform.finance.presentation.transaction.mapper.FinancialTransactionPresentationMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.magyen.platform.production.application.usecase.GetEmployeeProductionEarningsUseCase;

import java.time.Clock;

/**
 * Ensambla los beans del módulo financiero que no se registran por estereotipos Spring.
 */
@Configuration
public class FinanceConfiguration {

    /**
     * Reloj de aplicación para lecturas con semántica "hoy" (pending/overdue/upcoming).
     * Los tests pueden sobrescribirlo con un Clock fijo.
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public PaymentPresentationMapper paymentPresentationMapper() {
        return new PaymentPresentationMapper();
    }

    @Bean
    public PaymentPersistenceMapper paymentPersistenceMapper() {
        return new PaymentPersistenceMapper();
    }

    @Bean
    public FinancialTransactionPresentationMapper financialTransactionPresentationMapper() {
        return new FinancialTransactionPresentationMapper();
    }

    @Bean
    public FinancialTransactionPersistenceMapper financialTransactionPersistenceMapper() {
        return new FinancialTransactionPersistenceMapper();
    }

    @Bean
    public RegisterPaymentUseCase registerPaymentUseCase(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            FinancialTransactionRepository financialTransactionRepository
    ) {
        return new RegisterPaymentUseCase(
                orderRepository,
                paymentRepository,
                financialTransactionRepository
        );
    }

    @Bean
    public SynchronizeCommercialPaymentFinancialTransactionUseCase
    synchronizeCommercialPaymentFinancialTransactionUseCase(
            PaymentRepository paymentRepository,
            FinancialTransactionRepository financialTransactionRepository
    ) {
        return new SynchronizeCommercialPaymentFinancialTransactionUseCase(
                paymentRepository,
                financialTransactionRepository
        );
    }

    @Bean
    public RegisterPlotterPaymentIncomeUseCase registerPlotterPaymentIncomeUseCase(
            FinancialTransactionRepository financialTransactionRepository
    ) {
        return new RegisterPlotterPaymentIncomeUseCase(financialTransactionRepository);
    }

    @Bean
    public GetPaymentUseCase getPaymentUseCase(PaymentRepository paymentRepository) {
        return new GetPaymentUseCase(paymentRepository);
    }

    @Bean
    public GetPaymentsByOrderUseCase getPaymentsByOrderUseCase(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository
    ) {
        return new GetPaymentsByOrderUseCase(orderRepository, paymentRepository);
    }

    @Bean
    public RegisterFinancialTransactionUseCase registerFinancialTransactionUseCase(
            FinancialTransactionRepository financialTransactionRepository
    ) {
        return new RegisterFinancialTransactionUseCase(financialTransactionRepository);
    }

    @Bean
    public GetFinancialTransactionUseCase getFinancialTransactionUseCase(
            FinancialTransactionRepository financialTransactionRepository
    ) {
        return new GetFinancialTransactionUseCase(financialTransactionRepository);
    }

    @Bean
    public GetFinancialTransactionsUseCase getFinancialTransactionsUseCase(
            FinancialTransactionRepository financialTransactionRepository
    ) {
        return new GetFinancialTransactionsUseCase(financialTransactionRepository);
    }

    @Bean
    public RecurringFinancialObligationPresentationMapper recurringFinancialObligationPresentationMapper() {
        return new RecurringFinancialObligationPresentationMapper();
    }

    @Bean
    public RecurringFinancialObligationPersistenceMapper recurringFinancialObligationPersistenceMapper() {
        return new RecurringFinancialObligationPersistenceMapper();
    }

    @Bean
    public CreateRecurringFinancialObligationUseCase createRecurringFinancialObligationUseCase(
            RecurringFinancialObligationRepository recurringFinancialObligationRepository
    ) {
        return new CreateRecurringFinancialObligationUseCase(recurringFinancialObligationRepository);
    }

    @Bean
    public GetRecurringFinancialObligationUseCase getRecurringFinancialObligationUseCase(
            RecurringFinancialObligationRepository recurringFinancialObligationRepository
    ) {
        return new GetRecurringFinancialObligationUseCase(recurringFinancialObligationRepository);
    }

    @Bean
    public GetRecurringFinancialObligationsUseCase getRecurringFinancialObligationsUseCase(
            RecurringFinancialObligationRepository recurringFinancialObligationRepository
    ) {
        return new GetRecurringFinancialObligationsUseCase(recurringFinancialObligationRepository);
    }

    @Bean
    public UpdateRecurringFinancialObligationUseCase updateRecurringFinancialObligationUseCase(
            RecurringFinancialObligationRepository recurringFinancialObligationRepository
    ) {
        return new UpdateRecurringFinancialObligationUseCase(recurringFinancialObligationRepository);
    }

    @Bean
    public DeactivateRecurringFinancialObligationUseCase deactivateRecurringFinancialObligationUseCase(
            RecurringFinancialObligationRepository recurringFinancialObligationRepository
    ) {
        return new DeactivateRecurringFinancialObligationUseCase(recurringFinancialObligationRepository);
    }

    @Bean
    public RecurringFinancialObligationOccurrencePresentationMapper
    recurringFinancialObligationOccurrencePresentationMapper() {
        return new RecurringFinancialObligationOccurrencePresentationMapper();
    }

    @Bean
    public RecurringFinancialObligationOccurrencePersistenceMapper
    recurringFinancialObligationOccurrencePersistenceMapper() {
        return new RecurringFinancialObligationOccurrencePersistenceMapper();
    }

    @Bean
    public CreateRecurringFinancialObligationOccurrenceUseCase createRecurringFinancialObligationOccurrenceUseCase(
            RecurringFinancialObligationRepository recurringFinancialObligationRepository,
            RecurringFinancialObligationOccurrenceRepository occurrenceRepository
    ) {
        return new CreateRecurringFinancialObligationOccurrenceUseCase(
                recurringFinancialObligationRepository,
                occurrenceRepository
        );
    }

    @Bean
    public GenerateRecurringFinancialObligationOccurrencesUseCase
    generateRecurringFinancialObligationOccurrencesUseCase(
            RecurringFinancialObligationRepository recurringFinancialObligationRepository,
            RecurringFinancialObligationOccurrenceRepository occurrenceRepository
    ) {
        return new GenerateRecurringFinancialObligationOccurrencesUseCase(
                recurringFinancialObligationRepository,
                occurrenceRepository
        );
    }

    @Bean
    public GetRecurringFinancialObligationOccurrenceUseCase getRecurringFinancialObligationOccurrenceUseCase(
            RecurringFinancialObligationOccurrenceRepository occurrenceRepository
    ) {
        return new GetRecurringFinancialObligationOccurrenceUseCase(occurrenceRepository);
    }

    @Bean
    public GetRecurringFinancialObligationOccurrencesUseCase getRecurringFinancialObligationOccurrencesUseCase(
            RecurringFinancialObligationOccurrenceRepository occurrenceRepository
    ) {
        return new GetRecurringFinancialObligationOccurrencesUseCase(occurrenceRepository);
    }

    @Bean
    public PayRecurringFinancialObligationOccurrenceUseCase payRecurringFinancialObligationOccurrenceUseCase(
            RecurringFinancialObligationOccurrenceRepository occurrenceRepository,
            RecurringFinancialObligationRepository recurringFinancialObligationRepository,
            FinancialTransactionRepository financialTransactionRepository
    ) {
        return new PayRecurringFinancialObligationOccurrenceUseCase(
                occurrenceRepository,
                recurringFinancialObligationRepository,
                financialTransactionRepository
        );
    }

    @Bean
    public CancelRecurringFinancialObligationOccurrenceUseCase cancelRecurringFinancialObligationOccurrenceUseCase(
            RecurringFinancialObligationOccurrenceRepository occurrenceRepository
    ) {
        return new CancelRecurringFinancialObligationOccurrenceUseCase(occurrenceRepository);
    }

    @Bean
    public GetPendingFinancialObligationOccurrencesUseCase getPendingFinancialObligationOccurrencesUseCase(
            RecurringFinancialObligationOccurrenceRepository occurrenceRepository,
            RecurringFinancialObligationRepository recurringFinancialObligationRepository,
            Clock clock
    ) {
        return new GetPendingFinancialObligationOccurrencesUseCase(
                occurrenceRepository,
                recurringFinancialObligationRepository,
                clock
        );
    }

    @Bean
    public GetOverdueFinancialObligationOccurrencesUseCase getOverdueFinancialObligationOccurrencesUseCase(
            RecurringFinancialObligationOccurrenceRepository occurrenceRepository,
            RecurringFinancialObligationRepository recurringFinancialObligationRepository,
            Clock clock
    ) {
        return new GetOverdueFinancialObligationOccurrencesUseCase(
                occurrenceRepository,
                recurringFinancialObligationRepository,
                clock
        );
    }

    @Bean
    public GetUpcomingFinancialObligationOccurrencesUseCase getUpcomingFinancialObligationOccurrencesUseCase(
            RecurringFinancialObligationOccurrenceRepository occurrenceRepository,
            RecurringFinancialObligationRepository recurringFinancialObligationRepository,
            Clock clock
    ) {
        return new GetUpcomingFinancialObligationOccurrencesUseCase(
                occurrenceRepository,
                recurringFinancialObligationRepository,
                clock
        );
    }

    @Bean
    public FinancialPeriodSummaryPresentationMapper financialPeriodSummaryPresentationMapper() {
        return new FinancialPeriodSummaryPresentationMapper();
    }

    @Bean
    public GetFinancialPeriodSummaryUseCase getFinancialPeriodSummaryUseCase(
            FinancialTransactionRepository financialTransactionRepository
    ) {
        return new GetFinancialPeriodSummaryUseCase(financialTransactionRepository);
    }

    @Bean
    public PayrollEmployeePresentationMapper payrollEmployeePresentationMapper() {
        return new PayrollEmployeePresentationMapper();
    }

    @Bean
    public PayrollEmployeePersistenceMapper payrollEmployeePersistenceMapper() {
        return new PayrollEmployeePersistenceMapper();
    }

    @Bean
    public PayrollDeductionPresentationMapper payrollDeductionPresentationMapper() {
        return new PayrollDeductionPresentationMapper();
    }

    @Bean
    public PayrollDeductionPersistenceMapper payrollDeductionPersistenceMapper() {
        return new PayrollDeductionPersistenceMapper();
    }

    @Bean
    public PayrollPeriodPresentationMapper payrollPeriodPresentationMapper() {
        return new PayrollPeriodPresentationMapper();
    }

    @Bean
    public PayrollPeriodPersistenceMapper payrollPeriodPersistenceMapper() {
        return new PayrollPeriodPersistenceMapper();
    }

    @Bean
    public CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository
    ) {
        return new CreatePayrollEmployeeUseCase(payrollEmployeeRepository);
    }

    @Bean
    public GetPayrollEmployeeUseCase getPayrollEmployeeUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository
    ) {
        return new GetPayrollEmployeeUseCase(payrollEmployeeRepository);
    }

    @Bean
    public GetPayrollEmployeesUseCase getPayrollEmployeesUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository
    ) {
        return new GetPayrollEmployeesUseCase(payrollEmployeeRepository);
    }

    @Bean
    public UpdatePayrollEmployeeCompensationUseCase updatePayrollEmployeeCompensationUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository
    ) {
        return new UpdatePayrollEmployeeCompensationUseCase(payrollEmployeeRepository);
    }

    @Bean
    public ActivatePayrollEmployeeUseCase activatePayrollEmployeeUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository
    ) {
        return new ActivatePayrollEmployeeUseCase(payrollEmployeeRepository);
    }

    @Bean
    public DeactivatePayrollEmployeeUseCase deactivatePayrollEmployeeUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository
    ) {
        return new DeactivatePayrollEmployeeUseCase(payrollEmployeeRepository);
    }

    @Bean
    public CreatePayrollDeductionUseCase createPayrollDeductionUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository,
            PayrollDeductionRepository payrollDeductionRepository,
            Clock clock
    ) {
        return new CreatePayrollDeductionUseCase(
                payrollEmployeeRepository,
                payrollDeductionRepository,
                clock
        );
    }

    @Bean
    public GetPayrollDeductionsUseCase getPayrollDeductionsUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository,
            PayrollDeductionRepository payrollDeductionRepository
    ) {
        return new GetPayrollDeductionsUseCase(
                payrollEmployeeRepository,
                payrollDeductionRepository
        );
    }

    @Bean
    public CancelPayrollDeductionUseCase cancelPayrollDeductionUseCase(
            PayrollDeductionRepository payrollDeductionRepository
    ) {
        return new CancelPayrollDeductionUseCase(payrollDeductionRepository);
    }

    @Bean
    public GeneratePayrollPeriodsUseCase generatePayrollPeriodsUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository,
            PayrollPeriodRepository payrollPeriodRepository
    ) {
        return new GeneratePayrollPeriodsUseCase(payrollEmployeeRepository, payrollPeriodRepository);
    }

    @Bean
    public GetPayrollPeriodsUseCase getPayrollPeriodsUseCase(
            PayrollPeriodRepository payrollPeriodRepository,
            PayrollEmployeeRepository payrollEmployeeRepository
    ) {
        return new GetPayrollPeriodsUseCase(payrollPeriodRepository, payrollEmployeeRepository);
    }

    @Bean
    public GetPayrollPeriodUseCase getPayrollPeriodUseCase(
            PayrollPeriodRepository payrollPeriodRepository,
            PayrollEmployeeRepository payrollEmployeeRepository
    ) {
        return new GetPayrollPeriodUseCase(payrollPeriodRepository, payrollEmployeeRepository);
    }

    @Bean
    public PayPayrollPeriodUseCase payPayrollPeriodUseCase(
            PayrollPeriodRepository payrollPeriodRepository,
            PayrollEmployeeRepository payrollEmployeeRepository,
            FinancialTransactionRepository financialTransactionRepository
    ) {
        return new PayPayrollPeriodUseCase(
                payrollPeriodRepository,
                payrollEmployeeRepository,
                financialTransactionRepository
        );
    }

    @Bean
    public CancelPayrollPeriodUseCase cancelPayrollPeriodUseCase(
            PayrollPeriodRepository payrollPeriodRepository
    ) {
        return new CancelPayrollPeriodUseCase(payrollPeriodRepository);
    }

    @Bean
    public ResolveProductionLaborOperatorUseCase resolveProductionLaborOperatorUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository
    ) {
        return new ResolveProductionLaborOperatorUseCase(payrollEmployeeRepository);
    }

    @Bean
    public RegisterProductionLaborPaymentExpenseUseCase registerProductionLaborPaymentExpenseUseCase(
            FinancialTransactionRepository financialTransactionRepository
    ) {
        return new RegisterProductionLaborPaymentExpenseUseCase(financialTransactionRepository);
    }

    @Bean
    public EmployeeProductionEarningsPort employeeProductionEarningsPort(
            GetEmployeeProductionEarningsUseCase getEmployeeProductionEarningsUseCase
    ) {
        return new PayrollEmployeeProductionEarningsAdapter(getEmployeeProductionEarningsUseCase);
    }

    @Bean
    public GetPayrollEmployeeProductionEarningsUseCase getPayrollEmployeeProductionEarningsUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository,
            EmployeeProductionEarningsPort employeeProductionEarningsPort
    ) {
        return new GetPayrollEmployeeProductionEarningsUseCase(
                payrollEmployeeRepository,
                employeeProductionEarningsPort
        );
    }

    @Bean
    public EmployeeSellerCommissionsPort employeeSellerCommissionsPort(
            GetSellerCommissionPerformanceUseCase getSellerCommissionPerformanceUseCase
    ) {
        return new PayrollEmployeeSellerCommissionsAdapter(getSellerCommissionPerformanceUseCase);
    }

    @Bean
    public GetPayrollEmployeeCommissionsUseCase getPayrollEmployeeCommissionsUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository,
            EmployeeSellerCommissionsPort employeeSellerCommissionsPort
    ) {
        return new GetPayrollEmployeeCommissionsUseCase(
                payrollEmployeeRepository,
                employeeSellerCommissionsPort
        );
    }

    @Bean
    public GetPayrollEmployeePerformanceUseCase getPayrollEmployeePerformanceUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository,
            GetPayrollEmployeeCommissionsUseCase getPayrollEmployeeCommissionsUseCase
    ) {
        return new GetPayrollEmployeePerformanceUseCase(
                payrollEmployeeRepository,
                getPayrollEmployeeCommissionsUseCase
        );
    }

    @Bean
    public GetPayrollEmployeeFinancialSummaryUseCase getPayrollEmployeeFinancialSummaryUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository,
            GetPayrollEmployeeCommissionsUseCase getPayrollEmployeeCommissionsUseCase,
            GetPayrollEmployeeProductionEarningsUseCase getPayrollEmployeeProductionEarningsUseCase,
            GetPayrollDeductionsUseCase getPayrollDeductionsUseCase,
            Clock clock
    ) {
        return new GetPayrollEmployeeFinancialSummaryUseCase(
                payrollEmployeeRepository,
                getPayrollEmployeeCommissionsUseCase,
                getPayrollEmployeeProductionEarningsUseCase,
                getPayrollDeductionsUseCase,
                clock
        );
    }

    @Bean
    public EnsureInventoryPurchaseExpenseUseCase ensureInventoryPurchaseExpenseUseCase(
            FinancialTransactionRepository financialTransactionRepository
    ) {
        return new EnsureInventoryPurchaseExpenseUseCase(financialTransactionRepository);
    }

    @Bean
    public EnsurePlotterInternalServiceLedgerUseCase ensurePlotterInternalServiceLedgerUseCase(
            FinancialTransactionRepository financialTransactionRepository
    ) {
        return new EnsurePlotterInternalServiceLedgerUseCase(financialTransactionRepository);
    }
}
