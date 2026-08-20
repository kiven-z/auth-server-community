package com.auth.service.system.authorization.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.authorization.feign.dto.AuthorizationInvalidationEventDetailInnerDTO;
import com.auth.service.system.authorization.feign.dto.AuthorizationInvalidationEventInnerQuery;
import com.auth.service.system.authorization.feign.dto.AuthorizationInvalidationEventPageInnerDTO;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationEventOpsQuery;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationEventDetailVO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationEventPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 授权失效幂等事件运维转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface AuthorizationInvalidationEventOpsConverter {

	AuthorizationInvalidationEventOpsConverter INSTANCE = Mappers
		.getMapper(AuthorizationInvalidationEventOpsConverter.class);

	/**
	 * API 查询条件 → 运维查询
	 * @param query API 查询
	 * @return 运维查询
	 */
	AuthorizationInvalidationEventOpsQuery toOpsQuery(AuthorizationInvalidationEventQuery query);

	/**
	 * 运维查询 → Feign 分页查询
	 * @param query 运维查询
	 * @return Feign 查询
	 */
	AuthorizationInvalidationEventInnerQuery toInnerQuery(AuthorizationInvalidationEventOpsQuery query);

	/**
	 * Feign 分页行 → 分页 VO
	 * @param dto Feign 分页行
	 * @return 分页 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	AuthorizationInvalidationEventPageVO toPageVO(AuthorizationInvalidationEventPageInnerDTO dto);

	/**
	 * Feign 详情 → 详情 VO
	 * @param dto Feign 详情
	 * @return 详情 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	AuthorizationInvalidationEventDetailVO toDetailVO(AuthorizationInvalidationEventDetailInnerDTO dto);

}
