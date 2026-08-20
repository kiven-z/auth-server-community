package com.auth.service.system.message.support.template;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.ChannelOptions;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 将模板渠道默认选项合并进发送命令
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class ChannelDefaultsEnricher {

	private final MessageTemplateLoader messageTemplateLoader;

	/**
	 * 自定义正文不读模板；其余按 scene 加载默认并与请求 options 合并
	 * @param command 发送命令
	 * @return 合并后的同一命令实例
	 */
	public TemplateMessageCommand enrich(TemplateMessageCommand command) {
		if (command == null || Boolean.TRUE.equals(command.getCustomBody())) {
			return command;
		}
		if (CharSequenceUtil.isBlank(command.getTemplateCode())) {
			return command;
		}
		MessageChannel channel = command.getChannel();
		if (channel == null) {
			return command;
		}
		MessageTemplateEntity template = messageTemplateLoader.loadEnabled(channel, command.getTemplateCode());
		ChannelOptions defaults = ChannelDefaultsJsonSupport.parse(channel, template.getChannelDefaultsJson());
		if (defaults == null) {
			return command;
		}
		command.setOptions(ChannelDefaultsJsonSupport.merge(defaults, command.getOptions()));
		return command;
	}

}
