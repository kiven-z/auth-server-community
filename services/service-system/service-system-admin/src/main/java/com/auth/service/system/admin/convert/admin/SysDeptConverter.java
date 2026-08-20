package com.auth.service.system.admin.convert.admin;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptForm;
import com.auth.service.system.admin.model.po.dept.SysDeptPageRowPO;
import com.auth.service.system.admin.model.vo.dept.SysDeptDetailVO;
import com.auth.service.system.admin.model.vo.dept.SysDeptListVO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 部门实体与列表 VO 转换。
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface SysDeptConverter {

	SysDeptConverter INSTANCE = Mappers.getMapper(SysDeptConverter.class);

	/**
	 * 列表行 VO
	 * @param entity 部门实体
	 * @return 列表 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	@Mapping(target = "effective", ignore = true)
	SysDeptListVO toListVo(SysDeptEntity entity);

	/**
	 * 列表行 VO 批量
	 * @param entities 部门实体列表
	 * @return 列表 VO
	 */
	List<SysDeptListVO> toListVoList(List<SysDeptEntity> entities);

	/**
	 * 分页 PO → 列表行 VO
	 * @param row 分页持久层投影
	 * @return 列表 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	@BeanMapping(ignoreUnmappedSourceProperties = { "parentDeptCode", "parentDeptName" })
	SysDeptListVO toListVoFromPageRow(SysDeptPageRowPO row);

	/**
	 * 分页/列表 PO → 列表行 VO 批量
	 * @param rows 持久层投影列表
	 * @return 列表 VO
	 */
	List<SysDeptListVO> toListVoFromPageRowList(List<SysDeptPageRowPO> rows);

	/**
	 * 新增表单 → 实体
	 * @param form 新增表单
	 * @return 待持久化实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "version", ignore = true)
	SysDeptEntity toEntity(SysDeptForm form);

	/**
	 * 更新表单合并到已有实体
	 * @param form 更新表单
	 * @param entity 已有实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "parentId", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "version", ignore = true)
	void applyUpdateForm(SysDeptForm form, @org.mapstruct.MappingTarget SysDeptEntity entity);

	/**
	 * 实体 → 详情 VO
	 * @param entity 部门实体
	 * @return 详情 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	@Mapping(target = "effective", ignore = true)
	@Mapping(target = "boundUserCount", ignore = true)
	@Mapping(target = "boundPostCount", ignore = true)
	SysDeptDetailVO toDetailVo(SysDeptEntity entity);

}
