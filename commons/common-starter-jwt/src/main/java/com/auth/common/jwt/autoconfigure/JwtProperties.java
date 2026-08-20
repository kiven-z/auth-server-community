package com.auth.common.jwt.autoconfigure;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.jwt.model.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import static com.auth.common.jwt.model.SignatureAlgorithm.HS256;
import static com.auth.common.jwt.model.SignatureAlgorithm.RS256;

/**
 * auth.common.jwt.* 配置
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.common.jwt")
@Validated
public class JwtProperties {

	/**
	 * HS256 或 RS256
	 */
	private SignatureAlgorithm algorithm = HS256;

	@NotBlank
	private String issuer;

	/**
	 * HS256 对称密钥（UTF-8 字节长度须 ≥ 32）
	 */
	private String secret;

	/**
	 * Access 过期时间（秒）
	 */
	private long accessExpired = 3600L;

	/**
	 * Refresh 过期时间（秒），必须大于 access
	 */
	private long refreshExpired = 604800L;

	/**
	 * 校验 exp 时允许的时钟偏移（秒）
	 */
	private long clockSkewSeconds = 30L;

	/**
	 * PKCS12 路径，支持 classpath: 与 file: 或绝对路径
	 */
	private String keystorePath;

	private String password;

	private String alias = "jwtkey";

	/**
	 * 开发联调：keystore 不存在时是否自动生成（生产应关闭）
	 */
	private boolean autoGenerate = false;

	@PostConstruct
	public void validate() {
		if (this.refreshExpired <= this.accessExpired) {
			throw new IllegalStateException(
					"auth.common.jwt.refresh-expired must be greater than auth.common.jwt.access-expired.");
		}

		if (this.algorithm.equals(HS256) && CharSequenceUtil.isBlank(this.secret)) {
			throw new IllegalStateException("auth.common.jwt.secret is required for HS256.");
		}

		if (this.algorithm.equals(RS256)) {
			if (CharSequenceUtil.isBlank(this.keystorePath)) {
				throw new IllegalStateException("auth.common.jwt.keystore-path is required for RS256.");
			}
			if (CharSequenceUtil.isBlank(this.password)) {
				throw new IllegalStateException("auth.common.jwt.password is required for RS256.");
			}
		}
	}

}
