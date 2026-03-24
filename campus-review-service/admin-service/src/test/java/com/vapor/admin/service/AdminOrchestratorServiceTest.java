package com.vapor.admin.service;

import com.vapor.common.api.ApiResponse;
import com.vapor.common.util.UserContextUtil;
import com.vapor.common.web.UserContext;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * AdminOrchestratorService 单元测试。
 */
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class AdminOrchestratorServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private UserContext userContext;

    private AdminOrchestratorService adminService;

    private static final ParameterizedTypeReference<ApiResponse<List<UserDTO>>> USER_LIST_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<ApiResponse<List<RestaurantDTO>>> RESTAURANT_LIST_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<ApiResponse<RestaurantDTO>> RESTAURANT_CREATE_TYPE =
            new ParameterizedTypeReference<>() {};

    @BeforeEach
    void setUp() {
        adminService = new AdminOrchestratorService(restClient, "http://localhost:8101", "http://localhost:8103");
    }

    @Test
    @DisplayName("获取用户列表成功")
    void getUserList_success() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            when(userContext.getUserId()).thenReturn(100L);
            when(userContext.getRoles()).thenReturn(Set.of("ADMIN"));

            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

            ApiResponse<List<UserDTO>> response = ApiResponse.ok(List.of(
                    new UserDTO(1L, "zhangsan@example.edu.cn", "2024001", "张三", Set.of("USER"), false, Instant.now())
            ));
            when(responseSpec.body(USER_LIST_TYPE)).thenReturn(response);

            // When
            List<UserDTO> result = adminService.getUserList();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("张三", result.get(0).nickname());
            verify(restClient).get();
        }
    }

    @Test
    @DisplayName("获取用户列表 - 返回空列表")
    void getUserList_emptyList() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            when(userContext.getUserId()).thenReturn(100L);
            when(userContext.getRoles()).thenReturn(Set.of("ADMIN"));

            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(USER_LIST_TYPE)).thenReturn(null);

            // When
            List<UserDTO> result = adminService.getUserList();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    @DisplayName("封禁用户成功")
    void banUser_success() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            when(userContext.getUserId()).thenReturn(100L);
            when(userContext.getRoles()).thenReturn(Set.of("ADMIN"));

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toBodilessEntity()).thenReturn(null);

            // When
            assertDoesNotThrow(() -> adminService.banUser(1L));

            // Then
            verify(restClient).post();
        }
    }

    @Test
    @DisplayName("解封用户成功")
    void unbanUser_success() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            when(userContext.getUserId()).thenReturn(100L);
            when(userContext.getRoles()).thenReturn(Set.of("ADMIN"));

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toBodilessEntity()).thenReturn(null);

            // When
            assertDoesNotThrow(() -> adminService.unbanUser(1L));

            // Then
            verify(restClient).post();
        }
    }

    @Test
    @DisplayName("获取餐厅列表成功")
    void getRestaurantList_success() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            when(userContext.getUserId()).thenReturn(100L);
            when(userContext.getRoles()).thenReturn(Set.of("ADMIN"));

            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

            ApiResponse<List<RestaurantDTO>> response = ApiResponse.ok(List.of(
                    new RestaurantDTO(1L, "第一食堂", "南湖校区", "校园北区 1 号楼", "提供各式家常菜", "https://example.com/1.jpg", Instant.now())
            ));
            when(responseSpec.body(RESTAURANT_LIST_TYPE)).thenReturn(response);

            // When
            List<RestaurantDTO> result = adminService.getRestaurantList();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("第一食堂", result.get(0).name());
            verify(restClient).get();
        }
    }

    @Test
    @DisplayName("获取餐厅列表 - 返回空列表")
    void getRestaurantList_emptyList() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            when(userContext.getUserId()).thenReturn(100L);
            when(userContext.getRoles()).thenReturn(Set.of("ADMIN"));

            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(RESTAURANT_LIST_TYPE)).thenReturn(null);

            // When
            List<RestaurantDTO> result = adminService.getRestaurantList();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    @DisplayName("创建餐厅成功")
    void createRestaurant_success() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            when(userContext.getUserId()).thenReturn(100L);
            when(userContext.getRoles()).thenReturn(Set.of("ADMIN"));

            RestaurantCreateRequest request = new RestaurantCreateRequest(
                    "第一食堂",
                    "南湖校区",
                    "校园北区 1 号楼",
                    "提供各式家常菜",
                    "https://example.com/1.jpg"
            );

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.body(any(RestaurantCreateRequest.class))).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);

            ApiResponse<RestaurantDTO> response = ApiResponse.ok(
                    new RestaurantDTO(1L, "第一食堂", "南湖校区", "校园北区 1 号楼", "提供各式家常菜", "https://example.com/1.jpg", Instant.now())
            );
            when(responseSpec.body(RESTAURANT_CREATE_TYPE)).thenReturn(response);

            // When
            RestaurantDTO result = adminService.createRestaurant(request);

            // Then
            assertNotNull(result);
            assertEquals("第一食堂", result.name());
            verify(restClient).post();
        }
    }

    @Test
    @DisplayName("创建餐厅 - 返回 null")
    void createRestaurant_nullResponse() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            when(userContext.getUserId()).thenReturn(100L);
            when(userContext.getRoles()).thenReturn(Set.of("ADMIN"));

            RestaurantCreateRequest request = new RestaurantCreateRequest(
                    "第一食堂",
                    "南湖校区",
                    null,
                    null,
                    null
            );

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.body(any(RestaurantCreateRequest.class))).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(RESTAURANT_CREATE_TYPE)).thenReturn(null);

            // When
            RestaurantDTO result = adminService.createRestaurant(request);

            // Then
            assertNull(result);
        }
    }

    @Test
    @DisplayName("删除餐厅成功")
    void deleteRestaurant_success() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            when(userContext.getUserId()).thenReturn(100L);
            when(userContext.getRoles()).thenReturn(Set.of("ADMIN"));

            when(restClient.delete()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toBodilessEntity()).thenReturn(null);

            // When
            assertDoesNotThrow(() -> adminService.deleteRestaurant(1L));

            // Then
            verify(restClient).delete();
        }
    }

    @Test
    @DisplayName("获取用户列表 - 带有多角色")
    void getUserList_withMultipleRoles() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            when(userContext.getUserId()).thenReturn(100L);
            when(userContext.getRoles()).thenReturn(Set.of("ADMIN", "SUPER_ADMIN"));

            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(USER_LIST_TYPE)).thenReturn(null);

            // When
            adminService.getUserList();

            // Then
            verify(requestHeadersSpec).header(eq("X-User-Roles"), anyString());
        }
    }
}
