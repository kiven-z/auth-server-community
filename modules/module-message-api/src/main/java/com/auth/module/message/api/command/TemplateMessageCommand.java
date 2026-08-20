package com.auth.module.message.api.command;

import com.auth.module.message.api.channel.MessageChannel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 跨渠道模板化消息发送命令（跨服务契约）
 *
 * @author Bunny
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateMessageCommand implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 发送渠道
	 */
	@NotNull
	private MessageChannel channel;

	/**
	 * 场景/模板编码（与 message_template.scene_code 一致；customBody=true 时可省略）
	 */
	private String templateCode;

	/**
	 * 接收目标列表（语义由渠道解释：邮箱 / 手机号 / 用户 ID 等）
	 */
	@NotEmpty(message = "targets must not be empty")
	private List<String> targets;

	/**
	 * 模板变量（FreeMarker 等渲染引擎使用）
	 */
	private transient Map<String, Object> variables;

	/**
	 * 是否使用自定义正文（true 时使用 body，不查库渲染；SMS 渠道不支持）
	 */
	private Boolean customBody;

	/**
	 * 自定义正文（customBody=true 时必填）
	 */
	private String body;

	/**
	 * 渠道扩展参数（类型须与 {@link #channel} 一致，见 {@link ChannelOptions}）
	 */
	@Valid
	private ChannelOptions options;

}
