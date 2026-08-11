package com.magyen.platform.finance.presentation;

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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class FinancialTransactionApiContractTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void postCreatesAndGetEndpointsReturnTransaction() throws Exception {
        MvcResult created = mockMvc.perform(
                        post("/api/v1/finance/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "type": "EXPENSE",
                                          "amount": 150000.00,
                                          "transactionDate": "2026-08-10",
                                          "category": "Servicios",
                                          "description": "Pago de energía",
                                          "observation": "Factura agosto",
                                          "sourceType": "SERVICE",
                                          "sourceId": null
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.amount").value(150000.00))
                .andExpect(jsonPath("$.transactionDate").value("2026-08-10"))
                .andExpect(jsonPath("$.category").value("Servicios"))
                .andExpect(jsonPath("$.description").value("Pago de energía"))
                .andExpect(jsonPath("$.observation").value("Factura agosto"))
                .andExpect(jsonPath("$.sourceType").value("SERVICE"))
                .andExpect(jsonPath("$.sourceId").value(nullValue()))
                .andReturn();

        String transactionId = com.jayway.jsonpath.JsonPath.read(
                created.getResponse().getContentAsString(),
                "$.transactionId"
        );

        mockMvc.perform(get("/api/v1/finance/transactions/{transactionId}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.amount").value(150000.00));

        mockMvc.perform(get("/api/v1/finance/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.transactions[*].transactionId", hasItem(transactionId)));
    }

    @Test
    void rejectsInvalidAmount() throws Exception {
        mockMvc.perform(
                        post("/api/v1/finance/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "type": "EXPENSE",
                                          "amount": 0,
                                          "transactionDate": "2026-08-10",
                                          "category": "Servicios"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/finance/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "type": "EXPENSE",
                                          "amount": -10.00,
                                          "transactionDate": "2026-08-10",
                                          "category": "Servicios"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingRequiredDataAndInvalidSourceType() throws Exception {
        mockMvc.perform(
                        post("/api/v1/finance/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "amount": 100.00,
                                          "transactionDate": "2026-08-10",
                                          "category": "Servicios"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/finance/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "type": "EXPENSE",
                                          "transactionDate": "2026-08-10",
                                          "category": "Servicios"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/finance/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "type": "EXPENSE",
                                          "amount": 100.00,
                                          "category": "Servicios"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/finance/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "type": "EXPENSE",
                                          "amount": 100.00,
                                          "transactionDate": "2026-08-10"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/finance/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "type": "EXPENSE",
                                          "amount": 100.00,
                                          "transactionDate": "2026-08-10",
                                          "category": "Servicios",
                                          "sourceType": "UNKNOWN"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMalformedUuid() throws Exception {
        mockMvc.perform(get("/api/v1/finance/transactions/{transactionId}", "not-a-uuid"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/finance/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "type": "INCOME",
                                          "amount": 100.00,
                                          "transactionDate": "2026-08-10",
                                          "category": "Ventas",
                                          "sourceType": "COMMERCIAL_ORDER",
                                          "sourceId": "not-a-uuid"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }
}
