package com.auth.service.system.admin.service.admin;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.LogUserPasswordHistoryEntity;
import com.auth.service.system.admin.model.query.log.LogUserPasswordHistoryQuery;
import com.auth.service.system.admin.model.vo.loguserpasswordhistory.LogUserPasswordHistoryDetailVO;
import com.auth.service.system.admin.model.vo.loguserpasswordhistory.LogUserPasswordHistoryPageVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 密码历史日志服务
 *
 * @author Bunny
 */
public interface LogUserPasswordHistoryService extends IService<LogUserPasswordHistoryEntity> {

	/**
	 * 分页查询密码历史日志
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<LogUserPasswordHistoryPageVO> getPage(LogUserPasswordHistoryQuery query);

	/**
	 * 获取密码历史详情
	 * @param id 主键
	 * @return 详情
	 */
	LogUserPasswordHistoryDetailVO getDetail(Long id);

	/**
	 * 记录一次密码变更
	 * @param userId 用户主键
	 * @param passwordHash 加密后的密码
	 * @param changeIp 客户端 IP
	 */
	void recordChange(Long userId, String passwordHash, String changeIp);

}
