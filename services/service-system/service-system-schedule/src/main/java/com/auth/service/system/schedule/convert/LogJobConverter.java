package com.auth.service.system.schedule.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.schedule.model.entity.LogJobEntity;
import com.auth.service.system.schedule.model.po.LogJobPageRowPO;
import com.auth.service.system.schedule.model.vo.LogJobDetailVO;
import com.auth.service.system.schedule.model.vo.LogJobPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 任务日志转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface LogJobConverter {

	LogJobConverter INSTANCE = Mappers.getMapper(LogJobConverter.class);

	/**
	 * 任务日志分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	LogJobPageVO toPageVO(LogJobPageRowPO po);

	/**
	 * 实体 → 详情 VO（实体 Boolean 与 API 语义一致，直接映射）
	 * @param entity 任务日志实体
	 * @return 详情 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	LogJobDetailVO toDetailVo(LogJobEntity entity);

}
