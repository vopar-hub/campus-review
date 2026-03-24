package com.vapor.restaurant.controller;

import com.vapor.common.api.ApiResponse;
import com.vapor.restaurant.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传接口。
 *
 * 提供文件上传到 MinIO 的能力。
 */
@RestController
@RequestMapping("/api/files")
@Tag(name = "文件管理", description = "文件上传等接口")
public class FileUploadController {

    private final MinioService minioService;

    /**
     * 构造文件上传控制器。
     *
     * @param minioService MinIO 文件服务
     */
    public FileUploadController(MinioService minioService) {
        this.minioService = minioService;
    }

    /**
     * 上传文件。
     *
     * @param file 上传的文件
     * @param dir 目录（可选，默认为 uploads）
     * @return 文件访问信息
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传文件到 MinIO 并返回访问 URL")
    public ApiResponse<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "dir", required = false, defaultValue = "uploads") String dir
    ) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        String fileUrl = minioService.uploadFile(file, dir);

        Map<String, String> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("filename", file.getOriginalFilename());
        result.put("contentType", file.getContentType());
        result.put("size", String.valueOf(file.getSize()));

        return ApiResponse.ok(result);
    }
}
