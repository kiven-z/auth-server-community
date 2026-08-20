package com.auth.service.system.file.model.vo;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/**
 * 文件记录详情
 *
 * @author Bunny
 */
@Schema(name = "FileRecordDetailVO", title = "文件记录详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class FileRecordDetailVO extends BaseResponse {

	@Schema(title = "存储平台")
	private StoragePlatformEnum storagePlatform;

	@Schema(title = "上传模式")
	private String uploadMode;

	@Schema(title = "存储桶名称")
	private String bucket;

	@Schema(title = "对象键")
	private String objectKey;

	@Schema(title = "访问URL")
	private String url;

	@Schema(title = "临时访问URL")
	private String accessUrl;

	@Schema(title = "原始文件名")
	private String originalName;

	@Schema(title = "扩展名")
	private String extension;

	@Schema(title = "文件类型")
	private String contentType;

	@Schema(title = "文件大小（字节）")
	private Long size;

	@Schema(title = "是否私有文件")
	private Boolean isPrivate;

	@Schema(title = "对象ETag")
	private String etag;

	@Schema(title = "业务类型")
	private String bizType;

	@Schema(title = "业务ID")
	private String bizId;

	@Schema(title = "备注")
	private String remark;

	@Schema(title = "删除来源")
	private String deleteSource;

	@JsonStringFormat
	@Schema(title = "删除人")
	private Long deletedBy;

	@Schema(title = "删除时间")
	private Instant deletedAt;

}
