package com.auth.service.system.message.mapper;

import com.auth.service.system.message.model.entity.MessageChannelDeliveryEntity;
import com.auth.service.system.message.model.po.MessageChannelDeliveryPageRowPO;
import com.auth.service.system.message.model.query.MessageChannelDeliveryQuery;
import com.auth.service.system.message.model.value.delivery.ChannelDeliveryResultUpdate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 渠道消息投递记录 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface MessageChannelDeliveryMapper extends BaseMapper<MessageChannelDeliveryEntity> {

	/**
	 * 分页查询渠道投递记录
	 * @param pageParams 分页参数
	 * @param query 查询条件
	 * @return 分页行
	 */
	IPage<MessageChannelDeliveryPageRowPO> selectChannelDeliveryPage(
			@Param("page") Page<MessageChannelDeliveryEntity> pageParams,
			@Param("query") MessageChannelDeliveryQuery query);

	/**
	 * 按目标逐条回写投递结果
	 * @param taskId 任务 ID
	 * @param channel 逻辑渠道
	 * @param list 每个目标一条更新参数
	 * @return 影响行数
	 */
	int batchUpdateResult(@Param("taskId") Long taskId, @Param("channel") String channel,
			@Param("list") List<ChannelDeliveryResultUpdate> list);

}
