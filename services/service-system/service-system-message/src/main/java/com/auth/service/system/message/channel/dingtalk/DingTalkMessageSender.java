package com.auth.service.system.message.channel.dingtalk;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.dingtalk.DingTalkChannelOptions;
import com.auth.module.message.api.model.enums.MessageContentType;
import com.auth.service.system.message.channel.AbstractRecordingMessageSender;
import com.auth.service.system.message.channel.dingtalk.client.DingTalkWorkNoticeClient;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.model.value.delivery.ChannelSendResult;
import com.auth.service.system.message.model.value.dingtalk.RenderedDingTalkNotice;
import com.auth.service.system.message.support.delivery.ChannelDeliveryRecorder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;

/**
 * 钉钉工作通知渠道发送实现（统一消息编排）
 *
 * @author Bunny
 */
@Component
public class DingTalkMessageSender extends AbstractRecordingMessageSender {

	private final DingTalkTemplatePipeline dingTalkTemplatePipeline;

	private final DingTalkWorkNoticeClient workNoticeClient;

	public DingTalkMessageSender(DingTalkTemplatePipeline dingTalkTemplatePipeline,
			DingTalkWorkNoticeClient workNoticeClient, ChannelDeliveryRecorder deliveryRecorder) {
		super(deliveryRecorder);
		this.dingTalkTemplatePipeline = dingTalkTemplatePipeline;
		this.workNoticeClient = workNoticeClient;
	}

	@Override
	public MessageChannel channel() {
		return MessageChannel.DING_TALK;
	}

	@Override
	protected ChannelSendResult doSend(TemplateMessageCommand command) {
		RenderedDingTalkNotice rendered = resolveContent(command);

		String providerMsgId = workNoticeClient.sendWorkNotice(command.getTargets(), rendered.messageType(),
				rendered.title(), rendered.content());
		return ChannelSendResult.sharedSuccess(command.getTargets(), providerMsgId, Instant.now());
	}

	/**
	 * 解析发送正文：自定义正文或模板渲染
	 * @param command 发送命令
	 * @return 渲染结果
	 */
	private RenderedDingTalkNotice resolveContent(TemplateMessageCommand command) {
		Boolean customBody = command.getCustomBody();
		if (customBody == null || !customBody) {
			Map<String, Object> variables = Optional.ofNullable(command.getVariables()).orElseGet(HashMap::new);
			return dingTalkTemplatePipeline.render(command.getTemplateCode(), variables);
		}

		if (CharSequenceUtil.isBlank(command.getBody())) {
			throw new MessageException(PARAM_REQUIRED, "钉钉消息正文");
		}

		DingTalkChannelOptions options = command.getOptions() instanceof DingTalkChannelOptions dingTalk ? dingTalk
				: new DingTalkChannelOptions();
		MessageContentType type = Objects.requireNonNullElse(options.getMessageType(), MessageContentType.TEXT);
		return RenderedDingTalkNotice.builder()
			.messageType(type)
			.title(options.getTitle())
			.content(command.getBody())
			.build();
	}

}
