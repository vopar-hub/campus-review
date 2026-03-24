package com.vapor.ranking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vapor.ranking.entity.HotRestaurantRankEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 热门餐馆榜数据访问接口。
 *
 * 基于 MyBatis-Plus {@link BaseMapper} 提供 CRUD 能力。
 */
public interface HotRestaurantRankMapper extends BaseMapper<HotRestaurantRankEntity> {

    /**
     * 根据餐馆ID列表查询排行榜数据。
     *
     * @param restaurantIds 餐馆ID列表
     * @return 排行榜实体列表
     */
    @Select("<script>SELECT * FROM hot_restaurant_rank WHERE restaurant_id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<HotRestaurantRankEntity> selectByRestaurantIds(@Param("ids") List<Long> restaurantIds);
}
