package com.auth.service.system.admin.convert.admin.menu;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuSaveForm;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 菜单转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface SysMenuConverter {

	SysMenuConverter INSTANCE = Mappers.getMapper(SysMenuConverter.class);

	/**
	 * 详情 VO：继承列表 VO 的映射 + 额外字段 parentId、status 等继承自 toListVo 的映射规则
	 * @param entity 菜单实体
	 * @return 详情 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	@Mapping(target = "boundRoleCount", ignore = true)
	SysMenuDetailVO toDetailVo(SysMenuEntity entity);

	/**
	 * 列表行 VO
	 * @param entity 菜单实体
	 * @return 列表 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	SysMenuListVO toListVo(SysMenuEntity entity);

	/**
	 * 列表行 VO 批量
	 * @param entities 菜单实体列表
	 * @return 列表 VO
	 */
	List<SysMenuListVO> toListVoList(List<SysMenuEntity> entities);

	/**
	 * 映射基本字段
	 * @param form 菜单保存表单
	 * @return 菜单实体
	 */
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "extraMeta", ignore = true)
	SysMenuEntity toMenuEntity(SysMenuSaveForm form);

}
