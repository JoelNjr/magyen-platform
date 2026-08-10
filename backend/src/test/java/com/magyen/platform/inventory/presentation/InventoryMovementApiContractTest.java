package com.magyen.platform.inventory.presentation;

import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class InventoryMovementApiContractTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    private MockMvc mockMvc;
    private InventoryItem inventoryItem;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        inventoryItem = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("API-" + UUID.randomUUID().toString().substring(0, 8)),
                "Hilo blanco",
                "THREAD",
                "ROLL",
                new BigDecimal("8.0000"),
                new BigDecimal("2.0000"),
                "Hilo blanco industrial"
        ));
    }

    @Test
    void registersValidMovementWithCreatedStatus() throws Exception {
        mockMvc.perform(
                        post("/api/v1/inventory/{inventoryItemId}/movements", inventoryItem.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "movementType": "IN",
                                          "quantity": 2.0000,
                                          "unitOfMeasure": "ROLL",
                                          "observation": "Purchase order receipt"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movementId").exists())
                .andExpect(jsonPath("$.inventoryItemId").value(inventoryItem.getId().toString()))
                .andExpect(jsonPath("$.movementType").value("IN"))
                .andExpect(jsonPath("$.quantity").value(2.0))
                .andExpect(jsonPath("$.resultingStock").value(10.0))
                .andExpect(jsonPath("$.observation").value("Purchase order receipt"));
    }

    @Test
    void rejectsInvalidMovementWithBadRequest() throws Exception {
        mockMvc.perform(
                        post("/api/v1/inventory/{inventoryItemId}/movements", inventoryItem.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "movementType": "OUT",
                                          "quantity": 50.0000,
                                          "observation": "Too much"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingInventoryItemWithBadRequest() throws Exception {
        mockMvc.perform(
                        post("/api/v1/inventory/{inventoryItemId}/movements", UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "movementType": "IN",
                                          "quantity": 1.0000
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void preservesExistingIncreaseAndDecreaseStockContracts() throws Exception {
        mockMvc.perform(
                        patch("/api/v1/inventory/{inventoryItemId}/increase-stock", inventoryItem.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "quantity": 1.5000
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventoryItemId").value(inventoryItem.getId().toString()))
                .andExpect(jsonPath("$.stock").value(9.5))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(
                        patch("/api/v1/inventory/{inventoryItemId}/decrease-stock", inventoryItem.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "quantity": 0.5000
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(9.0))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
