package com.vapor.ranking.config;

import com.vapor.common.web.UserContext;
import com.vapor.common.web.UserContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Feign 请求拦截器 - 自动传递用户上下文。
 */
@Component
public class FeignRequestInterceptor implements RequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(FeignRequestInterceptor.class);

    @Override
    public void apply(RequestTemplate template) {
        UserContext ctx = UserContextHolder.get();
        if (ctx != null && ctx.getUserId() != null) {
            template.header("X-User-Id", String.valueOf(ctx.getUserId()));
            template.header("X-User-Roles", String.join(",", ctx.getRoles()));
            log.debug("Feign 请求拦截器已添加用户上下文：userId={}, roles={}, request={}.{}",
                      ctx.getUserId(), ctx.getRoles(), template.method(), template.url());
        } else {
            log.debug("Feign 请求拦截器 - 未登录用户，跳过上下文传递：{}.{}",
                      template.method(), template.url());
        }
    }
}
