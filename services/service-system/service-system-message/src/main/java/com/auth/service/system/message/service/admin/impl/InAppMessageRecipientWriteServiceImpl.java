package com.auth.service.system.message.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.constants.BatchSizes;
import com.auth.service.system.message.mapper.InAppMessageRecipientMapper;
import com.auth.service.system.message.model.entity.InAppMessageRecipientEntity;
import com.auth.service.system.message.service.admin.InAppMessageRecipientWriteService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 站内信收件箱写入服务实现
 *
 * @author Bunny
 */
@Service
public class InAppMessageRecipientWriteServiceImpl
		extends ServiceImpl<InAppMessageRecipientMapper, InAppMessageRecipientEntity>
		implements InAppMessageRecipientWriteService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public int insertBatch(List<InAppMessageRecipientEntity> rows) {
		if (CollUtil.isEmpty(rows)) {
			return 0;
		}
		for (InAppMessageRecipientEntity row : rows) {
			row.setId(IdWorker.getId());
			row.setVersion(0L);
		}

		int inserted = 0;
		for (List<InAppMessageRecipientEntity> partition : CollUtil.split(rows, BatchSizes.SIZE_500)) {
			inserted += baseMapper.insertIgnoreBatch(partition);
		}
		return inserted;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int countByMessageId(Long messageId) {
		if (messageId == null) {
			return 0;
		}
		long count = super.count(Wrappers.<InAppMessageRecipientEntity>lambdaQuery()
			.eq(InAppMessageRecipientEntity::getMessageId, messageId));
		return Math.toIntExact(count);
	}

}
