package com.vapor.restaurant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 配置属性。
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * MinIO 服务端点 URL。
     */
    private String endpoint;

    /**
     * 访问密钥 ID。
     */
    private String accessKey;

    /**
     * 秘密访问密钥。
     */
    private String secretKey;

    /**
     * 存储桶名称。
     */
    private String bucketName;
}
