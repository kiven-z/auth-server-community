package com.auth.service.system.file.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.file.api.model.enums.FileDeleteSource;
import com.auth.module.file.api.model.request.OwnedFileAssertByUrlRequest;
import com.auth.module.file.api.model.request.OwnedFileDeleteByUrlRequest;
import com.auth.module.file.zip.ZipStreamWriter;
import com.auth.module.file.zip.ZipStreamWriter.ZipEntryPayload;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.service.FileRecordService;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import com.auth.service.system.file.utils.FileObjectKeyUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文件记录查询与治理服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class FileRecordServiceImpl extends ServiceImpl<FileRecordMapper, FileRecordEntity>
		implements FileRecordService {

	private final StoragePlatformFacadeRegistry facadeRegistry;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteByIds(List<Long> ids, Long ownerUserId, String deleteSource) {
		List<Long> normalizedIds = CollUtil.emptyIfNull(ids).stream().filter(Objects::nonNull).distinct().toList();
		if (normalizedIds.isEmpty()) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "ids");
		}

		List<FileRecordEntity> entities = baseMapper.selectIsDeletedByIds(normalizedIds, ownerUserId, null, false,
				null);
		if (CollUtil.isEmpty(entities) || entities.size() != normalizedIds.size()) {
			log.warn("File record not found or unavailable(deleteByIds): ids={}", normalizedIds);
			throw new FileStorageException(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		}

		Long deletedBy = SecurityUserUtils.getUserId();
		applyDeletion(entities, deleteSource, deletedBy);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void tryDeleteOwnedByUrl(OwnedFileDeleteByUrlRequest request) {
		String url = request.getUrl();
		Long ownerUserId = request.getOwnerUserId();
		String bizType = request.getBizType();
		String deleteSource = request.getDeleteSource();

		Optional<String> objectKey = FileObjectKeyUtil.resolveObjectKeyFromUrl(url);
		if (objectKey.isEmpty()) {
			return;
		}

		FileRecordEntity entity = baseMapper.selectActiveByUrlAndOwner(null, objectKey.get(), ownerUserId, bizType);
		if (entity == null) {
			log.debug("Skip owned file delete by url: not found, url={}, ownerUserId={}", url, ownerUserId);
			return;
		}

		applyDeletion(List.of(entity), deleteSource, ownerUserId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public void assertOwnedFileUrl(OwnedFileAssertByUrlRequest request) {
		String url = request.getUrl();
		Long ownerUserId = request.getOwnerUserId();
		String bizType = request.getBizType();

		FileRecordEntity entity = baseMapper.selectActiveByUrlAndOwner(url, null, ownerUserId, bizType);
		if (entity == null) {
			log.warn("Owned file assert failed: url={}, ownerUserId={}, bizType={}", url, ownerUserId, bizType);
			throw new FileStorageException(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public StreamingResponseBody batchDownload(List<Long> ids, Long ownerUserId) {
		// 1. 为空返回
		ids = CollUtil.emptyIfNull(ids).stream().filter(Objects::nonNull).distinct().toList();
		if (ids.isEmpty()) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "ids");
		}

		// 2. 一次 SQL 完成存在性 + 归属 + 未删除校验（同 deleteByIds）
		List<FileRecordEntity> entities = baseMapper.selectIsDeletedByIds(ids, ownerUserId, null, false, null);
		if (CollUtil.isEmpty(entities) || entities.size() != ids.size()) {
			log.warn("File record not found or unavailable(batchDownload): ids={}", ids);
			throw new FileStorageException(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		}

		// 3. 按请求 ID 顺序排列（Mapper 不保证顺序）
		Map<Long, FileRecordEntity> entityById = entities.stream()
			.collect(Collectors.toMap(FileRecordEntity::getId, Function.identity()));
		List<FileRecordEntity> orderedEntities = ids.stream().map(entityById::get).toList();

		// 4. 构建 ZIP 条目（懒加载 InputStream，真正写流时才下载）
		List<ZipEntryPayload> payloads = orderedEntities.stream().map(entity -> {
			FileStorageProvider provider = facadeRegistry.resolve(entity.getStoragePlatform()).provider();
			String entryName = ZipStreamWriter.resolveEntryName(entity.getOriginalName(), entity.getExtension(),
					"file-" + entity.getId());
			return new ZipEntryPayload(entryName, () -> provider.download(entity.getBucket(), entity.getObjectKey()));
		}).toList();

		// 5. 返回 StreamingResponseBody（延迟写 ZIP）
		return outputStream -> {
			try {
				ZipStreamWriter.write(outputStream, payloads);
			}
			catch (IOException exception) {
				throw new SystemBusinessException(SystemCommonResultCode.OPERATION_FAILED, exception.getMessage());
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updatePrivacyByIds(List<Long> ids, boolean targetPrivate, Long ownerUserId) {
		ids = CollUtil.emptyIfNull(ids).stream().filter(Objects::nonNull).distinct().toList();
		if (ids.isEmpty()) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "ids");
		}

		// 与 deleteByIds / batchDownload 相同：活跃文件 + 可选归属过滤
		List<FileRecordEntity> entities = baseMapper.selectIsDeletedByIds(ids, ownerUserId, null, false, null);
		if (CollUtil.isEmpty(entities) || entities.size() != ids.size()) {
			log.warn("File record not found or unavailable(updatePrivacyByIds): ids={}", ids);
			throw new FileStorageException(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		}

		List<FileRecordEntity> changedEntities = new ArrayList<>();
		for (FileRecordEntity entity : entities) {
			if ((entity.getIsPrivate() != null && entity.getIsPrivate()) == targetPrivate) {
				continue;
			}

			String newObjectKey = FileObjectKeyUtil.switchVisibilityPrefix(entity.getObjectKey(), targetPrivate);
			FileStorageProvider provider = facadeRegistry.resolve(entity.getStoragePlatform()).provider();
			provider.move(entity.getBucket(), entity.getObjectKey(), newObjectKey);

			String newUrl = provider.resolvePublicUrl(entity.getBucket(), newObjectKey);
			entity.setUrl(newUrl);
			entity.setIsPrivate(targetPrivate);
			entity.setObjectKey(newObjectKey);
			changedEntities.add(entity);
		}

		if (CollUtil.isNotEmpty(changedEntities)) {
			super.updateBatchById(changedEntities);
		}
	}

	/**
	 * 对给定记录执行删除
	 * @param entities 待删除记录
	 * @param deleteSource 逻辑删除来源
	 * @param deletedBy 删除人
	 */
	private void applyDeletion(List<FileRecordEntity> entities, String deleteSource, Long deletedBy) {
		if (CollUtil.isEmpty(entities)) {
			return;
		}

		if (FileDeleteSource.parse(deleteSource).isEmpty()) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "deleteSource");
		}

		// @TableLogic 会拦截 updateById/updateBatchById 对 is_deleted 的写入，私有文件走自定义 SQL
		List<Long> privateIds = entities.stream()
			.filter(entity -> entity.getIsPrivate() != null && entity.getIsPrivate())
			.map(FileRecordEntity::getId)
			.toList();
		if (CollUtil.isNotEmpty(privateIds)) {
			baseMapper.softDeleteByIds(privateIds, deleteSource, deletedBy);
		}

		List<FileRecordEntity> publicEntities = entities.stream()
			.filter(entity -> entity.getIsPrivate() != null && !entity.getIsPrivate())
			.toList();
		if (publicEntities.isEmpty()) {
			return;
		}

		for (FileRecordEntity entity : publicEntities) {
			FileStorageProvider provider = facadeRegistry.resolve(entity.getStoragePlatform()).provider();
			provider.delete(entity.getBucket(), entity.getObjectKey());
		}
		baseMapper.deletePhysicallyByIds(publicEntities.stream().map(FileRecordEntity::getId).toList(), false);
	}

}
