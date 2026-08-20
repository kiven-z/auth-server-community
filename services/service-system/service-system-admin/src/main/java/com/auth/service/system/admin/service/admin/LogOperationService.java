package com.auth.service.system.admin.service.admin;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.LogOperationEntity;
import com.auth.service.system.admin.model.query.log.LogOperationQuery;
import com.auth.service.system.admin.model.vo.logoperation.LogOperationDetailVO;
import com.auth.service.system.admin.model.vo.logoperation.LogOperationPageVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 操作日志服务
 *
 * @author Bunny
 */
public interface LogOperationService extends IService<LogOperationEntity> {

	/**
	 * 分页查询操作日志
	 * @param query 筛选条件
	 * @return 分页数据
	 */
	PageResponse<LogOperationPageVO> getPage(LogOperationQuery query);

	/**
	 * 获取操作日志详情
	 * @param id 主键
	 * @return 详情
	 */
	LogOperationDetailVO getDetail(Long id);

}
