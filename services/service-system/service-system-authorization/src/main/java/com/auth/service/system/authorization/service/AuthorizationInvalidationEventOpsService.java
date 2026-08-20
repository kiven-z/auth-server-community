package com.auth.service.system.authorization.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.authorization.feign.dto.AuthorizationInvalidationEventDetailInnerDTO;
import com.auth.service.system.authorization.feign.dto.AuthorizationInvalidationEventPageInnerDTO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationEventStatsPO;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationEventOpsQuery;

/**
 * 授权失效幂等事件运维门面
 *
 * @author Bunny
 */
public interface AuthorizationInvalidationEventOpsService {

	/**
	 * 分页查询幂等事件
	 * @param query 查询条件
	 * @return 分页结果
	 */
	PageResponse<AuthorizationInvalidationEventPageInnerDTO> getPage(AuthorizationInvalidationEventOpsQuery query);

	/**
	 * 查询幂等事件详情
	 * @param id 主键
	 * @return 详情，不存在时抛出
	 * {@link com.auth.service.system.common.exception.SystemBusinessException}
	 */
	AuthorizationInvalidationEventDetailInnerDTO getDetail(Long id);

	/**
	 * 统计幂等事件各状态记录数
	 * @return 状态统计
	 */
	AuthorizationInvalidationEventStatsPO getEventStats();

	/**
	 * 释放 processing 占位
	 * @param id 主键
	 * @return 是否成功释放
	 */
	boolean releaseProcessingClaim(Long id);

}
