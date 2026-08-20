package com.auth.service.system.message.controller.admin;

import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.common.core.model.response.Result;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.service.system.message.model.form.inapp.InAppMessageCategoryForm;
import com.auth.service.system.message.model.query.InAppMessageCategoryQuery;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryPageVO;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryVO;
import com.auth.service.system.message.service.admin.InAppMessageCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 站内信业务分类
 *
 * @author Bunny
 */
@Tag(name = "站内信业务分类", description = "分类 CRUD 与启停")
@Validated
@RequestMapping("/api/system/message/in-app/categories")
@RestController
public class InAppMessageCategoryController {

	private final InAppMessageCategoryService inAppMessageCategoryService;

	public InAppMessageCategoryController(InAppMessageCategoryService inAppMessageCategoryService) {
		this.inAppMessageCategoryService = inAppMessageCategoryService;
	}

	@Operation(summary = "查询业务分类扁平列表")
	@PreAuthorize("@auth.decide('message:category:query')")
	@GetMapping("/list")
	public Result<List<InAppMessageCategoryPageVO>> listCategories(@Valid InAppMessageCategoryQuery query) {
		List<InAppMessageCategoryPageVO> data = inAppMessageCategoryService.listCategories(query);
		return Result.success(data);
	}

	@Operation(summary = "查询业务分类详情")
	@PreAuthorize("@auth.decide('message:category:detail')")
	@GetMapping("{id}")
	public Result<InAppMessageCategoryDetailVO> getCategoryById(@PathVariable("id") Long id) {
		InAppMessageCategoryDetailVO data = inAppMessageCategoryService.getCategoryById(id);
		return Result.success(data);
	}

	@Operation(summary = "新增业务分类")
	@PreAuthorize("@auth.decide('message:category:create')")
	@PostMapping()
	public Result<String> create(@Validated(CreateGroup.class) @RequestBody InAppMessageCategoryForm form) {
		inAppMessageCategoryService.create(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@Operation(summary = "更新业务分类")
	@PreAuthorize("@auth.decide('message:category:update')")
	@PutMapping()
	public Result<String> update(@Validated(UpdateGroup.class) @RequestBody InAppMessageCategoryForm form) {
		inAppMessageCategoryService.update(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@Operation(summary = "批量启停业务分类")
	@PreAuthorize("@auth.decide('message:category:update')")
	@PutMapping("status")
	public Result<String> batchUpdateStatus(@Validated @RequestBody IdsEnableStatusForm form) {
		inAppMessageCategoryService.batchUpdateStatus(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@Operation(summary = "批量删除业务分类")
	@PreAuthorize("@auth.decide('message:category:delete')")
	@DeleteMapping()
	public Result<String> batchDelete(@RequestBody @NotEmpty(message = "分类ID列表不能为空") List<Long> ids) {
		inAppMessageCategoryService.batchDelete(ids);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@AuthenticatedApi
	@Operation(summary = "查询业务大类列表")
	@GetMapping("/majors")
	public Result<List<InAppMessageCategoryVO>> listMajors(@RequestParam(required = false) Boolean status) {
		List<InAppMessageCategoryVO> data = inAppMessageCategoryService.listMajors(status);
		return Result.success(data);
	}

	@AuthenticatedApi
	@Operation(summary = "查询业务小类列表")
	@GetMapping("/children")
	public Result<List<InAppMessageCategoryVO>> listChildren(@RequestParam Long parentId,
			@RequestParam(required = false) Boolean status) {
		List<InAppMessageCategoryVO> data = inAppMessageCategoryService.listChildren(parentId, status);
		return Result.success(data);
	}

}
