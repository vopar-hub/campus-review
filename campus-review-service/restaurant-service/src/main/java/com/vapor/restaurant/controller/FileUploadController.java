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
import java.util.Set;

/**
 * 文件上传接口。
 *
 * 提供文件上传到 MinIO 的能力。
 */
@RestController
@RequestMapping("/api/files")
@Tag(name = "文件管理", description = "文件上传等接口")
public class FileUploadController {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

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

        // 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过 5MB");
        }

        // 校验文件类型（MIME 类型）
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("不支持的文件类型，仅支持：" + String.join(", ", ALLOWED_CONTENT_TYPES));
        }

        // 校验文件扩展名（防止 MIME 类型伪造）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !originalFilename.isBlank()) {
            String extension = getFileExtension(originalFilename);
            if (!isValidExtension(extension)) {
                throw new IllegalArgumentException("不支持的文件扩展名，仅支持：.jpg, .jpeg, .png, .gif, .webp");
            }
        }

        String fileUrl = minioService.uploadFile(file, dir);

        Map<String, String> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("filename", file.getOriginalFilename());
        result.put("contentType", contentType);
        result.put("size", String.valueOf(file.getSize()));

        return ApiResponse.ok(result);
    }

    /**
     * 获取文件扩展名（小写）。
     *
     * @param filename 文件名
     * @return 扩展名（包含点号，如 ".jpg"）
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot).toLowerCase();
    }

    /**
     * 判断扩展名是否合法。
     *
     * @param extension 文件扩展名
     * @return 是否合法
     */
    private boolean isValidExtension(String extension) {
        return Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp").contains(extension);
    }
}
