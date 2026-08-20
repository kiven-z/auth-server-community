package com.auth.service.auth.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.auth.model.po.invalidation.AuthorizationInvalidationEventPageRowPO;
import com.auth.service.auth.model.vo.authorization.AuthorizationInvalidationEventDetailVO;
import com.auth.service.auth.model.vo.authorization.AuthorizationInvalidationEventPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 授权失效幂等事件 Row → VO 映射
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class, uses = AuthorizationInvalidationEventMappingSupport.class)
public interface AuthorizationInvalidationEventConverter {

	AuthorizationInvalidationEventConverter INSTANCE = Mappers.getMapper(AuthorizationInvalidationEventConverter.class);

	/**
	 * 分页行投影 → 分页 VO
	 * @param row 分页行投影
	 * @return 分页 VO
	 */
	@Mapping(target = "processing", source = "impactedUserCount", qualifiedByName = "toProcessingFlag")
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	AuthorizationInvalidationEventPageVO toPageVO(AuthorizationInvalidationEventPageRowPO row);

	/**
	 * 详情行投影 → 详情 VO
	 * @param row 详情行投影
	 * @return 详情 VO
	 */
	@Mapping(target = "processing", source = "impactedUserCount", qualifiedByName = "toProcessingFlag")
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	AuthorizationInvalidationEventDetailVO toDetailVo(AuthorizationInvalidationEventPageRowPO row);

}
