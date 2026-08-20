package com.auth.service.system.admin.convert.admin.log;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.admin.model.po.logauthorizationaudit.LogAuthorizationAuditPageRowPO;
import com.auth.service.system.admin.model.vo.logauthorizationaudit.LogAuthorizationAuditDetailVO;
import com.auth.service.system.admin.model.vo.logauthorizationaudit.LogAuthorizationAuditPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 权限决策审计转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface LogAuthorizationAuditConverter {

	LogAuthorizationAuditConverter INSTANCE = Mappers.getMapper(LogAuthorizationAuditConverter.class);

	/**
	 * 分页行 PO → VO
	 * @param po 持久层投影
	 * @return 分页 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	LogAuthorizationAuditPageVO toPageVO(LogAuthorizationAuditPageRowPO po);

	/**
	 * 实体 → 详情 VO
	 * @param entity 权限决策审计实体
	 * @return 详情 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	LogAuthorizationAuditDetailVO toDetailVo(LogAuthorizationAuditEntity entity);

}
