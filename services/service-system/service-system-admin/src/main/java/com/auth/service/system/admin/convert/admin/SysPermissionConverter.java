package com.auth.service.system.admin.convert.admin;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.form.permission.SysPermissionForm;
import com.auth.service.system.admin.model.vo.permission.SysPermissionDetailVO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 权限转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface SysPermissionConverter {

	SysPermissionConverter INSTANCE = Mappers.getMapper(SysPermissionConverter.class);

	/**
	 * 新增表单 → 实体
	 * @param form 表单
	 * @return 实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "version", ignore = true)
	SysPermissionEntity toEntity(SysPermissionForm form);

	/**
	 * 新增表单列表 → 实体列表
	 * @param forms 表单列表
	 * @return 实体列表
	 */
	List<SysPermissionEntity> toEntityList(List<SysPermissionForm> forms);

	/**
	 * 更新表单字段合并到已有实体
	 * @param form 更新表单
	 * @param entity 已有实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "version", ignore = true)
	void applyUpdateForm(SysPermissionForm form, @MappingTarget SysPermissionEntity entity);

	/**
	 * 实体 → 详情 VO
	 * @param entity 权限实体
	 * @return 详情 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	@Mapping(target = "boundRoleCount", ignore = true)
	SysPermissionDetailVO toDetailVo(SysPermissionEntity entity);

	/**
	 * Entity → 分页行 VO
	 * @param entity 权限实体
	 * @return 分页行 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	SysPermissionPageVO toPageVo(SysPermissionEntity entity);

}
