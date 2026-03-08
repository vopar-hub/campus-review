package com.vapor.ranking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class RankingServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getHotRestaurants() throws Exception {
        // 验证接口能正常响应（不验证具体数据因为依赖下游服务）
        mockMvc.perform(get("/api/rankings/hot-restaurants")
                        .param("topN", "5"))
                .andExpect(status().isOk());
    }
}
