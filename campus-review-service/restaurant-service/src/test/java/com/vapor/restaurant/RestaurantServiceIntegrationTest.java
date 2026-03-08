package com.vapor.restaurant;

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
class RestaurantServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createThenSearch() throws Exception {
        // 创建餐馆
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "测试餐厅",
                                  "campus": "主校区",
                                  "address": "校园路 1 号",
                                  "description": "测试描述",
                                  "coverImageUrl": "http://example.com/cover.jpg"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value("测试餐厅"));

        // 按名称搜索
        mockMvc.perform(get("/api/restaurants")
                        .param("name", "测试餐厅"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").isNumber());

        // 按校区搜索
        mockMvc.perform(get("/api/restaurants")
                        .param("campus", "主校区"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].campus").value("主校区"));
    }
}
