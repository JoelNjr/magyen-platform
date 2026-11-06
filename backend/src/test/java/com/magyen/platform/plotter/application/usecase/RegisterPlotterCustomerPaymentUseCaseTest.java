package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.EditPlotterJobCommand;
import com.magyen.platform.plotter.application.dto.RegisterPlotterCustomerPaymentCommand;
import com.magyen.platform.plotter.application.dto.RegisterPlotterCustomerPaymentResult;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderView;
import com.magyen.platform.plotter.application.port.PlotterInternalServiceFinancePort;
import com.magyen.platform.plotter.application.port.PlotterJobInventoryPort;
import com.magyen.platform.plotter.application.port.PlotterPaperRollView;
import com.magyen.platform.plotter.application.port.PlotterPaymentFinancePort;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterPayment;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import com.magyen.platform.plotter.domain.exception.PlotterRegistrationConflictException;
import com.magyen.platform.plotter.presentation.plotterjob.controller.PlotterJobController;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegisterPlotterCustomerPaymentUseCaseTest {

    @Test
    void rejectsAMissingRegistrationKeyBeforeAnyPaymentOrIncome() {
        Harness harness = Harness.withOutstanding("10000");

        assertThrows(PlotterDomainException.class, () -> harness.useCase.execute(command(
                harness.customerId,
                "1000",
                null
        )));

        assertEquals(0, harness.payments.size());
        assertEquals(0, harness.incomePaymentIds.size());
    }

    @Test
    void replaysTheSameKeyForTheSameCustomerWithoutASecondPaymentOrIncome() {
        Harness harness = Harness.withOutstanding("10000");
        UUID key = UUID.randomUUID();

        RegisterPlotterCustomerPaymentResult first = harness.useCase.execute(command(harness.customerId, "4000", key));
        RegisterPlotterCustomerPaymentResult replay = harness.useCase.execute(command(harness.customerId, "4000", key));

        assertEquals(first.paymentId(), replay.paymentId());
        assertTrue(replay.alreadyRegistered());
        assertEquals(1, harness.payments.size());
        assertEquals(List.of(first.paymentId()), harness.incomePaymentIds);
    }

    @Test
    void rejectsTheSameKeyForAnotherCustomerWithoutCreatingIncome() {
        Harness harness = Harness.withOutstanding("10000");
        UUID key = UUID.randomUUID();
        harness.useCase.execute(command(harness.customerId, "4000", key));

        assertThrows(PlotterRegistrationConflictException.class, () -> harness.useCase.execute(command(
                UUID.randomUUID(),
                "4000",
                key
        )));

        assertEquals(1, harness.payments.size());
        assertEquals(1, harness.incomePaymentIds.size());
    }

    @Test
    void usesTheUniqueKeyConflictToReturnTheOriginalPayment() {
        Harness harness = Harness.withOutstanding("10000");
        UUID key = UUID.randomUUID();
        harness.payments.publishRegistrationKey = false;

        RegisterPlotterCustomerPaymentResult first = harness.useCase.execute(command(harness.customerId, "2500", key));
        RegisterPlotterCustomerPaymentResult second = harness.useCase.execute(command(harness.customerId, "2500", key));

        assertEquals(first.paymentId(), second.paymentId());
        assertTrue(second.alreadyRegistered());
        assertEquals(1, harness.payments.size());
        assertEquals(List.of(first.paymentId()), harness.incomePaymentIds);
    }

    @Test
    void doesNotCreateIncomeWhenThePaymentExceedsTheOutstandingBalance() {
        Harness harness = Harness.withOutstanding("1000");

        assertThrows(PlotterDomainException.class, () -> harness.useCase.execute(command(
                harness.customerId,
                "1001",
                UUID.randomUUID()
        )));

        assertEquals(0, harness.payments.size());
        assertEquals(0, harness.incomePaymentIds.size());
    }

    @Test
    void editingAJobDoesNotCreateAPaymentOrFinanceIncome() {
        Harness harness = Harness.withOutstanding("10000");
        UUID key = UUID.randomUUID();
        harness.useCase.execute(command(harness.customerId, "1000", key));
        int incomeCalls = harness.incomePaymentIds.size();

        harness.edit.execute(new EditPlotterJobCommand(
                harness.job.getId(),
                harness.job.getCreationDate(),
                harness.job.getPaperInventoryItemId(),
                harness.job.getPrintedMeters(),
                harness.job.getPricePerMeter(),
                new BigDecimal("9000.00"),
                "corrected",
                null
        ));

        assertEquals(1, harness.payments.size());
        assertEquals(incomeCalls, harness.incomePaymentIds.size());
        assertEquals(0, harness.internalLedgerCalls);
    }

    @Test
    void cancellingAJobKeepsTheCustomerPaymentAndItsFinanceIncome() {
        Harness harness = Harness.withOutstanding("10000");
        PlotterJob coveringJob = PlotterJob.create(
                harness.customerId,
                LocalDate.of(2026, 8, 2),
                UUID.randomUUID(),
                new BigDecimal("1"),
                new BigDecimal("10000"),
                null
        );
        harness.jobs.add(coveringJob);
        UUID key = UUID.randomUUID();
        RegisterPlotterCustomerPaymentResult payment = harness.useCase.execute(
                command(harness.customerId, "4000", key)
        );

        harness.cancel.execute(harness.job.getId());

        assertEquals(1, harness.payments.size());
        assertEquals(payment.paymentId(), harness.payments.get(0).getId());
        assertEquals(List.of(payment.paymentId()), harness.incomePaymentIds);
        assertTrue(harness.jobs.stream().noneMatch(job -> job.getId().equals(harness.job.getId())));
    }

    @Test
    void legacyPaymentEndpointIsGoneAndDoesNotRegisterAPayment() throws Exception {
        PlotterJobController controller = new PlotterJobController(null, null, null, null, null);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/v1/plotter/jobs/{plotterJobId}/payments", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":1000,\"paymentDate\":\"2026-08-04\"}"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.status").value(410))
                .andExpect(jsonPath("$.message").value(
                        "Plotter payments must be registered through the customer running account: POST /api/v1/plotter/customer-accounts/{customerId}/payments."
                ));
    }

    private static RegisterPlotterCustomerPaymentCommand command(UUID customerId, String amount, UUID registrationKey) {
        return new RegisterPlotterCustomerPaymentCommand(
                customerId,
                new BigDecimal(amount),
                LocalDate.of(2026, 8, 4),
                null,
                registrationKey
        );
    }

    private static final class Harness {
        private final UUID customerId = UUID.randomUUID();
        private final PlotterJob job;
        private final List<PlotterJob> jobs = new ArrayList<>();
        private final InMemoryPayments payments = new InMemoryPayments();
        private final List<UUID> incomePaymentIds = new ArrayList<>();
        private int internalLedgerCalls;
        private final RegisterPlotterCustomerPaymentUseCase useCase;
        private final EditPlotterJobUseCase edit;
        private final CancelPlotterJobUseCase cancel;

        private Harness(String outstanding) {
            this.job = PlotterJob.create(
                    customerId,
                    LocalDate.of(2026, 8, 1),
                    UUID.randomUUID(),
                    new BigDecimal("1"),
                    new BigDecimal(outstanding),
                    null
            );
            jobs.add(job);
            JobRepository jobRepository = new JobRepository(jobs);
            PlotterPaymentFinancePort finance = (plotterPaymentId, amount, paymentDate, observation) -> {
                if (!incomePaymentIds.contains(plotterPaymentId)) {
                    incomePaymentIds.add(plotterPaymentId);
                }
            };
            this.useCase = new RegisterPlotterCustomerPaymentUseCase(
                    jobRepository,
                    payments,
                    finance,
                    new Orders()
            );
            PlotterInternalServiceFinancePort internalFinance = new PlotterInternalServiceFinancePort() {
                @Override
                public void ensureInternalServiceLedger(UUID plotterJobId, BigDecimal amount, LocalDate transactionDate, String observation) {
                    internalLedgerCalls++;
                }

                @Override
                public void synchronizeInternalServiceLedger(UUID plotterJobId, BigDecimal amount, LocalDate transactionDate, String observation) {
                    internalLedgerCalls++;
                }

                @Override
                public void reverseInternalServiceLedger(UUID plotterJobId) {
                    internalLedgerCalls++;
                }

                @Override
                public boolean hasInternalServiceLedger(UUID plotterJobId) {
                    return false;
                }
            };
            PlotterJobInventoryPort inventory = new PlotterJobInventoryPort() {
                @Override
                public PlotterPaperRollView requirePlotterPaperRoll(UUID paperInventoryItemId) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public com.magyen.platform.plotter.application.port.PlotterJobInventoryConsumeResult consumePaperMeters(
                        UUID paperInventoryItemId,
                        BigDecimal printedMeters,
                        UUID plotterJobId,
                        String observation
                ) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void correctPaperConsumption(UUID paperInventoryItemId, BigDecimal printedMeters, UUID plotterJobId, String observation) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void reversePaperConsumption(UUID plotterJobId) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean hasPaperConsumption(UUID plotterJobId) {
                    return false;
                }
            };
            this.edit = new EditPlotterJobUseCase(jobRepository, payments, inventory, new Orders(), internalFinance);
            this.cancel = new CancelPlotterJobUseCase(jobRepository, payments, inventory, internalFinance);
        }

        private static Harness withOutstanding(String outstanding) {
            return new Harness(outstanding);
        }
    }

    private static final class JobRepository implements PlotterJobRepository {
        private final List<PlotterJob> jobs;

        private JobRepository(List<PlotterJob> jobs) {
            this.jobs = jobs;
        }

        @Override
        public PlotterJob save(PlotterJob plotterJob) {
            jobs.removeIf(job -> job.getId().equals(plotterJob.getId()));
            jobs.add(plotterJob);
            return plotterJob;
        }

        @Override
        public Optional<PlotterJob> findById(UUID id) {
            return jobs.stream().filter(job -> job.getId().equals(id)).findFirst();
        }

        @Override
        public List<PlotterJob> findByOrderId(UUID orderId) {
            return List.of();
        }

        @Override
        public List<PlotterJob> findAll() {
            return List.copyOf(jobs);
        }

        @Override
        public void deleteById(UUID id) {
            jobs.removeIf(job -> job.getId().equals(id));
        }
    }

    private static final class InMemoryPayments implements PlotterPaymentRepository {
        private final List<PlotterPayment> payments = new ArrayList<>();
        private boolean publishRegistrationKey = true;

        @Override
        public PlotterPayment save(PlotterPayment payment) {
            boolean duplicateKey = payment.getRegistrationKey() != null && payments.stream()
                    .anyMatch(existing -> payment.getRegistrationKey().equals(existing.getRegistrationKey()));
            if (duplicateKey) {
                publishRegistrationKey = true;
                throw new DataIntegrityViolationException("uq_plotter_payments_registration_key");
            }
            payments.add(payment);
            return payment;
        }

        @Override
        public Optional<PlotterPayment> findById(UUID id) {
            return payments.stream().filter(payment -> payment.getId().equals(id)).findFirst();
        }

        @Override
        public List<PlotterPayment> findByPlotterJobIdNewestFirst(UUID plotterJobId) {
            return payments.stream()
                    .filter(payment -> plotterJobId.equals(payment.getPlotterJobId()))
                    .toList();
        }

        @Override
        public List<PlotterPayment> findAll() {
            return List.copyOf(payments);
        }

        @Override
        public Optional<PlotterPayment> findByRegistrationKey(UUID registrationKey) {
            if (!publishRegistrationKey) {
                return Optional.empty();
            }
            return payments.stream()
                    .filter(payment -> registrationKey.equals(payment.getRegistrationKey()))
                    .findFirst();
        }

        private int size() {
            return payments.size();
        }

        private PlotterPayment get(int index) {
            return payments.get(index);
        }
    }

    private static final class Orders implements PlotterCommercialOrderPort {
        @Override
        public PlotterCommercialOrderView requireExistingOrder(UUID orderId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PlotterCommercialOrderView requireOrderOpenForPlotterJob(UUID orderId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<PlotterCommercialOrderView> findOrdersOpenForPlotterJob() {
            return List.of();
        }

        @Override
        public PlotterCommercialOrderView requireOrderEligibleForHistoricalPlotterJob(UUID orderId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<PlotterCommercialOrderView> findOrdersEligibleForHistoricalPlotterJob(LocalDate fromDate, LocalDate toDate) {
            return List.of();
        }

        @Override
        public Optional<PlotterCommercialOrderView> findOrder(UUID orderId) {
            return Optional.empty();
        }

        @Override
        public Optional<String> findCustomerName(UUID customerId) {
            return Optional.empty();
        }

        @Override
        public void requireExternalPlotterCustomer(UUID customerId) {
        }
    }
}
