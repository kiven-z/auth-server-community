package com.auth.service.system.message.support.validation;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.dingtalk.DingTalkChannelOptions;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.module.message.api.model.inapp.InAppChannelOptions;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.support.template.TemplateMessageCommandValidation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.message.exception.MessageResultCode.MESSAGE_COMMAND_INVALID;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link TemplateMessageCommandValidation} 单元测试
 */
@DisplayName("TemplateMessageCommandValidation 命令式全量校验")
class TemplateMessageCommandValidationTest {

	private TemplateMessageCommandValidation commandValidation;

	@BeforeEach
	void setUp() {
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		commandValidation = new TemplateMessageCommandValidation(validator);
	}

	@Test
	@DisplayName("command 为 null 时抛出 PARAM_REQUIRED")
	void validate_shouldThrowWhenCommandNull() {
		assertThatThrownBy(() -> commandValidation.validate(null)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(PARAM_REQUIRED);
	}

	@Test
	@DisplayName("targets 为空时抛出 MESSAGE_COMMAND_INVALID")
	void validate_shouldThrowWhenTargetsEmpty() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("login-email-code")
			.targets(Collections.emptyList())
			.build();

		assertThatThrownBy(() -> commandValidation.validate(command)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(MESSAGE_COMMAND_INVALID);
	}

	@Test
	@DisplayName("缺少 targets 时抛出 MESSAGE_COMMAND_INVALID")
	void validate_shouldThrowWhenTargetsMissing() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("LOGIN_NOTICE")
			.build();

		assertThatThrownBy(() -> commandValidation.validate(command)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(MESSAGE_COMMAND_INVALID);
	}

	@Test
	@DisplayName("模板模式：缺少 templateCode 时抛出 MESSAGE_COMMAND_INVALID")
	void validate_shouldThrowWhenTemplateCodeMissing() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.targets(List.of("a@example.com"))
			.customBody(Boolean.FALSE)
			.build();

		assertThatThrownBy(() -> commandValidation.validate(command)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(MESSAGE_COMMAND_INVALID);
	}

	@Test
	@DisplayName("自定义正文模式：缺少 body 时抛出 MESSAGE_COMMAND_INVALID")
	void validate_shouldThrowWhenCustomBodyWithoutBody() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.DING_TALK)
			.targets(List.of("user-1"))
			.customBody(Boolean.TRUE)
			.build();

		assertThatThrownBy(() -> commandValidation.validate(command)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(MESSAGE_COMMAND_INVALID);
	}

	@Test
	@DisplayName("自定义正文模式：存在 body 时校验通过")
	void validate_shouldPassWhenCustomBodyWithBody() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.DING_TALK)
			.targets(List.of("user-1"))
			.customBody(Boolean.TRUE)
			.body("hello")
			.build();

		assertThatCode(() -> commandValidation.validate(command)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("channel 与 options 不匹配时抛出 MESSAGE_COMMAND_INVALID")
	void validate_shouldThrowWhenChannelOptionsMismatch() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("login-email-code")
			.targets(List.of("a@example.com"))
			.options(new DingTalkChannelOptions())
			.build();

		assertThatThrownBy(() -> commandValidation.validate(command)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(MESSAGE_COMMAND_INVALID);
	}

	@Test
	@DisplayName("SMS 渠道：带 options 时抛出 MESSAGE_COMMAND_INVALID")
	void validate_shouldThrowForSmsWithAnyOptions() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS)
			.templateCode("100001")
			.targets(List.of("13800000000"))
			.options(new EmailChannelOptions())
			.build();

		assertThatThrownBy(() -> commandValidation.validate(command)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(MESSAGE_COMMAND_INVALID);
	}

	@Test
	@DisplayName("IN_APP 渠道：匹配 InAppChannelOptions 时校验通过")
	void validate_shouldPassForInAppWithInAppOptions() {
		InAppChannelOptions options = new InAppChannelOptions();
		options.setCategoryId(104L);
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.IN_APP)
			.templateCode("notice")
			.targets(List.of("1001"))
			.options(options)
			.build();

		assertThatCode(() -> commandValidation.validate(command)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("IN_APP 渠道：options 类型不匹配时抛出 MESSAGE_COMMAND_INVALID")
	void validate_shouldThrowForInAppWithEmailOptions() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.IN_APP)
			.templateCode("notice")
			.targets(List.of("1001"))
			.options(new EmailChannelOptions())
			.build();

		assertThatThrownBy(() -> commandValidation.validate(command)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(MESSAGE_COMMAND_INVALID);
	}

	@Test
	@DisplayName("options 为空时校验通过")
	void validate_shouldPassWhenOptionsNull() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS)
			.templateCode("100001")
			.targets(List.of("13800000000"))
			.build();

		assertThatCode(() -> commandValidation.validate(command)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("合法命令校验通过")
	void validate_shouldPassWhenCommandValid() {
		EmailChannelOptions options = new EmailChannelOptions();
		options.setHasHtml(Boolean.TRUE);
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("login-email-code")
			.targets(List.of("a@example.com"))
			.options(options)
			.build();

		assertThatCode(() -> commandValidation.validate(command)).doesNotThrowAnyException();
	}

}
