package com.auth.service.auth.support.login.ratelimit;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;

import static com.auth.module.security.contract.redis.SecurityRedisKey.LOGIN_CODE_LIMIT;

/**
 * 基于 Redis 的限流切面
 *
 * <p>
 * 处理逻辑：
 * <ol>
 * <li>从方法参数中解析出用户标识符（例如 "#email"），将其值作为 Redis 的 Key；</li>
 * <li>若该 Key 不存在，则默认从 0 开始，调用 redisTemplate 自增 1；</li>
 * <li>为该 Key 设置过期时间</li>
 * <li>在过期时间窗口内，若计数超过上限（{@link #MAX_LIMIT_COUNT}），则拒绝当前请求</li>
 * </ol>
 *
 * @author Bunny
 */
@Aspect
@Component
public class RateLimitAspect {

	/**
	 * 最大限制次数
	 */
	private static final int MAX_LIMIT_COUNT = 5;

	/**
	 * SPEL 表达式前缀
	 */
	private static final String SPEL_PREFIX = "#";

	/**
	 * 方法参数名发现器
	 */
	private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

	/**
	 * 表达式解析器
	 */
	private final ExpressionParser parser = new SpelExpressionParser();

	private final RedisTemplate<String, Object> redisTemplate;

	public RateLimitAspect(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 检查限流
	 * @param joinPoint 连接点
	 * @param rateLimit 限流注解
	 */
	@Before("@annotation(rateLimit)")
	public void checkRateLimit(JoinPoint joinPoint, RateLimit rateLimit) {
		String principal = parsePrincipal(joinPoint, rateLimit.principal());
		if (CharSequenceUtil.isBlank(principal)) {
			throw new AuthBusinessException(AuthResultCode.BAD_REQUEST_MISSING_EMAIL_EXCEPTION);
		}

		// 获取 Redis 键并开始计数
		String redisKey = LOGIN_CODE_LIMIT.key(principal);
		Long count = redisTemplate.opsForValue().increment(redisKey);
		if (count == null) {
			throw new AuthBusinessException(AuthResultCode.TOO_MANY_REQUESTS);
		}

		// 窗口从第一次计数开始
		if (Long.valueOf(1L).equals(count)) {
			Boolean expireResult = redisTemplate.expire(redisKey, LOGIN_CODE_LIMIT.getDefaultTtl());
			if (expireResult == null || !expireResult) {
				throw new AuthBusinessException(AuthResultCode.TOO_MANY_REQUESTS);
			}
		}
		if (count > MAX_LIMIT_COUNT) {
			throw new AuthBusinessException(AuthResultCode.TOO_MANY_REQUESTS);
		}
	}

	/**
	 * 解析 principal 表达式
	 * @param joinPoint 连接点
	 * @param principalExpression principal 表达式
	 * @return principal
	 */
	private String parsePrincipal(JoinPoint joinPoint, String principalExpression) {
		if (CharSequenceUtil.isBlank(principalExpression)) {
			return "";
		}
		if (!principalExpression.startsWith(SPEL_PREFIX)) {
			return principalExpression;
		}
		try {
			MethodSignature signature = (MethodSignature) joinPoint.getSignature();
			Object target = joinPoint.getTarget();
			Objects.requireNonNull(target);

			Method method = signature.getMethod();
			Class<?> targetClass = target.getClass();
			Method mostSpecificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
			Method specificMethod = BridgeMethodResolver.findBridgedMethod(mostSpecificMethod);

			EvaluationContext context = new MethodBasedEvaluationContext(target, specificMethod, joinPoint.getArgs(),
					PARAMETER_NAME_DISCOVERER);
			String value = parser.parseExpression(principalExpression).getValue(context, String.class);
			return CharSequenceUtil.nullToEmpty(value);
		}
		catch (Exception e) {
			return "";
		}
	}

}