package com.auth.service.system.admin.convert.admin.log;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.entity.LogOperationEntity;
import com.auth.service.system.admin.model.po.logoperation.LogOperationPageRowPO;
import com.auth.service.system.admin.model.vo.logoperation.LogOperationDetailVO;
import com.auth.service.system.admin.model.vo.logoperation.LogOperationPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 操作日志 PO / VO 转换
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface LogOperationConverter {

	LogOperationConverter INSTANCE = Mappers.getMapper(LogOperationConverter.class);

	/**
	 * 分页行 PO → VO
	 * @param po 查询投影
	 * @return 分页 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	LogOperationPageVO toPageVO(LogOperationPageRowPO po);

	/**
	 * 实体 → 详情 VO
	 * @param entity 操作日志实体
	 * @return 详情 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	LogOperationDetailVO toDetailVo(LogOperationEntity entity);

}
