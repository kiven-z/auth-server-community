package com.auth.service.system.file.storage.core.classifier;

/**
 * 存储异常分类器
 *
 * @author Bunny
 */
public interface StorageExceptionClassifier {

	/**
	 * 用于删除等幂等场景：远端已不存在时视为成功，避免因存储侧脏数据阻塞 DB 侧 purge
	 * @param throwable SDK 抛出的原始异常
	 * @return true 表示对象或桶已不存在
	 */
	boolean isNotFound(Throwable throwable);

}
