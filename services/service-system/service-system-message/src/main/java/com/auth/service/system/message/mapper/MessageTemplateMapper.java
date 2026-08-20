package com.auth.service.system.message.mapper;

import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.po.MessageTemplatePageRowPO;
import com.auth.service.system.message.model.query.MessageTemplateQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 统一消息模板表 Mapper（message_template）
 *
 * @author Bunny
 */
@Mapper
public interface MessageTemplateMapper extends BaseMapper<MessageTemplateEntity> {

	/**
	 * 按场景编码与渠道查询当前启用的最高优先级模板行
	 * @param sceneCode 场景编码（发送命令 templateCode）
	 * @param channel 渠道名
	 * @return 模板行，未找到时为 null
	 */
	MessageTemplateEntity selectEnabledBySceneCodeAndChannel(@Param("sceneCode") String sceneCode,
			@Param("channel") String channel);

	/**
	 * 消息模板分页列表（channel 由查询条件动态传入）
	 * @param pageParams 分页参数
	 * @param query 查询条件（含必填 channel）
	 * @return 分页结果
	 */
	IPage<MessageTemplatePageRowPO> selectMessageTemplatePage(@Param("page") Page<MessageTemplateEntity> pageParams,
			@Param("query") MessageTemplateQuery query);

}
