package com.auth.service.system.schedule.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.schedule.model.constants.ScheduleAuditBizModule;
import com.auth.service.system.schedule.model.form.SysJobGroupForm;
import com.auth.service.system.schedule.model.form.SysJobGroupUpdateForm;
import com.auth.service.system.schedule.model.query.SysJobGroupQuery;
import com.auth.service.system.schedule.model.vo.SysJobGroupDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobGroupPageVO;
import com.auth.service.system.schedule.service.SysJobGroupService;
import com.auth.service.system.schedule.service.SysJobScheduleSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 定时任务-分组
 *
 * @author Bunny
 */
@Tag(name = "定时任务-分组", description = "任务分组 CRUD 与启停")
@RequestMapping("/api/system/job-group")
@RestController
public class SysJobGroupController {

	private final SysJobGroupService sysJobGroupService;

	private final SysJobScheduleSyncService sysJobScheduleSyncService;

	public SysJobGroupController(SysJobGroupService sysJobGroupService,
			SysJobScheduleSyncService sysJobScheduleSyncService) {
		this.sysJobGroupService = sysJobGroupService;
		this.sysJobScheduleSyncService = sysJobScheduleSyncService;
	}

	@Operation(summary = "分页查询任务分组")
	@PreAuthorize("@auth.decide('schedule:jobgroup:query')")
	@GetMapping
	public Result<PageResponse<SysJobGroupPageVO>> page(SysJobGroupQuery query) {
		PageResponse<SysJobGroupPageVO> response = sysJobGroupService.getPage(query);
		return Result.success(response);
	}

	@Operation(summary = "搜索启用任务分组")
	@PreAuthorize("@auth.decide('schedule:jobgroup:query')")
	@GetMapping("options")
	public Result<List<SysJobGroupPageVO>> options(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "limit", defaultValue = "20") int limit) {
		int max = Math.max(limit, 1);
		limit = Math.min(max, 60);
		List<SysJobGroupPageVO> responses = sysJobGroupService.listEnabledOptions(keyword, limit);

		return Result.success(responses);
	}

	@Operation(summary = "查询任务分组详情")
	@PreAuthorize("@auth.decide('schedule:jobgroup:query')")
	@GetMapping("{id}")
	public Result<SysJobGroupDetailVO> detail(@PathVariable("id") Long id) {
		SysJobGroupDetailVO detail = sysJobGroupService.getDetail(id);

		return Result.success(detail);
	}

	@Operation(summary = "新增任务分组")
	@PreAuthorize("@auth.decide('schedule:jobgroup:create')")
	@PostMapping
	public Result<String> create(@Validated(CreateGroup.class) @RequestBody SysJobGroupForm form) {
		sysJobGroupService.create(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "JOB_GROUP", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = ScheduleAuditBizModule.SYS_JOB_GROUP, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新任务分组")
	@PreAuthorize("@auth.decide('schedule:jobgroup:update')")
	@PutMapping
	public Result<String> update(@Validated(UpdateGroup.class) @RequestBody SysJobGroupUpdateForm form) {
		sysJobGroupService.update(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "JOB_GROUP", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = ScheduleAuditBizModule.SYS_JOB_GROUP, operation = OperationLogKind.DELETE)
	@Operation(summary = "删除任务分组")
	@PreAuthorize("@auth.decide('schedule:jobgroup:delete')")
	@DeleteMapping("{id}")
	public Result<String> delete(@PathVariable("id") Long id) {
		sysJobGroupService.deleteById(id);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "JOB_GROUP", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = ScheduleAuditBizModule.SYS_JOB_GROUP, operation = OperationLogKind.UPDATE)
	@Operation(summary = "批量启停分组下全部任务")
	@PreAuthorize("@auth.decide('schedule:job:update')")
	@PutMapping("{groupCode}/jobs/status")
	public Result<String> updateAllJobsStatus(@PathVariable("groupCode") String groupCode,
			@Parameter(description = "运行状态(true=正常调度，false=暂停)") @RequestParam Boolean status) {
		Assert.notNull(status, "status must not be null");
		sysJobScheduleSyncService.batchUpdateStatusByGroupCode(groupCode, status);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
