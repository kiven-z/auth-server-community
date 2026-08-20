package com.auth.service.system.schedule.controller;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.schedule.model.vo.QuartzTaskClassVO;
import com.auth.service.system.schedule.model.vo.QuartzTaskMethodVO;
import com.auth.service.system.schedule.service.SysJobTaskCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 定时任务-目录
 *
 * @author Bunny
 */
@Tag(name = "定时任务-目录", description = "白名单类与可调用方法")
@RequestMapping("/api/system/job/catalog")
@RestController
public class SysJobTaskCatalogController {

	private final SysJobTaskCatalogService sysJobTaskCatalogService;

	public SysJobTaskCatalogController(SysJobTaskCatalogService sysJobTaskCatalogService) {
		this.sysJobTaskCatalogService = sysJobTaskCatalogService;
	}

	@Operation(summary = "查询任务白名单类列表")
	@PreAuthorize("@auth.decide('schedule:job:query')")
	@GetMapping("classes")
	public Result<List<QuartzTaskClassVO>> classes() {
		List<QuartzTaskClassVO> data = sysJobTaskCatalogService.listQuartzClasses();
		return Result.success(data);
	}

	@Operation(summary = "查询任务白名单方法列表")
	@PreAuthorize("@auth.decide('schedule:job:query')")
	@GetMapping("methods")
	public Result<List<QuartzTaskMethodVO>> methods(@RequestParam("className") String className) {
		List<QuartzTaskMethodVO> data = sysJobTaskCatalogService.listCallableMethods(className);
		return Result.success(data);
	}

}
