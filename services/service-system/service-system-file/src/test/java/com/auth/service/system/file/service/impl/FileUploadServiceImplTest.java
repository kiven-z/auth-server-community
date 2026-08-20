package com.auth.service.system.file.service.impl;

import com.auth.module.file.api.model.dto.FileUploadResultDTO;
import com.auth.module.file.api.model.enums.FileUploadMode;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.model.form.FileUploadForm;
import com.auth.service.system.file.model.form.MultipleFileUploadForm;
import com.auth.service.system.file.model.value.FileUploadCommand;
import com.auth.service.system.file.model.value.StoredFile;
import com.auth.service.system.file.storage.core.StoragePlatformFacade;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * {@link FileUploadServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileUploadServiceImpl 上传")
@ExtendWith(MockitoExtension.class)
class FileUploadServiceImplTest {

	@Mock
	private StoragePlatformFacadeRegistry facadeRegistry;

	@Mock
	private StoragePlatformFacade storagePlatformFacade;

	@Mock
	private FileStorageProvider fileStorageProvider;

	@Mock
	private FileRecordMapper fileRecordMapper;

	private FileUploadServiceImpl fileUploadService;

	private static byte[] minimalPngBytes() {
		return new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48,
				0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90,
				0x77, 0x53, (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41, 0x54, 0x08, (byte) 0xD7, 0x63,
				(byte) 0xF8, (byte) 0xCF, (byte) 0xC0, 0x00, 0x00, 0x03, 0x01, 0x01, 0x00, 0x18, (byte) 0xDD,
				(byte) 0x8D, (byte) 0xB0, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60,
				(byte) 0x82 };
	}

	private static byte[] minimalJpegBytes() {
		return new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46,
				0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, (byte) 0xFF, (byte) 0xD9 };
	}

	private static byte[] minimalPdfBytes() {
		return "%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n".getBytes(StandardCharsets.ISO_8859_1);
	}

	@BeforeEach
	void setUp() throws Exception {
		FileUploadProperties fileUploadProperties = new FileUploadProperties();
		fileUploadProperties.setDefaultPlatform(StoragePlatformEnum.MINIO);
		fileUploadService = spy(new FileUploadServiceImpl(fileUploadProperties, facadeRegistry));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(fileUploadService, fileRecordMapper);
	}

	@Test
	@DisplayName("upload：avatar 固定公开前缀并落库 isPrivate=false")
	void uploadUsesPublicPrefixAndPersistsResolvedVisibilityForAvatar() {
		// 验证 avatar 业务类型固定公开，且对象键以 public/avatar 开头。
		FileUploadForm form = buildForm(new MockMultipartFile("file", "avatar.png", "image/png", minimalPngBytes()));
		form.setIsPrivate(true);
		StoredFile storedFile = buildStoredFile(StoragePlatformEnum.MINIO, "public/avatar/20260702/object.png");
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(fileStorageProvider);
		when(fileStorageProvider.upload(any(FileUploadCommand.class))).thenReturn(storedFile);
		doAnswer(invocation -> {
			FileRecordEntity entity = invocation.getArgument(0);
			entity.setId(1001L);
			return true;
		}).when(fileUploadService).save(any(FileRecordEntity.class));

		FileUploadResultDTO response = fileUploadService.upload(form);

		ArgumentCaptor<FileRecordEntity> entityCaptor = ArgumentCaptor.forClass(FileRecordEntity.class);
		verify(fileUploadService).save(entityCaptor.capture());
		FileRecordEntity savedEntity = entityCaptor.getValue();
		assertThat(savedEntity.getStoragePlatform()).isEqualTo(StoragePlatformEnum.MINIO);
		assertThat(savedEntity.getUploadMode()).isEqualTo(FileUploadMode.SIMPLE.getCode());
		assertThat(savedEntity.getBizType()).isEqualTo("avatar");
		assertThat(savedEntity.getIsPrivate()).isFalse();
		ArgumentCaptor<FileUploadCommand> commandCaptor = ArgumentCaptor.forClass(FileUploadCommand.class);
		verify(fileStorageProvider).upload(commandCaptor.capture());
		assertThat(commandCaptor.getValue().getObjectKey()).startsWith("public/avatar/");
		assertThat(response.getId()).isEqualTo(1001L);
		assertThat(response.getStoragePlatform()).isEqualTo(StoragePlatformEnum.MINIO);
	}

	@Test
	@DisplayName("upload：attachment 默认私有并写入 private 前缀")
	void uploadPersistsPrivateFlagForAttachmentBizType() {
		// 验证 attachment 策略默认私有，且对象键以 private/attachment 开头。
		FileUploadForm form = buildForm(
				new MockMultipartFile("file", "private.txt", "text/plain", "demo".getBytes(StandardCharsets.UTF_8)));
		form.setBizType("attachment");
		StoredFile storedFile = buildStoredFile(StoragePlatformEnum.MINIO, "private/attachment/20260702/object.txt");
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(fileStorageProvider);
		when(fileStorageProvider.upload(any(FileUploadCommand.class))).thenReturn(storedFile);
		doReturn(true).when(fileUploadService).save(any(FileRecordEntity.class));

		fileUploadService.upload(form);

		ArgumentCaptor<FileRecordEntity> entityCaptor = ArgumentCaptor.forClass(FileRecordEntity.class);
		verify(fileUploadService).save(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getIsPrivate()).isTrue();
		ArgumentCaptor<FileUploadCommand> commandCaptor = ArgumentCaptor.forClass(FileUploadCommand.class);
		verify(fileStorageProvider).upload(commandCaptor.capture());
		assertThat(commandCaptor.getValue().getObjectKey()).startsWith("private/attachment/");
	}

	@Test
	@DisplayName("upload：未知 bizType 时抛出 IllegalArgumentException")
	void uploadThrowsWhenBizTypeInvalid() {
		// 验证未知业务类型在严格模式下被拒绝。
		FileUploadForm form = buildForm(new MockMultipartFile("file", "avatar.jpg", "image/jpeg", minimalJpegBytes()));
		form.setBizType("foo");

		assertThatThrownBy(() -> fileUploadService.upload(form)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("foo");
	}

	@Test
	@DisplayName("upload：请求指定平台时优先使用请求平台")
	void uploadUsesRequestPlatformWhenSpecified() {
		// 验证请求参数 storagePlatform 优先于默认配置。
		FileUploadForm form = buildForm(new MockMultipartFile("file", "doc.pdf", "application/pdf", minimalPdfBytes()));
		form.setBizType("attachment");
		form.setStoragePlatform("aliyun_oss");
		StoredFile storedFile = buildStoredFile(StoragePlatformEnum.ALIYUN_OSS, "attachment/20260702/object.pdf");
		when(facadeRegistry.resolve(StoragePlatformEnum.ALIYUN_OSS)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(fileStorageProvider);
		when(fileStorageProvider.upload(any(FileUploadCommand.class))).thenReturn(storedFile);
		doReturn(true).when(fileUploadService).save(any(FileRecordEntity.class));

		fileUploadService.upload(form);

		verify(facadeRegistry).resolve(StoragePlatformEnum.ALIYUN_OSS);
	}

	@Test
	@DisplayName("upload：文件为空时抛出 PARAM_REQUIRED")
	void uploadThrowsWhenFileMissing() {
		// 验证空文件参数会在服务层被拦截。
		FileUploadForm form = new FileUploadForm();

		assertThatThrownBy(() -> fileUploadService.upload(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);
	}

	@Test
	@DisplayName("upload：平台非法时抛出 IllegalArgumentException")
	void uploadThrowsWhenPlatformInvalid() {
		// 验证非法平台字符串会被枚举严格解析直接拒绝。
		FileUploadForm form = buildForm(new MockMultipartFile("file", "avatar.jpg", "image/jpeg", minimalJpegBytes()));
		form.setStoragePlatform("s3");

		assertThatThrownBy(() -> fileUploadService.upload(form)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Unsupported storage platform");
	}

	@Test
	@DisplayName("upload：危险后缀在落存储前被拒绝")
	void uploadRejectsBlockedExtensionBeforeStorage() {
		// 验证黑名单后缀不会触发存储上传
		FileUploadForm form = buildForm(
				new MockMultipartFile("file", "malware.exe", "application/octet-stream", minimalPngBytes()));
		form.setBizType("attachment");

		assertThatThrownBy(() -> fileUploadService.upload(form)).isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_EXTENSION_BLOCKED);
		verifyNoInteractions(facadeRegistry);
	}

	@Test
	@DisplayName("uploadMultiple：多个文件均成功上传并批量落库")
	void uploadMultipleSucceedsForMultipleFiles() {
		// 验证多文件上传走统一路径并 saveBatch 批量持久化。
		MultipleFileUploadForm form = buildMultipleForm(
				new MockMultipartFile("files", "a.png", "image/png", minimalPngBytes()),
				new MockMultipartFile("files", "b.png", "image/png", minimalPngBytes()));
		StoredFile storedFile = buildStoredFile(StoragePlatformEnum.MINIO, "public/avatar/20260702/object.png");
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(fileStorageProvider);
		when(fileStorageProvider.upload(any(FileUploadCommand.class))).thenReturn(storedFile);
		stubSaveBatchWithIds(1001L, 1002L);

		List<FileUploadResultDTO> responses = fileUploadService.uploadMultiple(form);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<FileRecordEntity>> batchCaptor = ArgumentCaptor.forClass(List.class);
		verify(fileUploadService).saveBatch(batchCaptor.capture());
		assertThat(batchCaptor.getValue()).hasSize(2);
		assertThat(responses).hasSize(2);
		assertThat(responses.get(0).getId()).isEqualTo(1001L);
		assertThat(responses.get(1).getId()).isEqualTo(1002L);
		verify(fileStorageProvider, times(2)).upload(any(FileUploadCommand.class));
	}

	@Test
	@DisplayName("uploadMultiple：列表含空文件时抛出 PARAM_REQUIRED")
	void uploadMultipleThrowsWhenFileIsEmpty() {
		// 验证批量上传不会静默跳过空文件。
		MultipleFileUploadForm form = buildMultipleForm(
				new MockMultipartFile("files", "empty.png", "image/png", new byte[0]),
				new MockMultipartFile("files", "a.png", "image/png", minimalPngBytes()));

		assertThatThrownBy(() -> fileUploadService.uploadMultiple(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);
	}

	@Test
	@DisplayName("uploadMultiple：列表含 null 文件时抛出 PARAM_REQUIRED")
	void uploadMultipleThrowsWhenFileIsNull() {
		// 验证批量上传对 null 文件与单文件上传一致地拒绝。
		List<MultipartFile> files = new ArrayList<>();
		files.add(null);
		files.add(new MockMultipartFile("files", "a.png", "image/png", minimalPngBytes()));
		MultipleFileUploadForm form = buildMultipleForm(files);

		assertThatThrownBy(() -> fileUploadService.uploadMultiple(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);
	}

	@Test
	@DisplayName("uploadMultiple：请求指定平台时优先使用请求平台")
	void uploadMultipleUsesRequestPlatformWhenSpecified() {
		// 验证批量上传与单文件上传共享平台解析逻辑。
		MultipleFileUploadForm form = buildMultipleForm(
				new MockMultipartFile("files", "doc.pdf", "application/pdf", minimalPdfBytes()));
		form.setBizType("attachment");
		form.setStoragePlatform("aliyun_oss");
		StoredFile storedFile = buildStoredFile(StoragePlatformEnum.ALIYUN_OSS, "attachment/20260702/object.pdf");
		when(facadeRegistry.resolve(StoragePlatformEnum.ALIYUN_OSS)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(fileStorageProvider);
		when(fileStorageProvider.upload(any(FileUploadCommand.class))).thenReturn(storedFile);
		doReturn(true).when(fileUploadService).saveBatch(anyList());

		fileUploadService.uploadMultiple(form);

		verify(facadeRegistry).resolve(StoragePlatformEnum.ALIYUN_OSS);
	}

	private FileUploadForm buildForm(MockMultipartFile file) {
		FileUploadForm form = new FileUploadForm();
		form.setFile(file);
		form.setBizType("avatar");
		form.setBizId("u1001");
		form.setRemark("test");
		return form;
	}

	private MultipleFileUploadForm buildMultipleForm(MockMultipartFile... files) {
		return buildMultipleForm(List.of(files));
	}

	private MultipleFileUploadForm buildMultipleForm(List<MultipartFile> files) {
		MultipleFileUploadForm form = new MultipleFileUploadForm();
		form.setFiles(files);
		form.setBizType("avatar");
		form.setBizId("u1001");
		form.setRemark("test");
		return form;
	}

	private StoredFile buildStoredFile(StoragePlatformEnum platform, String objectKey) {
		return StoredFile.builder()
			.storagePlatform(platform)
			.bucket("public")
			.objectKey(objectKey)
			.url("https://cdn.example.com/" + objectKey)
			.originalName("origin")
			.extension("png")
			.contentType("image/png")
			.size(1024L)
			.etag("etag-value")
			.build();
	}

	private void stubSaveBatchWithIds(Long... ids) {
		doAnswer(invocation -> {
			List<FileRecordEntity> batch = invocation.getArgument(0);
			for (int i = 0; i < ids.length; i++) {
				batch.get(i).setId(ids[i]);
			}
			return true;
		}).when(fileUploadService).saveBatch(anyList());
	}

}
