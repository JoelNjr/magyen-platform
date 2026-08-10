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

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class InventoryMovementSourceApiContractTest {

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
                MaterialCode.of("SRCAPI-" + UUID.randomUUID().toString().substring(0, 8)),
                "Hilo",
                "THREAD",
                "ROLL",
                new BigDecimal("12.0000"),
                null,
                null,
                new BigDecimal("500.00")
        ));
    }

    @Test
    void legacyRequestDefaultsToManualSource() throws Exception {
        mockMvc.perform(
                        post("/api/v1/inventory/{inventoryItemId}/movements", inventoryItem.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "movementType": "OUT",
                                          "quantity": 2.0000,
                                          "observation": "Salida manual"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceType").value("MANUAL"))
                .andExpect(jsonPath("$.sourceId").value(nullValue()))
                .andExpect(jsonPath("$.unitCost").value(500.00))
                .andExpect(jsonPath("$.totalCost").value(1000.00))
                .andExpect(jsonPath("$.resultingStock").value(10.0));

        mockMvc.perform(get("/api/v1/inventory/{inventoryItemId}/movements", inventoryItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movements[0].sourceType").value("MANUAL"))
                .andExpect(jsonPath("$.movements[0].sourceId").value(nullValue()));
    }

    @Test
    void explicitManualRequestWorks() throws Exception {
        mockMvc.perform(
                        post("/api/v1/inventory/{inventoryItemId}/movements", inventoryItem.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "movementType": "IN",
                                          "quantity": 1.0000,
                                          "sourceType": "MANUAL",
                                          "sourceId": null,
                                          "observation": "Entrada manual"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceType").value("MANUAL"))
                .andExpect(jsonPath("$.sourceId").value(nullValue()));
    }

    @Test
    void rejectsInvalidSourceCombination() throws Exception {
        mockMvc.perform(
                        post("/api/v1/inventory/{inventoryItemId}/movements", inventoryItem.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "movementType": "OUT",
                                          "quantity": 1.0000,
                                          "sourceType": "PRODUCTION"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/inventory/{inventoryItemId}/movements", inventoryItem.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "movementType": "OUT",
                                          "quantity": 1.0000,
                                          "sourceType": "UNKNOWN"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }
}
