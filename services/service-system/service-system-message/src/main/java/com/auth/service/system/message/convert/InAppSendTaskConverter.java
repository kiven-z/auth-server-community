package com.auth.service.system.message.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.po.InAppSendTaskPageRowPO;
import com.auth.service.system.message.model.po.InAppSendTaskRecipientPageRowPO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskPageVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskRecipientPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 站内信发送任务转换器（读模型）
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface InAppSendTaskConverter {

	InAppSendTaskConverter INSTANCE = Mappers.getMapper(InAppSendTaskConverter.class);

	/**
	 * 分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	InAppSendTaskPageVO toPageVO(InAppSendTaskPageRowPO po);

	/**
	 * 实体 → 详情 VO（recipientScopeIds / includeChildren 由调用方按需回填）
	 * @param entity 发送任务实体
	 * @return 详情 VO
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	@Mapping(target = "recipientScopeIds", ignore = true)
	@Mapping(target = "includeChildren", ignore = true)
	@Mapping(target = "categoryName", ignore = true)
	InAppSendTaskDetailVO toDetailVo(InAppMessageEntity entity);

	/**
	 * 收件人分页行 PO → VO
	 * @param po 持久层投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	@Mapping(target = "username", ignore = true)
	InAppSendTaskRecipientPageVO toRecipientPageVO(InAppSendTaskRecipientPageRowPO po);

}
