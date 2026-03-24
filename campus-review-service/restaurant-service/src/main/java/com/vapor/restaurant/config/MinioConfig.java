package com.vapor.restaurant.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 配置类。
 */
@Configuration
public class MinioConfig {

    /**
     * 创建 MinIO 客户端。
     *
     * @param properties MinIO 配置属性
     * @return MinIO 客户端
     */
    @Bean
    public MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
