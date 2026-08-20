package com.auth.service.system.admin.convert.admin.log;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.system.admin.model.po.loglogin.LogLoginLogPageRowPO;
import com.auth.service.system.admin.model.vo.loglogin.LogLoginLogDetailVO;
import com.auth.service.system.admin.model.vo.loglogin.LogLoginLogPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 登录日志转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface LogLoginLogConverter {

	LogLoginLogConverter INSTANCE = Mappers.getMapper(LogLoginLogConverter.class);

	/**
	 * 登录日志分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	LogLoginLogPageVO toPageVO(LogLoginLogPageRowPO po);

	/**
	 * 实体 → 详情 VO
	 * @param entity 登录日志实体
	 * @return 详情 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	LogLoginLogDetailVO toDetailVo(LoginLogEntity entity);

}
