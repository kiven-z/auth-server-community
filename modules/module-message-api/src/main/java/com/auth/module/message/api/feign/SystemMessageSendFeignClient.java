package com.auth.module.message.api.feign;

import com.auth.common.core.model.response.Result;
import com.auth.module.message.api.command.TemplateMessageCommand;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用 service-system 消息发送内部接口。
 *
 * @author Bunny
 */
@FeignClient(name = "service-system", contextId = "systemMessageSend", path = "/api/system/inner/message")
public interface SystemMessageSendFeignClient {

	/**
	 * 按模板发送消息（多渠道统一入口）。
	 * @param command 模板发送命令
	 * @return 统一响应，data 可为消息 ID
	 */
	@PostMapping("send")
	Result<String> sendByTemplate(@RequestBody TemplateMessageCommand command);

}
