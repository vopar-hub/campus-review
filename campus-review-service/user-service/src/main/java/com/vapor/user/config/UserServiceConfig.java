package com.vapor.user.config;

import com.vapor.common.web.RequestIdFilter;
import com.vapor.common.web.RequestLoggingFilter;
import com.vapor.common.web.UserContextFilter;
import com.vapor.utils.jwt.JwtService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;

/**
 * 用户微服务配置。
 *
 * 提供 Web 过滤器、密码编码器与 JWT 组件等基础 Bean，并启用 Mapper 扫描。
 */
@Configuration
@MapperScan("com.vapor.user.mapper")
public class UserServiceConfig {

    /**
     * OpenAPI 文档配置。
     *
     * @return OpenAPI 实例
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("用户服务 API")
                        .version("v1")
                        .description("用户注册、登录、个人信息管理等接口")
                        .contact(new Contact()
                                .name("Campus Review Team")
                                .email("support@campus.edu")));
    }

    /**
     * 请求 ID 过滤器。
     *
     * @return 请求 ID 过滤器
     */
    @Bean
    public RequestIdFilter requestIdFilter() {
        return new RequestIdFilter();
    }

    /**
     * 请求日志过滤器。
     *
     * @return 请求日志过滤器
     */
    @Bean
    public RequestLoggingFilter requestLoggingFilter() {
        return new RequestLoggingFilter();
    }

    /**
     * 用户上下文过滤器。
     *
     * 从请求头解析用户信息并写入线程上下文。
     *
     * @return 用户上下文过滤器
     */
    @Bean
    public UserContextFilter userContextFilter() {
        return new UserContextFilter();
    }

    /**
     * 密码编码器。
     *
     * @return BCrypt 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * JWT 服务组件。
     *
     * @param secret JWT 密钥
     * @param ttlSeconds token 有效期（秒）
     * @return JWT 服务
     */
    @Bean
    public JwtService jwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.ttl-seconds:86400}") long ttlSeconds
    ) {
        return new JwtService(secret, Duration.ofSeconds(ttlSeconds));
    }
}
