package com.vapor.restaurant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RestaurantService 集成测试。
 *
 * 测试餐厅管理接口的端到端集成。
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class RestaurantAdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("获取餐厅列表接口集成测试")
    void getRestaurantList_integration() throws Exception {
        mockMvc.perform(get("/api/admin/restaurants")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").value("success"))
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
        request.put("coverImageUrl", "https://example.com/restaurant/1/cover.jpg");

        mockMvc.perform(post("/api/admin/restaurants")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.name").value("第一食堂"))
                .andExpect(jsonPath("$.data.campus").value("南湖校区"));
    }

    @Test
    @DisplayName("创建餐厅 - 缺少必填字段 name")
    void createRestaurant_missingName() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("campus", "南湖校区");

        mockMvc.perform(post("/api/admin/restaurants")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("创建餐厅 - 缺少必填字段 campus")
    void createRestaurant_missingCampus() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("name", "第一食堂");

        mockMvc.perform(post("/api/admin/restaurants")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("创建餐厅 - 可选字段为空")
    void createRestaurant_optionalFieldsNull() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("name", "新餐厅");
        request.put("campus", "滨湖校区");
        // address, description, coverImageUrl 为 null

        mockMvc.perform(post("/api/admin/restaurants")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("删除餐厅接口集成测试")
    void deleteRestaurant_integration() throws Exception {
        mockMvc.perform(delete("/api/admin/restaurants/1")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    @DisplayName("删除餐厅 - 非管理员拒绝访问")
    void deleteRestaurant_nonAdmin() throws Exception {
        mockMvc.perform(delete("/api/admin/restaurants/1")
                        .header("X-User-Id", "999")
                        .header("X-User-Roles", "USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("获取餐厅列表 - 缺少用户上下文")
    void getRestaurantList_missingUserContext() throws Exception {
        mockMvc.perform(get("/api/admin/restaurants"))
                .andExpect(status().is4xxClientError());
    }
}
