package com.magyen.platform.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
        "magyen.cors.allowed-origins=https://magyen.com,https://www.magyen.com,https://magyen-platform-frontend.onrender.com"
})
class CorsConfigurationApiContractTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void officialApexOriginIsAllowedOnLoginPreflight() throws Exception {
        mockMvc.perform(
                        options("/api/v1/auth/login")
                                .header(HttpHeaders.ORIGIN, "https://magyen.com")
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                )
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://magyen.com"));
    }

    @Test
    void officialWwwAndLegacyRenderOriginsAreAllowedOnLoginPreflight() throws Exception {
        mockMvc.perform(
                        options("/api/v1/auth/login")
                                .header(HttpHeaders.ORIGIN, "https://www.magyen.com")
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                )
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://www.magyen.com"));

        mockMvc.perform(
                        options("/api/v1/auth/login")
                                .header(HttpHeaders.ORIGIN, "https://magyen-platform-frontend.onrender.com")
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                )
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "https://magyen-platform-frontend.onrender.com"
                ));
    }

    @Test
    void unknownOriginIsRejected() throws Exception {
        mockMvc.perform(
                        options("/api/v1/auth/login")
                                .header(HttpHeaders.ORIGIN, "https://evil.example")
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                )
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
