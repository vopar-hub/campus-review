package com.vapor.restaurant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vapor.common.error.BizException;
import com.vapor.common.util.UserContextUtil;
import com.vapor.common.web.UserContext;
import com.vapor.model.restaurant.RestaurantCreateRequest;
import com.vapor.model.restaurant.RestaurantDTO;
import com.vapor.restaurant.entity.RestaurantEntity;
import com.vapor.restaurant.mapper.RestaurantMapper;
import com.vapor.restaurant.service.impl.RestaurantAdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RestaurantAdminServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class RestaurantAdminServiceImplTest {

    @Mock
    private RestaurantMapper restaurantMapper;

    @Mock
    private UserContext userContext;

    @InjectMocks
    private RestaurantAdminServiceImpl restaurantAdminService;

    @BeforeEach
    void setUp() {
        restaurantAdminService = new RestaurantAdminServiceImpl(restaurantMapper);
    }

    @Test
    @DisplayName("获取餐厅列表成功")
    void getRestaurantList_success() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            mocked.when(UserContextUtil::requireUserId).thenReturn(100L);
            mocked.when(UserContextUtil::requireAdmin).thenAnswer(invocation -> null);

            RestaurantEntity entity1 = new RestaurantEntity();
            entity1.setId(1L);
            entity1.setName("第一食堂");
            entity1.setCampus("南湖校区");
            entity1.setAddress("校园北区 1 号楼");
            entity1.setDescription("提供各式家常菜，物美价廉");
            entity1.setCoverImageUrl("https://example.com/restaurant/1/cover.jpg");
            entity1.setCreatedAt(Instant.now());

            RestaurantEntity entity2 = new RestaurantEntity();
            entity2.setId(2L);
            entity2.setName("第二食堂");
            entity2.setCampus("南湖校区");
            entity2.setAddress("校园南区 3 号楼");
            entity2.setDescription("特色小吃");
            entity2.setCoverImageUrl(null);
            entity2.setCreatedAt(Instant.now());

            when(restaurantMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(entity1, entity2));

            // When
            List<RestaurantDTO> result = restaurantAdminService.getRestaurantList();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("第一食堂", result.get(0).name());
            assertEquals("第二食堂", result.get(1).name());
            assertEquals("南湖校区", result.get(0).campus());
            assertNotNull(result.get(0).coverImageUrl());
            assertNull(result.get(1).coverImageUrl());
        }
    }

    @Test
    @DisplayName("获取餐厅列表 - 空列表")
    void getRestaurantList_empty() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            mocked.when(UserContextUtil::requireUserId).thenReturn(100L);
            mocked.when(UserContextUtil::requireAdmin).thenAnswer(invocation -> null);

            when(restaurantMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

            // When
            List<RestaurantDTO> result = restaurantAdminService.getRestaurantList();

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
            mocked.when(UserContextUtil::requireUserId).thenReturn(100L);
            mocked.when(UserContextUtil::requireAdmin).thenAnswer(invocation -> null);

            RestaurantCreateRequest request = new RestaurantCreateRequest(
                    "第一食堂",
                    "南湖校区",
                    "校园北区 1 号楼",
                    "提供各式家常菜，物美价廉",
                    "https://example.com/restaurant/1/cover.jpg"
            );

            RestaurantEntity entity = new RestaurantEntity();
            entity.setId(1L);
            entity.setName("第一食堂");
            entity.setCampus("南湖校区");
            entity.setAddress("校园北区 1 号楼");
            entity.setDescription("提供各式家常菜，物美价廉");
            entity.setCoverImageUrl("https://example.com/restaurant/1/cover.jpg");
            entity.setCreatedAt(Instant.now());

            when(restaurantMapper.insert(entity)).thenReturn(1);

            // When
            RestaurantDTO result = restaurantAdminService.create(request);

            // Then
            assertNotNull(result);
            assertEquals("第一食堂", result.name());
            assertEquals("南湖校区", result.campus());
            assertEquals("校园北区 1 号楼", result.address());
            verify(restaurantMapper).insert(any(RestaurantEntity.class));
        }
    }

    @Test
    @DisplayName("创建餐厅成功 - 可选字段为空")
    void createRestaurant_optionalFieldsNull() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            mocked.when(UserContextUtil::requireUserId).thenReturn(100L);
            mocked.when(UserContextUtil::requireAdmin).thenAnswer(invocation -> null);

            RestaurantCreateRequest request = new RestaurantCreateRequest(
                    "新餐厅",
                    "滨湖校区",
                    null,
                    null,
                    null
            );

            RestaurantEntity entity = new RestaurantEntity();
            entity.setId(2L);
            entity.setName("新餐厅");
            entity.setCampus("滨湖校区");
            entity.setAddress(null);
            entity.setDescription(null);
            entity.setCoverImageUrl(null);
            entity.setCreatedAt(Instant.now());

            when(restaurantMapper.insert(entity)).thenReturn(1);

            // When
            RestaurantDTO result = restaurantAdminService.create(request);

            // Then
            assertNotNull(result);
            assertEquals("新餐厅", result.name());
            assertEquals("滨湖校区", result.campus());
            assertNull(result.address());
            assertNull(result.description());
            assertNull(result.coverImageUrl());
        }
    }

    @Test
    @DisplayName("删除餐厅成功")
    void deleteRestaurant_success() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            mocked.when(UserContextUtil::requireUserId).thenReturn(100L);
            mocked.when(UserContextUtil::requireAdmin).thenAnswer(invocation -> null);

            when(restaurantMapper.deleteById(1L)).thenReturn(1);

            // When
            assertDoesNotThrow(() -> restaurantAdminService.delete(1L));

            // Then
            verify(restaurantMapper).deleteById(1L);
        }
    }

    @Test
    @DisplayName("删除餐厅 - 餐厅不存在")
    void deleteRestaurant_notFound() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            mocked.when(UserContextUtil::requireUserId).thenReturn(100L);
            mocked.when(UserContextUtil::requireAdmin).thenAnswer(invocation -> null);

            when(restaurantMapper.deleteById(999L)).thenReturn(0);

            // When & Then
            BizException exception = assertThrows(BizException.class, () -> restaurantAdminService.delete(999L));
            assertEquals("餐厅不存在", exception.getMessage());
        }
    }

    @Test
    @DisplayName("获取餐厅列表 - 验证查询按 ID 倒序")
    void getRestaurantList_orderByDesc() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            mocked.when(UserContextUtil::requireUserId).thenReturn(100L);
            mocked.when(UserContextUtil::requireAdmin).thenAnswer(invocation -> null);

            when(restaurantMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

            // When
            restaurantAdminService.getRestaurantList();

            // Then
            verify(restaurantMapper).selectList(any(LambdaQueryWrapper.class));
        }
    }
}
