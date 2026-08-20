package com.auth.service.system.file.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.file.convert.FileRecordQueryConverter;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.model.po.FileRecordPageRowPO;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordDetailVO;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.auth.service.system.file.service.FileRecordQueryService;
import com.auth.service.system.file.support.FileUrlSigner;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 文件记录查询服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class FileRecordQueryServiceImpl extends ServiceImpl<FileRecordMapper, FileRecordEntity>
		implements FileRecordQueryService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final FileUrlSigner fileUrlSigner;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<FileRecordPageVO> getPage(FileRecordPageQuery query, Boolean isDeleted) {
		Page<FileRecordEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());

		IPage<FileRecordPageRowPO> page = baseMapper.selectListByPage(pageParams, query, isDeleted);
		IPage<FileRecordPageVO> voPage = page.convert(FileRecordQueryConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public FileRecordDetailVO getDetail(Long id, Long ownerUserId, Boolean isPrivate, boolean expectedDeleted,
			List<String> deleteSources) {
		FileRecordEntity entity = baseMapper.selectDetailById(id, ownerUserId, isPrivate, expectedDeleted,
				deleteSources);
		if (entity == null) {
			log.warn("File record not found or unavailable: id={}, ownerUserId={}", id, ownerUserId);
			throw new FileStorageException(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		}

		FileRecordDetailVO detailVo = FileRecordQueryConverter.INSTANCE.toDetailVo(entity);
		String bucket = entity.getBucket();
		String objectKey = entity.getObjectKey();
		String contentType = entity.getContentType();
		String accessUrl = fileUrlSigner.isPreviewContentTypeAllowed(contentType)
				? fileUrlSigner.sign(bucket, objectKey, contentType, entity.getStoragePlatform())
				: fileUrlSigner.signDownload(bucket, objectKey, entity.getStoragePlatform());
		detailVo.setAccessUrl(accessUrl);

		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detailVo), null, null);

		return detailVo;
	}

}
