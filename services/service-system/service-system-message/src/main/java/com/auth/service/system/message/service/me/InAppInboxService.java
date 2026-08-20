package com.auth.service.system.message.service.me;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.message.model.query.InAppInboxQuery;
import com.auth.service.system.message.model.vo.inapp.InAppInboxDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppInboxPageVO;
import com.auth.service.system.message.model.vo.inapp.InAppInboxUnreadCountVO;

import java.util.List;

/**
 * 用户侧站内信收件箱服务
 *
 * @author Bunny
 */
public interface InAppInboxService {

	/**
	 * 分页查询当前用户收件箱
	 * @param query 查询条件
	 * @return 收件箱分页数据
	 */
	PageResponse<InAppInboxPageVO> getInboxPage(InAppInboxQuery query);

	/**
	 * 查询当前用户未读角标
	 * @return 各大类未读与总数
	 */
	InAppInboxUnreadCountVO getUnreadCount();

	/**
	 * 查询站内信详情（打开即标已读）
	 * @param messageId 站内信 ID
	 * @return 详情；不可见时抛业务异常
	 */
	InAppInboxDetailVO getInboxDetail(Long messageId);

	/**
	 * 将指定站内信标为已读
	 * @param messageIds 站内信 ID 列表
	 */
	void markRead(List<Long> messageIds);

	/**
	 * 将当前大类下可见未读全部标为已读
	 * @param majorCategoryId 业务大类 ID
	 */
	void markAllRead(Long majorCategoryId);

	/**
	 * 批量删除站内信（软删除）
	 * @param messageIds 站内信 ID 列表
	 */
	void batchDelete(List<Long> messageIds);

	/**
	 * 清空当前大类可见站内信（软删除）
	 * @param majorCategoryId 业务大类 ID
	 */
	void deleteAll(Long majorCategoryId);

}
