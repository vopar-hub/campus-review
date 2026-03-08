package com.vapor.riskcontrol;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RiskControlServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void auditAndRateLimit() throws Exception {
        mockMvc.perform(post("/api/risk/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.allowed").value(true));

        mockMvc.perform(get("/api/risk/ratelimit")
                        .param("key", "ip:127.0.0.1")
                        .param("limit", "1")
                        .param("windowSeconds", "60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.allowed").value(true));

        mockMvc.perform(get("/api/risk/ratelimit")
                        .param("key", "ip:127.0.0.1")
                        .param("limit", "1")
                        .param("windowSeconds", "60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.allowed").value(false));
    }
}

