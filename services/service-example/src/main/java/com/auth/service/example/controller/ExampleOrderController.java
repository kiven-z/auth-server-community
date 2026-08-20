package com.auth.service.example.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.service.example.model.query.ExampleOrderQuery;
import com.auth.service.example.model.vo.ExampleOrderVO;
import com.auth.service.example.service.ExampleOrderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 数据权限演示单接口
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@RequestMapping("/api/example/orders")
@RestController
public class ExampleOrderController {

	private final ExampleOrderService exampleOrderService;

	@AuthenticatedApi
	@Operation(summary = "分页查询数据权限演示单", description = "按登录画像 deptScope 行级过滤")
	@GetMapping
	public Result<PageResponse<ExampleOrderVO>> page(@Validated ExampleOrderQuery query) {
		PageResponse<ExampleOrderVO> response = exampleOrderService.getPage(query);
		return Result.success(response);
	}

}
