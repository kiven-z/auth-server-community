package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignRoleForm;
import com.auth.service.system.admin.model.form.menu.SysMenuMoveForm;
import com.auth.service.system.admin.model.form.menu.SysMenuSaveForm;
import com.auth.service.system.admin.model.query.menu.SysMenuQuery;
import com.auth.service.system.admin.model.vo.menu.RouteNodeVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;
import com.auth.service.system.admin.model.vo.reference.ext.MenuAssignedRoleReferenceVO;
import com.auth.service.system.admin.service.admin.SysMenuService;
import com.auth.service.system.authorization.model.constants.AuthorizationAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 系统菜单
 *
 * @author Bunny
 */
@Tag(name = "系统菜单", description = "动态路由、CRUD、角色分配")
@RequestMapping("/api/system/menu")
@RestController
public class SysMenuController {

	private final SysMenuService sysMenuService;

	public SysMenuController(SysMenuService sysMenuService) {
		this.sysMenuService = sysMenuService;
	}

	@AuthenticatedApi
	@Operation(summary = "查询动态路由列表", description = "返回当前用户可见路由树；子级有角色即可，祖先无绑定时由后端补壳")
	@GetMapping("/web-routes")
	public Result<List<RouteNodeVO>> getWebRoutes() {
		Long userId = SecurityUserUtils.getUserId();
		List<RouteNodeVO> menuList = sysMenuService.listWebRoutes(userId);
		return Result.success(menuList);
	}

	@Operation(summary = "查询菜单扁平列表", description = "全量返回，由前端建树")
	@PreAuthorize("@auth.decide('sys:menu:query')")
	@GetMapping("/list")
	public Result<List<SysMenuListVO>> list(SysMenuQuery query) {
		List<SysMenuListVO> data = sysMenuService.listFlat(query);
		return Result.success(data);
	}

	@Operation(summary = "分页查询菜单", description = "表格视图")
	@PreAuthorize("@auth.decide('sys:menu:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysMenuListVO>> page(SysMenuQuery query) {
		PageResponse<SysMenuListVO> response = sysMenuService.pageFlat(query);
		return Result.success(response);
	}

	@Operation(summary = "查询菜单详情")
	@PreAuthorize("@auth.decide('sys:menu:query')")
	@GetMapping("/{id}")
	public Result<SysMenuDetailVO> detail(@PathVariable("id") Long id) {
		SysMenuDetailVO detail = sysMenuService.getDetail(id);
		return Result.success(detail);
	}

	@Operation(summary = "查询菜单已分配角色")
	@PreAuthorize("@auth.decide('sys:menu:query')")
	@GetMapping("/{id}/roles")
	public Result<List<MenuAssignedRoleReferenceVO>> listRoles(@PathVariable("id") Long id) {
		List<MenuAssignedRoleReferenceVO> data = sysMenuService.listAssignedRoles(id);
		return Result.success(data);
	}

	@OperationLog(targetType = "MENU", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.SYS_MENU, operation = OperationLogKind.CREATE)
	@Operation(summary = "新增菜单")
	@PreAuthorize("@auth.decide('sys:menu:create')")
	@PostMapping
	public Result<String> create(@Valid @RequestBody SysMenuSaveForm form) {
		Long id = sysMenuService.create(form);
		return Result.success(String.valueOf(id), OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "MENU", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.SYS_MENU, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新菜单")
	@PreAuthorize("@auth.decide('sys:menu:update')")
	@PutMapping
	public Result<String> update(@Validated(UpdateGroup.class) @RequestBody SysMenuSaveForm form) {
		sysMenuService.update(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "MENU", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.SYS_MENU, operation = OperationLogKind.DELETE)
	@Operation(summary = "批量删除菜单")
	@PreAuthorize("@auth.decide('sys:menu:delete')")
	@DeleteMapping
	public Result<String> remove(@RequestBody List<Long> ids) {
		sysMenuService.deleteByIds(ids);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "MENU", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.SYS_MENU, operation = OperationLogKind.UPDATE)
	@Operation(summary = "移动菜单")
	@PreAuthorize("@auth.decide('sys:menu:update')")
	@PutMapping("/{id}/move")
	public Result<String> move(@PathVariable("id") Long id, @Valid @RequestBody SysMenuMoveForm form) {
		sysMenuService.move(id, form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "MENU", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.SYS_MENU, operation = OperationLogKind.UPDATE)
	@Operation(summary = "批量启停菜单")
	@PreAuthorize("@auth.decide('sys:menu:update')")
	@PutMapping("/status")
	public Result<String> batchUpdateStatus(@Valid @RequestBody IdsEnableStatusForm form) {
		sysMenuService.batchUpdateStatus(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "MENU_ROLE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.SYS_MENU_ROLE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "覆盖分配菜单角色")
	@PreAuthorize("@auth.decide('sys:menu:update')")
	@PutMapping("/{id}/roles")
	public Result<String> replaceRoles(@PathVariable("id") Long id, @Valid @RequestBody SysMenuAssignRoleForm form) {
		sysMenuService.replaceMenuRoles(id, form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
