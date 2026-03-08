package com.vapor.notification;

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
class NotificationServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sendAndReadMessage() throws Exception {
        // 查询收件箱（验证接口能响应）
        mockMvc.perform(get("/api/notifications/inbox")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk());

        // 标记消息为已读（验证接口能响应）
        mockMvc.perform(post("/api/notifications/1/read")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }
}
