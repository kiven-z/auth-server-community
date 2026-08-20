package com.auth.service.system.message.mapper;

import com.auth.service.system.message.model.entity.InAppMessageUserStatusEntity;
import com.auth.service.system.message.model.po.InAppSendTaskRecipientPageRowPO;
import com.auth.service.system.message.model.query.InAppSendTaskRecipientQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 站内信公开消息用户状态 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface InAppMessageUserStatusMapper extends BaseMapper<InAppMessageUserStatusEntity> {

	/**
	 * 批量 upsert 已读状态（撞 uk 时仅补已读字段）
	 * @param rows 待写入状态行
	 * @return 影响行数
	 */
	int upsertReadBatch(@Param("rows") List<InAppMessageUserStatusEntity> rows);

	/**
	 * 批量 upsert 软删除状态（撞 uk 时仅补删除字段，不改已读）
	 * @param rows 待写入状态行
	 * @return 影响行数
	 */
	int upsertDeletedBatch(@Param("rows") List<InAppMessageUserStatusEntity> rows);

	/**
	 * 管理端：按发送任务分页查询读扩散互动用户
	 * @param page 分页参数
	 * @param messageId 站内信任务 ID
	 * @param query 筛选条件
	 * @return 分页行
	 */
	IPage<InAppSendTaskRecipientPageRowPO> selectUserStatusPage(Page<InAppSendTaskRecipientPageRowPO> page,
			@Param("messageId") Long messageId, @Param("query") InAppSendTaskRecipientQuery query);

}
