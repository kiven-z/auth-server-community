package com.auth.service.system.admin.service.admin;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.model.vo.user.SysUserDetailVO;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import com.auth.service.system.admin.model.vo.user.SysUserProfileVO;
import com.auth.service.system.admin.model.vo.user.SysUserSearchItemVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 系统用户只读查询服务（档案聚合、关键词搜索等）
 *
 * @author Bunny
 */
public interface SysUserQueryService extends IService<UserEntity> {

	/**
	 * 分页查询用户
	 * @param query 筛选条件
	 * @return 分页结果
	 */
	PageResponse<SysUserPageVO> getPage(SysUserPageQuery query);

	/**
	 * 获取用户档案（标量 + 部门/岗位关联数）
	 * @param userId 用户ID
	 * @return 用户档案
	 */
	SysUserProfileVO getProfile(Long userId);

	/**
	 * 获取用户详情（档案 + 授权关系计数）
	 * @param userId 用户ID
	 * @return 用户详情
	 */
	SysUserDetailVO getDetail(Long userId);

	/**
	 * 按关键字搜索用户（仅正常未删除）
	 * @param keyword 关键字；空或仅空白时返回空列表，不访问数据库
	 * @param limit 最大条数；null 时使用默认上限，超过绝对上限时截断
	 * @return 搜索结果，不含密码
	 */
	List<SysUserSearchItemVO> searchByKeyword(String keyword, Integer limit);

}
