package com.auth.service.system.message.service.admin;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.message.model.entity.MessageChannelDeliveryEntity;
import com.auth.service.system.message.model.query.MessageChannelDeliveryQuery;
import com.auth.service.system.message.model.vo.delivery.MessageChannelDeliveryDetailVO;
import com.auth.service.system.message.model.vo.delivery.MessageChannelDeliveryPageVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 渠道投递记录服务
 *
 * @author Bunny
 */
public interface MessageChannelDeliveryService extends IService<MessageChannelDeliveryEntity> {

	/**
	 * 分页查询渠道投递记录
	 * @param query 查询条件
	 * @return 投递记录分页数据
	 */
	PageResponse<MessageChannelDeliveryPageVO> getChannelDeliveryPage(MessageChannelDeliveryQuery query);

	/**
	 * 按主键查询渠道投递记录详情
	 * @param id 投递记录主键
	 * @return 投递记录详情
	 */
	MessageChannelDeliveryDetailVO getChannelDeliveryById(Long id);

	/**
	 * 按主键批量删除渠道投递记录
	 * @param ids 投递记录主键列表
	 */
	void batchDelete(List<Long> ids);

}
