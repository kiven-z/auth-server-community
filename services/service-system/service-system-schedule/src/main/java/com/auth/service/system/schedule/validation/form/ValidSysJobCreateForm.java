package com.auth.service.system.schedule.validation.form;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 新增任务表单一致性校验
 *
 * @author Bunny
 */
@Constraint(validatedBy = SysJobCreateFormConsistencyValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidSysJobCreateForm {

	String message() default "invalid job create form";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
