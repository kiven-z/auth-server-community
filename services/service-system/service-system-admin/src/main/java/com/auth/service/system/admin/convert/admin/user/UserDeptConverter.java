package com.auth.service.system.admin.convert.admin.user;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.entity.UserDeptEntity;
import com.auth.service.system.admin.model.form.user.UserDeptAssignForm;
import com.auth.service.system.admin.model.po.user.UserDeptPageRowPO;
import com.auth.service.system.admin.model.vo.user.UserDeptPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 用户部门关联转换
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface UserDeptConverter {

	/**
	 * 单例
	 */
	UserDeptConverter INSTANCE = Mappers.getMapper(UserDeptConverter.class);

	/**
	 * 分页行 PO 转 VO
	 * @param row 分页行
	 * @return VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	UserDeptPageVO toPageVo(UserDeptPageRowPO row);

	/**
	 * 表单转实体（userId 由服务层填充）
	 * @param form 关联表单
	 * @return 待持久化实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "userId", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "version", ignore = true)
	UserDeptEntity toEntity(UserDeptAssignForm form);

	/**
	 * 表单字段合并到已有实体（更新；不覆盖主键与审计字段）
	 * @param form 关联表单
	 * @param entity 已有实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "userId", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "version", ignore = true)
	void applyUpdateForm(UserDeptAssignForm form, @org.mapstruct.MappingTarget UserDeptEntity entity);

}
