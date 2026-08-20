package com.auth.service.system.message.controller.internal;

import com.auth.common.core.model.response.Result;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.service.system.message.service.admin.MessageDispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部-统一消息发送
 *
 * @author Bunny
 */
@InternalApi
@Tag(name = "内部-统一消息发送", description = "按 channel 路由至邮件、短信、钉钉等渠道")
@RequestMapping("/api/system/inner/message")
@RestController
public class InternalMessageSendController {

	private final MessageDispatchService messageDispatchService;

	public InternalMessageSendController(MessageDispatchService messageDispatchService) {
		this.messageDispatchService = messageDispatchService;
	}

	@Operation(summary = "发送消息")
	@PostMapping("/send")
	public Result<String> sendByTemplate(@RequestBody TemplateMessageCommand command) {
		messageDispatchService.sendByTemplate(command);
		return Result.success("Message send request accepted");
	}

}
