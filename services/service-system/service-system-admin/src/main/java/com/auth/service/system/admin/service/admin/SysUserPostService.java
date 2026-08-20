package com.auth.service.system.admin.service.admin;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.UserPostEntity;
import com.auth.service.system.admin.model.form.user.UserPostAssignForm;
import com.auth.service.system.admin.model.form.user.UserPostRelationUpdateForm;
import com.auth.service.system.admin.model.query.user.UserPostPageQuery;
import com.auth.service.system.admin.model.vo.user.UserPostPageVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户岗位关联服务
 *
 * @author Bunny
 */
public interface SysUserPostService extends IService<UserPostEntity> {

	/**
	 * 分页查询用户岗位关联（基表全量，含失效任职）
	 * @param userId 用户 ID
	 * @param query 查询条件
	 * @return 分页结果
	 */
	PageResponse<UserPostPageVO> getPage(Long userId, UserPostPageQuery query);

	/**
	 * 新增用户岗位关联（仅计算有效岗位）
	 * @param userId 用户 ID
	 * @param form 关联表单
	 */
	void create(Long userId, UserPostAssignForm form);

	/**
	 * 更新用户岗位关联（设为主岗时岗位须计算有效）
	 * @param userId 用户 ID
	 * @param id 关联主键
	 * @param form 更新表单
	 */
	void update(Long userId, Long id, UserPostRelationUpdateForm form);

	/**
	 * 批量删除用户岗位关联
	 * @param userId 用户 ID
	 * @param ids 关联主键列表
	 */
	void removeBatch(Long userId, List<Long> ids);

	/**
	 * 清空用户全部岗位关联
	 * @param userId 用户 ID
	 */
	void removeAll(Long userId);

}
