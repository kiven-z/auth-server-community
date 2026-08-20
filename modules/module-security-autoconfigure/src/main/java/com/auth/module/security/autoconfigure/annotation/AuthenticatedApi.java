package com.auth.module.security.autoconfigure.annotation;

import java.lang.annotation.*;

/**
 * 已认证 API：仅需要登录
 *
 * @author Bunny
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthenticatedApi {

}
