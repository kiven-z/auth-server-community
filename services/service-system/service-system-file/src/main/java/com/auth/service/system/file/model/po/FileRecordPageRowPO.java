package com.auth.service.system.file.model.po;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 文件记录分页行（持久层查询投影，非 API 出参）
 *
 * @author Bunny
 */
@Schema(name = "FileRecordPageRowPO", title = "文件记录分页行")
@Getter
@Setter
@ToString
public class FileRecordPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键")
	private Long id;

	@Schema(title = "存储平台")
	private StoragePlatformEnum storagePlatform;

	@Schema(title = "上传模式")
	private String uploadMode;

	@Schema(title = "原始文件名")
	private String originalName;

	@Schema(title = "MIME 类型")
	private String contentType;

	@Schema(title = "文件大小（字节）")
	private Long size;

	@Schema(title = "是否私有文件")
	private Boolean isPrivate;

	@Schema(title = "业务类型")
	private String bizType;

	@Schema(title = "业务ID")
	private String bizId;

	@Schema(title = "创建时间")
	private Instant createdAt;

	@Schema(title = "更新时间")
	private Instant updatedAt;

	@Schema(title = "创建用户")
	private Long createdBy;

	@Schema(title = "更新用户")
	private Long updatedBy;

}
