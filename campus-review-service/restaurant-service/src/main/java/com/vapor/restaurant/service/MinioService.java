package com.vapor.restaurant.service;

import io.minio.PutObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * MinIO 文件服务。
 *
 * 提供文件上传、获取访问 URL 等能力。
 */
@Service
public class MinioService {
    private static final Logger log = LoggerFactory.getLogger(MinioService.class);

    private final MinioClient minioClient;
    private final String bucketName;
    private final String endpoint;
    private boolean minioAvailable = true;

    /**
     * 构造 MinIO 服务。
     *
     * @param minioClient MinIO 客户端
     * @param bucketName 存储桶名称
     * @param endpoint MinIO 端点
     */
    public MinioService(MinioClient minioClient,
                        @Value("${minio.bucket-name}") String bucketName,
                        @Value("${minio.endpoint}") String endpoint) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
        this.endpoint = endpoint;
    }

    /**
     * 初始化存储桶（确保存储桶存在）。
     */
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            log.info("MinIO 存储桶 [{}] 初始化成功", bucketName);
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.warn("MinIO 不可用，跳过存储桶初始化：{}", e.getMessage());
            minioAvailable = false;
        }
    }

    /**
     * 上传文件到 MinIO。
     *
     * @param file 上传的文件
     * @param dir 目录（可选，如 "restaurants"）
     * @return 文件访问 URL
     */
    public String uploadFile(MultipartFile file, String dir) {
        if (!minioAvailable) {
            log.warn("MinIO 不可用，无法上传文件");
            throw new RuntimeException("MinIO 服务不可用");
        }
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = generateUniqueFilename(extension);

            // 构建对象路径
            String objectName = dir != null && !dir.isBlank()
                    ? dir + "/" + filename
                    : filename;

            // 上传文件
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            // 返回文件访问 URL
            return getFileUrl(objectName);
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("上传文件失败", e);
        }
    }

    /**
     * 上传文件到 MinIO（默认目录）。
     *
     * @param file 上传的文件
     * @return 文件访问 URL
     */
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, "uploads");
    }

    /**
     * 获取文件的预签名访问 URL。
     *
     * @param objectName 对象名称（路径）
     * @return 文件访问 URL
     */
    public String getFileUrl(String objectName) {
        try {
            // 生成预签名 URL（7 天有效期）
            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(io.minio.http.Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return presignedUrl;
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("获取文件 URL 失败", e);
        }
    }

    /**
     * 生成唯一文件名。
     *
     * @param extension 文件扩展名
     * @return 唯一文件名
     */
    private String generateUniqueFilename(String extension) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return date + "/" + uuid + extension;
    }
}
