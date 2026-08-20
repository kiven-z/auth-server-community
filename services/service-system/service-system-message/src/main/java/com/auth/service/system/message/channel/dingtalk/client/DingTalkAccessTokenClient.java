package com.auth.service.system.message.channel.dingtalk.client;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
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

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.message.exception.MessageResultCode.DING_TALK_OAUTH_TOKEN_REQUEST_FAILED;

/**
 * 钉钉应用 accessToken 获取与内存缓存
 *
 * @author Bunny
 */
@Slf4j
@Component
public class DingTalkAccessTokenClient {

	private static final String OAUTH_TOKEN_URL = "https://api.dingtalk.com/v1.0/oauth2/accessToken";

	/**
	 * 提前刷新秒数，避免临界过期
	 */
	private static final long REFRESH_SKEW_SECONDS = 120L;

	private final DingTalkProperties dingTalkProperties;

	private final RestTemplate restTemplate;

	private final AtomicReference<CachedToken> cachedToken = new AtomicReference<>();

	public DingTalkAccessTokenClient(DingTalkProperties dingTalkProperties, RestTemplate restTemplate) {
		this.dingTalkProperties = dingTalkProperties;
		this.restTemplate = restTemplate;
	}

	/**
	 * 获取有效 accessToken（带缓存）
	 * @return accessToken
	 */
	public String getAccessToken() {
		CachedToken current = cachedToken.get();
		if (current != null && current.isValid()) {
			return current.token();
		}
		return refreshAccessToken();
	}

	/**
	 * 刷新并缓存 accessToken
	 * @return accessToken
	 */
	private synchronized String refreshAccessToken() {
		CachedToken current = cachedToken.get();
		if (current != null && current.isValid()) {
			return current.token();
		}

		dingTalkProperties.assertSendRequiredConfigured();

		// 获取clientId和clientSecret
		String clientId = dingTalkProperties.getClientId();
		String clientSecret = dingTalkProperties.getClientSecret();

		// 创建请求头
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Map<String, String> body = Map.of("appKey", clientId, "appSecret", clientSecret);
		HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

		try {
			// 发起请求
			ResponseEntity<String> response = restTemplate.postForEntity(OAUTH_TOKEN_URL, entity, String.class);
			String responseBody = response.getBody();
			if (CharSequenceUtil.isBlank(responseBody)) {
				throw new MessageException(PARAM_REQUIRED, "OAuth 响应体");
			}

			// 解析响应体
			JSONObject json = JSONUtil.parseObj(responseBody);
			String accessToken = json.getStr("accessToken");
			if (CharSequenceUtil.isBlank(accessToken)) {
				throw new MessageException(PARAM_REQUIRED, "accessToken");
			}

			// 计算过期时间
			Long expireIn = json.getLong("expireIn");
			long expireSeconds = expireIn != null && expireIn > 0 ? expireIn : 7200L;
			Instant expiresAt = Instant.now().plusSeconds(expireSeconds);

			// 缓存token
			cachedToken.set(new CachedToken(accessToken, expiresAt));
			log.debug("DingTalk accessToken refreshed, expiresIn={}s", expireSeconds);
			return accessToken;
		}
		catch (RestClientException ex) {
			log.error("DingTalk OAuth token request failed", ex);
			throw new MessageException(DING_TALK_OAUTH_TOKEN_REQUEST_FAILED, ex.getMessage());
		}
	}

	private record CachedToken(String token, Instant expiresAt) {
		boolean isValid() {
			Instant otherInstant = expiresAt.minusSeconds(REFRESH_SKEW_SECONDS);
			return CharSequenceUtil.isNotBlank(token) && Instant.now().isBefore(otherInstant);
		}
	}

}
