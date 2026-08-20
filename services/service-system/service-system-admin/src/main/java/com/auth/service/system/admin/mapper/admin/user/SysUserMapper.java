package com.auth.service.system.admin.mapper.admin.user;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.model.po.user.*;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 系统用户 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysUserMapper extends BaseMapper<UserEntity> {

	/**
	 * 分页查询用户
	 * @param page 分页参数
	 * @param query 筛选条件
	 * @param orderBySql 已由白名单校验的 ORDER BY 片段
	 * @return 分页结果
	 */
	IPage<SysUserPageRowPO> selectListByPage(@Param("page") Page<UserEntity> page,
			@Param("query") SysUserPageQuery query, @Param("orderBySql") String orderBySql);

	/**
	 * 按业务键批量查询用户行（username / email / phone / employee_no 任一命中）
	 * @param usernames 待匹配用户名
	 * @param emails 待匹配邮箱
	 * @param phones 待匹配手机号
	 * @param employeeNos 待匹配工号
	 * @param excludeUserId 更新时排除的用户 ID，新增时为 null
	 * @return 命中行的业务键投影
	 */
	List<UserBusinessKeyRowPO> selectRowsByBusinessKeys(@Param("usernames") Collection<String> usernames,
			@Param("emails") Collection<String> emails, @Param("phones") Collection<String> phones,
			@Param("employeeNos") Collection<String> employeeNos, @Param("excludeUserId") Long excludeUserId);

	/**
	 * 按用户ID查询部门关联列表
	 * @param userId 用户ID
	 * @return 部门项
	 */
	List<UserDeptProfilePO> selectDeptProfileByUserId(@Param("userId") Long userId);

	/**
	 * 按用户ID查询岗位关联列表
	 * @param userId 用户ID
	 * @return 岗位项
	 */
	List<UserPostProfilePO> selectPostProfileByUserId(@Param("userId") Long userId);

	/**
	 * 关键词模糊搜索用户（未删除、状态正常），匹配用户名/昵称/邮箱/手机/工号。
	 * @param keyword 关键字（非空，由调用方保证）
	 * @param limit 最大返回条数
	 * @return 用户列表，不含密码等敏感列
	 */
	List<UserSearchItemPO> searchByKeyword(@Param("keyword") String keyword, @Param("limit") int limit);

}
