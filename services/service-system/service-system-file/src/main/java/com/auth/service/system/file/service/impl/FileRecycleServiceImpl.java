package com.auth.service.system.file.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.service.FileRecycleService;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 文件回收站服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class FileRecycleServiceImpl extends ServiceImpl<FileRecordMapper, FileRecordEntity>
		implements FileRecycleService {

	private final StoragePlatformFacadeRegistry facadeRegistry;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void restoreByIds(List<Long> ids, Long ownerUserId, List<String> deleteSources) {
		ids = CollUtil.emptyIfNull(ids).stream().filter(Objects::nonNull).distinct().toList();
		if (ids.isEmpty()) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "ids");
		}

		// 私有文件才可以被回收
		List<FileRecordEntity> entities = baseMapper.selectIsDeletedByIds(ids, ownerUserId, true, true, deleteSources);
		if (CollUtil.isEmpty(entities) || entities.size() != ids.size()) {
			log.warn("File record not found or unavailable: ids={}", ids);
			throw new FileStorageException(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		}

		Long userId = SecurityUserUtils.getUserId();
		baseMapper.restoreByIds(ids, userId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void purgeByIds(List<Long> ids, Long ownerUserId, List<String> deleteSources) {
		ids = CollUtil.emptyIfNull(ids).stream().filter(Objects::nonNull).distinct().toList();
		if (ids.isEmpty()) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "ids");
		}
		List<FileRecordEntity> entities = baseMapper.selectIsDeletedByIds(ids, ownerUserId, true, true, deleteSources);
		if (CollUtil.isEmpty(entities) || entities.size() != ids.size()) {
			log.warn("File record not found or unavailable(purgeByIds): ids={}", ids);
			throw new FileStorageException(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		}

		// 先删存储再删DB
		for (FileRecordEntity entity : entities) {
			FileStorageProvider provider = facadeRegistry.resolve(entity.getStoragePlatform()).provider();
			provider.delete(entity.getBucket(), entity.getObjectKey());
		}
		baseMapper.deletePhysicallyByIds(entities.stream().map(FileRecordEntity::getId).toList(), true);
	}

}
