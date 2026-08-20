package com.auth.service.system.admin.mapper.admin.post;

import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.po.post.PostDeptCodePairPO;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.po.post.SysPostSearchItemPO;
import com.auth.service.system.admin.model.po.reference.DeptReferencePO;
import com.auth.service.system.admin.model.query.post.SysPostQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统岗位 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysPostMapper extends BaseMapper<SysPostEntity> {

	/**
	 * 分页查询岗位
	 * @param page 分页参数
	 * @param query 筛选条件
	 * @param orderBySql 已由白名单校验的 ORDER BY 片段
	 * @return 分页结果
	 */
	IPage<SysPostPageRowPO> selectListByPage(@Param("page") Page<SysPostEntity> page,
			@Param("query") SysPostQuery query, @Param("orderBySql") String orderBySql);

	/**
	 * 是否存在用户-岗位关联（基表行，含主体已停用）
	 * @param postId 岗位 ID
	 * @return 引用行数
	 */
	@Select("SELECT COUNT(1) FROM user_post up WHERE up.post_id = #{postId}")
	long countUserPostByPostId(@Param("postId") Long postId);

	/**
	 * 查询可用于挂载岗位的部门主键（须计算有效）
	 * @param deptIds 部门主键列表
	 * @return 可挂载的部门 ID 列表
	 */
	List<Long> selectAssignableDeptIds(@Param("deptIds") List<Long> deptIds);

	/**
	 * 按 (部门 ID, 岗位编码) 批量查询已存在的岗位键
	 * @param pairs 部门与岗位编码对列表（调用方应已去重）
	 * @return 库中已存在的键对
	 */
	List<PostDeptCodePairPO> selectReferenceByDeptPostPairs(@Param("pairs") List<PostDeptCodePairPO> pairs);

	/**
	 * 是否在计算有效视图中
	 * @param postId 岗位 ID
	 * @return 命中行数
	 */
	@Select("SELECT COUNT(1) FROM v_post_effective WHERE id = #{postId}")
	long countEffectiveById(@Param("postId") Long postId);

	/**
	 * 岗位关键词搜索（编码/名称前缀匹配，限制条数；无关键字时返回默认列表）
	 * @param keyword 搜索关键字（前缀匹配，可选）
	 * @param status 启用状态
	 * @param limit 返回条数上限
	 * @return 搜索行列表
	 */
	List<SysPostSearchItemPO> search(@Param("keyword") String keyword, @Param("status") Boolean status,
			@Param("limit") int limit);

	/**
	 * 查询岗位所属部门快照
	 * @param postId 岗位 ID
	 * @return 部门快照，岗位或部门不存在时为 null
	 */
	DeptReferencePO selectBoundDeptByPostId(@Param("postId") Long postId);

}
