package com.auth.service.system.message.controller.me;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.service.system.message.model.query.InAppInboxQuery;
import com.auth.service.system.message.model.vo.inapp.InAppInboxDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppInboxPageVO;
import com.auth.service.system.message.model.vo.inapp.InAppInboxUnreadCountVO;
import com.auth.service.system.message.service.me.InAppInboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 站内信收件箱
 *
 * @author Bunny
 */
@Tag(name = "站内信收件箱", description = "用户侧收件、已读与删除")
@AuthenticatedApi
@Validated
@RequestMapping("/api/system/me/message/in-app")
@RestController
public class InAppInboxController {

	private final InAppInboxService inAppInboxService;

	public InAppInboxController(InAppInboxService inAppInboxService) {
		this.inAppInboxService = inAppInboxService;
	}

	@Operation(summary = "分页查询我的站内信")
	@GetMapping("/inbox")
	public Result<PageResponse<InAppInboxPageVO>> getInboxPage(@Valid InAppInboxQuery query) {
		PageResponse<InAppInboxPageVO> pageResponse = inAppInboxService.getInboxPage(query);
		return Result.success(pageResponse);
	}

	@Operation(summary = "查询我的站内信未读角标")
	@GetMapping("/inbox/unread-count")
	public Result<InAppInboxUnreadCountVO> getUnreadCount() {
		InAppInboxUnreadCountVO unreadCount = inAppInboxService.getUnreadCount();
		return Result.success(unreadCount);
	}

	@Operation(summary = "查询站内信详情", description = "当前用户可见时返回正文；打开即标已读")
	@GetMapping("/inbox/{messageId}")
	public Result<InAppInboxDetailVO> getInboxDetail(@PathVariable Long messageId) {
		InAppInboxDetailVO detail = inAppInboxService.getInboxDetail(messageId);
		return Result.success(detail);
	}

	@Operation(summary = "批量标记站内信已读")
	@PostMapping("/inbox/read")
	public Result<String> markRead(@RequestBody @NotEmpty(message = "消息ID列表不能为空") List<Long> messageIds) {
		inAppInboxService.markRead(messageIds);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@Operation(summary = "标记当前大类全部已读", description = "仅处理该大类下当前用户可见的未读消息")
	@PostMapping("/inbox/read-all")
	public Result<String> markAllRead(@RequestParam @NotNull(message = "业务大类ID不能为空") Long majorCategoryId) {
		inAppInboxService.markAllRead(majorCategoryId);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@Operation(summary = "批量删除我的站内信", description = "用户侧逻辑删除；入参为站内信 messageId 数组")
	@DeleteMapping("/inbox")
	public Result<String> batchDelete(@RequestBody @NotEmpty(message = "消息ID列表不能为空") List<Long> messageIds) {
		inAppInboxService.batchDelete(messageIds);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@Operation(summary = "删除当前大类全部站内信", description = "仅软删除该大类下当前用户可见消息")
	@DeleteMapping("/inbox/all")
	public Result<String> deleteAll(@RequestParam @NotNull(message = "业务大类ID不能为空") Long majorCategoryId) {
		inAppInboxService.deleteAll(majorCategoryId);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
