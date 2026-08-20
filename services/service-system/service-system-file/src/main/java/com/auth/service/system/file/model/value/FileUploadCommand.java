package com.auth.service.system.file.model.value;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import lombok.Builder;
import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传命令
 *
 * @author Bunny
 */
@Value
@Builder
public class FileUploadCommand {

	/**
	 * 存储平台
	 */
	StoragePlatformEnum storagePlatform;

	/**
	 * 上传文件
	 */
	MultipartFile file;

	/**
	 * 对象键
	 */
	String objectKey;

}
