package com.vapor.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * API 文档导出工具类
 * 在应用启动时自动导出 OpenAPI/Swagger 文档到 Markdown 文件
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "api.doc.export.enabled", havingValue = "true", matchIfMissing = true)
public class ApiDocExporter implements CommandLineRunner {

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${api.doc.export.path:docs}")
    private String exportPath;

    @Value("${api.doc.export.openapi-url:http://localhost:{port}/v3/api-docs}")
    private String openApiUrl;

    @Autowired(required = false)
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ResourceLoader resourceLoader;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(String... args) throws Exception {
        log.info("开始导出 API 文档 - 服务名：{}, 端口：{}", applicationName, serverPort);
        
        try {
            // 等待应用完全启动
            Thread.sleep(3000);
            
            // 导出 OpenAPI 文档
            exportOpenApiDoc();
            
            // 导出本地路由文档
            exportLocalRoutes();
            
            log.info("API 文档导出完成");
        } catch (Exception e) {
            log.error("API 文档导出失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 从 OpenAPI 端点导出文档
     */
    private void exportOpenApiDoc() {
        String url = openApiUrl.replace("{port}", String.valueOf(serverPort));
        log.info("获取 OpenAPI 文档：{}", url);

        try {
            RestTemplate restTemplate = new RestTemplate();
            String openApiJson = restTemplate.getForObject(url, String.class);
            
            if (openApiJson != null && openApiJson.contains("openapi")) {
                String markdown = convertOpenApiToMarkdown(openApiJson);
                saveToFile(markdown, "api-docs.md");
                log.info("OpenAPI 文档导出成功：{}/api-docs.md", exportPath);
            } else {
                log.warn("未获取到有效的 OpenAPI 文档");
            }
        } catch (Exception e) {
            log.warn("无法从 {} 获取 OpenAPI 文档：{}", url, e.getMessage());
        }
    }

    /**
     * 导出本地路由信息
     */
    private void exportLocalRoutes() {
        if (handlerMapping == null) {
            log.warn("RequestMappingHandlerMapping 不可用，跳过本地路由导出");
            return;
        }

        StringBuilder markdown = new StringBuilder();
        markdown.append("# ").append(applicationName).append(" API 接口文档\n\n");
        markdown.append("**服务端口**: ").append(serverPort).append("\n\n");
        markdown.append("**生成时间**: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n\n");
        markdown.append("---\n\n");

        Map<String, List<RouteInfo>> groupedRoutes = new TreeMap<>();

        handlerMapping.getHandlerMethods().forEach((info, method) -> {
            String groupName = getControllerGroupName(method);
            RouteInfo routeInfo = extractRouteInfo(info, method);
            groupedRoutes.computeIfAbsent(groupName, k -> new ArrayList<>()).add(routeInfo);
        });

        groupedRoutes.forEach((groupName, routes) -> {
            markdown.append("## ").append(groupName).append("\n\n");
            
            routes.forEach(route -> {
                markdown.append("### ").append(route.method).append(" `").append(route.path).append("`\n\n");
                
                if (route.summary != null && !route.summary.isEmpty()) {
                    markdown.append("**").append(route.summary).append("**\n\n");
                }
                
                if (route.description != null && !route.description.isEmpty()) {
                    markdown.append(route.description).append("\n\n");
                }
                
                if (!route.params.isEmpty()) {
                    markdown.append("**请求参数**:\n\n");
                    markdown.append("| 参数 | 位置 | 类型 | 必填 | 说明 |\n");
                    markdown.append("|------|------|------|------|------|\n");
                    route.params.forEach(param -> {
                        markdown.append("| ").append(param.name)
                                .append(" | ").append(param.position)
                                .append(" | ").append(param.type)
                                .append(" | ").append(param.required ? "是" : "否")
                                .append(" | ").append(param.description != null ? param.description : "-")
                                .append(" |\n");
                    });
                    markdown.append("\n");
                }

                markdown.append("**响应示例**:\n\n");
                markdown.append("```json\n");
                markdown.append("{\n");
                markdown.append("  \"code\": 0,\n");
                markdown.append("  \"message\": \"OK\",\n");
                markdown.append("  \"data\": {}\n");
                markdown.append("}\n");
                markdown.append("```\n\n");
                
                markdown.append("---\n\n");
            });
        });

        saveToFile(markdown.toString(), "api-routes.md");
        log.info("本地路由文档导出成功：{}/api-routes.md", exportPath);
    }

    /**
     * 将 OpenAPI JSON 转换为 Markdown
     */
    private String convertOpenApiToMarkdown(String openApiJson) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(openApiJson);
        
        StringBuilder markdown = new StringBuilder();
        
        // 标题
        JsonNode info = root.get("info");
        String title = info.has("title") ? info.get("title").asText() : applicationName;
        String version = info.has("version") ? info.get("version").asText() : "v1";
        
        markdown.append("# ").append(title).append("\n\n");
        markdown.append("**版本**: ").append(version).append("\n\n");
        markdown.append("**服务端口**: ").append(serverPort).append("\n\n");
        markdown.append("**生成时间**: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n\n");
        markdown.append("---\n\n");

        // 服务器信息
        JsonNode servers = root.get("servers");
        if (servers != null && servers.isArray()) {
            markdown.append("## 服务器\n\n");
            for (JsonNode server : servers) {
                markdown.append("- ").append(server.has("url") ? server.get("url").asText() : "").append("\n");
            }
            markdown.append("\n---\n\n");
        }

        // 标签分组
        Map<String, List<JsonNode>> pathsByTag = new TreeMap<>();
        JsonNode paths = root.get("paths");
        
        if (paths != null && paths.isObject()) {
            paths.fields().forEachRemaining(entry -> {
                String path = entry.getKey();
                JsonNode pathNode = entry.getValue();
                
                pathNode.fields().forEachRemaining(methodEntry -> {
                    String method = methodEntry.getKey().toUpperCase();
                    JsonNode operation = methodEntry.getValue();
                    
                    if (operation.has("tags") && operation.get("tags").isArray()) {
                        operation.get("tags").forEach(tagNode -> {
                            String tag = tagNode.asText();
                            JsonNode routeInfo = objectMapper.createObjectNode();
                            ((com.fasterxml.jackson.databind.node.ObjectNode) routeInfo)
                                    .put("path", path)
                                    .put("method", method);
                            
                            if (operation.has("summary")) {
                                ((com.fasterxml.jackson.databind.node.ObjectNode) routeInfo)
                                        .put("summary", operation.get("summary").asText());
                            }
                            if (operation.has("description")) {
                                ((com.fasterxml.jackson.databind.node.ObjectNode) routeInfo)
                                        .put("description", operation.get("description").asText());
                            }
                            if (operation.has("parameters")) {
                                ((com.fasterxml.jackson.databind.node.ObjectNode) routeInfo)
                                        .set("parameters", operation.get("parameters"));
                            }
                            if (operation.has("requestBody")) {
                                ((com.fasterxml.jackson.databind.node.ObjectNode) routeInfo)
                                        .set("requestBody", operation.get("requestBody"));
                            }
                            if (operation.has("responses")) {
                                ((com.fasterxml.jackson.databind.node.ObjectNode) routeInfo)
                                        .set("responses", operation.get("responses"));
                            }
                            
                            pathsByTag.computeIfAbsent(tag, k -> new ArrayList<>()).add(routeInfo);
                        });
                    }
                });
            });
        }

        // 按标签输出
        pathsByTag.forEach((tag, routes) -> {
            markdown.append("## ").append(tag).append("\n\n");
            
            routes.forEach(route -> {
                try {
                    markdown.append("### ").append(route.get("method").asText())
                            .append(" `").append(route.get("path").asText()).append("`\n\n");
                    
                    if (route.has("summary")) {
                        markdown.append("**").append(route.get("summary").asText()).append("**\n\n");
                    }
                    if (route.has("description")) {
                        markdown.append(route.get("description").asText()).append("\n\n");
                    }
                    
                    // 请求参数
                    if (route.has("parameters") && route.get("parameters").isArray()) {
                        markdown.append("**请求参数**:\n\n");
                        markdown.append("| 参数 | 位置 | 类型 | 必填 | 说明 |\n");
                        markdown.append("|------|------|------|------|------|\n");
                        
                        route.get("parameters").forEach(param -> {
                            String name = param.has("name") ? param.get("name").asText() : "-";
                            String in = param.has("in") ? param.get("in").asText() : "-";
                            String type = param.has("schema") && param.get("schema").has("type") 
                                    ? param.get("schema").get("type").asText() : "-";
                            String required = param.has("required") && param.get("required").asBoolean() ? "是" : "否";
                            String desc = param.has("description") ? param.get("description").asText() : "-";
                            
                            markdown.append("| ").append(name)
                                    .append(" | ").append(in)
                                    .append(" | ").append(type)
                                    .append(" | ").append(required)
                                    .append(" | ").append(desc)
                                    .append(" |\n");
                        });
                        markdown.append("\n");
                    }
                    
                    // 请求体
                    if (route.has("requestBody")) {
                        JsonNode requestBody = route.get("requestBody");
                        markdown.append("**请求体**:\n\n");
                        if (requestBody.has("content")) {
                            requestBody.get("content").fields().forEachRemaining(contentEntry -> {
                                String contentType = contentEntry.getKey();
                                JsonNode schema = contentEntry.getValue();
                                markdown.append("Content-Type: `").append(contentType).append("`\n\n");
                                if (schema.has("schema")) {
                                    markdown.append("Schema:\n```json\n");
                                    markdown.append(prettyPrintSchema(schema.get("schema"))).append("\n```\n\n");
                                }
                            });
                        }
                    }
                    
                    // 响应
                    if (route.has("responses")) {
                        markdown.append("**响应**:\n\n");
                        markdown.append("| 状态码 | 说明 |\n");
                        markdown.append("|--------|------|\n");
                        
                        route.get("responses").fields().forEachRemaining(responseEntry -> {
                            String statusCode = responseEntry.getKey();
                            JsonNode response = responseEntry.getValue();
                            String desc = response.has("description") ? response.get("description").asText() : "-";
                            markdown.append("| ").append(statusCode).append(" | ").append(desc).append(" |\n");
                        });
                        markdown.append("\n");
                    }
                    
                    markdown.append("---\n\n");
                } catch (Exception e) {
                    log.warn("解析路由文档失败：{}", e.getMessage());
                }
            });
        });

        return markdown.toString();
    }

    /**
     * 美化输出 Schema
     */
    private String prettyPrintSchema(JsonNode schema) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        } catch (JsonProcessingException e) {
            return schema.toString();
        }
    }

    /**
     * 获取控制器分组名称
     */
    private String getControllerGroupName(HandlerMethod method) {
        Class<?> beanType = method.getBeanType();
        return beanType.getSimpleName().replace("Controller", "");
    }

    /**
     * 提取路由信息
     */
    private RouteInfo extractRouteInfo(RequestMappingInfo info, HandlerMethod method) {
        RouteInfo route = new RouteInfo();
        
        Set<org.springframework.web.bind.annotation.RequestMethod> methods = info.getMethodsCondition().getMethods();
        route.method = methods.isEmpty() ? "GET" : methods.iterator().next().name();
        
        Set<String> paths = info.getPathPatternsCondition().getPatternValues();
        route.path = paths.isEmpty() ? "/" : paths.iterator().next();
        
        route.summary = method.getMethod().getName();
        
        // 提取参数信息
        Arrays.stream(method.getMethodParameters()).forEach(param -> {
            RouteParam routeParam = new RouteParam();
            routeParam.name = param.getParameter().getName();
            routeParam.type = param.getParameter().getType().getSimpleName();
            routeParam.position = "query"; // 简化处理
            routeParam.required = true;
            route.params.add(routeParam);
        });
        
        return route;
    }

    /**
     * 保存内容到文件
     */
    private void saveToFile(String content, String filename) {
        try {
            Path path = Paths.get(exportPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("创建导出目录：{}", exportPath);
            }
            
            File file = new File(exportPath, filename);
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                writer.write(content);
            }
            
            log.info("文件已保存：{}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("保存文件失败：{} - {}", filename, e.getMessage(), e);
        }
    }

    /**
     * 路由信息内部类
     */
    private static class RouteInfo {
        String method;
        String path;
        String summary;
        String description;
        List<RouteParam> params = new ArrayList<>();
    }

    /**
     * 路由参数内部类
     */
    private static class RouteParam {
        String name;
        String position;
        String type;
        boolean required;
        String description;
    }
}
