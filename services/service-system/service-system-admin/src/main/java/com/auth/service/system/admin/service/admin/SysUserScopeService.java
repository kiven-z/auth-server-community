package com.auth.service.system.admin.service.admin;

import com.auth.service.system.admin.model.entity.UserScopeEntity;
import com.auth.service.system.admin.model.form.scope.SysDataScopeForm;
import com.auth.service.system.admin.model.vo.user.SysUserScopeVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户数据范围服务
 *
 * @author Bunny
 */
public interface SysUserScopeService extends IService<UserScopeEntity> {

	/**
	 * 按用户查询数据范围配置；无配置行时返回 null（表示继承角色合并结果）
	 * @param userId 用户 ID
	 * @return 范围配置；未配置为 null
	 */
	SysUserScopeVO getByUserId(Long userId);

	/**
	 * 保存用户数据范围（写后触发授权失效）
	 * @param userId 用户 ID
	 * @param form 范围表单
	 */
	void upsert(Long userId, SysDataScopeForm form);

	/**
	 * 清除用户数据范围覆盖（无行时幂等成功）
	 * @param userId 用户 ID
	 */
	void clearByUserId(Long userId);

}
