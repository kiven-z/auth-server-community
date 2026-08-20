package com.auth.service.system.message.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.message.model.constants.MessageAuditBizModule;
import com.auth.service.system.message.model.query.MessageChannelDeliveryQuery;
import com.auth.service.system.message.model.vo.delivery.MessageChannelDeliveryDetailVO;
import com.auth.service.system.message.model.vo.delivery.MessageChannelDeliveryPageVO;
import com.auth.service.system.message.service.admin.MessageChannelDeliveryService;
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
 * 渠道投递记录
 *
 * @author Bunny
 */
@Tag(name = "渠道投递记录", description = "跨渠道投递记录查询")
@Validated
@RequestMapping("/api/system/message/channel-delivery")
@RestController
public class MessageChannelDeliveryController {

	private final MessageChannelDeliveryService messageChannelDeliveryService;

	public MessageChannelDeliveryController(MessageChannelDeliveryService messageChannelDeliveryService) {
		this.messageChannelDeliveryService = messageChannelDeliveryService;
	}

	@Operation(summary = "分页查询渠道投递记录")
	@PreAuthorize("@auth.decide('message:delivery:query')")
	@GetMapping()
	public Result<PageResponse<MessageChannelDeliveryPageVO>> getChannelDeliveryPage(
			@Valid MessageChannelDeliveryQuery query) {
		PageResponse<MessageChannelDeliveryPageVO> pageResponse = messageChannelDeliveryService
			.getChannelDeliveryPage(query);
		return Result.success(pageResponse);
	}

	@Operation(summary = "查询渠道投递记录详情")
	@PreAuthorize("@auth.decide('message:delivery:detail')")
	@GetMapping("{id}")
	public Result<MessageChannelDeliveryDetailVO> getChannelDeliveryById(@PathVariable("id") Long id) {
		MessageChannelDeliveryDetailVO detailVO = messageChannelDeliveryService.getChannelDeliveryById(id);
		return Result.success(detailVO);
	}

	@OperationLog(targetType = "MESSAGE_CHANNEL_DELIVERY_DELETE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = MessageAuditBizModule.SYS_MESSAGE_CHANNEL_DELIVERY, operation = OperationLogKind.DELETE)
	@Operation(summary = "批量删除渠道投递记录")
	@PreAuthorize("@auth.decide('message:delivery:delete')")
	@DeleteMapping()
	public Result<String> batchDelete(@RequestBody @NotEmpty(message = "投递记录ID列表不能为空") List<Long> ids) {
		messageChannelDeliveryService.batchDelete(ids);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
