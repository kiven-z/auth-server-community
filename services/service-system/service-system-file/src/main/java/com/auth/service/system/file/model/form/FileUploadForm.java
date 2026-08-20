package com.auth.service.system.file.model.form;

import com.auth.module.file.api.model.dto.FileUploadMetadata;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * 单文件上传表单
 *
 * @author Bunny
 */
@Schema(name = "FileUploadForm", title = "文件上传表单")
@Getter
@Setter
public class FileUploadForm extends FileUploadMetadata {

	@Schema(title = "上传文件", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "文件不能为空")
	private transient MultipartFile file;

}
