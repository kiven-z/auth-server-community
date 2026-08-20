package com.auth.service.system.message.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.message.model.entity.MessageChannelDeliveryEntity;
import com.auth.service.system.message.model.po.MessageChannelDeliveryPageRowPO;
import com.auth.service.system.message.model.vo.delivery.MessageChannelDeliveryDetailVO;
import com.auth.service.system.message.model.vo.delivery.MessageChannelDeliveryPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 渠道投递记录转换器（读模型）
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface MessageChannelDeliveryConverter {

	MessageChannelDeliveryConverter INSTANCE = Mappers.getMapper(MessageChannelDeliveryConverter.class);

	/**
	 * 分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	MessageChannelDeliveryPageVO toPageVO(MessageChannelDeliveryPageRowPO po);

	/**
	 * 实体 → 详情 VO
	 * @param entity 投递记录实体
	 * @return 详情 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	MessageChannelDeliveryDetailVO toDetailVo(MessageChannelDeliveryEntity entity);

}
