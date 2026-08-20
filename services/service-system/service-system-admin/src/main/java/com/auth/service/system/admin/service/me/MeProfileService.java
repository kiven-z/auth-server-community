package com.auth.service.system.admin.service.me;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.model.form.me.MeAvatarUpdateForm;
import com.auth.service.system.admin.model.form.me.MeProfileUpdateForm;
import com.auth.service.system.admin.model.vo.me.MeOrgBindingsVO;
import com.auth.service.system.admin.model.vo.me.MeProfileVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 当前登录用户个人资料服务
 *
 * @author Bunny
 */
public interface MeProfileService extends IService<UserEntity> {

	/**
	 * 获取当前用户个人资料（含主部门）
	 * @return 展示资料
	 */
	MeProfileVO getMyProfile();

	/**
	 * 查询当前用户有效组织任职（部门与岗位）
	 * @return 组织任职
	 */
	MeOrgBindingsVO getMyOrgBindings();

	/**
	 * 更新当前用户个人资料
	 * @param form 资料表单
	 */
	void updateMyProfile(MeProfileUpdateForm form);

	/**
	 * 更新当前用户头像
	 * @param form 头像
	 */
	void updateMyAvatar(MeAvatarUpdateForm form);

}
