package com.auth.service.system.file.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.model.po.FileRecordPageRowPO;
import com.auth.service.system.file.model.vo.FileRecordDetailVO;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 文件记录查询对象映射
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface FileRecordQueryConverter {

	FileRecordQueryConverter INSTANCE = Mappers.getMapper(FileRecordQueryConverter.class);

	/**
	 * 分页行 PO 映射为分页 VO
	 * @param po 持久层投影
	 * @return 分页行
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	FileRecordPageVO toPageVo(FileRecordPageRowPO po);

	/**
	 * 文件实体映射为详情
	 * @param entity 文件实体
	 * @return 详情
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	@Mapping(target = "accessUrl", ignore = true)
	FileRecordDetailVO toDetailVo(FileRecordEntity entity);

}
