package com.auth.service.system.admin.mapper.admin.menu;

import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.query.menu.SysMenuQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统菜单
 *
 * @author Bunny
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenuEntity> {

	/**
	 * 条件查询全部菜单
	 * @param query 查询条件
	 * @return 菜单列表
	 */
	List<SysMenuEntity> selectListByQuery(@Param("query") SysMenuQuery query);

	/**
	 * 分页查询菜单
	 * @param page 分页参数
	 * @param query 查询条件
	 * @return 分页菜单列表
	 */
	IPage<SysMenuEntity> selectPageByQuery(IPage<SysMenuEntity> page, @Param("query") SysMenuQuery query);

	/**
	 * 查找待删节点中，仍存在
	 * @param ids 待删除菜单 ID 列表
	 * @return 阻塞删除的父菜单 ID
	 */
	Long selectFirstBlockedParentId(@Param("ids") List<Long> ids);

}
