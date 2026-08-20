package com.auth.service.system.admin.convert.admin;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.po.post.SysPostSearchItemPO;
import com.auth.service.system.admin.model.vo.post.SysPostDetailVO;
import com.auth.service.system.admin.model.vo.post.SysPostPageVO;
import com.auth.service.system.admin.model.vo.post.SysPostSearchItemVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 岗位转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface SysPostConverter {

	SysPostConverter INSTANCE = Mappers.getMapper(SysPostConverter.class);

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
	SysPostEntity toEntity(SysPostForm form);

	/**
	 * 表单字段合并到已有实体（更新；不覆盖主键与审计字段）
	 * @param form 表单
	 * @param entity 已有实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "version", ignore = true)
	void applyUpdateForm(SysPostForm form, @org.mapstruct.MappingTarget SysPostEntity entity);

	/**
	 * 分页行 PO → 分页 VO
	 * @param po 持久层投影
	 * @return 分页行
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	SysPostPageVO toPageVo(SysPostPageRowPO po);

	/**
	 * 实体 → 详情 VO
	 * @param entity 岗位实体
	 * @return 详情 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	@Mapping(target = "effective", ignore = true)
	@Mapping(target = "boundDept", ignore = true)
	@Mapping(target = "boundUserCount", ignore = true)
	SysPostDetailVO toDetailVo(SysPostEntity entity);

	/**
	 * 搜索行 PO 列表 → 搜索项 VO 列表
	 * @param poList 持久层投影列表
	 * @return 搜索项 VO 列表
	 */
	List<SysPostSearchItemVO> toSearchItemVoList(List<SysPostSearchItemPO> poList);

}
