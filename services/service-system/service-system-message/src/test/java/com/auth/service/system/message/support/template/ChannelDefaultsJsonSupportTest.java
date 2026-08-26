package com.auth.service.system.message.support.template;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.ChannelOptions;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.module.message.api.model.inapp.InAppChannelOptions;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.support.template.ChannelDefaultsJsonSupport.InAppChannelDefaults;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_INVALID;
import static com.auth.service.system.message.exception.MessageResultCode.MESSAGE_COMMAND_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ChannelDefaultsJsonSupport} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ChannelDefaultsJsonSupport 渠道默认选项")
class ChannelDefaultsJsonSupportTest {

	@Test
	@DisplayName("parse：空白 JSON 返回 null")
	void parse_blank_returnsNull() {
		assertThat(ChannelDefaultsJsonSupport.parse(MessageChannel.IN_APP, null)).isNull();
		assertThat(ChannelDefaultsJsonSupport.parse(MessageChannel.IN_APP, " ")).isNull();
	}

	@Test
	@DisplayName("parse：SMS 忽略 JSON")
	void parse_sms_returnsNull() {
		assertThat(ChannelDefaultsJsonSupport.parse(MessageChannel.SMS, "{\"foo\":1}")).isNull();
	}

	@Test
	@DisplayName("parse：无 type 的站内信 JSON 能读出小类与跳转")
	void parse_inAppWithoutType() {
		String json = "{\"linkUrl\":\"/personal/inbox\",\"categoryId\":104}";

		InAppChannelOptions options = (InAppChannelOptions) ChannelDefaultsJsonSupport.parse(MessageChannel.IN_APP,
				json);

		assertThat(options.getCategoryId()).isEqualTo(104L);
		assertThat(options.getLinkUrl()).isEqualTo("/personal/inbox");
	}

	@Test
	@DisplayName("parse：非法 JSON 抛出 DATA_INVALID")
	void parse_invalidJson_throws() {
		assertThatThrownBy(() -> ChannelDefaultsJsonSupport.parse(MessageChannel.IN_APP, "{"))
			.isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(DATA_INVALID);
	}

	@Test
	@DisplayName("toInAppJson/parseInApp：往返保留小类与跳转，空白链接省略")
	void toInAppJson_parseInApp_roundTrip() {
		String json = ChannelDefaultsJsonSupport.toInAppJson(104L, " /personal/inbox ");
		InAppChannelDefaults defaults = ChannelDefaultsJsonSupport.parseInApp(json);

		assertThat(defaults.getCategoryId()).isEqualTo(104L);
		assertThat(defaults.getLinkUrl()).isEqualTo("/personal/inbox");
		assertThat(json).doesNotContain("title");

		String withoutLink = ChannelDefaultsJsonSupport.toInAppJson(104L, "  ");
		assertThat(withoutLink).doesNotContain("linkUrl");
	}

	@Test
	@DisplayName("merge：请求非空字段覆盖模板默认")
	void merge_requestOverlaysDefaults() {
		InAppChannelOptions defaults = new InAppChannelOptions();
		defaults.setCategoryId(104L);
		defaults.setLinkUrl("/from-template");
		InAppChannelOptions request = new InAppChannelOptions();
		request.setLinkUrl("/from-request");

		InAppChannelOptions merged = (InAppChannelOptions) ChannelDefaultsJsonSupport.merge(defaults, request);

		assertThat(merged.getCategoryId()).isEqualTo(104L);
		assertThat(merged.getLinkUrl()).isEqualTo("/from-request");
		assertThat(defaults.getLinkUrl()).isEqualTo("/from-template");
	}

	@Test
	@DisplayName("merge：请求空白字符串不覆盖模板默认")
	void merge_blankRequestString_keepsDefault() {
		InAppChannelOptions defaults = new InAppChannelOptions();
		defaults.setLinkUrl("/from-template");
		InAppChannelOptions request = new InAppChannelOptions();
		request.setLinkUrl("  ");

		InAppChannelOptions merged = (InAppChannelOptions) ChannelDefaultsJsonSupport.merge(defaults, request);

		assertThat(merged.getLinkUrl()).isEqualTo("/from-template");
	}

	@Test
	@DisplayName("merge：任一侧为空则返回另一侧")
	void merge_nullSide_returnsOther() {
		EmailChannelOptions defaults = new EmailChannelOptions();
		defaults.setHasHtml(Boolean.TRUE);

		assertThat(ChannelDefaultsJsonSupport.merge(null, defaults)).isSameAs(defaults);
		assertThat(ChannelDefaultsJsonSupport.merge(defaults, null)).isSameAs(defaults);
		assertThat(ChannelDefaultsJsonSupport.merge(null, null)).isNull();
	}

	@Test
	@DisplayName("merge：类型不一致抛出 MESSAGE_COMMAND_INVALID")
	void merge_typeMismatch_throws() {
		ChannelOptions defaults = new InAppChannelOptions();
		ChannelOptions request = new EmailChannelOptions();

		assertThatThrownBy(() -> ChannelDefaultsJsonSupport.merge(defaults, request))
			.isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(MESSAGE_COMMAND_INVALID);
	}

}
