package com.auth.service.system.message.support.inapp;

import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.model.enums.RecipientScopeType;
import com.auth.service.system.message.model.form.inapp.InAppComposeForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_RECIPIENT_SCOPE_INVALID;
import static com.auth.service.system.message.exception.MessageResultCode.MESSAGE_COMMAND_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link InAppComposeFormValidation} 单元测试
 *
 * @author Bunny
 */
@DisplayName("InAppComposeFormValidation 管理端发送条件校验")
class InAppComposeFormValidationTest {

	/**
	 * 构造已满足 Bean 必填的基础表单
	 * @return 表单
	 */
	private static InAppComposeForm baseForm() {
		InAppComposeForm form = new InAppComposeForm();
		form.setTitle("标题");
		form.setBody("正文");
		form.setContentType("TEXT");
		return form;
	}

	@Test
	@DisplayName("合法 DEPT 表单：返回范围类型")
	void validate_shouldAcceptValidDeptForm() {
		// DEPT 带 ids 应通过
		InAppComposeForm form = baseForm();
		form.setRecipientScopeType(RecipientScopeType.DEPT.name());
		form.setRecipientScopeIds(List.of(1L));

		assertThat(InAppComposeFormValidation.validate(form)).isEqualTo(RecipientScopeType.DEPT);
	}

	@Test
	@DisplayName("ALL 可不传范围 ID")
	void validate_all_shouldAllowEmptyIds() {
		// ALL 不强制 ids
		InAppComposeForm form = baseForm();
		form.setRecipientScopeType(RecipientScopeType.ALL.name());

		assertThat(InAppComposeFormValidation.validate(form)).isEqualTo(RecipientScopeType.ALL);
	}

	@Test
	@DisplayName("USER 缺少 ids：范围非法")
	void validate_userWithoutIds_shouldReject() {
		// USER 必须带 ids
		InAppComposeForm form = baseForm();
		form.setRecipientScopeType(RecipientScopeType.USER.name());

		assertThatThrownBy(() -> InAppComposeFormValidation.validate(form)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_RECIPIENT_SCOPE_INVALID);
	}

	@Test
	@DisplayName("非法范围类型：范围非法")
	void validate_unknownType_shouldReject() {
		// 无法解析的范围类型
		InAppComposeForm form = baseForm();
		form.setRecipientScopeType("FOO");

		assertThatThrownBy(() -> InAppComposeFormValidation.validate(form)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_RECIPIENT_SCOPE_INVALID);
	}

	@Test
	@DisplayName("非法正文类型：命令非法")
	void validate_invalidContentType_shouldReject() {
		// contentType 须为枚举
		InAppComposeForm form = baseForm();
		form.setRecipientScopeType(RecipientScopeType.ALL.name());
		form.setContentType("HTML");

		assertThatThrownBy(() -> InAppComposeFormValidation.validate(form)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(MESSAGE_COMMAND_INVALID);
	}

}
