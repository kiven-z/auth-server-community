package com.auth.service.system.message.exception;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 消息发送、模板渲染与渠道相关结果码
 *
 * @author Bunny
 */
@Getter
public enum MessageResultCode implements SystemResultCode {

	/**
	 * 消息发送命令业务规则不合法
	 */
	MESSAGE_COMMAND_INVALID(400, 283, "MESSAGE_COMMAND_INVALID", "system.message.send.command_invalid"),

	/**
	 * 消息渠道未实现或不支持
	 */
	MESSAGE_CHANNEL_UNSUPPORTED(400, 280, "MESSAGE_CHANNEL_UNSUPPORTED", "system.message.channel.unsupported"),

	/**
	 * 消息渠道已禁用
	 */
	MESSAGE_CHANNEL_DISABLED(400, 281, "MESSAGE_CHANNEL_DISABLED", "system.message.channel.disabled"),

	/**
	 * 消息模板不存在或已禁用
	 */
	TEMPLATE_NOT_FOUND(404, 316, "TEMPLATE_NOT_FOUND", "system.message.template.not_found"),

	/**
	 * 缺少必填模板变量
	 */
	TEMPLATE_VARS_MISSING(422, 317, "TEMPLATE_VARS_MISSING", "system.message.template.vars_missing"),

	/**
	 * 模板渲染失败（{0}=模板名，{1}=详情）
	 */
	TEMPLATE_RENDER_FAILED(500, 295, "TEMPLATE_RENDER_FAILED", "system.message.template.render_failed"),

	/**
	 * SMS 只走模板（templateCode + variables），不支持自定义正文
	 */
	SMS_CUSTOM_BODY_NOT_SUPPORTED(400, 296, "SMS_CUSTOM_BODY_NOT_SUPPORTED", "system.sms.custom_body.not_supported"),

	/**
	 * Dypns 发送被中断
	 */
	SMS_DYPNS_SEND_INTERRUPTED(502, 299, "SMS_DYPNS_SEND_INTERRUPTED", "system.sms.dypns.send_interrupted"),

	/**
	 * Dypns 发送失败（{0}=详情）
	 */
	SMS_DYPNS_SEND_FAILED(502, 300, "SMS_DYPNS_SEND_FAILED", "system.sms.dypns.send_failed"),

	/**
	 * 钉钉 OAuth 请求失败（{0}=详情）
	 */
	DING_TALK_OAUTH_TOKEN_REQUEST_FAILED(502, 303, "DING_TALK_OAUTH_TOKEN_REQUEST_FAILED",
			"system.dingtalk.oauth.token_request_failed"),

	/**
	 * 钉钉工作通知 HTTP 请求失败（{0}=详情）
	 */
	DING_TALK_WORK_NOTICE_HTTP_ERROR(502, 306, "DING_TALK_WORK_NOTICE_HTTP_ERROR",
			"system.dingtalk.work_notice.http_error"),

	/**
	 * 钉钉工作通知发送失败（{0}=详情）
	 */
	DING_TALK_WORK_NOTICE_FAILED(502, 308, "DING_TALK_WORK_NOTICE_FAILED", "system.dingtalk.work_notice.failed"),

	/**
	 * 站内信接收目标非法（须为系统用户 ID）（{0}=详情）
	 */
	IN_APP_TARGET_INVALID(400, 319, "IN_APP_TARGET_INVALID", "system.in_app.target.invalid"),

	/**
	 * 站内信接收范围不合法（{0}=详情）
	 */
	IN_APP_RECIPIENT_SCOPE_INVALID(400, 320, "IN_APP_RECIPIENT_SCOPE_INVALID", "system.in_app.recipient_scope.invalid"),

	/**
	 * 站内信发送任务不存在（{0}=taskId）
	 */
	IN_APP_SEND_TASK_NOT_FOUND(404, 322, "IN_APP_SEND_TASK_NOT_FOUND", "system.in_app.send_task.not_found"),

	/**
	 * 站内信发送任务当前状态不允许该操作（补发 / 撤回 / 删除等）
	 */
	IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED(409, 323, "IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED",
			"system.in_app.send_task.operation_not_allowed"),

	/**
	 * 渠道投递存在失败目标（{0}=失败数，{1}=总数）
	 */
	MESSAGE_DELIVERY_FAILED(502, 324, "MESSAGE_DELIVERY_FAILED", "system.message.delivery.failed"),

	/**
	 * 站内信业务分类不存在或不可用
	 */
	IN_APP_MESSAGE_CATEGORY_NOT_FOUND(404, 326, "IN_APP_MESSAGE_CATEGORY_NOT_FOUND",
			"system.in_app.message_category.not_found");

	private final int httpStatus;

	private final int code;

	private final String error;

	private final String messageKey;

	MessageResultCode(int httpStatus, int code, String error, String messageKey) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.error = error;
		this.messageKey = messageKey;
	}

}
