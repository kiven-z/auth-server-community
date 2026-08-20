package com.auth.module.security.autoconfigure.annotation;

import java.lang.annotation.*;

/**
 * 内部 API：仅允许服务间调用（必须携带 X-Internal-JWT）
 *
 * @author Bunny
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InternalApi {

}
