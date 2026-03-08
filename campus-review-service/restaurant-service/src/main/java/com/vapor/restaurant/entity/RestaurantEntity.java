package com.vapor.restaurant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * 餐馆表实体。
 *
 * 对应数据库 restaurants 表，包含餐馆基础信息与封面图等字段。
 */
@Data
@TableName("restaurants")
public class RestaurantEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String campus;
    private String address;
    private String description;
    private String coverImageUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
