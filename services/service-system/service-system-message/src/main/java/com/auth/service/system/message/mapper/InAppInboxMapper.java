package com.auth.service.system.message.mapper;

import com.auth.service.system.message.model.po.InAppInboxDetailRowPO;
import com.auth.service.system.message.model.po.InAppInboxMajorUnreadRowPO;
import com.auth.service.system.message.model.po.InAppInboxPageRowPO;
import com.auth.service.system.message.model.query.InAppInboxQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户侧站内信收件箱 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface InAppInboxMapper {

	/**
	 * 分页查询当前用户收件箱
	 * @param page 分页参数
	 * @param userId 当前用户 ID
	 * @param query 查询条件
	 * @return 分页行
	 */
	IPage<InAppInboxPageRowPO> selectInboxPage(Page<InAppInboxPageRowPO> page, @Param("userId") Long userId,
			@Param("query") InAppInboxQuery query);

	/**
	 * 启用大类全量 + 各未读数
	 * @param userId 当前用户 ID
	 * @return 大类未读行
	 */
	List<InAppInboxMajorUnreadRowPO> selectUnreadCountByMajor(@Param("userId") Long userId);

	/**
	 * 当前大类下对当前用户可见的未读站内信 ID（写扩散 ∪ 读扩散）
	 * @param userId 当前用户 ID
	 * @param majorCategoryId 业务大类 ID
	 * @return 未读消息 ID
	 */
	List<Long> selectUnreadMessageIdsByMajor(@Param("userId") Long userId,
			@Param("majorCategoryId") Long majorCategoryId);

	/**
	 * 当前大类下对当前用户可见且未软删的站内信 ID（写扩散 ∪ 读扩散）
	 * @param userId 当前用户 ID
	 * @param majorCategoryId 业务大类 ID
	 * @return 可见消息 ID
	 */
	List<Long> selectVisibleMessageIdsByMajor(@Param("userId") Long userId,
			@Param("majorCategoryId") Long majorCategoryId);

	/**
	 * 按当前用户可见性查询收件箱详情（写扩散 ∪ 读扩散）
	 * @param userId 当前用户 ID
	 * @param messageId 站内信 ID
	 * @return 可见时返回详情行，否则 null
	 */
	InAppInboxDetailRowPO selectInboxDetail(@Param("userId") Long userId, @Param("messageId") Long messageId);

}
