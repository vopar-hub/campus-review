package com.vapor.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AdminServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminOperations() throws Exception {
        // 查询待审核评价列表（验证接口能响应，不验证具体数据因为依赖下游服务）
        mockMvc.perform(get("/api/admin/reviews/pending")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk());

        // 封禁用户（验证接口能响应）
        mockMvc.perform(post("/api/admin/users/1/ban")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk());

        // 解封用户（验证接口能响应）
        mockMvc.perform(post("/api/admin/users/1/unban")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk());
    }
}
