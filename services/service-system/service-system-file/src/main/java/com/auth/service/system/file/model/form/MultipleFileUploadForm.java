package com.auth.service.system.file.model.form;

import com.auth.module.file.api.model.dto.FileUploadMetadata;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 多文件上传表单
 *
 * @author Bunny
 */
@Schema(name = "MultipleFileUploadForm", title = "多文件上传表单")
@Getter
@Setter
public class MultipleFileUploadForm extends FileUploadMetadata {

	@Schema(title = "上传文件列表", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 20, message = "单次最多上传20个文件")
	@NotEmpty(message = "上传文件列表不能为空")
	private transient List<MultipartFile> files;

}
