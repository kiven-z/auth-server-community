package com.auth.service.system.schedule.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.schedule.model.entity.JobEntity;
import com.auth.service.system.schedule.model.form.SysJobCreateForm;
import com.auth.service.system.schedule.model.form.SysJobUpdateForm;
import com.auth.service.system.schedule.model.po.SysJobDetailRowPO;
import com.auth.service.system.schedule.model.po.SysJobPageRowPO;
import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * 定时任务
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface SysJobConverter {

	SysJobConverter INSTANCE = Mappers.getMapper(SysJobConverter.class);

	/**
	 * 分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	@Mapping(target = "lastExecutionStatus", ignore = true)
	@Mapping(target = "lastExecutionTime", ignore = true)
	SysJobPageVO toPageVO(SysJobPageRowPO po);

	/**
	 * 详情行 PO → 详情 VO（运行时字段在 Service 层补充）
	 * @param po 持久层查询投影
	 * @return 详情 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	@Mapping(target = "previousFireTime", ignore = true)
	@Mapping(target = "nextFireTime", ignore = true)
	@Mapping(target = "quartzRuntimeStatus", ignore = true)
	@Mapping(target = "quartzFireTime", ignore = true)
	@Mapping(target = "lastExecutionStatus", ignore = true)
	@Mapping(target = "lastExecutionTime", ignore = true)
	SysJobDetailVO toDetailVo(SysJobDetailRowPO po);

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
	JobEntity toEntity(SysJobCreateForm form);

	/**
	 * 更新表单 → 覆盖已有实体（禁止改 JobKey 三要素；invokeTarget 仅非 null 时覆盖）
	 * @param form 更新表单
	 * @param entity 数据库已有实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "jobName", ignore = true)
	@Mapping(target = "jobGroup", ignore = true)
	@Mapping(target = "jobClass", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "invokeTarget", source = "invokeTarget",
			nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateEntity(SysJobUpdateForm form, @MappingTarget JobEntity entity);

}
