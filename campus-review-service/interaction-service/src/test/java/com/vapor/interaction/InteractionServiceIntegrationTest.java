package com.vapor.interaction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class InteractionServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void likeAndFavorite() throws Exception {
        // 点赞
        mockMvc.perform(post("/api/interactions/like")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetType": "review",
                                  "targetId": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 收藏
        mockMvc.perform(post("/api/interactions/favorite")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetType": "review",
                                  "targetId": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 查询互动计数
        mockMvc.perform(get("/api/interactions/count")
                        .param("targetType", "review")
                        .param("targetId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.likeCount").isNumber())
                .andExpect(jsonPath("$.data.favoriteCount").isNumber());

        // 取消点赞
        mockMvc.perform(post("/api/interactions/unlike")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetType": "review",
                                  "targetId": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 取消收藏
        mockMvc.perform(post("/api/interactions/unfavorite")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetType": "review",
                                  "targetId": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
