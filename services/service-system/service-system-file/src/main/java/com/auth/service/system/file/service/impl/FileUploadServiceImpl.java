package com.auth.service.system.file.service.impl;

import com.auth.module.file.api.model.dto.FileUploadMetadata;
import com.auth.module.file.api.model.dto.FileUploadResultDTO;
import com.auth.module.file.api.model.enums.FileUploadMode;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.module.file.api.policy.FileBizType;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.convert.FileUploadConverter;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.model.form.FileUploadForm;
import com.auth.service.system.file.model.form.MultipleFileUploadForm;
import com.auth.service.system.file.model.value.FileUploadCommand;
import com.auth.service.system.file.model.value.StoredFile;
import com.auth.service.system.file.service.FileUploadService;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import com.auth.service.system.file.support.FileUploadContentValidator;
import com.auth.service.system.file.utils.FileObjectKeyUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

/**
 * 文件上传服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class FileUploadServiceImpl extends ServiceImpl<FileRecordMapper, FileRecordEntity>
		implements FileUploadService {

	private final FileUploadProperties fileUploadProperties;

	private final StoragePlatformFacadeRegistry facadeRegistry;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public FileUploadResultDTO upload(FileUploadForm form) {
		FileRecordEntity entity = buildUploadedEntity(form.getFile(), form, FileUploadMode.SIMPLE);
		super.save(entity);

		return FileUploadConverter.INSTANCE.toResultDTO(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public List<FileUploadResultDTO> uploadMultiple(MultipleFileUploadForm form) {
		List<FileRecordEntity> entities = form.getFiles()
			.stream()
			.map(file -> buildUploadedEntity(file, form, FileUploadMode.SIMPLE))
			.toList();

		super.saveBatch(entities);
		return entities.stream().map(FileUploadConverter.INSTANCE::toResultDTO).toList();
	}

	/**
	 * 校验文件、解析平台并上传，组装待落库实体
	 * @param file 上传文件
	 * @param metadata 上传元数据
	 * @param uploadMode 上传模式
	 * @return 文件记录实体
	 */
	private FileRecordEntity buildUploadedEntity(MultipartFile file, FileUploadMetadata metadata,
			FileUploadMode uploadMode) {
		if (file == null || file.isEmpty()) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "file");
		}

		// 1. 解析存储平台
		String storagePlatform = metadata.getStoragePlatform();
		StoragePlatformEnum platform = StoragePlatformEnum.fromNullable(storagePlatform);
		platform = Objects.requireNonNullElse(platform, fileUploadProperties.getDefaultPlatform());

		// 2. 解析业务类型与可见性，并按策略校验内容
		FileBizType bizType = FileBizType.require(metadata.getBizType());
		FileUploadContentValidator.validate(file, bizType);
		String bizTypeCode = bizType.getCode();
		boolean isPrivate = bizType.getVisibilityRule().resolve(metadata.getIsPrivate());

		// 3. 上传文件
		FileStorageProvider provider = facadeRegistry.resolve(platform).provider();
		String objectKey = FileObjectKeyUtil.build(bizTypeCode, isPrivate, file.getOriginalFilename());
		FileUploadCommand command = FileUploadCommand.builder()
			.storagePlatform(platform)
			.file(file)
			.objectKey(objectKey)
			.build();
		StoredFile storedFile = provider.upload(command);

		// 4. 组装待落库实体
		FileRecordEntity entity = new FileRecordEntity();
		entity.setStoragePlatform(storedFile.getStoragePlatform());
		entity.setBucket(storedFile.getBucket());
		entity.setObjectKey(storedFile.getObjectKey());
		entity.setUrl(storedFile.getUrl());
		entity.setOriginalName(storedFile.getOriginalName());
		entity.setExtension(storedFile.getExtension());
		entity.setContentType(storedFile.getContentType());
		entity.setSize(storedFile.getSize());
		entity.setEtag(storedFile.getEtag());
		entity.setBizType(bizTypeCode);
		entity.setBizId(metadata.getBizId());
		entity.setRemark(metadata.getRemark());
		entity.setIsPrivate(isPrivate);
		entity.setUploadMode(uploadMode.getCode());
		return entity;
	}

}
