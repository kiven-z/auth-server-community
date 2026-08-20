package com.auth.service.system.message.support.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.core.utils.JsonSupport;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.ChannelOptions;
import com.auth.module.message.api.model.dingtalk.DingTalkChannelOptions;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.module.message.api.model.inapp.InAppChannelOptions;
import com.auth.service.system.message.exception.MessageException;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import java.util.Map;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_INVALID;
import static com.auth.service.system.message.exception.MessageResultCode.MESSAGE_COMMAND_INVALID;

/**
 * 模板渠道默认选项 JSON：解析、序列化与请求覆盖合并
 *
 * @author Bunny
 */
@UtilityClass
public class ChannelDefaultsJsonSupport {

	private static final CopyOptions REQUEST_OVERLAY = CopyOptions.create()
		.setIgnoreNullValue(true)
		.setFieldValueEditor((name, value) -> {
			if (value instanceof String str && CharSequenceUtil.isBlank(str)) {
				return null;
			}
			return value;
		});

	/**
	 * 按渠道解析模板默认选项（空白或 SMS 返回 null）
	 * @param channel 发送渠道
	 * @param json 模板 channel_defaults_json
	 * @return 渠道默认选项
	 */
	@SuppressWarnings("unchecked")
	public static ChannelOptions parse(MessageChannel channel, String json) {
		if (channel == null || CharSequenceUtil.isBlank(json)) {
			return null;
		}
		Class<? extends ChannelOptions> type = optionsType(channel);
		if (type == null) {
			return null;
		}
		try {
			Map<String, Object> raw = JsonSupport.fromJson(json, Map.class);
			return BeanUtil.toBean(raw, type);
		}
		catch (IllegalArgumentException ex) {
			throw new MessageException(DATA_INVALID, ex.getMessage());
		}
	}

	/**
	 * 解析站内信模板默认（空白返回 null）
	 * @param json 模板 channel_defaults_json
	 * @return 站内信默认选项
	 */
	public static InAppChannelDefaults parseInApp(String json) {
		if (CharSequenceUtil.isBlank(json)) {
			return null;
		}
		try {
			return JsonSupport.fromJson(json, InAppChannelDefaults.class);
		}
		catch (IllegalArgumentException ex) {
			throw new MessageException(DATA_INVALID, ex.getMessage());
		}
	}

	/**
	 * 序列化站内信模板默认
	 * @param categoryId 默认小类主键
	 * @param linkUrl 默认跳转链接
	 * @return JSON 字符串
	 */
	public static String toInAppJson(Long categoryId, String linkUrl) {
		InAppChannelDefaults defaults = new InAppChannelDefaults();
		defaults.setCategoryId(categoryId);
		defaults.setLinkUrl(CharSequenceUtil.trimToNull(linkUrl));
		try {
			return JsonSupport.toJson(defaults);
		}
		catch (IllegalArgumentException ex) {
			throw new MessageException(DATA_INVALID, ex.getMessage());
		}
	}

	/**
	 * 请求非空字段覆盖模板默认；任一侧为空则返回另一侧
	 * @param defaults 模板默认
	 * @param request 请求覆盖
	 * @return 合并后的选项
	 */
	public static ChannelOptions merge(ChannelOptions defaults, ChannelOptions request) {
		if (defaults == null) {
			return request;
		}
		if (request == null) {
			return defaults;
		}
		if (!defaults.getClass().equals(request.getClass())) {
			throw new MessageException(MESSAGE_COMMAND_INVALID, "channel and options type mismatch");
		}
		ChannelOptions merged = BeanUtil.copyProperties(defaults, defaults.getClass());
		BeanUtil.copyProperties(request, merged, REQUEST_OVERLAY);
		return merged;
	}

	private static Class<? extends ChannelOptions> optionsType(MessageChannel channel) {
		return switch (channel) {
			case EMAIL -> EmailChannelOptions.class;
			case DING_TALK -> DingTalkChannelOptions.class;
			case IN_APP -> InAppChannelOptions.class;
			case SMS -> null;
		};
	}

	/**
	 * 站内信模板默认落库结构（不含发送期 title / contentType）
	 *
	 * @author Bunny
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Getter
	@Setter
	public static class InAppChannelDefaults {

		private Long categoryId;

		private String linkUrl;

	}

}
