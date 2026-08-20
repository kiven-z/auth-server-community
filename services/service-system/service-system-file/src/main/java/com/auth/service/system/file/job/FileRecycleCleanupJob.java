package com.auth.service.system.file.job;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.constants.BatchSizes;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.config.properties.FileRecycleCleanupProperties;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 文件回收站过期清理：物理删除超过保留期的逻辑删除记录及对应存储对象
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class FileRecycleCleanupJob {

	private final FileRecycleCleanupProperties properties;

	private final FileRecordMapper fileRecordMapper;

	private final StoragePlatformFacadeRegistry facadeRegistry;

	/**
	 * 按保留期分批清理过期回收站文件
	 */
	@Scheduled(cron = "${auth.file.recycle-cleanup.cron:0 0 3 * * ?}")
	public void purgeExpired() {
		Boolean enabled = properties.getEnabled();
		if (enabled == null || !enabled) {
			return;
		}

		Integer retentionDays = properties.getRetentionDays();
		Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
		int total = 0;
		for (int round = 0; round < properties.getMaxRounds(); round++) {
			int purged = purgeExpiredBefore(cutoff);
			total += purged;
			if (purged < BatchSizes.SIZE_500) {
				break;
			}
		}
		if (total > 0) {
			log.info("Purged expired recycle files: count={}, cutoff={}", total, cutoff);
		}
	}

	/**
	 * 清理过期回收站文件
	 * @param cutoff 截止时间
	 * @return 清理数量
	 */
	private int purgeExpiredBefore(Instant cutoff) {
		if (cutoff == null) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "cutoff");
		}
		List<FileRecordEntity> entities = fileRecordMapper.selectExpiredDeleted(cutoff, BatchSizes.SIZE_500);
		if (CollUtil.isEmpty(entities)) {
			return 0;
		}

		for (FileRecordEntity entity : entities) {
			StoragePlatformEnum platform = entity.getStoragePlatform();
			FileStorageProvider provider = facadeRegistry.resolve(platform).provider();
			provider.delete(entity.getBucket(), entity.getObjectKey());
		}

		List<Long> ids = entities.stream().map(FileRecordEntity::getId).toList();
		fileRecordMapper.deletePhysicallyByIds(ids, true);

		return entities.size();
	}

}
