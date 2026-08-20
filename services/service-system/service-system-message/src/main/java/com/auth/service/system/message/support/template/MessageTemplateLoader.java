package com.auth.service.system.message.support.template;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.MessageTemplateMapper;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import org.springframework.stereotype.Component;

import static com.auth.service.system.message.exception.MessageResultCode.TEMPLATE_NOT_FOUND;

/**
 * 按场景编码与渠道加载已启用的消息模板
 *
 * @author Bunny
 */
@Component
public class MessageTemplateLoader {

	private final MessageTemplateMapper messageTemplateMapper;

	public MessageTemplateLoader(MessageTemplateMapper messageTemplateMapper) {
		this.messageTemplateMapper = messageTemplateMapper;
	}

	/**
	 * 加载已启用的模板
	 * @param channel 发送渠道
	 * @param sceneCode 场景编码
	 * @return 模板实体
	 */
	public MessageTemplateEntity loadEnabled(MessageChannel channel, String sceneCode) {
		MessageTemplateEntity template = messageTemplateMapper.selectEnabledBySceneCodeAndChannel(sceneCode,
				channel.name());
		if (template == null) {
			throw new MessageException(TEMPLATE_NOT_FOUND, channel.name(), sceneCode);
		}
		return template;
	}

}
