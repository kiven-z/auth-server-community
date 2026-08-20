package com.auth.service.system.admin.service.admin;

import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptForm;
import com.auth.service.system.admin.model.form.dept.SysDeptMoveForm;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 部门写服务
 *
 * @author Bunny
 */
public interface SysDeptService extends IService<SysDeptEntity> {

	/**
	 * 批量新增部门（单条创建与 Excel 导入共用）
	 * @param forms 新增表单列表
	 */
	void createBatchFromImport(List<SysDeptForm> forms);

	/**
	 * 更新部门元数据
	 * @param form 保存表单
	 */
	void updateMeta(SysDeptForm form);

	/**
	 * 批量启停部门
	 * @param form 主键列表与目标状态
	 */
	void batchUpdateStatus(IdsEnableStatusForm form);

	/**
	 * 变更父部门并重算闭包
	 * @param form 移动表单
	 */
	void move(SysDeptMoveForm form);

	/**
	 * 删除部门（逻辑删除）
	 * @param deptId 部门 ID
	 */
	void deleteById(Long deptId);

}
