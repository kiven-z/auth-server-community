package com.auth.service.system.message.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.message.model.entity.InAppMessageCategoryEntity;
import com.auth.service.system.message.model.form.inapp.InAppMessageCategoryForm;
import com.auth.service.system.message.model.po.InAppMessageCategoryDetailRowPO;
import com.auth.service.system.message.model.po.InAppMessageCategoryPageRowPO;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryPageVO;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 站内信业务分类转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface InAppMessageCategoryConverter {

	InAppMessageCategoryConverter INSTANCE = Mappers.getMapper(InAppMessageCategoryConverter.class);

	/**
	 * 新增表单 → 实体
	 * @param form 新增表单
	 * @return 分类实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "sortOrder", source = "sortOrder", defaultValue = "0")
	InAppMessageCategoryEntity toEntity(InAppMessageCategoryForm form);

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
	@Mapping(target = "sortOrder", source = "sortOrder", defaultValue = "0")
	void applyUpdateForm(InAppMessageCategoryForm form, @MappingTarget InAppMessageCategoryEntity entity);

	/**
	 * 详情行 PO → 详情 VO（父级字段已由 SQL 左联带出）
	 * @param po 详情投影
	 * @return 详情 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	InAppMessageCategoryDetailVO toDetailVo(InAppMessageCategoryDetailRowPO po);

	/**
	 * 列表行 PO → 返回 VO
	 * @param po 持久层查询投影
	 * @return 返回对象
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	InAppMessageCategoryPageVO toPageVO(InAppMessageCategoryPageRowPO po);

	/**
	 * 列表行 → VO 列表
	 * @param rows 持久层投影列表
	 * @return VO 列表
	 */
	List<InAppMessageCategoryPageVO> toPageVOList(List<InAppMessageCategoryPageRowPO> rows);

	/**
	 * 实体 → 扁平选项 VO（无 children）
	 * @param entity 分类实体
	 * @return 选项 VO
	 */
	default InAppMessageCategoryVO toFlatVo(InAppMessageCategoryEntity entity) {
		return InAppMessageCategoryVO.builder()
			.id(entity.getId())
			.code(entity.getCode())
			.name(entity.getName())
			.sortOrder(entity.getSortOrder())
			.children(null)
			.build();
	}

}
