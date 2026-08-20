package com.auth.service.system.file.service.impl;

import com.auth.module.file.api.model.enums.FileDeleteSource;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link FileRecycleServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileRecycleServiceImpl 回收站")
@ExtendWith(MockitoExtension.class)
class FileRecycleServiceImplTest {

	@Mock
	private FileRecordMapper fileRecordMapper;

	@Mock
	private StoragePlatformFacadeRegistry facadeRegistry;

	@Mock
	private StoragePlatformFacade storagePlatformFacade;

	@Mock
	private FileStorageProvider minioProvider;

	private FileRecycleServiceImpl fileRecycleService;

	@BeforeEach
	void setUp() throws Exception {
		fileRecycleService = spy(new FileRecycleServiceImpl(facadeRegistry));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(fileRecycleService, fileRecordMapper);
	}

	@Test
	@DisplayName("restoreByIds：批量恢复已删除私有记录")
	void restoreByIdsRestoresDeletedRecords() {
		// 验证恢复流程会校验已删除私有记录并调用 Mapper 恢复。
		FileRecordEntity deleted = buildDeletedEntity(8001L);
		when(fileRecordMapper.selectIsDeletedByIds(List.of(8001L), null, true, true, null))
			.thenReturn(List.of(deleted));

		try (MockedStatic<SecurityUserUtils> securityUser = mockStatic(SecurityUserUtils.class)) {
			securityUser.when(SecurityUserUtils::getUserId).thenReturn(7001L);
			fileRecycleService.restoreByIds(List.of(8001L), null, null);
		}

		verify(fileRecordMapper).selectIsDeletedByIds(List.of(8001L), null, true, true, null);
		verify(fileRecordMapper).restoreByIds(List.of(8001L), 7001L);
	}

	@Test
	@DisplayName("restoreByIds：个人恢复透传归属与用户可见删除来源")
	void restoreByIdsPassesPersonalScopeToMapper() {
		// 验证个人恢复会把 ownerUserId 与 deleteSources 透传给查询。
		FileRecordEntity deleted = buildDeletedEntity(8002L);
		when(fileRecordMapper.selectIsDeletedByIds(List.of(8002L), 9001L, true, true,
				FileDeleteSource.userRecycleSourceCodes()))
			.thenReturn(List.of(deleted));

		try (MockedStatic<SecurityUserUtils> securityUser = mockStatic(SecurityUserUtils.class)) {
			securityUser.when(SecurityUserUtils::getUserId).thenReturn(9001L);
			fileRecycleService.restoreByIds(List.of(8002L), 9001L, FileDeleteSource.userRecycleSourceCodes());
		}

		verify(fileRecordMapper).selectIsDeletedByIds(List.of(8002L), 9001L, true, true,
				FileDeleteSource.userRecycleSourceCodes());
		verify(fileRecordMapper).restoreByIds(List.of(8002L), 9001L);
	}

	@Test
	@DisplayName("restoreByIds：ids 为空时抛出 PARAM_REQUIRED")
	void restoreByIdsThrowsWhenIdsEmpty() {
		// 验证恢复参数为空时按参数校验规则返回异常。
		List<Long> emptyIds = List.of();
		assertThatThrownBy(() -> fileRecycleService.restoreByIds(emptyIds, null, null))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);
		verify(fileRecordMapper, never()).selectIsDeletedByIds(anyList(), any(), any(), anyBoolean(), any());
		verify(fileRecordMapper, never()).restoreByIds(anyList(), any());
	}

	@Test
	@DisplayName("restoreByIds：存在缺失记录时抛出 FILE_RECORD_NOT_FOUND")
	void restoreByIdsThrowsWhenRecordsMissing() {
		// 验证恢复遇到缺失记录会直接中断。
		when(fileRecordMapper.selectIsDeletedByIds(List.of(8101L), null, true, true, null)).thenReturn(List.of());

		List<Long> ids = List.of(8101L);
		assertThatThrownBy(() -> fileRecycleService.restoreByIds(ids, null, null))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		verify(fileRecordMapper, never()).restoreByIds(anyList(), any());
	}

	@Test
	@DisplayName("restoreByIds：部分 ID 未命中时抛出 FILE_RECORD_NOT_FOUND")
	void restoreByIdsThrowsWhenPartialRecordsMatched() {
		// 验证批量恢复要求全部 ID 在当前可见范围内命中。
		FileRecordEntity deleted = buildDeletedEntity(8102L);
		when(fileRecordMapper.selectIsDeletedByIds(List.of(8102L, 8103L), null, true, true, null))
			.thenReturn(List.of(deleted));

		List<Long> ids = List.of(8102L, 8103L);
		assertThatThrownBy(() -> fileRecycleService.restoreByIds(ids, null, null))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		verify(fileRecordMapper, never()).restoreByIds(anyList(), any());
	}

	@Test
	@DisplayName("restoreByIds：归一化 null 与重复 ID")
	void restoreByIdsNormalizesIdsBeforeRestore() {
		// 验证 null 与重复 ID 会在 Service 内归一化后再查询与恢复。
		FileRecordEntity deleted = buildDeletedEntity(8201L);
		when(fileRecordMapper.selectIsDeletedByIds(List.of(8201L), null, true, true, null))
			.thenReturn(List.of(deleted));

		try (MockedStatic<SecurityUserUtils> securityUser = mockStatic(SecurityUserUtils.class)) {
			securityUser.when(SecurityUserUtils::getUserId).thenReturn(7001L);
			fileRecycleService.restoreByIds(Arrays.asList(8201L, null, 8201L), null, null);
		}

		verify(fileRecordMapper).restoreByIds(List.of(8201L), 7001L);
	}

	@Test
	@DisplayName("purgeByIds：彻底删除存储对象与数据库记录")
	void purgeByIdsDeletesStorageAndDatabaseRows() {
		// 验证彻底删除会先清理存储对象再物理删除数据库行。
		FileRecordEntity deleted = buildDeletedEntity(8301L);
		when(fileRecordMapper.selectIsDeletedByIds(List.of(8301L), null, true, true, null))
			.thenReturn(List.of(deleted));
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);

		fileRecycleService.purgeByIds(Arrays.asList(8301L, null, 8301L), null, null);

		verify(minioProvider).delete("public", "a.png");
		verify(fileRecordMapper).deletePhysicallyByIds(List.of(8301L), true);
	}

	@Test
	@DisplayName("purgeByIds：未命中记录时抛出 FILE_RECORD_NOT_FOUND")
	void purgeByIdsThrowsWhenNoRecordsMatched() {
		// 验证不存在的 ID 会按记录不存在处理。
		when(fileRecordMapper.selectIsDeletedByIds(List.of(6101L), 9001L, true, true,
				FileDeleteSource.userRecycleSourceCodes()))
			.thenReturn(List.of());

		List<Long> ids = List.of(6101L);
		ThrowingCallable executable = () -> fileRecycleService.purgeByIds(ids, 9001L,
				FileDeleteSource.userRecycleSourceCodes());
		assertThatThrownBy(executable).isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);

		verify(fileRecordMapper, never()).deletePhysicallyByIds(anyList(), anyBoolean());
		verify(facadeRegistry, never()).resolve(any());
	}

	@Test
	@DisplayName("purgeByIds：部分 ID 未命中时抛出 FILE_RECORD_NOT_FOUND")
	void purgeByIdsThrowsWhenPartialRecordsMatched() {
		// 验证批量彻底删除要求全部 ID 在当前可见范围内命中。
		FileRecordEntity deleted = buildDeletedEntity(6102L);
		when(fileRecordMapper.selectIsDeletedByIds(List.of(6102L, 6103L), null, true, true, null))
			.thenReturn(List.of(deleted));

		List<Long> ids = List.of(6102L, 6103L);
		assertThatThrownBy(() -> fileRecycleService.purgeByIds(ids, null, null))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);

		verify(facadeRegistry, never()).resolve(any());
		verify(fileRecordMapper, never()).deletePhysicallyByIds(anyList(), anyBoolean());
	}

	@Test
	@DisplayName("purgeByIds：ids 为空时抛出 PARAM_REQUIRED")
	void purgeByIdsThrowsWhenIdsEmpty() {
		// 验证彻底删除参数为空时按参数校验规则返回异常。
		List<Long> emptyIds = List.of();
		assertThatThrownBy(() -> fileRecycleService.purgeByIds(emptyIds, null, null))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);
		verify(fileRecordMapper, never()).selectIsDeletedByIds(anyList(), any(), any(), anyBoolean(), any());
	}

	@Test
	@DisplayName("purgeByIds：个人彻底删除他人记录时拒绝")
	void purgeByIdsThrowsWhenRecordsMissing() {
		// 验证个人彻底删除未命中记录时按记录不存在处理。
		when(fileRecordMapper.selectIsDeletedByIds(List.of(8401L), 9002L, true, true,
				FileDeleteSource.userRecycleSourceCodes()))
			.thenReturn(List.of());

		List<Long> ids = List.of(8401L);
		ThrowingCallable executable = () -> fileRecycleService.purgeByIds(ids, 9002L,
				FileDeleteSource.userRecycleSourceCodes());
		assertThatThrownBy(executable).isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		verify(facadeRegistry, never()).resolve(any());
		verify(fileRecordMapper, never()).deletePhysicallyByIds(anyList(), anyBoolean());
	}

	private FileRecordEntity buildDeletedEntity(Long id) {
		FileRecordEntity entity = new FileRecordEntity();
		entity.setId(id);
		entity.setStoragePlatform(StoragePlatformEnum.MINIO);
		entity.setBucket("public");
		entity.setObjectKey("a.png");
		entity.setCreatedBy(1L);
		entity.setIsPrivate(true);
		entity.setDeleteSource(FileDeleteSource.USER_SELF.getCode());
		entity.setIsDeleted(true);
		return entity;
	}

}
