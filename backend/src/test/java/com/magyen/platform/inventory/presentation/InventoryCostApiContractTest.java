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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class InventoryCostApiContractTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void createAcceptsOptionalUnitCostAndGetExposesIt() throws Exception {
        String code = "CA-" + UUID.randomUUID().toString().substring(0, 8);

        String createBody = """
                {
                  "code": "%s",
                  "name": "Tela deportiva",
                  "category": "FABRIC",
                  "unitOfMeasure": "METER",
                  "stock": 100.0000,
                  "unitCost": 15000.00
                }
                """.formatted(code);

        String responseBody = mockMvc.perform(
                        post("/api/v1/inventory")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unitCost").value(15000.00))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String inventoryItemId = com.jayway.jsonpath.JsonPath.read(responseBody, "$.inventoryItemId");

        mockMvc.perform(get("/api/v1/inventory/{inventoryItemId}", inventoryItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unitCost").value(15000.00));
    }

    @Test
    void legacyCreateWithoutUnitCostRemainsValid() throws Exception {
        String code = "CL-" + UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(
                        post("/api/v1/inventory")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "code": "%s",
                                          "name": "Material legacy",
                                          "category": "OTHER",
                                          "unitOfMeasure": "UNIT",
                                          "stock": 5.0000
                                        }
                                        """.formatted(code))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unitCost").value(nullValue()));
    }

    @Test
    void updatesUnitCostAndMovementResponseExposesCostSnapshots() throws Exception {
        InventoryItem inventoryItem = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("CM-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("100.0000"),
                null,
                null,
                new BigDecimal("15000.00")
        ));

        mockMvc.perform(
                        post("/api/v1/inventory/{inventoryItemId}/movements", inventoryItem.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "movementType": "OUT",
                                          "quantity": 20.0000,
                                          "observation": "costed out"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unitCost").value(15000.00))
                .andExpect(jsonPath("$.totalCost").value(300000.00))
                .andExpect(jsonPath("$.resultingStock").value(80.0));

        mockMvc.perform(
                        patch("/api/v1/inventory/{inventoryItemId}/unit-cost", inventoryItem.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "unitCost": 18000.00
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unitCost").value(18000.00))
                .andExpect(jsonPath("$.stock").value(80.0));

        mockMvc.perform(get("/api/v1/inventory/{inventoryItemId}/movements", inventoryItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movements[0].unitCost").value(15000.00))
                .andExpect(jsonPath("$.movements[0].totalCost").value(300000.00));

        mockMvc.perform(
                        post("/api/v1/inventory/{inventoryItemId}/movements", inventoryItem.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "movementType": "OUT",
                                          "quantity": 10.0000
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unitCost").value(18000.00))
                .andExpect(jsonPath("$.totalCost").value(180000.00));
    }

    @Test
    void rejectsNegativeUnitCost() throws Exception {
        InventoryItem inventoryItem = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("CN-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("10.0000"),
                null
        ));

        mockMvc.perform(
                        patch("/api/v1/inventory/{inventoryItemId}/unit-cost", inventoryItem.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "unitCost": -1.00
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }
}
