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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class InventoryReadApiContractTest {

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
                MaterialCode.of("API3-" + UUID.randomUUID().toString().substring(0, 8)),
                "DTF film",
                "FILM",
                "METER",
                new BigDecimal("15.0000"),
                new BigDecimal("20.0000"),
                "DTF film"
        ));
    }

    @Test
    void listsInventoryItems() throws Exception {
        mockMvc.perform(get("/api/v1/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[?(@.inventoryItemId == '" + inventoryItem.getId() + "')].lowStock")
                        .value(org.hamcrest.Matchers.contains(true)));
    }

    @Test
    void getsInventoryDetailWithLowStock() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/{inventoryItemId}", inventoryItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventoryItemId").value(inventoryItem.getId().toString()))
                .andExpect(jsonPath("$.materialCode").value(inventoryItem.getMaterialCode().getValue()))
                .andExpect(jsonPath("$.description").value("DTF film"))
                .andExpect(jsonPath("$.stock").value(15.0))
                .andExpect(jsonPath("$.minimumStock").value(20.0))
                .andExpect(jsonPath("$.unitOfMeasure").value("METER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.lowStock").value(true));
    }

    @Test
    void getsEmptyAndPopulatedMovementHistory() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/{inventoryItemId}/movements", inventoryItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movements", hasSize(0)));

        mockMvc.perform(
                        post("/api/v1/inventory/{inventoryItemId}/movements", inventoryItem.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "movementType": "IN",
                                          "quantity": 1.0000,
                                          "observation": "restock"
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/inventory/{inventoryItemId}/movements", inventoryItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movements", hasSize(1)))
                .andExpect(jsonPath("$.movements[0].movementType").value("IN"))
                .andExpect(jsonPath("$.movements[0].observation").value("restock"))
                .andExpect(jsonPath("$.movements[0].resultingStock").value(16.0));
    }

    @Test
    void updatesMinimumStockAndRejectsInvalidValues() throws Exception {
        mockMvc.perform(
                        patch("/api/v1/inventory/{inventoryItemId}/minimum-stock", inventoryItem.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "minimumStock": 10
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minimumStock").value(10))
                .andExpect(jsonPath("$.stock").value(15.0))
                .andExpect(jsonPath("$.lowStock").value(false));

        mockMvc.perform(
                        patch("/api/v1/inventory/{inventoryItemId}/minimum-stock", inventoryItem.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "minimumStock": -1
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        patch("/api/v1/inventory/{inventoryItemId}/minimum-stock", UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "minimumStock": 5
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/inventory/{inventoryItemId}/movements", UUID.randomUUID()))
                .andExpect(status().isBadRequest());
    }
}
