package com.auth.service.system.admin.service.admin;

import com.auth.service.system.admin.model.entity.UserScopeEntity;
import com.auth.service.system.admin.model.form.scope.SysDataScopeForm;
import com.auth.service.system.admin.model.vo.user.SysUserScopeVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户数据范围服务
 *
 * @author Bunny
 */
public interface SysUserScopeService extends IService<UserScopeEntity> {

	/**
	 * 按用户查询数据范围配置
	 * <p>
	 * 无配置行时返回空列表（表示继承角色合并结果）
	 * </p>
	 * @param userId 用户 ID
	 * @return 各维范围配置；未配置为空列表
	 */
	List<SysUserScopeVO> getByUserId(Long userId);

	/**
	 * 按提交列表覆盖该用户的全部数据范围。先删除后添加
	 * @param userId 用户 ID
	 * @param forms 范围表单列表
	 */
	void replace(Long userId, List<SysDataScopeForm> forms);

}
