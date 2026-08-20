package com.auth.service.system.admin.mapper.admin.user;

import com.auth.service.system.admin.model.entity.SysUserConfigEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 用户配置项 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysUserConfigMapper extends BaseMapper<SysUserConfigEntity> {

	/**
	 * 按用户与白名单配置键批量查询
	 * @param userId 用户 ID
	 * @param configKeys 配置键集合
	 * @return 匹配的配置项列表
	 */
	List<SysUserConfigEntity> selectListByUserIdAndConfigKeys(@Param("userId") Long userId,
			@Param("configKeys") Collection<String> configKeys);

	/**
	 * 按用户物理删除全部配置项
	 * @param userId 用户 ID
	 * @return 影响行数
	 */
	@Delete("DELETE FROM sys_user_config WHERE user_id = #{userId}")
	int deleteByUserId(@Param("userId") Long userId);

}
