package com.auth.service.system.message.channel.email.transport;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.PrimitiveArrayUtil;
import com.auth.common.web.multipart.InMemoryMultipartFile;
import com.auth.module.message.api.model.email.AttachmentDTO;
import com.auth.service.system.message.exception.MessageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_FAILED;

/**
 * 将附件声明解析为可随信发送的二进制内容
 *
 * @author Bunny
 */
@Slf4j
@Component
public class EmailAttachmentResolver {

	private final RestTemplate restTemplate;

	public EmailAttachmentResolver(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	/**
	 * 将附件声明解析为可随信发送的二进制内容
	 * @param attachments 附件声明列表
	 * @return 可随信发送的二进制内容
	 */
	public List<MultipartFile> resolve(List<AttachmentDTO> attachments) {
		if (CollUtil.isEmpty(attachments)) {
			return Collections.emptyList();
		}

		return attachments.stream()
			// 1. 过滤掉空对象以及文件名为空的无效附件
			.filter(attachment -> attachment != null && CharSequenceUtil.isNotBlank(attachment.getFilename()))
			// 2. 映射为包含字节数组的临时包装对象（或直接转换，这里用流式转换）
			.map(attachment -> {
				byte[] content = resolveBytes(attachment);
				if (PrimitiveArrayUtil.isEmpty(content)) {
					return null; // 先标记为 null，后面统一过滤
				}
				return InMemoryMultipartFile.builder()
					.name(attachment.getFilename())
					.originalFilename(attachment.getFilename())
					.contentType(attachment.getContentType())
					.content(content)
					.build();
			})
			// 3. 过滤掉解析内容失败（返回 null）的项
			.filter(Objects::nonNull)
			// 4. 收集成 List
			.collect(Collectors.toList());
	}

	/**
	 * 将附件声明解析为可随信发送的二进制内容
	 * @param attachment 附件声明
	 * @return 可随信发送的二进制内容
	 */
	private byte[] resolveBytes(AttachmentDTO attachment) {
		// 有Base64编码，解析为二进制内容
		String attachmentBase64 = attachment.getBase64();
		if (CharSequenceUtil.isNotBlank(attachmentBase64)) {
			try {
				return Base64.decode(attachmentBase64);
			}
			catch (IllegalArgumentException e) {
				throw new MessageException(OPERATION_FAILED, "Attachment Base64 decode", e.getMessage());
			}
		}

		// 有字节数组，直接返回
		byte[] attachmentBytes = attachment.getBytes();
		if (attachmentBytes != null && attachmentBytes.length > 0) {
			return attachmentBytes;
		}

		String attachmentUrl = attachment.getUrl();
		if (CharSequenceUtil.isBlank(attachmentUrl)) {
			return new byte[0];
		}

		// 有URL，下载为二进制内容
		try {
			ResponseEntity<byte[]> response = restTemplate.getForEntity(attachmentUrl, byte[].class);
			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				return response.getBody();
			}
			throw new MessageException(OPERATION_FAILED, "Attachment download",
					"HTTP " + response.getStatusCode().value());
		}
		catch (MessageException e) {
			throw e;
		}
		catch (Exception e) {
			log.error("Failed to download attachment from URL", e);
			throw new MessageException(OPERATION_FAILED, "Attachment download", e.getMessage());
		}
	}

}
