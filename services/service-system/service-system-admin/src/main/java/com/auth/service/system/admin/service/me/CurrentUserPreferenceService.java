package com.auth.service.system.admin.service.me;

import com.auth.service.system.admin.model.entity.SysUserConfigEntity;
import com.auth.service.system.admin.model.form.me.MeUserPreferenceUpsertForm;
import com.auth.service.system.admin.model.vo.me.MeUserPreferenceListVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 当前登录用户 UI 偏好配置服务
 *
 * @author Bunny
 */
public interface CurrentUserPreferenceService extends IService<SysUserConfigEntity> {

	/**
	 * 查询当前用户 UI 偏好配置（仅白名单键）
	 * @return 配置列表；无记录时 items 为空数组
	 */
	MeUserPreferenceListVO listMyPreferences();

	/**
	 * 新增或更新当前用户 UI 偏好配置
	 * @param form 配置键与内容
	 */
	void upsertMyPreference(MeUserPreferenceUpsertForm form);

	/**
	 * 清空当前用户 UI 偏好配置（仅白名单键）
	 */
	void clearMyPreferences();

}
