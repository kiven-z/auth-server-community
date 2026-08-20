package com.auth.service.system.file.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

/**
 * 文件记录分页查询条件
 *
 * @author Bunny
 */
@Schema(name = "FileRecordPageQuery", title = "文件记录分页查询条件")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class FileRecordPageQuery extends PageQueryRequest {

	@Schema(title = "归属用户ID")
	private Long ownerUserId;

	@Schema(title = "业务类型")
	@Size(max = 64, message = "bizType 长度不能超过64个字符")
	private String bizType;

	@Schema(title = "业务ID")
	@Size(max = 64, message = "bizId 长度不能超过64个字符")
	private String bizId;

	@Schema(title = "MIME 类型")
	private String contentType;

	@Schema(title = "原始文件名")
	@Size(max = 255, message = "originalName 长度不能超过255个字符")
	private String originalName;

	@Schema(title = "创建开始时间（与 endTime 成对使用）")
	private Instant startTime;

	@Schema(title = "创建结束时间（与 startTime 成对使用）")
	private Instant endTime;

	@Schema(title = "是否私有文件")
	private Boolean isPrivate;

	@Schema(title = "删除来源")
	@Size(max = 32, message = "deleteSource 长度不能超过32个字符")
	private String deleteSource;

	@Schema(title = "删除来源集合")
	private List<@Size(max = 32, message = "deleteSource 长度不能超过32个字符") String> deleteSources;

	@AssertTrue(message = "startTime 与 endTime 必须成对指定")
	public boolean isTimeRangePaired() {
		return (startTime == null && endTime == null) || (startTime != null && endTime != null);
	}

	@AssertTrue(message = "startTime 不能晚于 endTime")
	public boolean isTimeRangeOrdered() {
		return startTime == null || endTime == null || !startTime.isAfter(endTime);
	}

}
