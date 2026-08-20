package com.auth.service.system.file.storage.core.s3;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.file.storage.core.classifier.StorageExceptionClassifier;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * S3 兼容异常分类器
 *
 * @author Bunny
 */
@Component
public class S3ExceptionClassifier implements StorageExceptionClassifier {

	/**
	 * S3 兼容：存储桶不存在
	 */
	private static final String ERROR_CODE_NO_SUCH_BUCKET = "NoSuchBucket";

	/**
	 * S3 兼容：对象不存在
	 */
	private static final String ERROR_CODE_NO_SUCH_KEY = "NoSuchKey";

	/**
	 * HTTP 404 Not Found
	 */
	private static final int HTTP_NOT_FOUND = 404;

	/**
	 * 消息级别兜底片段：桶不存在
	 */
	private static final String MESSAGE_BUCKET_NOT_EXIST = "The specified bucket does not exist";

	/**
	 * 消息级别兜底片段：对象不存在
	 */
	private static final String MESSAGE_KEY_NOT_EXIST = "The specified key does not exist";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isNotFound(Throwable throwable) {
		if (throwable instanceof S3Exception s3Exception) {
			return isS3NotFound(s3Exception);
		}
		return containsNotFoundMessage(throwable);
	}

	/**
	 * 判断是否为 S3 不存在异常
	 * @param exception 异常
	 * @return 是否为 S3 不存在异常
	 */
	private boolean isS3NotFound(S3Exception exception) {
		if (exception.statusCode() == HTTP_NOT_FOUND) {
			return true;
		}
		if (exception.awsErrorDetails() == null) {
			return false;
		}
		String errorCode = exception.awsErrorDetails().errorCode();
		return ERROR_CODE_NO_SUCH_BUCKET.equals(errorCode) || ERROR_CODE_NO_SUCH_KEY.equals(errorCode);
	}

	/**
	 * 判断是否包含不存在消息
	 * @param throwable 异常
	 * @return 是否包含不存在消息
	 */
	private boolean containsNotFoundMessage(Throwable throwable) {
		String message = throwable.getMessage();
		if (CharSequenceUtil.isBlank(message)) {
			return false;
		}
		return message.contains(MESSAGE_BUCKET_NOT_EXIST) || message.contains(MESSAGE_KEY_NOT_EXIST);
	}

}
