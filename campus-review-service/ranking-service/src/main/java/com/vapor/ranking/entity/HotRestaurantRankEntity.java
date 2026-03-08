package com.vapor.ranking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * 热门餐馆榜实体。
 *
 * 对应数据库 hot_restaurant_rank 表，记录餐馆排名与热度分。
 */
@Data
@TableName("hot_restaurant_rank")
public class HotRestaurantRankEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long restaurantId;
    private Double score;
    private Long rank;
    private Instant updatedAt;
}
