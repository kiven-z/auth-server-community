package com.auth.service.system.message.port;

import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.message.service.admin.MessageDispatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * {@link LocalMessageSendOperations} 单元测试
 */
@DisplayName("LocalMessageSendOperations 委托发送")
@ExtendWith(MockitoExtension.class)
class LocalMessageSendOperationsTest {

	@Mock
	private MessageDispatchService messageDispatchService;

	@InjectMocks
	private LocalMessageSendOperations localMessageSendOperations;

	@Test
	@DisplayName("调用 sendByTemplate：委托 MessageDispatchService")
	void sendByTemplate_shouldDelegateToDispatchService() {
		TemplateMessageCommand command = TemplateMessageCommand.builder().build();

		localMessageSendOperations.sendByTemplate(command);

		verify(messageDispatchService).sendByTemplate(command);
	}

}
