package com.auth.service.system.message.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.message.model.constants.MessageAuditBizModule;
import com.auth.service.system.message.model.form.email.EmailTemplateContentForm;
import com.auth.service.system.message.model.form.email.EmailTemplateForm;
import com.auth.service.system.message.model.form.email.EmailTemplateRenderForm;
import com.auth.service.system.message.service.admin.EmailTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 邮件模板
 *
 * @author Bunny
 */
@Tag(name = "邮件模板", description = "CRUD、内容编辑、预览渲染")
@RequestMapping("/api/system/message/email-template")
@RestController
public class EmailTemplateController {

	private final EmailTemplateService emailTemplateService;

	public EmailTemplateController(EmailTemplateService emailTemplateService) {
		this.emailTemplateService = emailTemplateService;
	}

	@Operation(summary = "预览邮件模板")
	@PreAuthorize("@auth.decide('message:template:detail')")
	@PostMapping("render")
	public Result<String> render(@Validated @RequestBody EmailTemplateRenderForm form) {
		String html = emailTemplateService.render(form);
		return Result.success(html);
	}

	@Operation(summary = "新增邮件模板")
	@PreAuthorize("@auth.decide('message:template:create')")
	@PostMapping()
	public Result<String> create(@Validated(CreateGroup.class) @RequestBody EmailTemplateForm form) {
		emailTemplateService.create(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "EMAIL_TEMPLATE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = MessageAuditBizModule.SYS_MESSAGE_TEMPLATE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新邮件模板")
	@PreAuthorize("@auth.decide('message:template:update')")
	@PutMapping()
	public Result<String> update(@Validated(UpdateGroup.class) @RequestBody EmailTemplateForm form) {
		emailTemplateService.update(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "EMAIL_TEMPLATE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = MessageAuditBizModule.SYS_MESSAGE_TEMPLATE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新邮件模板正文")
	@PreAuthorize("@auth.decide('message:template:update')")
	@PutMapping("content")
	public Result<String> updateContent(@Validated @RequestBody EmailTemplateContentForm form) {
		emailTemplateService.updateContent(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
