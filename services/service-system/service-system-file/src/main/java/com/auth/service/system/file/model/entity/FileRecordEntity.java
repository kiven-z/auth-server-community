package com.auth.service.system.file.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 文件元数据实体
 *
 * @author Bunny
 */
@TableName("file_record")
@Schema(name = "FileRecordEntity", title = "文件元数据")
@Getter
@Setter
public class FileRecordEntity extends BaseEntity {

	@Schema(title = "存储平台")
	private StoragePlatformEnum storagePlatform;

	@Schema(title = "上传模式")
	private String uploadMode;

	@Schema(title = "存储桶")
	private String bucket;

	@Schema(title = "对象键")
	private String objectKey;

	@Schema(title = "访问URL")
	private String url;

	@Schema(title = "原始文件名")
	private String originalName;

	@Schema(title = "扩展名")
	private String extension;

	@Schema(title = "MIME 类型")
	private String contentType;

	@Schema(title = "文件大小（字节）")
	private Long size;

	@Schema(title = "对象ETag")
	private String etag;

	@TableField("is_private")
	@Schema(title = "是否私有文件")
	private Boolean isPrivate;

	@Schema(title = "业务类型")
	private String bizType;

	@Schema(title = "业务ID")
	private String bizId;

	@TableField("is_deleted")
	@TableLogic(value = "0", delval = "1")
	@Schema(title = "逻辑删除")
	private Boolean isDeleted;

	@Schema(title = "删除来源")
	private String deleteSource;

	@Schema(title = "删除人")
	private Long deletedBy;

	@Schema(title = "删除时间")
	private Instant deletedAt;

}
