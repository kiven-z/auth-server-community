package com.auth.service.system.message.service.admin;

import com.auth.service.system.message.model.entity.InAppMessageRecipientEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 站内信收件箱写入服务
 *
 * @author Bunny
 */
public interface InAppMessageRecipientWriteService extends IService<InAppMessageRecipientEntity> {

	/**
	 * 批量写入收件箱
	 * @param rows 收件箱行
	 * @return 实际插入行数
	 */
	int insertBatch(List<InAppMessageRecipientEntity> rows);

	/**
	 * 统计站内信已投递行数
	 * @param messageId 站内信 ID
	 * @return 行数
	 */
	int countByMessageId(Long messageId);

}
