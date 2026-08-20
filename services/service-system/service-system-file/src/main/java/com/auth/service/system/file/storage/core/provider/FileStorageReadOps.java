package com.auth.service.system.file.storage.core.provider;

import java.io.InputStream;

/**
 * 文件存储读能力接口
 *
 * @author Bunny
 */
public interface FileStorageReadOps {

	/**
	 * 下载文件输入流
	 * @param bucket 存储桶
	 * @param objectKey 对象键
	 * @return 输入流
	 */
	InputStream download(String bucket, String objectKey);

	/**
	 * 生成对象读取预签名地址
	 * @param bucket 存储桶
	 * @param objectKey 对象键
	 * @param expireSeconds 过期秒数
	 * @return 预签名地址
	 */
	String presignGetUrl(String bucket, String objectKey, int expireSeconds);

	/**
	 * 解析对象公开访问地址
	 * @param bucket 存储桶
	 * @param objectKey 对象键
	 * @return 公开访问地址
	 */
	String resolvePublicUrl(String bucket, String objectKey);

	/**
	 * 判断对象是否已存在于目标键
	 * @param bucket 存储桶
	 * @param objectKey 对象键
	 * @return 是否存在
	 */
	boolean objectExists(String bucket, String objectKey);

}
