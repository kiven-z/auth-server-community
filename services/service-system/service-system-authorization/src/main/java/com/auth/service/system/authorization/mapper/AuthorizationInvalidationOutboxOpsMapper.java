package com.auth.service.system.authorization.mapper;

import com.auth.service.system.authorization.model.po.AuthorizationInvalidationFailureRateTrendBucketPO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationOutboxDetailRowPO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationOutboxPageRowPO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationOutboxStatsPO;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationFailureRateTrendQuery;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationOutboxOpsQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 授权失效 Outbox 运维只读 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface AuthorizationInvalidationOutboxOpsMapper {

	/**
	 * 分页查询 Outbox（运维只读）
	 * @param page 分页参数
	 * @param query 查询条件
	 * @return 分页结果
	 */
	IPage<AuthorizationInvalidationOutboxPageRowPO> selectListByPage(
			@Param("page") Page<AuthorizationInvalidationOutboxPageRowPO> page,
			@Param("query") AuthorizationInvalidationOutboxOpsQuery query);

	/**
	 * 按主键查询 Outbox 详情
	 * @param id 主键
	 * @return 详情行，不存在为 null
	 */
	AuthorizationInvalidationOutboxDetailRowPO selectDetailById(@Param("id") Long id);

	/**
	 * 统计 Outbox 各状态记录数
	 * @return 状态统计
	 */
	AuthorizationInvalidationOutboxStatsPO selectOutboxStats();

	/**
	 * 按时间桶统计失败率趋势
	 * @param query 查询条件（含粒度与已解析的起止时间）
	 * @return 分桶统计列表
	 */
	List<AuthorizationInvalidationFailureRateTrendBucketPO> selectFailureRateTrendBuckets(
			@Param("query") AuthorizationInvalidationFailureRateTrendQuery query);

}
