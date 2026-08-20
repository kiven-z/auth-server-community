package com.auth.service.system.message.service.admin;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.MessageTemplateRequireFieldsForm;
import com.auth.service.system.message.model.form.MessageTemplateStatusForm;
import com.auth.service.system.message.model.form.MessageTemplateTestSendForm;
import com.auth.service.system.message.model.query.MessageTemplateQuery;
import com.auth.service.system.message.model.vo.template.MessageTemplateDetailVO;
import com.auth.service.system.message.model.vo.template.MessageTemplatePageVO;
import com.auth.service.system.message.model.vo.template.MessageTemplateRequireFieldRow;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 消息模板服务
 *
 * @author Bunny
 */
public interface MessageTemplateService extends IService<MessageTemplateEntity> {

	/**
	 * 按渠道分页查询消息模板
	 * @param query 查询条件（channel 必填）
	 * @return 消息模板分页数据
	 */
	PageResponse<MessageTemplatePageVO> getMessageTemplatePage(MessageTemplateQuery query);

	/**
	 * 按主键与渠道查询消息模板详情
	 * @param id 模板主键
	 * @param channel 消息渠道
	 * @return 模板详情
	 */
	MessageTemplateDetailVO getMessageTemplateById(Long id, String channel);

	/**
	 * 查询模板变量声明列表（只解析不校验）
	 * @param id 模板主键
	 * @param channel 消息渠道（须与库中一致）
	 * @return 变量声明列表；库中为空则返回空列表
	 */
	List<MessageTemplateRequireFieldRow> getRequireFields(Long id, String channel);

	/**
	 * 按主键列表与渠道批量删除模板
	 * @param ids 模板主键列表
	 * @param channel 消息渠道（仅删除该渠道下的行）
	 */
	void batchDelete(List<Long> ids, String channel);

	/**
	 * 批量启停模板
	 * @param form 含 ids、status、channel
	 */
	void batchUpdateStatus(MessageTemplateStatusForm form);

	/**
	 * 更新模板变量声明（须与渠道匹配）
	 * @param form 含 id、channel、requireFields
	 */
	void updateRequireFields(MessageTemplateRequireFieldsForm form);

	/**
	 * 测试发送模板（按变量示例值渲染并投递）
	 * @param form 模板 ID、渠道与接收目标
	 */
	void testSend(MessageTemplateTestSendForm form);

}
