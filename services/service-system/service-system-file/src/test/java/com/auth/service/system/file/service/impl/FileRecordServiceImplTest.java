package com.auth.service.system.file.service.impl;

import com.auth.module.file.api.model.enums.FileDeleteSource;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.module.file.delivery.FileDelivery;
import com.auth.module.file.delivery.FileDownloadNames;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.storage.core.StoragePlatformFacade;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link FileRecordServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileRecordServiceImpl 查询与治理")
@ExtendWith(MockitoExtension.class)
class FileRecordServiceImplTest {

	@Mock
	private FileRecordMapper fileRecordMapper;

	@Mock
	private StoragePlatformFacadeRegistry facadeRegistry;

	@Mock
	private StoragePlatformFacade storagePlatformFacade;

	@Mock
	private FileStorageProvider minioProvider;

	private FileRecordServiceImpl fileRecordService;

	@BeforeEach
	void setUp() throws Exception {
		fileRecordService = spy(new FileRecordServiceImpl(facadeRegistry));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(fileRecordService, fileRecordMapper);
	}

	@Test
	@DisplayName("batchDownload：为空参数时抛出 PARAM_REQUIRED")
	void batchDownloadThrowsWhenIdsEmpty() {
		// 验证批量下载参数为空时按参数校验规则返回异常。
		List<Long> emptyIds = List.of();
		assertThatThrownBy(() -> fileRecordService.batchDownload(emptyIds, null))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);
		verify(fileRecordMapper, never()).selectIsDeletedByIds(anyList(), any(), any(), anyBoolean(), any());
	}

	@Test
	@DisplayName("batchDownload：管理端批量下载输出 ZIP 并处理同名文件")
	void batchDownloadBuildsZipForAdminAndResolvesDuplicateNames() throws IOException {
		// 验证管理端批量下载会输出 ZIP，并对同名文件自动重命名。
		FileRecordEntity first = buildEntity(6001L);
		first.setOriginalName("report.txt");
		first.setObjectKey("reports/6001.txt");
		FileRecordEntity second = buildEntity(6002L);
		second.setOriginalName("report.txt");
		second.setObjectKey("reports/6002.txt");

		when(fileRecordMapper.selectIsDeletedByIds(List.of(6001L, 6002L), null, null, false, null))
			.thenReturn(List.of(first, second));
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);
		when(minioProvider.download("public", "reports/6001.txt"))
			.thenReturn(new ByteArrayInputStream("content-1".getBytes()));
		when(minioProvider.download("public", "reports/6002.txt"))
			.thenReturn(new ByteArrayInputStream("content-2".getBytes()));

		MockHttpServletResponse response = writeBatchDownloadToResponse(List.of(6001L, 6002L), null);

		assertThat(response.getContentType()).isEqualTo("application/zip");
		assertThat(response.getHeader("Content-Disposition")).contains("attachment");

		Map<String, String> zipEntries = readZipEntries(response.getContentAsByteArray());
		assertThat(zipEntries).containsEntry("report.txt", "content-1").containsEntry("report(1).txt", "content-2");
		verify(fileRecordMapper).selectIsDeletedByIds(List.of(6001L, 6002L), null, null, false, null);
	}

	@Test
	@DisplayName("batchDownload：个人端下载透传 ownerUserId 到查询")
	void batchDownloadPassesOwnerUserIdToMapper() throws IOException {
		// 验证个人端批量下载通过 SQL 归属过滤限制可见范围。
		FileRecordEntity entity = buildEntity(7001L);
		when(fileRecordMapper.selectIsDeletedByIds(List.of(7001L), 9009L, null, false, null))
			.thenReturn(List.of(entity));
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);
		when(minioProvider.download("public", "a.png")).thenReturn(new ByteArrayInputStream("x".getBytes()));

		writeBatchDownloadToResponse(List.of(7001L), 9009L);

		verify(fileRecordMapper).selectIsDeletedByIds(List.of(7001L), 9009L, null, false, null);
	}

	@Test
	@DisplayName("deleteByIds：ids 为空时抛出 PARAM_REQUIRED")
	void deleteByIdsThrowsWhenIdsEmpty() {
		// 验证批量删除参数为空时按参数校验规则返回异常。
		List<Long> emptyIds = List.of();
		ThrowingCallable executable = () -> fileRecordService.deleteByIds(emptyIds, null,
				FileDeleteSource.ADMIN_ACTION.getCode());
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);
		verify(fileRecordMapper, never()).selectIsDeletedByIds(anyList(), any(), any(), anyBoolean(), any());
	}

	@Test
	@DisplayName("deleteByIds：未命中记录时抛出 FILE_RECORD_NOT_FOUND")
	void deleteByIdsThrowsWhenNoRecordsMatched() {
		// 验证不存在的 ID 会按记录不存在处理。
		when(fileRecordMapper.selectIsDeletedByIds(List.of(4101L), null, null, false, null)).thenReturn(List.of());

		List<Long> ids = List.of(4101L);
		ThrowingCallable executable = () -> fileRecordService.deleteByIds(ids, null,
				FileDeleteSource.ADMIN_ACTION.getCode());
		assertThatThrownBy(executable).isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);

		verify(fileRecordMapper, never()).deletePhysicallyByIds(anyList(), anyBoolean());
		verify(fileRecordService, never()).updateBatchById(anyList());
	}

	@Test
	@DisplayName("deleteByIds：部分 ID 未命中时抛出 FILE_RECORD_NOT_FOUND")
	void deleteByIdsThrowsWhenPartialRecordsMatched() {
		// 验证批量删除要求全部 ID 在当前可见范围内命中。
		FileRecordEntity entity = buildEntity(5001L);
		entity.setIsPrivate(true);
		when(fileRecordMapper.selectIsDeletedByIds(List.of(5001L, 5002L), 9001L, null, false, null))
			.thenReturn(List.of(entity));

		List<Long> ids = List.of(5001L, 5002L);
		ThrowingCallable executable = () -> fileRecordService.deleteByIds(ids, 9001L,
				FileDeleteSource.USER_SELF.getCode());
		assertThatThrownBy(executable).isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);

		verify(fileRecordService, never()).updateBatchById(anyList());
	}

	@Test
	@DisplayName("deleteByIds：个人删除透传 ownerUserId 并处理全部命中记录")
	void deleteByIdsUsesOwnerScopeForPersonalDelete() {
		// 验证个人删除会把 ownerUserId 透传给查询，并在全部命中时执行逻辑删除。
		FileRecordEntity entity = buildEntity(5001L);
		entity.setIsPrivate(true);
		when(fileRecordMapper.selectIsDeletedByIds(List.of(5001L), 9001L, null, false, null))
			.thenReturn(List.of(entity));
		when(fileRecordMapper.softDeleteByIds(List.of(5001L), FileDeleteSource.USER_SELF.getCode(), 9001L))
			.thenReturn(1);

		try (MockedStatic<SecurityUserUtils> securityUser = mockStatic(SecurityUserUtils.class)) {
			securityUser.when(SecurityUserUtils::getUserId).thenReturn(9001L);
			fileRecordService.deleteByIds(List.of(5001L), 9001L, FileDeleteSource.USER_SELF.getCode());
		}

		verify(fileRecordMapper).selectIsDeletedByIds(List.of(5001L), 9001L, null, false, null);
		verify(fileRecordMapper).softDeleteByIds(List.of(5001L), FileDeleteSource.USER_SELF.getCode(), 9001L);
		verify(fileRecordService, never()).updateBatchById(anyList());
		verify(fileRecordMapper, never()).deletePhysicallyByIds(anyList(), anyBoolean());
	}

	@Test
	@DisplayName("deleteByIds：管理端删除私有文件逻辑删除并写入 ADMIN_ACTION")
	void deleteByIdsMarksPrivateFilesDeletedWithAdminActionSource() {
		// 验证管理端删除私有文件时走自定义 softDelete，并标记 ADMIN_ACTION。
		FileRecordEntity entity = buildEntity(5101L);
		entity.setIsPrivate(true);
		when(fileRecordMapper.selectIsDeletedByIds(List.of(5101L), null, null, false, null))
			.thenReturn(List.of(entity));
		when(fileRecordMapper.softDeleteByIds(List.of(5101L), FileDeleteSource.ADMIN_ACTION.getCode(), 7002L))
			.thenReturn(1);

		try (MockedStatic<SecurityUserUtils> securityUser = mockStatic(SecurityUserUtils.class)) {
			securityUser.when(SecurityUserUtils::getUserId).thenReturn(7002L);
			fileRecordService.deleteByIds(List.of(5101L), null, FileDeleteSource.ADMIN_ACTION.getCode());
		}

		verify(fileRecordMapper).softDeleteByIds(List.of(5101L), FileDeleteSource.ADMIN_ACTION.getCode(), 7002L);
		verify(fileRecordService, never()).updateBatchById(anyList());
		verify(fileRecordMapper, never()).deletePhysicallyByIds(anyList(), anyBoolean());
	}

	@Test
	@DisplayName("deleteByIds：公开文件物理删除存储对象与数据库记录")
	void deleteByIdsPhysicallyDeletesPublicFiles() {
		// 验证公开文件删除会直接清理存储对象并物理删除数据库行。
		FileRecordEntity publicFile = buildEntity(5201L);
		publicFile.setIsPrivate(false);
		when(fileRecordMapper.selectIsDeletedByIds(List.of(5201L), null, null, false, null))
			.thenReturn(List.of(publicFile));
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);

		fileRecordService.deleteByIds(List.of(5201L), null, FileDeleteSource.ADMIN_ACTION.getCode());

		verify(minioProvider).delete("public", "a.png");
		verify(fileRecordMapper).deletePhysicallyByIds(List.of(5201L), false);
		verify(fileRecordMapper, never()).softDeleteByIds(anyList(), anyString(), any());
		verify(fileRecordService, never()).updateBatchById(anyList());
	}

	@Test
	@DisplayName("deleteByIds：isPrivate=null 时跳过（库表 NOT NULL，正常链路不应出现）")
	void deleteByIdsSkipsWhenIsPrivateNull() {
		// 验证 is_private 异常为空时不进入任一分支。
		FileRecordEntity entity = buildEntity(5202L);
		entity.setIsPrivate(null);
		when(fileRecordMapper.selectIsDeletedByIds(List.of(5202L), null, null, false, null))
			.thenReturn(List.of(entity));

		fileRecordService.deleteByIds(List.of(5202L), null, FileDeleteSource.ADMIN_ACTION.getCode());

		verify(fileRecordMapper, never()).softDeleteByIds(anyList(), anyString(), any());
		verify(fileRecordService, never()).updateBatchById(anyList());
		verify(fileRecordMapper, never()).deletePhysicallyByIds(anyList(), anyBoolean());
		verify(facadeRegistry, never()).resolve(any());
	}

	@Test
	@DisplayName("deleteByIds：混合公开与私有文件分别走物理删除与逻辑删除")
	void deleteByIdsSplitsPublicAndPrivateDeletionPaths() {
		// 验证同一批次内公开与私有文件会分别走物理删除与逻辑删除。
		FileRecordEntity publicFile = buildEntity(5301L);
		publicFile.setIsPrivate(false);
		FileRecordEntity privateFile = buildEntity(5302L);
		privateFile.setIsPrivate(true);
		when(fileRecordMapper.selectIsDeletedByIds(Arrays.asList(5301L, 5302L), null, null, false, null))
			.thenReturn(List.of(publicFile, privateFile));
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);
		when(fileRecordMapper.softDeleteByIds(List.of(5302L), FileDeleteSource.ADMIN_ACTION.getCode(), 9001L))
			.thenReturn(1);

		try (MockedStatic<SecurityUserUtils> securityUser = mockStatic(SecurityUserUtils.class)) {
			securityUser.when(SecurityUserUtils::getUserId).thenReturn(9001L);
			fileRecordService.deleteByIds(Arrays.asList(5301L, null, 5302L), null,
					FileDeleteSource.ADMIN_ACTION.getCode());
		}

		verify(fileRecordMapper).softDeleteByIds(List.of(5302L), FileDeleteSource.ADMIN_ACTION.getCode(), 9001L);
		verify(fileRecordService, never()).updateBatchById(anyList());
		verify(minioProvider).delete("public", "a.png");
		verify(fileRecordMapper).deletePhysicallyByIds(List.of(5301L), false);
	}

	@Test
	@DisplayName("deleteByIds：未知 deleteSource 时抛出 PARAM_REQUIRED")
	void deleteByIdsThrowsWhenDeleteSourceUnknown() {
		// 验证删除来源必须在枚举注册范围内。
		FileRecordEntity entity = buildEntity(5401L);
		entity.setIsPrivate(true);
		when(fileRecordMapper.selectIsDeletedByIds(List.of(5401L), null, null, false, null))
			.thenReturn(List.of(entity));

		ThrowingCallable executable = () -> fileRecordService.deleteByIds(List.of(5401L), null, "UNKNOWN_SOURCE");
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);

		verify(fileRecordMapper, never()).softDeleteByIds(anyList(), anyString(), any());
		verify(fileRecordService, never()).updateBatchById(anyList());
		verify(fileRecordMapper, never()).deletePhysicallyByIds(anyList(), anyBoolean());
	}

	@Test
	@DisplayName("updatePrivacyByIds：公开文件切换为私有时迁移对象并更新记录")
	void updatePrivacyByIdsMovesPublicToPrivate() {
		// 验证 public → private 会切换 objectKey 前缀、调用 move 并批量更新记录。
		FileRecordEntity entity = buildEntity(8001L);
		entity.setIsPrivate(false);
		entity.setObjectKey("public/avatar/20260702/a.png");
		when(fileRecordMapper.selectIsDeletedByIds(List.of(8001L), null, null, false, null))
			.thenReturn(List.of(entity));
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);
		when(minioProvider.resolvePublicUrl("public", "private/avatar/20260702/a.png"))
			.thenReturn("https://cdn.example.com/private/avatar/20260702/a.png");
		doReturn(true).when(fileRecordService).updateBatchById(anyList());

		fileRecordService.updatePrivacyByIds(List.of(8001L), true, null);

		verify(minioProvider).move("public", "public/avatar/20260702/a.png", "private/avatar/20260702/a.png");
		verify(fileRecordService).updateBatchById(argThat(entities -> {
			if (entities.size() != 1) {
				return false;
			}
			FileRecordEntity first = entities.iterator().next();
			return Boolean.TRUE.equals(first.getIsPrivate())
					&& "private/avatar/20260702/a.png".equals(first.getObjectKey());
		}));
	}

	@Test
	@DisplayName("updatePrivacyByIds：私有文件切换为公开时迁移对象并更新记录")
	void updatePrivacyByIdsMovesPrivateToPublic() {
		// 验证 private → public 会切换 objectKey 前缀、调用 move 并批量更新记录。
		FileRecordEntity entity = buildEntity(8002L);
		entity.setIsPrivate(true);
		entity.setObjectKey("private/attachment/20260702/a.txt");
		when(fileRecordMapper.selectIsDeletedByIds(List.of(8002L), null, null, false, null))
			.thenReturn(List.of(entity));
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);
		when(minioProvider.resolvePublicUrl("public", "public/attachment/20260702/a.txt"))
			.thenReturn("https://cdn.example.com/public/attachment/20260702/a.txt");
		doReturn(true).when(fileRecordService).updateBatchById(anyList());

		fileRecordService.updatePrivacyByIds(List.of(8002L), false, null);

		verify(minioProvider).move("public", "private/attachment/20260702/a.txt", "public/attachment/20260702/a.txt");
		verify(fileRecordService).updateBatchById(argThat(entities -> {
			if (entities.size() != 1) {
				return false;
			}
			FileRecordEntity first = entities.iterator().next();
			return Boolean.FALSE.equals(first.getIsPrivate())
					&& "public/attachment/20260702/a.txt".equals(first.getObjectKey());
		}));
	}

	@Test
	@DisplayName("updatePrivacyByIds：目标隐私与当前一致时跳过迁移与更新")
	void updatePrivacyByIdsSkipsWhenPrivacyUnchanged() {
		// 验证已是目标隐私状态时幂等跳过，不调用存储迁移也不写库。
		FileRecordEntity entity = buildEntity(8003L);
		entity.setIsPrivate(true);
		when(fileRecordMapper.selectIsDeletedByIds(List.of(8003L), null, null, false, null))
			.thenReturn(List.of(entity));

		fileRecordService.updatePrivacyByIds(List.of(8003L), true, null);

		verify(facadeRegistry, never()).resolve(any());
		verify(fileRecordService, never()).updateBatchById(anyList());
	}

	@Test
	@DisplayName("updatePrivacyByIds：历史无前缀私有文件切换为公开时迁移对象并更新记录")
	void updatePrivacyByIdsMovesLegacyPrivateKeyToPublic() {
		// 验证历史无前缀 objectKey 在 private → public 时会补 public 前缀并迁移存储。
		FileRecordEntity entity = buildEntity(8006L);
		entity.setIsPrivate(true);
		entity.setObjectKey("avatar/20260704/d58e5f1377e1454180f80a96c90bd7ec.png");
		when(fileRecordMapper.selectIsDeletedByIds(List.of(8006L), null, null, false, null))
			.thenReturn(List.of(entity));
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);
		when(minioProvider.resolvePublicUrl("public", "public/avatar/20260704/d58e5f1377e1454180f80a96c90bd7ec.png"))
			.thenReturn(
					"http://192.168.3.4:9000/auth-files-dev/public/avatar/20260704/d58e5f1377e1454180f80a96c90bd7ec.png");
		doReturn(true).when(fileRecordService).updateBatchById(anyList());

		fileRecordService.updatePrivacyByIds(List.of(8006L), false, null);

		verify(minioProvider).move("public", "avatar/20260704/d58e5f1377e1454180f80a96c90bd7ec.png",
				"public/avatar/20260704/d58e5f1377e1454180f80a96c90bd7ec.png");
		verify(fileRecordService).updateBatchById(argThat(entities -> {
			if (entities.size() != 1) {
				return false;
			}
			FileRecordEntity first = entities.iterator().next();
			return Boolean.FALSE.equals(first.getIsPrivate())
					&& "public/avatar/20260704/d58e5f1377e1454180f80a96c90bd7ec.png".equals(first.getObjectKey());
		}));
	}

	@Test
	@DisplayName("updatePrivacyByIds：已逻辑删除文件不可切换")
	void updatePrivacyByIdsThrowsWhenFileDeleted() {
		// 验证已逻辑删除文件在存在性校验阶段被拒绝。
		when(fileRecordMapper.selectIsDeletedByIds(List.of(8004L), null, null, false, null)).thenReturn(List.of());

		List<Long> ids = List.of(8004L);
		assertThatThrownBy(() -> fileRecordService.updatePrivacyByIds(ids, true, null))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		verify(facadeRegistry, never()).resolve(any());
	}

	@Test
	@DisplayName("updatePrivacyByIds：归一化 null 与重复 ID")
	void updatePrivacyByIdsNormalizesIdsBeforeUpdate() {
		// 验证 null 与重复 ID 会在 Service 内归一化后再查询。
		FileRecordEntity entity = buildEntity(8005L);
		entity.setIsPrivate(false);
		entity.setObjectKey("public/demo.png");
		when(fileRecordMapper.selectIsDeletedByIds(List.of(8005L), null, null, false, null))
			.thenReturn(List.of(entity));
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);
		when(minioProvider.resolvePublicUrl(anyString(), anyString())).thenReturn("https://cdn.example.com/demo.png");
		doReturn(true).when(fileRecordService).updateBatchById(anyList());

		fileRecordService.updatePrivacyByIds(Arrays.asList(8005L, null, 8005L), true, null);

		verify(fileRecordMapper).selectIsDeletedByIds(List.of(8005L), null, null, false, null);
	}

	private MockHttpServletResponse writeBatchDownloadToResponse(List<Long> ids, Long ownerUserId) throws IOException {
		StreamingResponseBody body = fileRecordService.batchDownload(ids, ownerUserId);
		ResponseEntity<StreamingResponseBody> downloadResponse = FileDelivery.deliver(body,
				FileDownloadNames.batchZip("file-records"), FileDelivery.APPLICATION_ZIP);
		MockHttpServletResponse response = new MockHttpServletResponse();
		HttpHeaders headers = downloadResponse.getHeaders();
		if (headers.getContentType() != null) {
			response.setContentType(headers.getContentType().toString());
		}
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, headers.getFirst(HttpHeaders.CONTENT_DISPOSITION));
		Assertions.assertNotNull(downloadResponse.getBody());
		downloadResponse.getBody().writeTo(response.getOutputStream());
		return response;
	}

	private Map<String, String> readZipEntries(byte[] content) throws IOException {
		Map<String, String> entries = new LinkedHashMap<>();
		try (InputStream inputStream = new ByteArrayInputStream(content);
				ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
			ZipEntry entry;
			while ((entry = zipInputStream.getNextEntry()) != null) {
				entries.put(entry.getName(), new String(zipInputStream.readAllBytes(), StandardCharsets.UTF_8));
				zipInputStream.closeEntry();
			}
		}
		return entries;
	}

	private FileRecordEntity buildEntity(Long id) {
		FileRecordEntity entity = new FileRecordEntity();
		entity.setId(id);
		entity.setStoragePlatform(StoragePlatformEnum.MINIO);
		entity.setContentType("image/png");
		entity.setUrl("https://cdn.example.com/a.png");
		entity.setOriginalName("a.png");
		entity.setBucket("public");
		entity.setObjectKey("a.png");
		entity.setCreatedBy(1L);
		return entity;
	}

}
