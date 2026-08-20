package com.auth.service.system.admin.service.admin;

import com.auth.service.system.admin.model.entity.RoleScopeEntity;
import com.auth.service.system.admin.model.form.scope.SysDataScopeForm;
import com.auth.service.system.admin.model.vo.role.SysRoleScopeVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 角色数据范围服务
 *
 * @author Bunny
 */
public interface SysRoleScopeService extends IService<RoleScopeEntity> {

	/**
	 * 按角色查询数据范围配置；无配置行时返回 null（画像侧兜底 SELF）
	 * @param roleId 角色 ID
	 * @return 范围配置；未配置为 null
	 */
	SysRoleScopeVO getByRoleId(Long roleId);

	/**
	 * 保存角色数据范围（写后触发授权失效）
	 * @param roleId 角色 ID
	 * @param form 范围表单
	 */
	void upsert(Long roleId, SysDataScopeForm form);

}
