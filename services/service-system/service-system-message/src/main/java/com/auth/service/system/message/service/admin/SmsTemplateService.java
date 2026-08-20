package com.auth.service.system.message.service.admin;

import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.sms.SmsTemplateForm;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 短信模板服务
 *
 * @author Bunny
 */
public interface SmsTemplateService extends IService<MessageTemplateEntity> {

	/**
	 * 新增短信模板
	 * @param form 短信模板表单
	 */
	void create(SmsTemplateForm form);

	/**
	 * 更新短信模板（不修改渠道与变量声明）
	 * @param form 短信模板表单
	 */
	void update(SmsTemplateForm form);

}
