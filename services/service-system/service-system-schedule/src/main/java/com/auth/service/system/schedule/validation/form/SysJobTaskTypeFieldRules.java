package com.auth.service.system.schedule.validation.form;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import jakarta.validation.ConstraintValidatorContext;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * 任务类型字段互斥/必填规则（表单 Bean Validation 使用）
 *
 * @author Bunny
 */
@UtilityClass
public class SysJobTaskTypeFieldRules {

	/**
	 * 检测任务类型相关字段是否违反规则
	 * @param taskType 任务类型
	 * @param jobClass 自定义类
	 * @param invokeTarget 调用目标
	 * @param requireJobClass 是否要求 jobClass 非空（新增表单为 true）
	 * @return 违反类型；无违反则为 empty
	 */
	public static Optional<Violation> detect(SysJobTaskType taskType, String jobClass, String invokeTarget,
			boolean requireJobClass) {
		return switch (taskType) {
			// Bean 调用模式必须填写 invokeTarget
			case BEAN_INVOKE -> CharSequenceUtil.isBlank(invokeTarget)
					? Optional.of(Violation.BEAN_MISSING_INVOKE_TARGET) : Optional.empty();
			// 自定义类模式必须填写 jobClass
			case CUSTOM_CLASS -> {
				if (requireJobClass && CharSequenceUtil.isBlank(jobClass)) {
					yield Optional.of(Violation.CUSTOM_MISSING_JOB_CLASS);
				}
				if (CharSequenceUtil.isNotBlank(invokeTarget)) {
					yield Optional.of(Violation.CUSTOM_FORBIDS_INVOKE_TARGET);
				}
				yield Optional.empty();
			}
		};
	}

	/**
	 * 将违反类型写入表单校验上下文
	 * @param violation 违反类型
	 * @param context 校验上下文
	 * @return 始终 false
	 */
	public static boolean applyFormViolation(Violation violation, ConstraintValidatorContext context) {
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(violation.getFormMessage())
			.addPropertyNode(violation.getProperty())
			.addConstraintViolation();
		return false;
	}

	/**
	 * 字段规则违反类型
	 */
	@Getter
	public enum Violation {

		/**
		 * Bean 调用模式必须填写 invokeTarget
		 */
		BEAN_MISSING_INVOKE_TARGET("invokeTarget", "Bean 调用模式必须填写 invokeTarget"),

		/**
		 * 自定义类模式必须填写 jobClass
		 */
		CUSTOM_MISSING_JOB_CLASS("jobClass", "自定义类模式必须填写 jobClass"),

		/**
		 * 自定义类模式不允许填写 invokeTarget
		 */
		CUSTOM_FORBIDS_INVOKE_TARGET("invokeTarget", "自定义类模式不允许填写 invokeTarget");

		private final String property;

		private final String formMessage;

		Violation(String property, String formMessage) {
			this.property = property;
			this.formMessage = formMessage;
		}

	}

}
