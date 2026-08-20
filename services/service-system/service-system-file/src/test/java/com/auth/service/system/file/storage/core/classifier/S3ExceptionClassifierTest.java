package com.auth.service.system.file.storage.core.classifier;

import com.auth.service.system.file.storage.core.s3.S3ExceptionClassifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link S3ExceptionClassifier} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("S3ExceptionClassifier S3 兼容异常分类")
class S3ExceptionClassifierTest {

	private final S3ExceptionClassifier classifier = new S3ExceptionClassifier();

	@Test
	@DisplayName("isNotFound：NoSuchBucketException 命中 statusCode 404")
	void isNotFound_returnsTrueForNoSuchBucket() {
		// 验证 SDK 特化的 NoSuchBucketException 命中 404 判定。
		S3Exception exception = NoSuchBucketException.builder().statusCode(404).message("not found").build();

		assertThat(classifier.isNotFound(exception)).isTrue();
	}

	@Test
	@DisplayName("isNotFound：NoSuchKeyException 命中 statusCode 404")
	void isNotFound_returnsTrueForNoSuchKey() {
		// 验证 SDK 特化的 NoSuchKeyException 命中 404 判定。
		S3Exception exception = NoSuchKeyException.builder().statusCode(404).message("not found").build();

		assertThat(classifier.isNotFound(exception)).isTrue();
	}

	@Test
	@DisplayName("isNotFound：无 statusCode 时按 errorCode 判定 NoSuchBucket")
	void isNotFound_fallsBackToErrorCode() {
		// 验证部分 SDK 场景下 statusCode 缺失时依然能识别 errorCode。
		S3Exception exception = (S3Exception) S3Exception.builder()
			.awsErrorDetails(AwsErrorDetails.builder().errorCode("NoSuchBucket").build())
			.message("bucket missing")
			.build();

		assertThat(classifier.isNotFound(exception)).isTrue();
	}

	@Test
	@DisplayName("isNotFound：AccessDenied 类型不会误判为幂等成功")
	void isNotFound_returnsFalseForAccessDenied() {
		// 验证 403 AccessDenied 依旧走失败路径，阻断 DB 侧 purge。
		S3Exception exception = (S3Exception) S3Exception.builder()
			.statusCode(403)
			.awsErrorDetails(AwsErrorDetails.builder().errorCode("AccessDenied").build())
			.message("Access Denied")
			.build();

		assertThat(classifier.isNotFound(exception)).isFalse();
	}

	@Test
	@DisplayName("isNotFound：非 S3Exception 时按消息片段兜底")
	void isNotFound_returnsTrueForFallbackMessage() {
		// 验证通用 Throwable 也能通过消息识别为对象缺失。
		Exception exception = new RuntimeException("The specified key does not exist");

		assertThat(classifier.isNotFound(exception)).isTrue();
	}

	@Test
	@DisplayName("isNotFound：非结构化且消息无关时返回 false")
	void isNotFound_returnsFalseForUnrelatedThrowable() {
		// 验证兜底逻辑不会把无关异常误判为存储侧不存在。
		Exception exception = new IllegalStateException("connection reset");

		assertThat(classifier.isNotFound(exception)).isFalse();
	}

}
