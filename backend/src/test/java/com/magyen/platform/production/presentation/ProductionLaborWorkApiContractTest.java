package com.magyen.platform.production.presentation;

import com.magyen.platform.production.application.dto.CreateProductionOperatorCommand;
import com.magyen.platform.production.application.dto.CreateProductionOperatorResult;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.usecase.CreateProductionOperatorUseCase;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.application.usecase.PlanProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.StartProductionOrderUseCase;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class ProductionLaborWorkApiContractTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private PlanProductionOrderUseCase planProductionOrderUseCase;

    @Autowired
    private StartProductionOrderUseCase startProductionOrderUseCase;

    @Autowired
    private CreateProductionOperatorUseCase createProductionOperatorUseCase;

    private MockMvc mockMvc;
    private UUID productionOrderId;
    private CreateProductionOperatorResult productionOperator;
    private UUID unknownOperatorId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        ProductionOrder created = productionOrderRepository.save(ProductionOrder.create(
                UUID.randomUUID(),
                LocalDate.now(),
                ProductionPriority.NORMAL,
                null,
                null,
                null
        ));
        productionOrderId = created.getId();

        productionOperator = createProductionOperatorUseCase.execute(new CreateProductionOperatorCommand(
                "Operario-API-" + UUID.randomUUID().toString().substring(0, 8)
        ));
        unknownOperatorId = UUID.randomUUID();
    }

    @Test
    void postsGetsPaysCancelsAndRejectsInvalidFlows() throws Exception {
        mockMvc.perform(
                        post("/api/v1/production-orders/{productionOrderId}/labor", productionOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "operatorEmployeeId": "%s",
                                          "workDate": "2026-08-10",
                                          "operation": "Confección",
                                          "quantity": 100,
                                          "unitOfMeasure": "UNIT",
                                          "unitRate": 800,
                                          "observation": "early"
                                        }
                                        """.formatted(productionOperator.operatorId()))
                )
                .andExpect(status().isBadRequest());

        moveToInProgress();

        mockMvc.perform(
                        post("/api/v1/production-orders/{productionOrderId}/labor", productionOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "operatorEmployeeId": "%s",
                                          "workDate": "2026-08-10",
                                          "operation": "Confección",
                                          "quantity": 100,
                                          "unitOfMeasure": "UNIT",
                                          "unitRate": 800,
                                          "calculatedAmount": 1,
                                          "observation": "client-amount-ignored"
                                        }
                                        """.formatted(productionOperator.operatorId()))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calculatedAmount").value(80000.0))
                .andExpect(jsonPath("$.status").value("PENDING"));

        String created = mockMvc.perform(
                        post("/api/v1/production-orders/{productionOrderId}/labor", productionOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "operatorEmployeeId": "%s",
                                          "workDate": "2026-08-10",
                                          "operation": "Acabado",
                                          "quantity": 10,
                                          "unitOfMeasure": "UNIT",
                                          "unitRate": 500
                                        }
                                        """.formatted(productionOperator.operatorId()))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calculatedAmount").value(5000.0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String laborWorkId = com.jayway.jsonpath.JsonPath.read(created, "$.laborWorkId");

        mockMvc.perform(
                        post("/api/v1/production-orders/{productionOrderId}/labor", productionOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "operatorEmployeeId": "%s",
                                          "workDate": "2026-08-10",
                                          "operation": "Confección",
                                          "quantity": 10,
                                          "unitOfMeasure": "UNIT",
                                          "unitRate": 500
                                        }
                                        """.formatted(unknownOperatorId))
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/production-orders/{productionOrderId}/labor", productionOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.laborWorks", hasSize(2)))
                .andExpect(jsonPath("$.laborCostSummary.totalLaborCost").value(85000.0));

        mockMvc.perform(get(
                        "/api/v1/production-orders/{productionOrderId}/labor/{laborWorkId}",
                        productionOrderId,
                        laborWorkId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.laborWorkId").value(laborWorkId))
                .andExpect(jsonPath("$.calculatedAmount").value(5000.0));

        mockMvc.perform(get("/api/v1/production/labor-operators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operators[?(@.employeeId == '%s')]".formatted(productionOperator.operatorId())).exists())
                .andExpect(jsonPath("$.operators[?(@.employeeId == '%s')]".formatted(unknownOperatorId)).doesNotExist());

        mockMvc.perform(
                        patch(
                                "/api/v1/production-orders/{productionOrderId}/labor/{laborWorkId}/pay",
                                productionOrderId,
                                laborWorkId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "paymentDate": "2026-08-11",
                                          "observation": "pago api"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.financialTransactionId").exists());

        mockMvc.perform(
                        patch(
                                "/api/v1/production-orders/{productionOrderId}/labor/{laborWorkId}/pay",
                                productionOrderId,
                                laborWorkId
                        )
                )
                .andExpect(status().isConflict());

        String cancellable = mockMvc.perform(
                        post("/api/v1/production-orders/{productionOrderId}/labor", productionOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "operatorEmployeeId": "%s",
                                          "workDate": "2026-08-10",
                                          "operation": "Corte",
                                          "quantity": 2,
                                          "unitOfMeasure": "UNIT",
                                          "unitRate": 1000
                                        }
                                        """.formatted(productionOperator.operatorId()))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String cancellableId = com.jayway.jsonpath.JsonPath.read(cancellable, "$.laborWorkId");

        mockMvc.perform(patch(
                        "/api/v1/production-orders/{productionOrderId}/labor/{laborWorkId}/cancel",
                        productionOrderId,
                        cancellableId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/v1/production-orders/{productionOrderId}", productionOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.laborCostSummary.totalLaborCost").value(85000.0))
                .andExpect(jsonPath("$.totalProductionCost").value(85000.0));
    }

    private void moveToInProgress() {
        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                productionOrderId,
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                ProductionPriority.NORMAL
        ));
        startProductionOrderUseCase.execute(new StartProductionOrderCommand(productionOrderId, null));
    }
}
