package com.auth.service.system.message.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.po.MessageTemplatePageRowPO;
import com.auth.service.system.message.model.vo.template.MessageTemplateDetailVO;
import com.auth.service.system.message.model.vo.template.MessageTemplatePageVO;
import com.auth.service.system.message.model.vo.template.MessageTemplateRequireFieldRow;
import com.auth.service.system.message.support.template.ChannelDefaultsJsonSupport;
import com.auth.service.system.message.support.template.ChannelDefaultsJsonSupport.InAppChannelDefaults;
import com.auth.service.system.message.support.template.MessageTemplateRequireFieldsJsonSupport;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 消息模板转换器（读模型）
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface MessageTemplateConverter {

	MessageTemplateConverter INSTANCE = Mappers.getMapper(MessageTemplateConverter.class);

	/**
	 * 数据库 require_fields JSON → 变量声明列表
	 * @param json JSON 字符串
	 * @return 变量声明列表
	 */
	@Named("parseRequireFields")
	default List<MessageTemplateRequireFieldRow> parseRequireFields(String json) {
		return MessageTemplateRequireFieldsJsonSupport.parse(json);
	}

	/**
	 * 分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	MessageTemplatePageVO toPageVO(MessageTemplatePageRowPO po);

	/**
	 * 实体 → 详情 VO
	 * @param entity 模板实体
	 * @return 详情 VO
	 */
	@Mapping(target = "templateCode", source = "sceneCode")
	@Mapping(target = "content", source = "bodyContent")
	@Mapping(target = "previewSubject", ignore = true)
	@Mapping(target = "previewContent", ignore = true)
	@Mapping(target = "contentType", ignore = true)
	@Mapping(target = "requireFields", source = "requireFields", qualifiedByName = "parseRequireFields")
	@Mapping(target = "categoryId", ignore = true)
	@Mapping(target = "linkUrl", ignore = true)
	@Mapping(target = "createdByName", ignore = true)
	@Mapping(target = "updatedByName", ignore = true)
	MessageTemplateDetailVO toDetailVo(MessageTemplateEntity entity);

	/**
	 * 从 channel_defaults_json 回填站内信默认小类与跳转
	 * @param entity 模板实体
	 * @param vo 详情 VO
	 */
	@AfterMapping
	default void fillInAppChannelDefaults(MessageTemplateEntity entity, @MappingTarget MessageTemplateDetailVO vo) {
		InAppChannelDefaults defaults = ChannelDefaultsJsonSupport.parseInApp(entity.getChannelDefaultsJson());
		if (defaults == null) {
			return;
		}
		vo.setCategoryId(defaults.getCategoryId());
		vo.setLinkUrl(defaults.getLinkUrl());
	}

}
