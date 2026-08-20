package com.auth.service.system.schedule.validation.form;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 修改任务表单一致性校验
 *
 * @author Bunny
 */
@Constraint(validatedBy = SysJobUpdateFormConsistencyValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidSysJobUpdateForm {

	String message() default "invalid job update form";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
