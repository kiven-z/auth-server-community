package com.auth.service.system.message.model.vo.template;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 消息模板详情
 *
 * @author Bunny
 */
@Schema(name = "MessageTemplateDetailVO", title = "消息模板详情返回对象")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class MessageTemplateDetailVO extends BaseResponse {

	@Schema(name = "channel", title = "消息渠道")
	private String channel;

	@Schema(name = "templateCode", title = "模板编码")
	private String templateCode;

	@Schema(name = "templateName", title = "模板名称")
	private String templateName;

	@Schema(name = "description", title = "模板描述")
	private String description;

	@Schema(name = "subject", title = "标题")
	private String subject;

	@Schema(name = "content", title = "正文模板")
	private String content;

	@Schema(name = "previewSubject", title = "预览标题")
	private String previewSubject;

	@Schema(name = "previewContent", title = "按 require_fields 示例值渲染后的正文")
	private String previewContent;

	@Schema(name = "contentType", title = "正文展示类型")
	private String contentType;

	@Schema(name = "requireFields", title = "变量声明列表")
	private List<MessageTemplateRequireFieldRow> requireFields;

	@Schema(name = "providerTemplateCode", title = "厂商模板编码")
	private String providerTemplateCode;

	@Schema(name = "imMessageType", title = "IM 消息类型")
	private String imMessageType;

	@Schema(name = "priority", title = "发送优先级")
	private Integer priority;

	@Schema(name = "status", title = "启用状态（true=启用模板，false=停用）")
	private Boolean status;

	@JsonStringFormat
	@Schema(name = "categoryId", title = "默认业务小类 ID")
	private Long categoryId;

	@Schema(name = "linkUrl", title = "默认跳转链接")
	private String linkUrl;

}
