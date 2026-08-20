package com.auth.service.system.message.service.admin;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.form.inapp.InAppComposeForm;
import com.auth.service.system.message.model.query.InAppSendTaskQuery;
import com.auth.service.system.message.model.query.InAppSendTaskRecipientQuery;
import com.auth.service.system.message.model.vo.inapp.InAppComposeResultVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskPageVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskRecipientPageVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 管理端站内信发送任务服务
 *
 * @author Bunny
 */
public interface InAppSendTaskService extends IService<InAppMessageEntity> {

	/**
	 * 分页查询站内信发送任务
	 * @param query 查询条件
	 * @return 发送任务分页数据
	 */
	PageResponse<InAppSendTaskPageVO> getSendTaskPage(InAppSendTaskQuery query);

	/**
	 * 查询站内信发送任务详情
	 * @param taskId 任务 ID
	 * @return 详情
	 */
	InAppSendTaskDetailVO getSendTaskById(Long taskId);

	/**
	 * 分页查询任务下收件人/互动用户
	 * <p>
	 * 写扩散查收件箱行；读扩散（ALL）查用户状态行（仅产生过已读/删除的用户）
	 * </p>
	 * @param taskId 任务 ID
	 * @param query 筛选条件
	 * @return 收件人分页数据
	 */
	PageResponse<InAppSendTaskRecipientPageVO> getRecipientPage(Long taskId, InAppSendTaskRecipientQuery query);

	/**
	 * 提交站内信发送任务
	 * @param form 发送表单
	 * @return 任务受理结果
	 */
	InAppComposeResultVO send(InAppComposeForm form);

	/**
	 * 补发站内信发送任务
	 * @param taskId 任务 ID
	 */
	void retry(Long taskId);

	/**
	 * 撤回站内信发送任务
	 * @param taskId 任务 ID
	 */
	void recall(Long taskId);

	/**
	 * 批量删除站内信发送任务
	 * @param ids 任务 ID 列表
	 */
	void batchDelete(List<Long> ids);

}
