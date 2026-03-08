package com.vapor.review;


import com.jayway.jsonpath.JsonPath;
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
class ReviewServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createPendingThenApprove() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "restaurantId": 1,
                                  "rating": 5,
                                  "content": "good"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        mockMvc.perform(get("/api/admin/reviews/pending")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").isNumber());

        String pendingJson = mockMvc.perform(get("/api/admin/reviews/pending")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Integer reviewId = JsonPath.read(pendingJson, "$.data[0].id");

        mockMvc.perform(post("/api/admin/reviews/" + reviewId + "/approve")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/reviews")
                        .param("restaurantId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].status").value("APPROVED"));
    }
}

