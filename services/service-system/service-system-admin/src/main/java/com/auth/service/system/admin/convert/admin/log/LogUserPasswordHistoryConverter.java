package com.auth.service.system.admin.convert.admin.log;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.po.loguserpasswordhistory.LogUserPasswordHistoryDetailRowPO;
import com.auth.service.system.admin.model.po.loguserpasswordhistory.LogUserPasswordHistoryPageRowPO;
import com.auth.service.system.admin.model.vo.loguserpasswordhistory.LogUserPasswordHistoryDetailVO;
import com.auth.service.system.admin.model.vo.loguserpasswordhistory.LogUserPasswordHistoryPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 密码历史日志转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface LogUserPasswordHistoryConverter {

	LogUserPasswordHistoryConverter INSTANCE = Mappers.getMapper(LogUserPasswordHistoryConverter.class);

	/**
	 * 密码历史日志分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	@Mapping(target = "changeTime", source = "createdAt")
	LogUserPasswordHistoryPageVO toPageVO(LogUserPasswordHistoryPageRowPO po);

	/**
	 * 详情 PO → 详情 VO
	 * @param po 持久层详情投影
	 * @return 详情 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	LogUserPasswordHistoryDetailVO toDetailVo(LogUserPasswordHistoryDetailRowPO po);

}
