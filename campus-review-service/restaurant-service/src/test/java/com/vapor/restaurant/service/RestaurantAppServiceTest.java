package com.vapor.restaurant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vapor.common.error.BizException;
import com.vapor.model.restaurant.RestaurantCreateRequest;
import com.vapor.model.restaurant.RestaurantDTO;
import com.vapor.restaurant.entity.RestaurantEntity;
import com.vapor.restaurant.mapper.RestaurantMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RestaurantAppService 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class RestaurantAppServiceTest {

    @Mock
    private RestaurantMapper restaurantMapper;

    @InjectMocks
    private RestaurantAppService restaurantService;

    private RestaurantCreateRequest createRequest;
    private RestaurantEntity testEntity;

    @BeforeEach
    void setUp() {
        createRequest = new RestaurantCreateRequest(
                " test 餐厅",
                "主校区",
                "学生服务中心一楼",
                "提供各类家常菜品",
                "http://example.com/cover.jpg"
        );

        testEntity = new RestaurantEntity();
        testEntity.setId(1L);
        testEntity.setName(" test 餐厅");
        testEntity.setCampus("主校区");
        testEntity.setAddress("学生服务中心一楼");
        testEntity.setDescription("提供各类家常菜品");
        testEntity.setCoverImageUrl("http://example.com/cover.jpg");
        testEntity.setCreatedAt(Instant.now());
        testEntity.setUpdatedAt(Instant.now());
    }

    @Test
    @DisplayName("创建餐馆成功")
    void create_success() {
        // Given
        when(restaurantMapper.insert(any(RestaurantEntity.class))).thenAnswer(invocation -> {
            RestaurantEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return 1;
        });

        // When
        RestaurantDTO result = restaurantService.create(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(" test 餐厅", result.name());
        assertEquals("主校区", result.campus());
        verify(restaurantMapper).insert(any(RestaurantEntity.class));
    }

    @Test
    @DisplayName("按 ID 查询餐馆成功")
    void getById_success() {
        // Given
        when(restaurantMapper.selectById(1L)).thenReturn(testEntity);

        // When
        RestaurantDTO result = restaurantService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(" test 餐厅", result.name());
    }

    @Test
    @DisplayName("按 ID 查询 - 餐馆不存在")
    void getById_notFound() {
        // Given
        when(restaurantMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> restaurantService.getById(999L));
        assertEquals("餐馆不存在", exception.getMessage());
    }

    @Test
    @DisplayName("餐馆检索 - 无条件")
    void search_noConditions() {
        // Given
        when(restaurantMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(testEntity));

        // When
        List<RestaurantDTO> results = restaurantService.search(null, null);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).id());
    }

    @Test
    @DisplayName("餐馆检索 - 按名称")
    void search_byName() {
        // Given
        when(restaurantMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(testEntity));

        // When
        List<RestaurantDTO> results = restaurantService.search(" test ", null);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(restaurantMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("餐馆检索 - 按校区")
    void search_byCampus() {
        // Given
        when(restaurantMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(testEntity));

        // When
        List<RestaurantDTO> results = restaurantService.search(null, "主校区");

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("餐馆检索 - 名称和校区组合")
    void search_combined() {
        // Given
        when(restaurantMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(testEntity));

        // When
        List<RestaurantDTO> results = restaurantService.search(" test ", "主校区");

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("餐馆检索 - 空字符串名称视为无条件")
    void search_emptyName() {
        // Given
        when(restaurantMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(testEntity));

        // When
        List<RestaurantDTO> results = restaurantService.search("  ", null);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("餐馆检索 - 返回按 ID 倒序")
    void search_orderByDesc() {
        // Given
        RestaurantEntity entity2 = new RestaurantEntity();
        entity2.setId(2L);
        entity2.setName("餐厅 2");
        entity2.setCampus("主校区");
        entity2.setAddress("地址 2");
        entity2.setDescription("描述 2");
        entity2.setCreatedAt(Instant.now());
        entity2.setUpdatedAt(Instant.now());

        when(restaurantMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(entity2, testEntity));

        // When
        List<RestaurantDTO> results = restaurantService.search(null, null);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(2L, results.get(0).id());
        assertEquals(1L, results.get(1).id());
    }
}
