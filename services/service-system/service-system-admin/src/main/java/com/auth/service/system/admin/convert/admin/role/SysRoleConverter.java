package com.auth.service.system.admin.convert.admin.role;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleForm;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
import com.auth.service.system.admin.model.vo.role.SysRoleOptionVO;
import com.auth.service.system.admin.model.vo.role.SysRolePageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 系统角色实体、表单与 VO 转换。
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface SysRoleConverter {

	SysRoleConverter INSTANCE = Mappers.getMapper(SysRoleConverter.class);

	/**
	 * 新增表单 → 实体
	 * @param form 角色新增表单
	 * @return 待持久化实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "version", ignore = true)
	SysRoleEntity toEntity(SysRoleForm form);

	/**
	 * 更新表单字段合并到已有实体（不覆盖主键与审计字段）。
	 * @param form 更新表单
	 * @param entity 已有实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "version", ignore = true)
	void applyUpdateForm(SysRoleForm form, @org.mapstruct.MappingTarget SysRoleEntity entity);

	/**
	 * 实体 → 详情 VO
	 * @param entity 角色实体
	 * @return 详情 VO
	 */
	@Mapping(target = "permissionCount", ignore = true)
	@Mapping(target = "menuCount", ignore = true)
	@Mapping(target = "grantUserCount", ignore = true)
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	SysRoleDetailVO toDetailVo(SysRoleEntity entity);

	/**
	 * 实体 → 分页行 VO
	 * @param entity 角色实体
	 * @return 分页行
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	SysRolePageVO toPageVo(SysRoleEntity entity);

	/**
	 * 实体列表 → 下拉选项列表
	 * @param entities 角色实体列表
	 * @return 下拉选项列表
	 */
	List<SysRoleOptionVO> toOptionVoList(List<SysRoleEntity> entities);

}
