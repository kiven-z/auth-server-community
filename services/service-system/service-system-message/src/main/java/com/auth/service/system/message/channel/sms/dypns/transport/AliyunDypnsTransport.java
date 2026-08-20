package com.auth.service.system.message.channel.sms.dypns.transport;

import cn.hutool.core.text.CharSequenceUtil;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.auth.service.system.message.config.properties.SmsProperties;
import com.auth.service.system.message.exception.MessageException;
import darabonba.core.client.ClientOverrideConfiguration;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.message.exception.MessageResultCode.SMS_DYPNS_SEND_FAILED;
import static com.auth.service.system.message.exception.MessageResultCode.SMS_DYPNS_SEND_INTERRUPTED;

/**
 * 阿里云 Dypns 验证码短信发送
 *
 * @author Bunny
 */
@Slf4j
@Component
public class AliyunDypnsTransport implements AutoCloseable {

	private final SmsProperties smsProperties;

	private final AtomicReference<AsyncClient> asyncClientRef = new AtomicReference<>();

	public AliyunDypnsTransport(SmsProperties smsProperties) {
		this.smsProperties = smsProperties;
	}

	/**
	 * 创建异步客户端
	 * @param smsProperties 短信配置
	 * @return 异步客户端
	 */
	private static AsyncClient createAsyncClient(SmsProperties smsProperties) {
		// 创建凭证提供者
		Credential credentials = Credential.builder()
			.accessKeyId(smsProperties.getAccessKeyId())
			.accessKeySecret(smsProperties.getAccessKeySecret())
			.build();
		StaticCredentialProvider credentialProvider = StaticCredentialProvider.create(credentials);

		// 创建异步客户端
		String endpoint = smsProperties.getEndpoint();
		ClientOverrideConfiguration overrideConfiguration = ClientOverrideConfiguration.create()
			.setEndpointOverride(endpoint);
		return AsyncClient.builder()
			.region(smsProperties.getRegion())
			.credentialsProvider(credentialProvider)
			.overrideConfiguration(overrideConfiguration)
			.build();
	}

	/**
	 * 获取或创建异步客户端
	 * @return 异步客户端
	 */
	private AsyncClient getOrCreateAsyncClient() {
		AsyncClient currentClient = asyncClientRef.get();
		if (currentClient != null) {
			return currentClient;
		}

		synchronized (this) {
			currentClient = asyncClientRef.get();
			if (currentClient == null) {
				currentClient = createAsyncClient(smsProperties);
				asyncClientRef.set(currentClient);
			}
			return currentClient;
		}
	}

	/**
	 * 向指定手机号发送验证码短信
	 * @param phone 手机号
	 * @param providerTemplateCode 厂商侧模板编码
	 * @param templateParamJson 模板变量 JSON
	 * @return 厂商回执 ID（优先 BizId，其次 RequestId）
	 */
	public String sendVerifyCode(String phone, String providerTemplateCode, String templateParamJson) {
		smsProperties.assertSendRequiredConfigured();

		// 验证手机号
		if (CharSequenceUtil.isBlank(phone)) {
			throw new MessageException(PARAM_REQUIRED, "手机号");
		}
		// 验证厂商模板编码
		if (CharSequenceUtil.isBlank(providerTemplateCode)) {
			throw new MessageException(PARAM_REQUIRED, "厂商模板编码");
		}

		// 构建请求
		SendSmsVerifyCodeRequest request = SendSmsVerifyCodeRequest.builder()
			.phoneNumber(phone)
			.signName(smsProperties.getSignName())
			.templateCode(providerTemplateCode)
			.templateParam(CharSequenceUtil.blankToDefault(templateParamJson, "{}"))
			.build();

		try {
			// 发送请求
			CompletableFuture<SendSmsVerifyCodeResponse> future = getOrCreateAsyncClient().sendSmsVerifyCode(request);
			SendSmsVerifyCodeResponse response = future.get();

			var body = response == null ? null : response.getBody();
			if (body == null || !"OK".equalsIgnoreCase(body.getCode())) {
				throw new MessageException(SMS_DYPNS_SEND_FAILED, String.valueOf(body));
			}

			var model = body.getModel();
			return model != null && CharSequenceUtil.isNotBlank(model.getBizId()) ? model.getBizId()
					: CharSequenceUtil.blankToDefault(body.getRequestId(), null);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new MessageException(SMS_DYPNS_SEND_INTERRUPTED);
		}
		catch (ExecutionException e) {
			throw new MessageException(SMS_DYPNS_SEND_FAILED, e.getMessage());
		}
	}

	@PreDestroy
	@Override
	public void close() {
		AsyncClient client = asyncClientRef.get();
		if (client != null) {
			try {
				client.close();
			}
			catch (Exception e) {
				log.warn("Failed to close Dypns client", e);
			}
		}
	}

}
