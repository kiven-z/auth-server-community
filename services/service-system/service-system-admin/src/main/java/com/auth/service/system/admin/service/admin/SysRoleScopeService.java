package com.auth.service.system.admin.service.admin;

import com.auth.service.system.admin.model.entity.RoleScopeEntity;
import com.auth.service.system.admin.model.form.scope.SysDataScopeForm;
import com.auth.service.system.admin.model.vo.role.SysRoleScopeVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 角色数据范围服务
 *
 * @author Bunny
 */
public interface SysRoleScopeService extends IService<RoleScopeEntity> {

	/**
	 * 按角色查询数据范围配置
	 * <p>
	 * 无配置行时返回空列表（画像侧兜底 SELF）
	 * </p>
	 * @param roleId 角色 ID
	 * @return 各维范围配置；未配置为空列表
	 */
	List<SysRoleScopeVO> getByRoleId(Long roleId);

	/**
	 * 按提交列表覆盖该角色的全部数据范围。先删除后添加
	 * @param roleId 角色 ID
	 * @param forms 范围表单列表
	 */
	void replace(Long roleId, List<SysDataScopeForm> forms);

}
