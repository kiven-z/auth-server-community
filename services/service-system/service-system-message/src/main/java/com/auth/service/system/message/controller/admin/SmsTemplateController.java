package com.auth.service.system.message.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.message.model.constants.MessageAuditBizModule;
import com.auth.service.system.message.model.form.sms.SmsTemplateForm;
import com.auth.service.system.message.service.admin.SmsTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 短信模板
 *
 * @author Bunny
 */
@Tag(name = "短信模板", description = "CRUD、内容编辑、预览渲染")
@RequestMapping("/api/system/message/sms-template")
@RestController
public class SmsTemplateController {

	private final SmsTemplateService smsTemplateService;

	public SmsTemplateController(SmsTemplateService smsTemplateService) {
		this.smsTemplateService = smsTemplateService;
	}

	@Operation(summary = "新增短信模板")
	@PreAuthorize("@auth.decide('message:template:create')")
	@PostMapping()
	public Result<String> create(@Validated(CreateGroup.class) @RequestBody SmsTemplateForm form) {
		smsTemplateService.create(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "SMS_TEMPLATE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = MessageAuditBizModule.SYS_MESSAGE_TEMPLATE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新短信模板")
	@PreAuthorize("@auth.decide('message:template:update')")
	@PutMapping()
	public Result<String> update(@Validated(UpdateGroup.class) @RequestBody SmsTemplateForm form) {
		smsTemplateService.update(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
