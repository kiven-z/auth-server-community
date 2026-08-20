package com.auth.service.system.schedule.validation.form;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.model.form.SysJobUpdateForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 修改任务表单一致性校验器
 *
 * @author Bunny
 */
public class SysJobUpdateFormConsistencyValidator
		implements ConstraintValidator<ValidSysJobUpdateForm, SysJobUpdateForm> {

	@Override
	public boolean isValid(SysJobUpdateForm form, ConstraintValidatorContext context) {
		if (form == null || CharSequenceUtil.isBlank(form.getTaskType())) {
			return true;
		}
		SysJobTaskType taskType = SysJobTaskType.resolve(form.getTaskType());
		return SysJobTaskTypeFieldRules.detect(taskType, null, form.getInvokeTarget(), false)
			.map(violation -> SysJobTaskTypeFieldRules.applyFormViolation(violation, context))
			.orElse(true);
	}

}
