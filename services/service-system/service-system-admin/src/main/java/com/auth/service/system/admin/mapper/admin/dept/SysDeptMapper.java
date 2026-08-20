package com.auth.service.system.admin.mapper.admin.dept;

import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.po.dept.SysDeptPageRowPO;
import com.auth.service.system.admin.model.po.dept.SysDeptPathRowPO;
import com.auth.service.system.admin.model.query.dept.SysDeptListQuery;
import com.auth.service.system.admin.model.query.dept.SysDeptPageQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统部门 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDeptEntity> {

	/**
	 * 条件查询全部部门（扁平，含计算有效）
	 * @param query 查询条件
	 * @return 部门列表
	 */
	List<SysDeptPageRowPO> selectListByQuery(@Param("query") SysDeptListQuery query);

	/**
	 * 分页查询部门
	 * @param page 分页参数
	 * @param query 查询条件
	 * @return 分页部门列表
	 */
	IPage<SysDeptPageRowPO> selectListByPage(@Param("page") Page<SysDeptEntity> page,
			@Param("query") SysDeptPageQuery query);

	/**
	 * 部门是否存在（不按启停过滤）
	 * @param deptId 部门 ID
	 * @return 匹配行数
	 */
	@Select("SELECT COUNT(1) FROM sys_dept WHERE id = #{deptId}")
	int countById(@Param("deptId") Long deptId);

	/**
	 * 部门是否计算有效（本节点及全部祖先均启用）
	 * @param deptId 部门 ID
	 * @return 匹配行数
	 */
	@Select("SELECT COUNT(1) FROM v_dept_effective WHERE id = #{deptId}")
	long countEffectiveById(@Param("deptId") Long deptId);

	/**
	 * 直接子部门数量
	 * @param parentId 父部门 ID
	 * @return 子部门行数
	 */
	@Select("SELECT COUNT(1) FROM sys_dept WHERE parent_id = #{parentId}")
	int countActiveDirectChildren(@Param("parentId") Long parentId);

	/**
	 * 批量查询部门从根到自身的名称路径
	 * @param ids 部门主键列表
	 * @return 路径投影列表
	 */
	List<SysDeptPathRowPO> selectDeptPathByIds(@Param("ids") List<Long> ids);

	/**
	 * 按部门编码批量查询启用中的部门
	 * @param deptCodes 部门编码列表
	 * @return 部门实体列表
	 */
	List<SysDeptEntity> selectActiveByDeptCodes(@Param("deptCodes") List<String> deptCodes);

	/**
	 * 子树内是否存在用户-部门关联、grant DEPT 主体或未删除岗位
	 * @param deptId 子树根部门 ID
	 * @return true 表示子树仍被引用，不可删除
	 */
	boolean existsSubtreeReference(@Param("deptId") Long deptId);

	/**
	 * descendantId 是否位于 ancestorId 的子树中
	 * @param ancestorId 祖先部门 ID
	 * @param descendantId 候选后代部门 ID
	 * @return 匹配行数，大于 0 表示是后代
	 */
	long countDescendantRelation(@Param("ancestorId") Long ancestorId, @Param("descendantId") Long descendantId);

}
