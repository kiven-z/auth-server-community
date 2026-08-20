package com.auth.service.system.message.service.admin;

import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.email.EmailTemplateContentForm;
import com.auth.service.system.message.model.form.email.EmailTemplateForm;
import com.auth.service.system.message.model.form.email.EmailTemplateRenderForm;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 邮件模板服务
 *
 * @author Bunny
 */
public interface EmailTemplateService extends IService<MessageTemplateEntity> {

	/**
	 * 离线渲染邮件模板
	 * @param form 内容与变量声明
	 * @return 渲染后的 HTML
	 */
	String render(EmailTemplateRenderForm form);

	/**
	 * 新增邮件模板
	 * @param form 邮件模板表单
	 */
	void create(EmailTemplateForm form);

	/**
	 * 更新邮件模板
	 * @param form 邮件模板表单
	 */
	void update(EmailTemplateForm form);

	/**
	 * 更新邮件正文
	 * @param form 含 id 与 content
	 */
	void updateContent(EmailTemplateContentForm form);

}
