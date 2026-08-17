package com.magyen.platform.inventory.presentation;

import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMaterialType;
import com.magyen.platform.inventory.domain.MaterialCode;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class InventoryPurchaseApiContractTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    private MockMvc mockMvc;
    private InventoryItem fabric;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        fabric = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("PAPI-" + UUID.randomUUID().toString().substring(0, 8)),
                "Sudáfrica",
                "FABRIC",
                "METER",
                BigDecimal.ZERO,
                null,
                null,
                null,
                InventoryMaterialType.FABRIC,
                null
        ));
    }

    @Test
    void postsPurchaseCreatesStockAndFinanceExpense() throws Exception {
        UUID purchaseId = UUID.randomUUID();

        mockMvc.perform(
                        post("/api/v1/inventory/{inventoryItemId}/purchases", fabric.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "purchaseId": "%s",
                                          "quantity": 100.0000,
                                          "unitCost": 10000.00,
                                          "purchaseDate": "2026-08-16",
                                          "observation": "compra"
                                        }
                                        """.formatted(purchaseId))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.purchaseId").value(purchaseId.toString()))
                .andExpect(jsonPath("$.inventoryItemId").value(fabric.getId().toString()))
                .andExpect(jsonPath("$.materialName").value("Sudáfrica"))
                .andExpect(jsonPath("$.quantity").value(100.0))
                .andExpect(jsonPath("$.unitCost").value(10000.0))
                .andExpect(jsonPath("$.totalCost").value(1000000.0))
                .andExpect(jsonPath("$.resultingStock").value(100.0))
                .andExpect(jsonPath("$.financeCategory").value("MATERIALS"))
                .andExpect(jsonPath("$.alreadyProcessed").value(false))
                .andExpect(jsonPath("$.financialTransactionId").exists());

        mockMvc.perform(
                        post("/api/v1/inventory/{inventoryItemId}/purchases", fabric.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "purchaseId": "%s",
                                          "quantity": 100.0000,
                                          "unitCost": 10000.00,
                                          "purchaseDate": "2026-08-16"
                                        }
                                        """.formatted(purchaseId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alreadyProcessed").value(true))
                .andExpect(jsonPath("$.totalCost").value(1000000.0));

        assertEquals(FinancialTransactionType.EXPENSE, financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.INVENTORY_PURCHASE, purchaseId)
                .orElseThrow()
                .getType());
        assertEquals(new BigDecimal("100.0000"),
                inventoryItemRepository.findById(fabric.getId()).orElseThrow().getStock());
    }

    @Test
    void rejectsZeroQuantityAndZeroUnitCost() throws Exception {
        mockMvc.perform(
                        post("/api/v1/inventory/{inventoryItemId}/purchases", fabric.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "quantity": 0,
                                          "unitCost": 10000.00,
                                          "purchaseDate": "2026-08-16"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/inventory/{inventoryItemId}/purchases", fabric.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "quantity": 1.0000,
                                          "unitCost": 0,
                                          "purchaseDate": "2026-08-16"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }
}
