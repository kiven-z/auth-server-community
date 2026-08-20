package com.auth.service.system.admin.support.dept;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.admin.mapper.admin.dept.DeptClosureMapper;
import com.auth.service.system.admin.model.po.dept.DeptClosureNodePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 闭包表维护入口：部门树结构变更时执行闭包 SQL
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class DeptClosureMaintainer {

	private final DeptClosureMapper deptClosureMapper;

	/**
	 * 批量新增部门后写入闭包路径
	 * @param nodes 新增节点列表
	 */
	public void insertNodes(List<DeptClosureNodePO> nodes) {
		if (CollUtil.isEmpty(nodes)) {
			return;
		}
		deptClosureMapper.insertPathsBatch(nodes);
	}

	/**
	 * 物理删除该部门,物理删除子树
	 * @param deptId 部门 ID
	 */
	public void removeClosurePaths(long deptId) {
		deptClosureMapper.deletePaths(deptId);
	}

	/**
	 * 变更父部门后重算闭包
	 * @param deptId 被移动部门 ID
	 * @param newParentId 新父部门 ID
	 */
	public void moveNode(long deptId, long newParentId) {
		deptClosureMapper.deleteMovePaths(deptId);
		deptClosureMapper.insertMovePaths(deptId, newParentId);
	}

}
