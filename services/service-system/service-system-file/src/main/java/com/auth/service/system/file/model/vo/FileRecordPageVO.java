package com.auth.service.system.file.model.vo;

import com.auth.common.core.model.response.BaseResponse;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件记录分页行
 *
 * @author Bunny
 */
@Schema(name = "FileRecordPageVO", title = "文件记录分页行")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class FileRecordPageVO extends BaseResponse {

	@Schema(title = "存储平台")
	private StoragePlatformEnum storagePlatform;

	@Schema(title = "上传模式")
	private String uploadMode;

	@Schema(title = "原始文件名")
	private String originalName;

	@Schema(title = "文件类型")
	private String contentType;

	@Schema(title = "文件大小（字节）")
	private Long size;

	@Schema(title = "是否私有文件")
	private Boolean isPrivate;

	@Schema(title = "业务类型")
	private String bizType;

	@Schema(title = "业务ID")
	private String bizId;

}
