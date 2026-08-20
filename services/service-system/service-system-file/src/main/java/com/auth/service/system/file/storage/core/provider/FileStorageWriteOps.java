package com.auth.service.system.file.storage.core.provider;

import com.auth.service.system.file.model.value.FileUploadCommand;
import com.auth.service.system.file.model.value.StoredFile;

/**
 * 文件存储写能力接口
 *
 * @author Bunny
 */
public interface FileStorageWriteOps {

	/**
	 * 上传文件并返回存储结果
	 * @param command 上传命令
	 * @return 上传结果
	 */
	StoredFile upload(FileUploadCommand command);

	/**
	 * 删除存储对象
	 * @param bucket 存储桶
	 * @param objectKey 对象键
	 */
	void delete(String bucket, String objectKey);

	/**
	 * 将存储对象从源键迁移到目标键（同桶 copy + delete）。
	 * @param bucket 存储桶
	 * @param sourceObjectKey 源对象键
	 * @param destinationObjectKey 目标对象键
	 */
	void move(String bucket, String sourceObjectKey, String destinationObjectKey);

}
