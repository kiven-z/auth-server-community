package com.auth.service.system.message.service.admin;

import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.inapp.InAppTemplateForm;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 站内信模板服务
 *
 * @author Bunny
 */
public interface InAppTemplateService extends IService<MessageTemplateEntity> {

	/**
	 * 新增站内信模板
	 * @param form 站内信模板表单
	 */
	void create(InAppTemplateForm form);

	/**
	 * 更新站内信模板
	 * @param form 站内信模板表单
	 */
	void update(InAppTemplateForm form);

}
