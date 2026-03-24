package com.vapor.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserService 集成测试。
 *
 * 测试用户管理接口的端到端集成。
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserAdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("获取用户列表接口集成测试")
    void getUserList_integration() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("封禁用户接口集成测试")
    void banUser_integration() throws Exception {
        mockMvc.perform(post("/api/admin/users/1/ban")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    @DisplayName("解封用户接口集成测试")
    void unbanUser_integration() throws Exception {
        mockMvc.perform(post("/api/admin/users/1/unban")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    @DisplayName("封禁用户 - 非管理员拒绝访问")
    void banUser_nonAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/users/1/ban")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("获取用户列表 - 缺少用户 ID")
    void getUserList_missingUserId() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("获取用户列表 - 缺少角色")
    void getUserList_missingRoles() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", "999"))
                .andExpect(status().isForbidden());
    }
}
