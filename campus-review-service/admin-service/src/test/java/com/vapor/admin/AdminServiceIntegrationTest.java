package com.vapor.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminService 集成测试。
 *
 * 测试后台管理接口的端到端集成，使用 Mock 验证请求能正确处理。
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AdminServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("获取用户列表接口集成测试")
    void getUserList_integration() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("封禁用户接口集成测试")
    void banUser_integration() throws Exception {
        mockMvc.perform(post("/api/admin/users/1/ban")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("解封用户接口集成测试")
    void unbanUser_integration() throws Exception {
        mockMvc.perform(post("/api/admin/users/1/unban")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("获取餐厅列表接口集成测试")
    void getRestaurantList_integration() throws Exception {
        mockMvc.perform(get("/api/admin/restaurants")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("创建餐厅接口集成测试")
    void createRestaurant_integration() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("name", "第一食堂");
        request.put("campus", "南湖校区");
        request.put("address", "校园北区 1 号楼");
        request.put("description", "提供各式家常菜，物美价廉");

        mockMvc.perform(post("/api/admin/restaurants")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("创建餐厅 - 参数校验失败")
    void createRestaurant_validationError() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("campus", "南湖校区");
        // 缺少必填字段 name

        mockMvc.perform(post("/api/admin/restaurants")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("删除餐厅接口集成测试")
    void deleteRestaurant_integration() throws Exception {
        mockMvc.perform(delete("/api/admin/restaurants/1")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("所有管理接口 - 缺少用户上下文")
    void adminOperations_missingUserContext() throws Exception {
        // 不传用户 ID 头，应该返回 401 或 403
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().is4xxClientError());
    }
}
