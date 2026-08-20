package com.auth.service.system.admin.service.admin;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.system.admin.model.query.log.LogLoginLogQuery;
import com.auth.service.system.admin.model.vo.loglogin.LogLoginLogDetailVO;
import com.auth.service.system.admin.model.vo.loglogin.LogLoginLogPageVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 登录日志服务
 *
 * @author Bunny
 */
public interface LogLoginService extends IService<LoginLogEntity> {

	/**
	 * 分页查询登录日志
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<LogLoginLogPageVO> getPage(LogLoginLogQuery query);

	/**
	 * 获取登录日志详情
	 * @param id 主键
	 * @return 详情
	 */
	LogLoginLogDetailVO getDetail(Long id);

}
