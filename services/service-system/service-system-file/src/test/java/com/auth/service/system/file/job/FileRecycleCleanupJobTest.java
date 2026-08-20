package com.auth.service.system.file.job;

import com.auth.common.core.constants.BatchSizes;
import com.auth.module.file.api.model.enums.FileDeleteSource;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.FileRecycleCleanupProperties;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.storage.core.StoragePlatformFacade;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link FileRecycleCleanupJob} 单元测试
 *
 * @author Bunny
 */
@DisplayName("FileRecycleCleanupJob 回收站过期清理")
@ExtendWith(MockitoExtension.class)
class FileRecycleCleanupJobTest {

	private static final int BATCH_SIZE = BatchSizes.SIZE_500;

	@Mock
	private FileRecycleCleanupProperties properties;

	@Mock
	private FileRecordMapper fileRecordMapper;

	@Mock
	private StoragePlatformFacadeRegistry facadeRegistry;

	@Mock
	private StoragePlatformFacade storagePlatformFacade;

	@Mock
	private FileStorageProvider minioProvider;

	@InjectMocks
	private FileRecycleCleanupJob cleanupJob;

	@Test
	@DisplayName("未启用时不执行清理")
	void purgeExpiredShouldSkipWhenDisabled() {
		// 验证开关关闭时不查询过期记录。
		when(properties.getEnabled()).thenReturn(false);

		cleanupJob.purgeExpired();

		verify(fileRecordMapper, never()).selectExpiredDeleted(any(), anyInt());
		verify(facadeRegistry, never()).resolve(any());
	}

	@Test
	@DisplayName("启用时按批次循环清理直到不足一批")
	void purgeExpiredShouldPurgeInBatchesWhenEnabled() {
		// 验证启用后按 maxRounds 与 batchSize 分批清理，满批继续、不足一批结束。
		when(properties.getEnabled()).thenReturn(true);
		when(properties.getRetentionDays()).thenReturn(90);
		when(properties.getMaxRounds()).thenReturn(20);

		List<FileRecordEntity> fullBatch = buildDeletedEntities(8501L);
		FileRecordEntity remainder = buildDeletedEntity(8501L + BATCH_SIZE);
		when(fileRecordMapper.selectExpiredDeleted(any(Instant.class), eq(BATCH_SIZE))).thenReturn(fullBatch,
				List.of(remainder));
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);

		cleanupJob.purgeExpired();

		verify(fileRecordMapper, times(2)).selectExpiredDeleted(any(Instant.class), eq(BATCH_SIZE));
		verify(minioProvider, times(BATCH_SIZE + 1)).delete("public", "a.png");
		verify(fileRecordMapper).deletePhysicallyByIds(fullBatch.stream().map(FileRecordEntity::getId).toList(), true);
		verify(fileRecordMapper).deletePhysicallyByIds(List.of(remainder.getId()), true);
	}

	@Test
	@DisplayName("启用时达到 maxRounds 后停止")
	void purgeExpiredShouldStopAtMaxRounds() {
		// 验证积压场景下不会超过配置的最大批次数。
		when(properties.getEnabled()).thenReturn(true);
		when(properties.getRetentionDays()).thenReturn(90);
		when(properties.getMaxRounds()).thenReturn(2);

		List<FileRecordEntity> fullBatch = buildDeletedEntities(8601L);
		when(fileRecordMapper.selectExpiredDeleted(any(Instant.class), eq(BATCH_SIZE))).thenReturn(fullBatch);
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);

		cleanupJob.purgeExpired();

		verify(fileRecordMapper, times(2)).selectExpiredDeleted(any(Instant.class), eq(BATCH_SIZE));
		verify(fileRecordMapper, times(2))
			.deletePhysicallyByIds(fullBatch.stream().map(FileRecordEntity::getId).toList(), true);
	}

	@Test
	@DisplayName("无过期记录时不访问存储")
	void purgeExpiredShouldSkipStorageWhenEmpty() {
		// 验证没有可清理记录时不访问对象存储。
		when(properties.getEnabled()).thenReturn(true);
		when(properties.getRetentionDays()).thenReturn(90);
		when(properties.getMaxRounds()).thenReturn(20);
		when(fileRecordMapper.selectExpiredDeleted(any(Instant.class), eq(BATCH_SIZE))).thenReturn(List.of());

		cleanupJob.purgeExpired();

		verify(facadeRegistry, never()).resolve(any());
		verify(fileRecordMapper, never()).deletePhysicallyByIds(anyList(), anyBoolean());
	}

	private List<FileRecordEntity> buildDeletedEntities(long startId) {
		return IntStream.range(0, BATCH_SIZE).mapToObj(i -> buildDeletedEntity(startId + i)).toList();
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
