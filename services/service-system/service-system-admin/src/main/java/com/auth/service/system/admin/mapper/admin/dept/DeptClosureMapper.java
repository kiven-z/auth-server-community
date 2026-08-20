package com.auth.service.system.admin.mapper.admin.dept;

import com.auth.service.system.admin.model.po.dept.DeptClosureDepthChainRowPO;
import com.auth.service.system.admin.model.po.dept.DeptClosureHealthStatsPO;
import com.auth.service.system.admin.model.po.dept.DeptClosureNodePO;
import com.auth.service.system.admin.model.po.dept.DeptClosureParentLinkRowPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门闭包表写
 *
 * @author Bunny
 */
@Mapper
public interface DeptClosureMapper {

	/**
	 * 批量新增节点后写入闭包
	 * @param nodes 新增节点列表
	 */
	void insertPathsBatch(@Param("nodes") List<DeptClosureNodePO> nodes);

	/**
	 * 物理删除子树在闭包表中的全部路径
	 * @param deptId 子树根节点 ID
	 */
	void deletePaths(@Param("deptId") Long deptId);

	/**
	 * 移动前先清理旧祖先到子树的跨树路径（保留子树内部路径）
	 * @param deptId 被移动部门 ID
	 */
	void deleteMovePaths(@Param("deptId") Long deptId);

	/**
	 * 移动后补齐新祖先到子树的跨树路径（须已更新 sys_dept.parent_id）
	 * @param deptId 被移动部门 ID
	 * @param newParentId 新父部门 ID，顶级传 0
	 */
	void insertMovePaths(@Param("deptId") Long deptId, @Param("newParentId") Long newParentId);

	/**
	 * 统计父子直连缺失的活跃部门数
	 * @return 异常总数
	 */
	long countParentLinkAnomalies();

	/**
	 * 查询父子直连缺失样本
	 * @param limit 样本上限
	 * @return 异常行
	 */
	List<DeptClosureParentLinkRowPO> selectParentLinkAnomalies(@Param("limit") int limit);

	/**
	 * 统计深度链条异常的活跃部门数
	 * @return 异常总数
	 */
	long countDepthChainAnomalies();

	/**
	 * 查询深度链条异常样本
	 * @param limit 样本上限
	 * @return 异常行
	 */
	List<DeptClosureDepthChainRowPO> selectDepthChainAnomalies(@Param("limit") int limit);

	/**
	 * 活跃部门的闭包行数健康度统计
	 * @return 统计快照
	 */
	DeptClosureHealthStatsPO selectHealthStats();

}
