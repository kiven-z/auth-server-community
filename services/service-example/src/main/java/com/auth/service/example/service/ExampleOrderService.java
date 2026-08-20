package com.auth.service.example.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.example.model.query.ExampleOrderQuery;
import com.auth.service.example.model.vo.ExampleOrderVO;

/**
 * 数据权限演示单服务
 *
 * @author Bunny
 */
public interface ExampleOrderService {

	/**
	 * 分页查询当前用户可见的演示单
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<ExampleOrderVO> getPage(ExampleOrderQuery query);

}
