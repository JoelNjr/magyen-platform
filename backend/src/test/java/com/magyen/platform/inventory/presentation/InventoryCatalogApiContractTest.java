package com.magyen.platform.inventory.presentation;

import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryItemStatus;
import com.magyen.platform.inventory.domain.InventoryMaterialType;
import com.magyen.platform.inventory.domain.MaterialCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class InventoryCatalogApiContractTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    private MockMvc mockMvc;
    private String materialCode;
    private InventoryItem firstRoll;
    private InventoryItem secondRoll;
    private InventoryItem ink;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        materialCode = "API-" + UUID.randomUUID().toString().substring(0, 8);
        firstRoll = inventoryItemRepository.save(paper(materialCode, uniqueRoll(), "20.0000"));
        secondRoll = inventoryItemRepository.save(paper(materialCode, uniqueRoll(), "33.1000"));
        ink = inventoryItemRepository.save(ink("INK-" + UUID.randomUUID().toString().substring(0, 8), "8.0000"));
    }

    @Test
    void listsCatalogGroupedByMaterialCode() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/materials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materials").isArray())
                .andExpect(jsonPath("$.materials[?(@.materialCode == '" + materialCode + "')]", hasSize(1)))
                .andExpect(jsonPath("$.materials[?(@.materialCode == '" + materialCode + "')].physicalUnitCount")
                        .value(org.hamcrest.Matchers.contains(2)))
                .andExpect(jsonPath("$.materials[?(@.materialCode == '" + materialCode + "')].paperMaterial")
                        .value(org.hamcrest.Matchers.contains(true)))
                .andExpect(jsonPath("$.materials[?(@.materialCode == '" + materialCode + "')].aggregatedStock")
                        .value(org.hamcrest.Matchers.contains(53.1)))
                .andExpect(jsonPath("$.materials[?(@.materialCode == '" + ink.getMaterialCode().getValue() + "')]", hasSize(1)))
                .andExpect(jsonPath("$.materials[?(@.materialCode == '" + ink.getMaterialCode().getValue() + "')].stockHoldingItemId")
                        .value(org.hamcrest.Matchers.contains(ink.getId().toString())));
    }

    @Test
    void materialDetailExposesCatalogIdentityAndPhysicalUnits() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/materials/{materialCode}", materialCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.material.materialCode").value(materialCode))
                .andExpect(jsonPath("$.material.paperMaterial").value(true))
                .andExpect(jsonPath("$.material.physicalUnitCount").value(2))
                .andExpect(jsonPath("$.units", hasSize(2)))
                .andExpect(jsonPath("$.units[?(@.inventoryItemId == '" + firstRoll.getId() + "')].paperRollNumber")
                        .value(org.hamcrest.Matchers.contains(firstRoll.getPaperRollNumber())))
                .andExpect(jsonPath("$.units[?(@.inventoryItemId == '" + secondRoll.getId() + "')].paperRollNumber")
                        .value(org.hamcrest.Matchers.contains(secondRoll.getPaperRollNumber())));
    }

    @Test
    void existingUnitDetailAndPlotterFilterKeepPhysicalSemantics() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/{inventoryItemId}", firstRoll.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventoryItemId").value(firstRoll.getId().toString()))
                .andExpect(jsonPath("$.materialCode").value(materialCode))
                .andExpect(jsonPath("$.paperRollNumber").value(firstRoll.getPaperRollNumber()))
                .andExpect(jsonPath("$.plotterPaperRoll").value(true));

        mockMvc.perform(get("/api/v1/inventory").param("plotterPaperRoll", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.inventoryItemId == '" + firstRoll.getId() + "')].paperRollNumber")
                        .value(org.hamcrest.Matchers.contains(firstRoll.getPaperRollNumber())))
                .andExpect(jsonPath("$.items[?(@.inventoryItemId == '" + firstRoll.getId() + "')].plotterPaperRoll")
                        .value(org.hamcrest.Matchers.contains(true)))
                .andExpect(jsonPath("$.items[?(@.materialCode == '" + ink.getMaterialCode().getValue() + "')]", hasSize(0)));
    }

    @Test
    void deactivatesZeroStockMaterialAndKeepsHistoricalDetail() throws Exception {
        InventoryItem removable = inventoryItemRepository.save(
                ink("OFF-" + UUID.randomUUID().toString().substring(0, 8), "0.0000")
        );

        mockMvc.perform(patch("/api/v1/inventory/materials/{materialCode}/deactivate",
                        removable.getMaterialCode().getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materialCode").value(removable.getMaterialCode().getValue()))
                .andExpect(jsonPath("$.status").value(InventoryItemStatus.INACTIVE.name()))
                .andExpect(jsonPath("$.deactivatedUnitCount").value(1));

        mockMvc.perform(get("/api/v1/inventory/materials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.materials[?(@.materialCode == '" + removable.getMaterialCode().getValue() + "')]",
                        hasSize(0)
                ));

        mockMvc.perform(get("/api/v1/inventory/materials/{materialCode}",
                        removable.getMaterialCode().getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.material.status").value(InventoryItemStatus.INACTIVE.name()));

        mockMvc.perform(patch("/api/v1/inventory/materials/{materialCode}/deactivate",
                        removable.getMaterialCode().getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(InventoryItemStatus.INACTIVE.name()));
    }

    @Test
    void unknownMaterialCodeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/materials/{materialCode}", "MAT-MISSING"))
                .andExpect(status().isBadRequest());
    }

    private static InventoryItem paper(String materialCode, String rollNumber, String stock) {
        return InventoryItem.create(
                MaterialCode.of(materialCode),
                "Papel Plotter",
                "PAPER",
                "METER",
                new BigDecimal(stock),
                new BigDecimal("5.0000"),
                "Papel",
                new BigDecimal("100.00"),
                InventoryMaterialType.PAPER,
                rollNumber
        );
    }

    private static InventoryItem ink(String materialCode, String stock) {
        return InventoryItem.create(
                MaterialCode.of(materialCode),
                "Tinta cian",
                "INK",
                "LITER",
                new BigDecimal(stock),
                null,
                "Tinta",
                new BigDecimal("25000.00"),
                InventoryMaterialType.INK,
                null
        );
    }

    private static String uniqueRoll() {
        return "RP-A" + UUID.randomUUID().toString().substring(0, 8);
    }
}
