package com.vapor.admin.config;

import com.vapor.common.util.UserContextUtil;
import com.vapor.common.web.UserContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

/**
 * Feign 请求拦截器 - 自动传递用户上下文。
 */
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        UserContext ctx = UserContextUtil.getUserContext();
        if (ctx != null && ctx.getUserId() != null) {
            template.header("X-User-Id", String.valueOf(ctx.getUserId()));
            template.header("X-User-Roles", String.join(",", ctx.getRoles()));
        }
    }
}
