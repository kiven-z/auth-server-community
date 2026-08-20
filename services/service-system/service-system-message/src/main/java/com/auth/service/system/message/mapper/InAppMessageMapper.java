package com.auth.service.system.message.mapper;

import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.po.InAppSendTaskPageRowPO;
import com.auth.service.system.message.model.query.InAppSendTaskQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.Instant;

/**
 * 消息发送任务 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface InAppMessageMapper extends BaseMapper<InAppMessageEntity> {

	/**
	 * 分页查询站内信发送任务
	 * @param pageParams 分页参数
	 * @param query 查询条件
	 * @return 分页行
	 */
	IPage<InAppSendTaskPageRowPO> selectSendTaskPage(@Param("page") Page<InAppMessageEntity> pageParams,
			@Param("query") InAppSendTaskQuery query);

	/**
	 * 状态 CAS 更新
	 * @param id 任务 ID
	 * @param expectedStatus 期望当前状态
	 * @param newStatus 目标状态
	 * @return 影响行数
	 */
	@Update("UPDATE in_app_message SET status = #{newStatus} WHERE id = #{id} AND status = #{expectedStatus}")
	int updateStatusCas(@Param("id") Long id, @Param("expectedStatus") String expectedStatus,
			@Param("newStatus") String newStatus);

	/**
	 * 写入终态并同步成功数/总数
	 * @param id 任务 ID
	 * @param status 终态
	 * @param deliveredCount 已投递数
	 * @return 影响行数
	 */
	int finishTask(@Param("id") Long id, @Param("status") String status, @Param("deliveredCount") int deliveredCount);

	/**
	 * 重置任务状态以便补发
	 * @param id 任务 ID
	 * @return 影响行数
	 */
	int resetForRetry(@Param("id") Long id);

	/**
	 * 将已投递/无接收人任务撤回
	 * @param id 任务 ID
	 * @param recalledAt 撤回时间
	 * @param recallUserId 撤回操作人
	 * @return 影响行数
	 */
	int recallTask(@Param("id") Long id, @Param("recalledAt") Instant recalledAt,
			@Param("recallUserId") Long recallUserId);

}
