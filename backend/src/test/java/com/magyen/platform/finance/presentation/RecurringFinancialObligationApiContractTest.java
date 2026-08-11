package com.magyen.platform.finance.presentation;

import com.magyen.platform.finance.domain.FinancialTransactionRepository;
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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class RecurringFinancialObligationApiContractTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void obligationCrudEndpointsWorkWithoutCreatingTransactions() throws Exception {
        long transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();

        MvcResult created = mockMvc.perform(
                        post("/api/v1/finance/obligations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Internet",
                                          "type": "SERVICE",
                                          "expectedAmount": 120000.00,
                                          "frequency": "MONTHLY",
                                          "dueDay": 15,
                                          "startDate": "2026-08-01",
                                          "endDate": null,
                                          "description": "Internet del taller",
                                          "observation": null
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.obligationId").exists())
                .andExpect(jsonPath("$.name").value("Internet"))
                .andExpect(jsonPath("$.type").value("SERVICE"))
                .andExpect(jsonPath("$.expectedAmount").value(120000.00))
                .andExpect(jsonPath("$.frequency").value("MONTHLY"))
                .andExpect(jsonPath("$.dueDay").value(15))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.endDate").value(nullValue()))
                .andReturn();

        String obligationId = com.jayway.jsonpath.JsonPath.read(
                created.getResponse().getContentAsString(),
                "$.obligationId"
        );

        mockMvc.perform(get("/api/v1/finance/obligations/{obligationId}", obligationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.obligationId").value(obligationId))
                .andExpect(jsonPath("$.name").value("Internet"));

        mockMvc.perform(get("/api/v1/finance/obligations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.obligations.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.obligations[*].obligationId", hasItem(obligationId)));

        mockMvc.perform(
                        put("/api/v1/finance/obligations/{obligationId}", obligationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Internet fibra",
                                          "type": "SERVICE",
                                          "expectedAmount": 130000.00,
                                          "frequency": "MONTHLY",
                                          "dueDay": 20,
                                          "startDate": "2026-08-01",
                                          "endDate": "2027-08-01",
                                          "description": "Actualizado",
                                          "observation": "Obs"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Internet fibra"))
                .andExpect(jsonPath("$.expectedAmount").value(130000.00))
                .andExpect(jsonPath("$.dueDay").value(20))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(patch("/api/v1/finance/obligations/{obligationId}/deactivate", obligationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.obligationId").value(obligationId))
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/api/v1/finance/obligations?active=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.obligations[*].obligationId", not(hasItem(obligationId))));

        assertEquals(
                transactionsBefore,
                financialTransactionRepository.findAllNewestFirst().size(),
                "Creating/updating/deactivating an obligation must not create FinancialTransactions"
        );
    }

    @Test
    void rejectsInvalidPayload() throws Exception {
        mockMvc.perform(
                        post("/api/v1/finance/obligations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Internet",
                                          "type": "SERVICE",
                                          "expectedAmount": 0,
                                          "frequency": "MONTHLY",
                                          "dueDay": 15,
                                          "startDate": "2026-08-01"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/finance/obligations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "type": "SERVICE",
                                          "expectedAmount": 100.00,
                                          "frequency": "MONTHLY",
                                          "startDate": "2026-08-01"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/finance/obligations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Internet",
                                          "type": "SERVICE",
                                          "expectedAmount": 100.00,
                                          "frequency": "DAILY",
                                          "startDate": "2026-08-01"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/finance/obligations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Internet",
                                          "type": "SERVICE",
                                          "expectedAmount": 100.00,
                                          "frequency": "MONTHLY",
                                          "dueDay": 32,
                                          "startDate": "2026-08-01"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }
}
