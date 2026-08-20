package com.auth.service.system.message.port;

import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.port.MessageSendOperations;
import com.auth.service.system.message.service.admin.MessageDispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 单体部署：进程内委托 {@link MessageDispatchService}
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class LocalMessageSendOperations implements MessageSendOperations {

	private final MessageDispatchService messageDispatchService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendByTemplate(TemplateMessageCommand command) {
		messageDispatchService.sendByTemplate(command);
	}

}
