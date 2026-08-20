package com.auth.service.system.message.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.message.model.po.InAppInboxDetailRowPO;
import com.auth.service.system.message.model.po.InAppInboxPageRowPO;
import com.auth.service.system.message.model.vo.inapp.InAppInboxDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppInboxPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 用户侧站内信收件箱转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface InAppInboxConverter {

	InAppInboxConverter INSTANCE = Mappers.getMapper(InAppInboxConverter.class);

	/**
	 * 分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	InAppInboxPageVO toPageVO(InAppInboxPageRowPO po);

	/**
	 * 详情行 PO → 详情 VO（isRead / readTime）
	 * @param po 当前用户可见的详情投影
	 * @return 详情 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	InAppInboxDetailVO toDetailVO(InAppInboxDetailRowPO po);

}
