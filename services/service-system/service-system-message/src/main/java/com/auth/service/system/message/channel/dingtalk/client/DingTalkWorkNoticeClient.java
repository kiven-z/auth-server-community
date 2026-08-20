package com.auth.service.system.message.channel.dingtalk.client;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.auth.module.message.api.model.enums.MessageContentType;
import com.auth.service.system.message.config.properties.DingTalkProperties;
import com.auth.service.system.message.exception.MessageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.message.exception.MessageResultCode.DING_TALK_WORK_NOTICE_FAILED;
import static com.auth.service.system.message.exception.MessageResultCode.DING_TALK_WORK_NOTICE_HTTP_ERROR;

/**
 * 钉钉工作通知客户端
 *
 * @author Bunny
 */
@Slf4j
@Component
public class DingTalkWorkNoticeClient {

	private static final String WORK_NOTICE_URL = "https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2";

	private final DingTalkProperties dingTalkProperties;

	private final DingTalkAccessTokenClient accessTokenClient;

	private final RestTemplate restTemplate;

	public DingTalkWorkNoticeClient(DingTalkProperties dingTalkProperties, DingTalkAccessTokenClient accessTokenClient,
			RestTemplate restTemplate) {
		this.dingTalkProperties = dingTalkProperties;
		this.accessTokenClient = accessTokenClient;
		this.restTemplate = restTemplate;
	}

	/**
	 * 发送工作通知
	 * @param userIds 接收人 userid 列表
	 * @param messageType 消息类型
	 * @param title Markdown 标题（TEXT 可空）
	 * @param content 正文
	 * @return 钉钉异步任务 ID（task_id）
	 */
	public String sendWorkNotice(List<String> userIds, MessageContentType messageType, String title, String content) {
		if (CollUtil.isEmpty(userIds)) {
			throw new MessageException(PARAM_REQUIRED, "接收人");
		}
		if (CharSequenceUtil.isBlank(content)) {
			throw new MessageException(PARAM_REQUIRED, "正文");
		}
		dingTalkProperties.assertSendRequiredConfigured();

		Long agentId = dingTalkProperties.getAgentId();

		// 解析消息类型
		MessageContentType resolvedType = Objects.requireNonNullElseGet(messageType,
				dingTalkProperties::getDefaultMessageType);
		String accessToken = accessTokenClient.getAccessToken();
		String useridList = userIds.stream().filter(CharSequenceUtil::isNotBlank).collect(Collectors.joining(","));

		// 构建请求体
		Map<String, Object> requestBody = Map.of("agent_id", agentId, "userid_list", useridList, "msg",
				buildMsgJson(resolvedType, title, content));

		// 构建请求URL
		String url = UriComponentsBuilder.fromUriString(WORK_NOTICE_URL)
			.queryParam("access_token", accessToken)
			.toUriString();

		// 构建请求头
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

		try {
			// 发起请求
			ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
			String taskId = assertSendSuccess(response.getBody(), agentId, useridList);
			log.debug("DingTalk work notice sent, agentId={}, userIds={}, taskId={}", agentId, useridList, taskId);
			return taskId;
		}
		catch (RestClientException ex) {
			log.error("DingTalk work notice HTTP error, agentId={}, userIds={}", agentId, useridList, ex);
			throw new MessageException(DING_TALK_WORK_NOTICE_HTTP_ERROR, ex.getMessage());
		}
	}

	/**
	 * 构建消息JSON
	 * @param messageType 消息类型
	 * @param title 标题
	 * @param content 内容
	 * @return 消息JSON
	 */
	private JSONObject buildMsgJson(MessageContentType messageType, String title, String content) {
		JSONObject payload = new JSONObject();
		String msgType;
		String payloadKey;

		if (messageType == MessageContentType.MARKDOWN) {
			msgType = "markdown";
			payloadKey = "markdown";
			payload.set("title", CharSequenceUtil.blankToDefault(title, "通知"));
			payload.set("text", content);
		}
		else {
			msgType = "text";
			payloadKey = "text";
			payload.set("content", content);
		}

		JSONObject msg = new JSONObject();
		msg.set("msgtype", msgType);
		msg.set(payloadKey, payload);
		return msg;
	}

	/**
	 * 断言发送成功并解析 task_id
	 * @param responseBody 响应体
	 * @param agentId 代理ID
	 * @param useridList 用户ID列表
	 * @return 钉钉异步任务 ID
	 */
	private String assertSendSuccess(String responseBody, Long agentId, String useridList) {
		if (CharSequenceUtil.isBlank(responseBody)) {
			throw new MessageException(PARAM_REQUIRED, "响应体");
		}

		JSONObject json = JSONUtil.parseObj(responseBody);
		Integer errcode = json.getInt("errcode");
		if (errcode != null && errcode == 0) {
			Object taskId = json.get("task_id");
			return taskId == null ? null : String.valueOf(taskId);
		}

		String detail = json.getStr("errmsg", responseBody);
		log.error("DingTalk work notice failed, agentId={}, userIds={}, detail={}", agentId, useridList, detail);
		throw new MessageException(DING_TALK_WORK_NOTICE_FAILED, detail);
	}

}
