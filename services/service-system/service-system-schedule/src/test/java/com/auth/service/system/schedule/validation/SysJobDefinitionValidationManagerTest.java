package com.auth.service.system.schedule.validation;

import com.auth.service.system.schedule.model.entity.JobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.validation.task.SysJobDefinitionValidationManager;
import com.auth.service.system.schedule.validation.task.SysJobTaskTypeDefinitionValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SysJobDefinitionValidationManager} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobDefinitionValidationManager 任务定义校验管理器")
class SysJobDefinitionValidationManagerTest {

	@Test
	@DisplayName("normalizeAndValidate：按任务类型归一化并调用对应校验器")
	void normalizeAndValidateRoutesToMatchedValidator() {
		RecorderValidator beanValidator = new RecorderValidator(SysJobTaskType.BEAN_INVOKE);
		RecorderValidator customValidator = new RecorderValidator(SysJobTaskType.CUSTOM_CLASS);
		SysJobDefinitionValidationManager manager = new SysJobDefinitionValidationManager(
				List.of(beanValidator, customValidator));

		JobEntity job = new JobEntity();
		job.setTaskType(SysJobTaskType.BEAN_INVOKE.name());
		job.setInvokeTarget("beanInvokeDemoTask.run()");

		manager.normalizeAndValidate(job);

		assertThat(job.getTaskType()).isEqualTo(SysJobTaskType.BEAN_INVOKE.name());
		assertThat(job.getHandlerCode()).isEqualTo("beanInvokeHandler");
		assertThat(beanValidator.invoked).isTrue();
		assertThat(customValidator.invoked).isFalse();
	}

	@Test
	@DisplayName("normalizeAndValidate：无专属校验器时仅归一化字段")
	void normalizeAndValidateSkipsWhenValidatorMissing() {
		SysJobDefinitionValidationManager manager = new SysJobDefinitionValidationManager(
				List.of(new RecorderValidator(SysJobTaskType.CUSTOM_CLASS)));

		JobEntity job = new JobEntity();
		job.setTaskType(SysJobTaskType.BEAN_INVOKE.name());
		job.setInvokeTarget("beanInvokeDemoTask.run()");

		manager.normalizeAndValidate(job);

		assertThat(job.getTaskType()).isEqualTo(SysJobTaskType.BEAN_INVOKE.name());
		assertThat(job.getHandlerCode()).isEqualTo("beanInvokeHandler");
	}

	/**
	 * 记录调用轨迹的测试校验器
	 */
	private static class RecorderValidator implements SysJobTaskTypeDefinitionValidator {

		private final SysJobTaskType type;

		private boolean invoked;

		private RecorderValidator(SysJobTaskType type) {
			this.type = type;
		}

		@Override
		public SysJobTaskType taskType() {
			return type;
		}

		@Override
		public void validate(JobEntity job) {
			this.invoked = true;
		}

	}

}
