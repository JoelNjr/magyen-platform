package com.magyen.platform.production.presentation;

import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.MaterialCode;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class ProductionMaterialConsumptionApiContractTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private PlanProductionOrderUseCase planProductionOrderUseCase;

    @Autowired
    private StartProductionOrderUseCase startProductionOrderUseCase;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    private MockMvc mockMvc;
    private UUID productionOrderId;
    private InventoryItem fabric;
    private InventoryItem thread;

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

        fabric = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("APIF-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("100.0000"),
                null,
                null,
                new BigDecimal("18000.00")
        ));
        thread = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("APIT-" + UUID.randomUUID().toString().substring(0, 8)),
                "Hilo",
                "THREAD",
                "ROLL",
                new BigDecimal("10.0000"),
                null
        ));
    }

    @Test
    void postsConsumptionIntegratesInventoryAndGetsHistory() throws Exception {
        moveToInProgress();

        String response = mockMvc.perform(
                        post("/api/v1/production-orders/{productionOrderId}/material-consumptions", productionOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "inventoryItemId": "%s",
                                          "quantity": 18.7000,
                                          "unitOfMeasure": "METER",
                                          "observation": "first"
                                        }
                                        """.formatted(fabric.getId()))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consumptionId").exists())
                .andExpect(jsonPath("$.productionOrderId").value(productionOrderId.toString()))
                .andExpect(jsonPath("$.inventoryItemId").value(fabric.getId().toString()))
                .andExpect(jsonPath("$.quantity").value(18.7))
                .andExpect(jsonPath("$.unitOfMeasure").value("METER"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String consumptionId = com.jayway.jsonpath.JsonPath.read(response, "$.consumptionId");

        mockMvc.perform(
                        post("/api/v1/production-orders/{productionOrderId}/material-consumptions", productionOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "inventoryItemId": "%s",
                                          "quantity": 2.0000,
                                          "unitOfMeasure": "ROLL",
                                          "observation": "second"
                                        }
                                        """.formatted(thread.getId()))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/production-orders/{productionOrderId}/material-consumptions", productionOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consumptions", hasSize(2)))
                .andExpect(jsonPath("$.consumptions[?(@.observation == 'first')].unitCost").value(18000.0))
                .andExpect(jsonPath("$.consumptions[?(@.observation == 'first')].totalCost").value(336600.0))
                .andExpect(jsonPath("$.materialCostSummary.totalMaterialCost").value(336600.0))
                .andExpect(jsonPath("$.materialCostSummary.consumptionCount").value(2))
                .andExpect(jsonPath("$.materialCostSummary.valuedConsumptionCount").value(1))
                .andExpect(jsonPath("$.materialCostSummary.unvaluedConsumptionCount").value(1))
                .andExpect(jsonPath("$.consumptions[?(@.observation == 'first')]").exists())
                .andExpect(jsonPath("$.consumptions[?(@.observation == 'second')]").exists());

        mockMvc.perform(get("/api/v1/production-orders/{productionOrderId}", productionOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materialCostSummary.totalMaterialCost").value(336600.0))
                .andExpect(jsonPath("$.materialCostSummary.consumptionCount").value(2))
                .andExpect(jsonPath("$.materialCostSummary.valuedConsumptionCount").value(1))
                .andExpect(jsonPath("$.materialCostSummary.unvaluedConsumptionCount").value(1));

        assertEquals(new BigDecimal("81.3000"), inventoryItemRepository.findById(fabric.getId()).orElseThrow().getStock());
        assertEquals(
                InventoryMovementSourceType.PRODUCTION,
                inventoryMovementRepository.findBySourceTypeAndSourceId(
                        InventoryMovementSourceType.PRODUCTION,
                        UUID.fromString(consumptionId)
                ).orElseThrow().getSourceType()
        );
    }

    @Test
    void rejectsInvalidLifecycleQuantityAndUnit() throws Exception {
        mockMvc.perform(
                        post("/api/v1/production-orders/{productionOrderId}/material-consumptions", productionOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "inventoryItemId": "%s",
                                          "quantity": 1.0000,
                                          "unitOfMeasure": "METER"
                                        }
                                        """.formatted(fabric.getId()))
                )
                .andExpect(status().isBadRequest());

        moveToInProgress();

        mockMvc.perform(
                        post("/api/v1/production-orders/{productionOrderId}/material-consumptions", productionOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "inventoryItemId": "%s",
                                          "quantity": 0,
                                          "unitOfMeasure": "METER"
                                        }
                                        """.formatted(fabric.getId()))
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/production-orders/{productionOrderId}/material-consumptions", productionOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "inventoryItemId": "%s",
                                          "quantity": 1.0000,
                                          "unitOfMeasure": "YARD"
                                        }
                                        """.formatted(fabric.getId()))
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/production-orders/{productionOrderId}/material-consumptions", productionOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "inventoryItemId": "%s",
                                          "quantity": 1.0000,
                                          "unitOfMeasure": "ROLL"
                                        }
                                        """.formatted(fabric.getId()))
                )
                .andExpect(status().isBadRequest());
    }

    private void moveToInProgress() {
        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                productionOrderId,
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                ProductionPriority.NORMAL
        ));
        startProductionOrderUseCase.execute(new StartProductionOrderCommand(productionOrderId));
    }
}
