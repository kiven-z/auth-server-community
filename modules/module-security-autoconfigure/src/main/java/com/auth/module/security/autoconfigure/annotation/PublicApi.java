package com.auth.module.security.autoconfigure.annotation;

import java.lang.annotation.*;

/**
 * 公共 API：允许所有（匿名）
 *
 * @author Bunny
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublicApi {

}
