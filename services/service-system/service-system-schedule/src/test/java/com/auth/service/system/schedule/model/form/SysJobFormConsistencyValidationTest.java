package com.auth.service.system.schedule.model.form;

import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 定时任务表单一致性校验测试
 *
 * @author Bunny
 */
@DisplayName("定时任务表单一致性校验")
class SysJobFormConsistencyValidationTest {

	private static Validator validator;

	@BeforeAll
	static void initValidator() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	private static SysJobCreateForm validCreateForm() {
		SysJobCreateForm form = new SysJobCreateForm();
		form.setJobName("demo-job");
		form.setJobGroup("DEFAULT");
		form.setTaskType("BEAN_INVOKE");
		form.setInvokeTarget("beanInvokeDemoTask.run()");
		form.setCronExpression("0/30 * * * * ?");
		form.setMisfirePolicy(2);
		form.setConcurrent(false);
		form.setStatus(true);
		form.setTaskType("BEAN_INVOKE");
		return form;
	}

	private static SysJobUpdateForm validUpdateForm() {
		SysJobUpdateForm form = new SysJobUpdateForm();
		form.setId(1L);
		form.setCronExpression("0/30 * * * * ?");
		form.setMisfirePolicy(2);
		form.setConcurrent(false);
		form.setStatus(true);
		return form;
	}

	@Test
	@DisplayName("CreateForm：Bean 调用模式缺少 invokeTarget 时校验失败")
	void createFormRejectsBeanModeWithoutInvokeTarget() {
		SysJobCreateForm form = validCreateForm();
		form.setTaskType("BEAN_INVOKE");
		form.setInvokeTarget(" ");

		Set<ConstraintViolation<SysJobCreateForm>> violations = validator.validate(form, CreateGroup.class);

		assertThat(violations).anyMatch(v -> "invokeTarget".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("UpdateForm：指定 Bean 模式但 invokeTarget 为空时校验失败")
	void updateFormRejectsBeanModeWithoutInvokeTarget() {
		SysJobUpdateForm form = validUpdateForm();
		form.setTaskType("BEAN_INVOKE");
		form.setInvokeTarget("");

		Set<ConstraintViolation<SysJobUpdateForm>> violations = validator.validate(form, UpdateGroup.class);

		assertThat(violations).anyMatch(v -> "invokeTarget".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("UpdateForm：未指定 taskType 时触发字段必填校验")
	void updateFormRejectsWhenTaskTypeMissing() {
		SysJobUpdateForm form = validUpdateForm();
		form.setTaskType(null);
		form.setInvokeTarget(null);
		form.setPayloadJson(null);

		Set<ConstraintViolation<SysJobUpdateForm>> violations = validator.validate(form, UpdateGroup.class);

		assertThat(violations).anyMatch(v -> "taskType".equals(v.getPropertyPath().toString()));
	}

}
