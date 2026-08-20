package com.auth.service.system.admin.convert.admin.user;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import com.auth.service.system.admin.model.po.user.UserSearchItemPO;
import com.auth.service.system.admin.model.vo.user.SysUserDetailVO;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import com.auth.service.system.admin.model.vo.user.SysUserProfileVO;
import com.auth.service.system.admin.model.vo.user.SysUserSearchItemVO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用户域 MapStruct 转换器（分页、表单、档案、详情、搜索）。
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface SysUserConverter {

	SysUserConverter INSTANCE = Mappers.getMapper(SysUserConverter.class);

	/**
	 * 分页行 PO → 分页 VO
	 * @param po 持久层投影
	 * @return 分页行
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	SysUserPageVO toPageVo(SysUserPageRowPO po);

	/**
	 * 新增表单 → 实体
	 * @param form 表单
	 * @return 实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "avatar", ignore = true)
	@Mapping(target = "age", ignore = true)
	@Mapping(target = "permVersion", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "version", ignore = true)
	UserEntity toEntity(SysUserForm form);

	/**
	 * 更新表单字段合并到已有实体
	 * @param form 更新表单
	 * @param entity 已有实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "avatar", ignore = true)
	@Mapping(target = "age", ignore = true)
	@Mapping(target = "permVersion", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "version", ignore = true)
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void applyUpdateForm(SysUserForm form, @org.mapstruct.MappingTarget UserEntity entity);

	/**
	 * 将用户实体转换为档案基本信息 VO
	 * @param user 用户实体
	 * @return 档案 VO
	 */
	@Mapping(target = "deptCount", ignore = true)
	@Mapping(target = "postCount", ignore = true)
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	SysUserProfileVO toProfileVo(UserEntity user);

	/**
	 * 将用户搜索档案列表转换为 VO 列表
	 * @param list 用户搜索档案列表
	 * @return 用户搜索档案 VO 列表
	 */
	List<SysUserSearchItemVO> toSearchItemVoList(List<UserSearchItemPO> list);

	/**
	 * 档案 VO → 详情 VO（授权计数由 Service 层补全）
	 * @param profile 用户档案
	 * @return 详情 VO
	 */
	@Mapping(target = "directRoleCount", ignore = true)
	@Mapping(target = "effectiveRoleCount", ignore = true)
	@Mapping(target = "effectivePermissionCount", ignore = true)
	SysUserDetailVO fromProfile(SysUserProfileVO profile);

}
