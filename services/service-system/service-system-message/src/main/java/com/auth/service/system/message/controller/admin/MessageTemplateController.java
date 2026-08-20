package com.auth.service.system.message.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.message.model.constants.MessageAuditBizModule;
import com.auth.service.system.message.model.form.MessageTemplateRequireFieldsForm;
import com.auth.service.system.message.model.form.MessageTemplateStatusForm;
import com.auth.service.system.message.model.form.MessageTemplateTestSendForm;
import com.auth.service.system.message.model.query.MessageTemplateQuery;
import com.auth.service.system.message.model.vo.template.MessageTemplateDetailVO;
import com.auth.service.system.message.model.vo.template.MessageTemplatePageVO;
import com.auth.service.system.message.model.vo.template.MessageTemplateRequireFieldRow;
import com.auth.service.system.message.service.admin.MessageTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 消息模板
 *
 * @author Bunny
 */
@Tag(name = "消息模板", description = "跨渠道公共查询、删除、启停")
@Validated
@RequestMapping("/api/system/message/template")
@RestController
public class MessageTemplateController {

	private final MessageTemplateService messageTemplateService;

	public MessageTemplateController(MessageTemplateService messageTemplateService) {
		this.messageTemplateService = messageTemplateService;
	}

	@Operation(summary = "分页查询消息模板")
	@PreAuthorize("@auth.decide('message:template:query')")
	@GetMapping()
	public Result<PageResponse<MessageTemplatePageVO>> getMessageTemplatePage(@Valid MessageTemplateQuery query) {
		PageResponse<MessageTemplatePageVO> pageResponse = messageTemplateService.getMessageTemplatePage(query);
		return Result.success(pageResponse);
	}

	@Operation(summary = "查询消息模板详情")
	@PreAuthorize("@auth.decide('message:template:detail')")
	@GetMapping("{id}")
	public Result<MessageTemplateDetailVO> getMessageTemplateById(@PathVariable("id") Long id,
			@RequestParam @NotBlank(message = "消息渠道不能为空") String channel) {
		MessageTemplateDetailVO detailVO = messageTemplateService.getMessageTemplateById(id, channel);
		return Result.success(detailVO);
	}

	@Operation(summary = "查询消息模板变量声明")
	@PreAuthorize("@auth.decide('message:template:detail')")
	@GetMapping("{id}/require-fields")
	public Result<List<MessageTemplateRequireFieldRow>> getRequireFields(@PathVariable("id") Long id,
			@RequestParam @NotBlank(message = "消息渠道不能为空") String channel) {
		List<MessageTemplateRequireFieldRow> requireFields = messageTemplateService.getRequireFields(id, channel);
		return Result.success(requireFields);
	}

	@OperationLog(targetType = "MESSAGE_TEMPLATE_DELETE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = MessageAuditBizModule.SYS_MESSAGE_TEMPLATE, operation = OperationLogKind.DELETE)
	@Operation(summary = "批量删除消息模板")
	@PreAuthorize("@auth.decide('message:template:delete')")
	@DeleteMapping()
	public Result<String> batchDelete(@RequestBody @NotEmpty(message = "模板ID列表不能为空") List<Long> ids,
			@RequestParam @NotBlank(message = "消息渠道不能为空") String channel) {
		messageTemplateService.batchDelete(ids, channel);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "MESSAGE_TEMPLATE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = MessageAuditBizModule.SYS_MESSAGE_TEMPLATE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "批量启停消息模板")
	@PreAuthorize("@auth.decide('message:template:update')")
	@PutMapping("status")
	public Result<String> batchUpdateStatus(@Validated @RequestBody MessageTemplateStatusForm form) {
		messageTemplateService.batchUpdateStatus(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "MESSAGE_TEMPLATE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = MessageAuditBizModule.SYS_MESSAGE_TEMPLATE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新消息模板变量列表")
	@PreAuthorize("@auth.decide('message:template:update')")
	@PutMapping("require-fields")
	public Result<String> updateRequireFields(@Validated @RequestBody MessageTemplateRequireFieldsForm form) {
		messageTemplateService.updateRequireFields(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@Operation(summary = "测试发送消息模板")
	@PreAuthorize("@auth.decide('message:template:update')")
	@PostMapping("test-send")
	public Result<String> testSend(@Validated @RequestBody MessageTemplateTestSendForm form) {
		messageTemplateService.testSend(form);
		return Result.success();
	}

}
