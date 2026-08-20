package com.auth.service.system.schedule.controller;

import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.schedule.model.constants.ScheduleAuditBizModule;
import com.auth.service.system.schedule.model.form.SysJobCreateForm;
import com.auth.service.system.schedule.model.form.SysJobUpdateForm;
import com.auth.service.system.schedule.model.query.SysJobQuery;
import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobPageVO;
import com.auth.service.system.schedule.service.SysJobDefinitionService;
import com.auth.service.system.schedule.service.SysJobScheduleSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 定时任务
 *
 * @author Bunny
 */
@Tag(name = "定时任务", description = "定义 CRUD、启停与立即执行")
@RequestMapping("/api/system/job")
@RestController
public class SysJobController {

	private final SysJobDefinitionService sysJobDefinitionService;

	private final SysJobScheduleSyncService sysJobScheduleSyncService;

	public SysJobController(SysJobDefinitionService sysJobDefinitionService,
			SysJobScheduleSyncService sysJobScheduleSyncService) {
		this.sysJobDefinitionService = sysJobDefinitionService;
		this.sysJobScheduleSyncService = sysJobScheduleSyncService;
	}

	@Operation(summary = "分页查询定时任务")
	@PreAuthorize("@auth.decide('schedule:job:query')")
	@GetMapping
	public Result<PageResponse<SysJobPageVO>> page(SysJobQuery query) {
		PageResponse<SysJobPageVO> response = sysJobDefinitionService.getPage(query);
		return Result.success(response);
	}

	@Operation(summary = "查询定时任务详情")
	@PreAuthorize("@auth.decide('schedule:job:detail')")
	@GetMapping("{id}")
	public Result<SysJobDetailVO> detail(@PathVariable("id") Long id) {
		SysJobDetailVO detail = sysJobDefinitionService.getDetail(id);
		return Result.success(detail);
	}

	@Operation(summary = "新增定时任务")
	@PreAuthorize("@auth.decide('schedule:job:create')")
	@PostMapping
	public Result<String> create(@Validated(CreateGroup.class) @RequestBody SysJobCreateForm form) {
		sysJobDefinitionService.create(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "JOB", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = ScheduleAuditBizModule.SYS_JOB, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新定时任务")
	@PreAuthorize("@auth.decide('schedule:job:update')")
	@PutMapping
	public Result<String> update(@Validated(UpdateGroup.class) @RequestBody SysJobUpdateForm form) {
		sysJobDefinitionService.update(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "JOB", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = ScheduleAuditBizModule.SYS_JOB, operation = OperationLogKind.DELETE)
	@Operation(summary = "删除定时任务")
	@PreAuthorize("@auth.decide('schedule:job:delete')")
	@DeleteMapping("{id}")
	public Result<String> delete(@PathVariable("id") Long id) {
		sysJobDefinitionService.deleteById(id);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "JOB", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = ScheduleAuditBizModule.SYS_JOB, operation = OperationLogKind.UPDATE)
	@Operation(summary = "批量启停定时任务")
	@PreAuthorize("@auth.decide('schedule:job:update')")
	@PutMapping("status")
	public Result<String> batchStatus(@Validated @RequestBody IdsEnableStatusForm form) {
		sysJobScheduleSyncService.batchUpdateStatus(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@Operation(summary = "立即执行定时任务")
	@PreAuthorize("@auth.decide('schedule:job:update')")
	@PostMapping("{id}/run")
	public Result<String> runOnce(@PathVariable("id") Long id) {
		sysJobDefinitionService.runOnce(id);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
