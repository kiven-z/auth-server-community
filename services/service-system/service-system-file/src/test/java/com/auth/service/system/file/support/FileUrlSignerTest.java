package com.auth.service.system.file.support;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.storage.core.StoragePlatformFacade;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link FileUrlSigner} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileUrlSigner 预览地址签名")
@ExtendWith(MockitoExtension.class)
class FileUrlSignerTest {

	@Mock
	private StoragePlatformFacadeRegistry facadeRegistry;

	@Mock
	private StoragePlatformFacade storagePlatformFacade;

	@Mock
	private FileStorageProvider storageProvider;

	@Test
	@DisplayName("sign：委派存储平台生成预签名地址")
	void signDelegatesToProviderPresignGetUrl() {
		// 验证签名器会调用存储平台能力生成真实预签名地址。
		FileUploadProperties properties = new FileUploadProperties();
		properties.setExpireSeconds(600);
		FileUrlSigner signer = new FileUrlSigner(properties, facadeRegistry);
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(storageProvider);
		when(storageProvider.presignGetUrl("public", "avatar/a.png", 600))
			.thenReturn("https://presigned.example.com/avatar/a.png?X-Amz-Signature=xxx");

		String signedUrl = signer.sign("public", "avatar/a.png", "image/png", StoragePlatformEnum.MINIO);

		verify(facadeRegistry).resolve(StoragePlatformEnum.MINIO);
		verify(storageProvider).presignGetUrl("public", "avatar/a.png", 600);
		assertThat(signedUrl).isEqualTo("https://presigned.example.com/avatar/a.png?X-Amz-Signature=xxx");
	}

	@Test
	@DisplayName("sign：不在白名单的文件类型禁止预览")
	void signThrowsWhenContentTypeNotAllowed() {
		// 验证文件类型不符合白名单时会抛出统一业务异常码。
		FileUploadProperties properties = new FileUploadProperties();
		properties.setAllowedContentTypes(java.util.List.of("image/*"));
		FileUrlSigner signer = new FileUrlSigner(properties, facadeRegistry);

		assertThatThrownBy(() -> signer.sign("public", "archive/a.zip", "application/zip", StoragePlatformEnum.MINIO))
			.isInstanceOf(FileStorageException.class)
			.extracting(exception -> ((FileStorageException) exception).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_PREVIEW_CONTENT_TYPE_NOT_ALLOWED);
	}

	@Test
	@DisplayName("isPreviewContentTypeAllowed：image/* 与 pdf 命中白名单")
	void isPreviewContentTypeAllowedMatchesWhitelist() {
		FileUploadProperties properties = new FileUploadProperties();
		FileUrlSigner signer = new FileUrlSigner(properties, facadeRegistry);

		assertThat(signer.isPreviewContentTypeAllowed("image/png")).isTrue();
		assertThat(signer.isPreviewContentTypeAllowed("application/pdf")).isTrue();
		assertThat(
				signer.isPreviewContentTypeAllowed("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
			.isFalse();
	}

	@Test
	@DisplayName("sign：bucket 为空时抛出 DATA_UNAVAILABLE")
	void signThrowsWhenBucketBlank() {
		// 验证缺少 bucket 时会阻断签名流程，避免生成无效访问地址。
		FileUploadProperties properties = new FileUploadProperties();
		FileUrlSigner signer = new FileUrlSigner(properties, facadeRegistry);

		assertThatThrownBy(() -> signer.sign("", "avatar/a.png", "image/png", StoragePlatformEnum.MINIO))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(exception -> ((SystemBusinessException) exception).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);
	}

}
