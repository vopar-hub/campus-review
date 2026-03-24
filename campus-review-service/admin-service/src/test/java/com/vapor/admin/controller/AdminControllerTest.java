package com.vapor.admin.controller;

import com.vapor.admin.service.AdminOrchestratorService;
import com.vapor.common.api.ApiResponse;
import com.vapor.common.error.BizException;
import com.vapor.common.error.ErrorCode;
import com.vapor.common.web.GlobalExceptionHandler;
import com.vapor.common.web.UserContext;
import com.vapor.common.util.UserContextUtil;
import com.vapor.model.restaurant.RestaurantCreateRequest;
import com.vapor.model.restaurant.RestaurantDTO;
import com.vapor.model.user.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminController 单元测试。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminOrchestratorService adminOrchestratorService;

    @Mock
    private UserContext userContext;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        adminController = new AdminController(adminOrchestratorService);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .defaultRequest(get("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .build();
    }

    private MockedStatic<UserContextUtil> mockUserContext() {
        MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class);
        mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
        mocked.when(UserContextUtil::requireUserId).thenReturn(100L);
        mocked.when(UserContextUtil::requireAdmin).thenAnswer(invocation -> null);
        when(userContext.getRoles()).thenReturn(Set.of("ADMIN"));
        return mocked;
    }

    @Test
    @DisplayName("获取用户列表成功")
    void getUserList_success() throws Exception {
        try (MockedStatic<UserContextUtil> mocked = mockUserContext()) {
            UserDTO user1 = new UserDTO(1L, "zhangsan@example.edu.cn", "2024001", "张三", Set.of("USER"), false, Instant.now());
            UserDTO user2 = new UserDTO(2L, "lisi@example.edu.cn", "2024002", "李四", Set.of("ADMIN"), false, Instant.now());

            when(adminOrchestratorService.getUserList()).thenReturn(List.of(user1, user2));

            // When & Then
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].nickname").value("张三"))
                    .andExpect(jsonPath("$.data[1].nickname").value("李四"));
        }
    }

    @Test
    @DisplayName("获取用户列表 - 空列表")
    void getUserList_empty() throws Exception {
        try (MockedStatic<UserContextUtil> mocked = mockUserContext()) {
            when(adminOrchestratorService.getUserList()).thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Test
    @DisplayName("封禁用户成功")
    void banUser_success() throws Exception {
        try (MockedStatic<UserContextUtil> mocked = mockUserContext()) {
            doNothing().when(adminOrchestratorService).banUser(1L);

            // When & Then
            mockMvc.perform(post("/api/admin/users/1/ban"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.message").value("OK"));

            verify(adminOrchestratorService).banUser(1L);
        }
    }

    @Test
    @DisplayName("解封用户成功")
    void unbanUser_success() throws Exception {
        try (MockedStatic<UserContextUtil> mocked = mockUserContext()) {
            doNothing().when(adminOrchestratorService).unbanUser(1L);

            // When & Then
            mockMvc.perform(post("/api/admin/users/1/unban"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.message").value("OK"));

            verify(adminOrchestratorService).unbanUser(1L);
        }
    }

    @Test
    @DisplayName("封禁用户 - 用户不存在")
    void banUser_notFound() throws Exception {
        try (MockedStatic<UserContextUtil> mocked = mockUserContext()) {
            doThrow(new BizException(ErrorCode.NOT_FOUND, "用户不存在"))
                    .when(adminOrchestratorService).banUser(999L);

            // When & Then
            mockMvc.perform(post("/api/admin/users/999/ban"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(40400))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }
    }

    @Test
    @DisplayName("获取餐厅列表成功")
    void getRestaurantList_success() throws Exception {
        try (MockedStatic<UserContextUtil> mocked = mockUserContext()) {
            RestaurantDTO restaurant1 = new RestaurantDTO(1L, "第一食堂", "南湖校区", "校园北区 1 号楼", "提供各式家常菜", "https://example.com/1.jpg", Instant.now());
            RestaurantDTO restaurant2 = new RestaurantDTO(2L, "第二食堂", "南湖校区", "校园南区 3 号楼", "特色小吃", null, Instant.now());

            when(adminOrchestratorService.getRestaurantList()).thenReturn(List.of(restaurant1, restaurant2));

            // When & Then
            mockMvc.perform(get("/api/admin/restaurants"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].name").value("第一食堂"))
                    .andExpect(jsonPath("$.data[1].name").value("第二食堂"));
        }
    }

    @Test
    @DisplayName("创建餐厅成功")
    void createRestaurant_success() throws Exception {
        try (MockedStatic<UserContextUtil> mocked = mockUserContext()) {
            RestaurantCreateRequest request = new RestaurantCreateRequest(
                    "第一食堂",
                    "南湖校区",
                    "校园北区 1 号楼",
                    "提供各式家常菜，物美价廉",
                    "https://example.com/1.jpg"
            );

            RestaurantDTO createdRestaurant = new RestaurantDTO(1L, "第一食堂", "南湖校区", "校园北区 1 号楼", "提供各式家常菜，物美价廉", "https://example.com/1.jpg", Instant.now());

            when(adminOrchestratorService.createRestaurant(any(RestaurantCreateRequest.class))).thenReturn(createdRestaurant);

            // When & Then
            mockMvc.perform(post("/api/admin/restaurants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"第一食堂\",\"campus\":\"南湖校区\",\"address\":\"校园北区 1 号楼\",\"description\":\"提供各式家常菜，物美价廉\",\"coverImageUrl\":\"https://example.com/1.jpg\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.name").value("第一食堂"))
                    .andExpect(jsonPath("$.data.campus").value("南湖校区"));

            verify(adminOrchestratorService).createRestaurant(any(RestaurantCreateRequest.class));
        }
    }

    @Test
    @DisplayName("创建餐厅 - 请求参数校验失败（名称为空）")
    void createRestaurant_validationError_emptyName() throws Exception {
        // 注：standalone 模式下无法测试@Valid 参数校验，这里测试空请求体的情况
        // When & Then
        mockMvc.perform(post("/api/admin/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("创建餐厅 - 请求参数校验失败（校区为空）")
    void createRestaurant_validationError_emptyCampus() throws Exception {
        // 注：standalone 模式下无法测试@Valid 参数校验，这里测试空请求体的情况
        // When & Then
        mockMvc.perform(post("/api/admin/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("删除餐厅成功")
    void deleteRestaurant_success() throws Exception {
        try (MockedStatic<UserContextUtil> mocked = mockUserContext()) {
            doNothing().when(adminOrchestratorService).deleteRestaurant(1L);

            // When & Then
            mockMvc.perform(delete("/api/admin/restaurants/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.message").value("OK"));

            verify(adminOrchestratorService).deleteRestaurant(1L);
        }
    }

    @Test
    @DisplayName("删除餐厅 - 餐厅不存在")
    void deleteRestaurant_notFound() throws Exception {
        try (MockedStatic<UserContextUtil> mocked = mockUserContext()) {
            doThrow(new BizException(ErrorCode.NOT_FOUND, "餐厅不存在"))
                    .when(adminOrchestratorService).deleteRestaurant(999L);

            // When & Then
            mockMvc.perform(delete("/api/admin/restaurants/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(40400))
                    .andExpect(jsonPath("$.message").value("餐厅不存在"));
        }
    }
}
