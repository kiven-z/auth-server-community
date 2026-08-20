package com.auth.service.system.message.mapper;

import com.auth.service.system.message.model.entity.InAppMessageRecipientEntity;
import com.auth.service.system.message.model.po.InAppSendTaskRecipientPageRowPO;
import com.auth.service.system.message.model.query.InAppSendTaskRecipientQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

/**
 * 站内信收件箱 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface InAppMessageRecipientMapper extends BaseMapper<InAppMessageRecipientEntity> {

	/**
	 * 批量 INSERT IGNORE，撞唯一键时跳过
	 * @param rows 待插入行
	 * @return 实际插入行数
	 */
	int insertIgnoreBatch(@Param("rows") List<InAppMessageRecipientEntity> rows);

	/**
	 * 将当前用户可见且未读的收件行标为已读
	 * @param userId 当前用户 ID
	 * @param messageIds 站内信 ID 列表
	 * @param readTime 已读时间
	 * @return 影响行数
	 */
	int markRead(@Param("userId") Long userId, @Param("messageIds") List<Long> messageIds,
			@Param("readTime") Instant readTime);

	/**
	 * 将当前用户可见且未删的收件行软删除
	 * @param userId 当前用户 ID
	 * @param messageIds 站内信 ID 列表
	 * @return 影响行数
	 */
	int markDeleted(@Param("userId") Long userId, @Param("messageIds") List<Long> messageIds);

	/**
	 * 管理端：按发送任务分页查询写扩散收件人
	 * @param page 分页参数
	 * @param messageId 站内信任务 ID
	 * @param query 筛选条件
	 * @return 分页行
	 */
	IPage<InAppSendTaskRecipientPageRowPO> selectRecipientPage(Page<InAppSendTaskRecipientPageRowPO> page,
			@Param("messageId") Long messageId, @Param("query") InAppSendTaskRecipientQuery query);

}
