package com.auth.service.system.message.channel.email.transport;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.model.dto.EmailSendDTO;
import com.auth.service.system.message.model.value.email.RenderedEmail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_FAILED;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.message.support.template.MessageConfigAssertions.assertNotBlank;
import static com.auth.service.system.message.support.template.MessageConfigAssertions.assertNotNull;

/**
 * JavaMail MIME 邮件传输
 *
 * @author Bunny
 */
@Slf4j
@Component
public class MimeMailTransport {

	private final JavaMailSender mailSender;

	private final EmailAttachmentResolver attachmentResolver;

	@Value("${spring.mail.username}")
	private String defaultFrom;

	public MimeMailTransport(JavaMailSender mailSender, EmailAttachmentResolver attachmentResolver) {
		this.mailSender = mailSender;
		this.attachmentResolver = attachmentResolver;
	}

	/**
	 * 同步发送邮件
	 * @param emailSendDTO 发送请求
	 * @param rendered 渲染后的主题和内容
	 * @return 邮件 Message-ID（厂商回执）
	 */
	public String send(EmailSendDTO emailSendDTO, RenderedEmail rendered) {
		try {
			String subject = rendered.subject();
			String body = rendered.body();
			// 验证发送请求
			validate(emailSendDTO, subject, body);

			// 创建 MIME 消息
			MimeMessage message = mailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(defaultFrom);
			helper.setTo(emailSendDTO.getTo().toArray(String[]::new));
			helper.setSubject(subject);
			helper.setText(body, emailSendDTO.getHasHtml() != null && emailSendDTO.getHasHtml());

			if (CharSequenceUtil.isNotBlank(emailSendDTO.getReplyTo())) {
				helper.setReplyTo(emailSendDTO.getReplyTo());
			}
			if (CollUtil.isNotEmpty(emailSendDTO.getCc())) {
				helper.setCc(emailSendDTO.getCc().toArray(String[]::new));
			}
			if (CollUtil.isNotEmpty(emailSendDTO.getBcc())) {
				helper.setBcc(emailSendDTO.getBcc().toArray(String[]::new));
			}
			helper.setSentDate(new Date());

			// 解析附件
			List<MultipartFile> attachmentFiles = attachmentResolver.resolve(emailSendDTO.getAttachments());
			addAttachments(helper, attachmentFiles);

			mailSender.send(message);
			return message.getMessageID();
		}
		catch (MessagingException | MailException e) {
			log.error("Failed to send email, templateCode={}, reason={}", emailSendDTO.getTemplateCode(),
					e.getMessage(), e);
			throw new MessageException(OPERATION_FAILED, "Email send", e.getMessage());
		}
	}

	/**
	 * 验证发送请求
	 * @param emailSendDTO 发送请求
	 * @param subject 主题
	 * @param body 内容
	 */
	private void validate(EmailSendDTO emailSendDTO, String subject, String body) {
		assertNotNull(emailSendDTO, "Email request");
		if (CollUtil.isEmpty(emailSendDTO.getTo())) {
			throw new MessageException(PARAM_REQUIRED, "Recipients");
		}
		assertNotBlank(subject, "Email subject");
		assertNotBlank(body, "Email body");
	}

	/**
	 * 添加附件
	 * @param helper 邮件助手
	 * @param attachments 附件列表
	 * @throws MessagingException 邮件发送异常
	 */
	private void addAttachments(MimeMessageHelper helper, List<MultipartFile> attachments) throws MessagingException {
		if (CollUtil.isEmpty(attachments)) {
			return;
		}
		for (MultipartFile file : attachments) {
			if (file == null || file.isEmpty()) {
				continue;
			}

			String filename = file.getOriginalFilename();
			if (CharSequenceUtil.isBlank(filename)) {
				throw new MessageException(PARAM_REQUIRED, "Attachment filename");
			}
			helper.addAttachment(Objects.requireNonNull(filename), Objects.requireNonNull(file));
		}
	}

}
