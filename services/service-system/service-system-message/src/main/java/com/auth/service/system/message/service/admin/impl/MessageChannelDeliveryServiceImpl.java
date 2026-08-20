package com.auth.service.system.message.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.message.convert.MessageChannelDeliveryConverter;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.MessageChannelDeliveryMapper;
import com.auth.service.system.message.model.entity.MessageChannelDeliveryEntity;
import com.auth.service.system.message.model.po.MessageChannelDeliveryPageRowPO;
import com.auth.service.system.message.model.query.MessageChannelDeliveryQuery;
import com.auth.service.system.message.model.vo.delivery.MessageChannelDeliveryDetailVO;
import com.auth.service.system.message.model.vo.delivery.MessageChannelDeliveryPageVO;
import com.auth.service.system.message.service.admin.MessageChannelDeliveryService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 渠道投递记录服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class MessageChannelDeliveryServiceImpl
		extends ServiceImpl<MessageChannelDeliveryMapper, MessageChannelDeliveryEntity>
		implements MessageChannelDeliveryService {

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<MessageChannelDeliveryPageVO> getChannelDeliveryPage(MessageChannelDeliveryQuery query) {
		Page<MessageChannelDeliveryEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<MessageChannelDeliveryPageRowPO> page = baseMapper.selectChannelDeliveryPage(pageParams, query);
		IPage<MessageChannelDeliveryPageVO> voPage = page.convert(MessageChannelDeliveryConverter.INSTANCE::toPageVO);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public MessageChannelDeliveryDetailVO getChannelDeliveryById(Long id) {
		MessageChannelDeliveryEntity entity = super.getById(id);
		if (entity == null) {
			throw new MessageException(DATA_NOT_EXIST);
		}
		MessageChannelDeliveryDetailVO vo = MessageChannelDeliveryConverter.INSTANCE.toDetailVo(entity);
		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(vo), null, null);
		return vo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void batchDelete(List<Long> ids) {
		if (CollUtil.isEmpty(ids)) {
			return;
		}
		baseMapper.deleteByIds(ids);
	}

}
