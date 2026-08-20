package com.auth.service.system.admin.service.admin;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.UserDeptEntity;
import com.auth.service.system.admin.model.form.user.UserDeptAssignForm;
import com.auth.service.system.admin.model.query.user.UserDeptPageQuery;
import com.auth.service.system.admin.model.vo.user.UserDeptPageVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户部门关联服务
 *
 * @author Bunny
 */
public interface SysUserDeptService extends IService<UserDeptEntity> {

	/**
	 * 分页查询用户部门关联
	 * @param userId 用户 ID
	 * @param query 查询条件
	 * @return 分页结果
	 */
	PageResponse<UserDeptPageVO> getPage(Long userId, UserDeptPageQuery query);

	/**
	 * 新增用户部门关联
	 * @param userId 用户 ID
	 * @param form 关联表单
	 */
	void create(Long userId, UserDeptAssignForm form);

	/**
	 * 更新用户部门关联
	 * @param userId 用户 ID
	 * @param id 关联主键
	 * @param form 关联表单
	 */
	void update(Long userId, Long id, UserDeptAssignForm form);

	/**
	 * 批量删除用户部门关联
	 * @param userId 用户 ID
	 * @param ids 关联主键列表
	 */
	void removeBatch(Long userId, List<Long> ids);

	/**
	 * 清空用户全部部门关联
	 * @param userId 用户 ID
	 */
	void removeAll(Long userId);

}
