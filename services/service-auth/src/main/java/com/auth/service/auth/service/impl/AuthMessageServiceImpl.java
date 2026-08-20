package com.auth.service.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.feign.SystemMessageSendFeignClient;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.module.message.api.model.enums.scene.EmailMessageScene;
import com.auth.module.message.api.model.enums.scene.SmsMessageScene;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.enums.CredentialDimension;
import com.auth.service.auth.service.AuthMessageService;
import com.auth.service.auth.support.redis.store.LoginVerificationCodeStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.auth.module.security.contract.redis.SecurityRedisKey.EMAIL_CODE;
import static com.auth.module.security.contract.redis.SecurityRedisKey.SMS_CODE;

/**
 * 认证消息服务实现：登录邮箱/短信验证码发送
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AuthMessageServiceImpl implements AuthMessageService {

	private final SystemMessageSendFeignClient systemMessageSendFeignClient;

	private final UserMapper userMapper;

	private final LoginVerificationCodeStore loginVerificationCodeStore;

	/**
	 * 发送登录邮箱验证码
	 * @param email 邮箱
	 */
	@Override
	public void sendEmailCode(String email) {
		String code = RandomUtil.randomNumbers(6);
		String digest = MD5.create().digestHex16(code);

		// 存入 Redis 时使用 MD5，登录校验侧对比 digestHex16
		loginVerificationCodeStore.storeDigest(CredentialDimension.EMAIL, email, digest);

		// 过期时间转成分钟
		long expireMinutes = EMAIL_CODE.getDefaultTtl().toMinutes();

		// 未找到活跃用户时静默返回，不暴露账号不存在
		UserEntity userEntity = userMapper.selectByCredential(CredentialDimension.EMAIL, email, true);
		if (userEntity == null) {
			log.warn("Send email code skipped: no active user found for email={}", email);
			return;
		}

		// 拼装发送对象
		String username = userEntity.getUsername();
		Map<String, Object> variables = Map.of("username", username, "code", code, "min", expireMinutes);
		EmailChannelOptions options = new EmailChannelOptions();
		options.setHasHtml(Boolean.TRUE);
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(EmailMessageScene.LOGIN_EMAIL.getChannel())
			.templateCode(EmailMessageScene.LOGIN_EMAIL.getTemplateCode())
			.targets(List.of(email))
			.variables(variables)
			.options(options)
			.build();

		systemMessageSendFeignClient.sendByTemplate(command);
	}

	/**
	 * 发送登录手机短信验证码
	 * @param phone 手机号
	 */
	@Override
	public void sendSmsCode(String phone) {
		String code = RandomUtil.randomNumbers(6);
		String digest = MD5.create().digestHex16(code);

		// 存入Redis
		loginVerificationCodeStore.storeDigest(CredentialDimension.PHONE, phone, digest);

		// 存入Redis
		long expireMinutes = SMS_CODE.getDefaultTtl().toMinutes();
		UserEntity userEntity = userMapper.selectByCredential(CredentialDimension.PHONE, phone, true);
		if (userEntity == null) {
			log.warn("Send SMS code skipped: no active user found for phone={}", phone);
			return;
		}

		// 拼装发送对象
		Map<String, Object> variables = Map.of("code", code, "min", expireMinutes);
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(SmsMessageScene.LOGIN_SMS.getChannel())
			.templateCode(SmsMessageScene.LOGIN_SMS.getTemplateCode())
			.targets(List.of(phone))
			.variables(variables)
			.build();
		systemMessageSendFeignClient.sendByTemplate(command);
	}

}
