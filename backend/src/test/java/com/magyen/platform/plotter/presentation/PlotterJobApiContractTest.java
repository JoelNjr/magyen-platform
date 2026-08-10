package com.magyen.platform.plotter.presentation;

import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class PlotterJobApiContractTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    private MockMvc mockMvc;
    private CreateInventoryItemResult paperRoll;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        paperRoll = createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "PLTAPI-" + UUID.randomUUID().toString().substring(0, 8),
                "Papel plotter API",
                "PAPER",
                "METER",
                new BigDecimal("100.0000"),
                new BigDecimal("20.0000"),
                null,
                new BigDecimal("4500.00"),
                "PAPER",
                true
        ));
    }

    @Test
    void createsListsAndGetsPlotterJob() throws Exception {
        UUID customerId = UUID.randomUUID();

        MvcResult createResult = mockMvc.perform(
                        post("/api/v1/plotter/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "paperInventoryItemId": "%s",
                                          "printedMeters": 10.5,
                                          "pricePerMeter": 8000,
                                          "observations": "Trabajo para uniformes"
                                        }
                                        """.formatted(customerId, paperRoll.inventoryItemId()))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plotterJobId").exists())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.paperInventoryItemId").value(paperRoll.inventoryItemId().toString()))
                .andExpect(jsonPath("$.printedMeters").value(10.5))
                .andExpect(jsonPath("$.pricePerMeter").value(8000.0))
                .andExpect(jsonPath("$.totalAmount").value(84000.0))
                .andExpect(jsonPath("$.status").value("REGISTERED"))
                .andExpect(jsonPath("$.observations").value("Trabajo para uniformes"))
                .andExpect(jsonPath("$.creationDate").exists())
                .andReturn();

        String plotterJobId = com.jayway.jsonpath.JsonPath.read(
                createResult.getResponse().getContentAsString(),
                "$.plotterJobId"
        );

        mockMvc.perform(get("/api/v1/plotter/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobs[*].plotterJobId", hasItem(plotterJobId)));

        mockMvc.perform(get("/api/v1/plotter/jobs/{plotterJobId}", plotterJobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plotterJobId").value(plotterJobId))
                .andExpect(jsonPath("$.totalAmount").value(84000.0))
                .andExpect(jsonPath("$.status").value("REGISTERED"));
    }

    @Test
    void rejectsInvalidMaterialInsufficientStockAndInvalidValues() throws Exception {
        CreateInventoryItemResult fabric = createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "FABAPI-" + UUID.randomUUID().toString().substring(0, 8),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("50.0000"),
                null,
                null,
                null,
                "FABRIC",
                false
        ));

        UUID customerId = UUID.randomUUID();

        mockMvc.perform(
                        post("/api/v1/plotter/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "paperInventoryItemId": "%s",
                                          "printedMeters": 1,
                                          "pricePerMeter": 8000
                                        }
                                        """.formatted(customerId, fabric.inventoryItemId()))
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/plotter/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "paperInventoryItemId": "%s",
                                          "printedMeters": 200,
                                          "pricePerMeter": 8000
                                        }
                                        """.formatted(customerId, paperRoll.inventoryItemId()))
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/plotter/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "paperInventoryItemId": "%s",
                                          "printedMeters": 0,
                                          "pricePerMeter": 8000
                                        }
                                        """.formatted(customerId, paperRoll.inventoryItemId()))
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/plotter/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "paperInventoryItemId": "%s",
                                          "printedMeters": 10.5,
                                          "pricePerMeter": -1
                                        }
                                        """.formatted(customerId, paperRoll.inventoryItemId()))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingRequiredFields() throws Exception {
        mockMvc.perform(
                        post("/api/v1/plotter/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "printedMeters": 10.5,
                                          "pricePerMeter": 8000
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }
}
