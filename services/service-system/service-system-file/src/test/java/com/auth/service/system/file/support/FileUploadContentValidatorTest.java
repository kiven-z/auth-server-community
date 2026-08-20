package com.auth.service.system.file.support;

import com.auth.module.file.api.policy.FileBizType;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link FileUploadContentValidator} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileUploadContentValidator 上传内容校验")
class FileUploadContentValidatorTest {

	/**
	 * 最小合法 PNG（1x1）字节，供魔数检测识别为 image/png
	 */
	private static byte[] minimalPngBytes() {
		return new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48,
				0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90,
				0x77, 0x53, (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41, 0x54, 0x08, (byte) 0xD7, 0x63,
				(byte) 0xF8, (byte) 0xCF, (byte) 0xC0, 0x00, 0x00, 0x03, 0x01, 0x01, 0x00, 0x18, (byte) 0xDD,
				(byte) 0x8D, (byte) 0xB0, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60,
				(byte) 0x82 };
	}

	/**
	 * 最小 PDF 头，供魔数检测识别为 application/pdf
	 */
	private static byte[] minimalPdfBytes() {
		return "%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n".getBytes(StandardCharsets.ISO_8859_1);
	}

	@Test
	@DisplayName("validate：合法 PNG 头像通过")
	void acceptsValidAvatarPng() {
		// 验证魔数与白名单、大小均满足时放行
		MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", minimalPngBytes());

		assertThatCode(() -> FileUploadContentValidator.validate(file, FileBizType.AVATAR)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("validate：exe 后缀命中黑名单")
	void rejectsBlockedExtension() {
		// 验证危险后缀在魔数检测前被拦截
		MockMultipartFile file = new MockMultipartFile("file", "payload.exe", "application/octet-stream",
				minimalPngBytes());

		assertThatThrownBy(() -> FileUploadContentValidator.validate(file, FileBizType.ATTACHMENT))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_EXTENSION_BLOCKED);
	}

	@Test
	@DisplayName("validate：超过业务大小上限")
	void rejectsOversizedFile() {
		// 验证 avatar 2MB 上限生效
		byte[] oversized = new byte[(int) FileBizType.AVATAR.maxSizeBytes() + 1];
		System.arraycopy(minimalPngBytes(), 0, oversized, 0, minimalPngBytes().length);
		MockMultipartFile file = new MockMultipartFile("file", "big.png", "image/png", oversized);

		assertThatThrownBy(() -> FileUploadContentValidator.validate(file, FileBizType.AVATAR))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_SIZE_EXCEEDED);
	}

	@Test
	@DisplayName("validate：伪装成 png 的文本被魔数拒绝")
	void rejectsMimeMismatchByMagic() {
		// 验证仅改后缀/Content-Type 无法绕过魔数白名单
		MockMultipartFile file = new MockMultipartFile("file", "fake.png", "image/png",
				"not-an-image".getBytes(StandardCharsets.UTF_8));

		assertThatThrownBy(() -> FileUploadContentValidator.validate(file, FileBizType.AVATAR))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_TYPE_NOT_ALLOWED);
	}

	@Test
	@DisplayName("validate：PDF 不允许作为头像")
	void rejectsPdfForAvatar() {
		// 验证业务类型白名单隔离
		MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", minimalPdfBytes());

		assertThatThrownBy(() -> FileUploadContentValidator.validate(file, FileBizType.AVATAR))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_TYPE_NOT_ALLOWED);
	}

	@Test
	@DisplayName("validate：PDF 允许作为附件")
	void acceptsPdfForAttachment() {
		// 验证 attachment 白名单包含 PDF
		MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", minimalPdfBytes());

		assertThatCode(() -> FileUploadContentValidator.validate(file, FileBizType.ATTACHMENT))
			.doesNotThrowAnyException();
	}

}
