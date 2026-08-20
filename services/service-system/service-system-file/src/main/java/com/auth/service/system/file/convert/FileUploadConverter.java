package com.auth.service.system.file.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.module.file.api.model.dto.FileUploadResultDTO;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 文件上传映射转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface FileUploadConverter {

	FileUploadConverter INSTANCE = Mappers.getMapper(FileUploadConverter.class);

	/**
	 * 文件实体映射为上传结果 DTO
	 * @param entity 文件实体
	 * @return 上传结果 DTO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	FileUploadResultDTO toResultDTO(FileRecordEntity entity);

}
