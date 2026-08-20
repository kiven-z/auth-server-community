package com.auth.service.system.admin.service.admin;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.model.form.user.SysUserAvatarUpdateForm;
import com.auth.service.system.admin.model.form.user.SysUserBatchStatusForm;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 系统用户服务
 *
 * @author Bunny
 */
public interface SysUserService extends IService<UserEntity> {

	/**
	 * 批量新增用户（单条创建与 Excel 导入共用）
	 * @param forms 用户资料列表
	 */
	void createBatchFromImport(List<SysUserForm> forms);

	/**
	 * 更新用户基础资料（不含用户名与账号状态）
	 * @param form 更新表单（须含主键）
	 */
	void update(SysUserForm form);

	/**
	 * 批量删除用户（逻辑删除）
	 * @param ids 用户主键列表
	 */
	void deleteByIds(List<Long> ids);

	/**
	 * 批量更新用户账号状态
	 * @param form 目标状态与用户主键列表
	 */
	void batchUpdateStatus(SysUserBatchStatusForm form);

	/**
	 * 管理员更新用户头像
	 * @param form 更新表单
	 */
	void updateAvatar(@Valid SysUserAvatarUpdateForm form);

}
