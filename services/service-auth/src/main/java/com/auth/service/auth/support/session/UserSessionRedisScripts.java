package com.auth.service.auth.support.session;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/**
 * 用户会话 Redis Lua 脚本持有者
 *
 * @author Bunny
 */
@Component
public class UserSessionRedisScripts {

	private final DefaultRedisScript<Long> registerScript = script("redis/session-register.lua");

	private final DefaultRedisScript<Long> terminateScript = script("redis/session-terminate.lua");

	private final DefaultRedisScript<Long> terminateOrphanScript = script("redis/session-terminate-orphan.lua");

	private final DefaultRedisScript<Long> rotateRefreshScript = script("redis/session-rotate-refresh.lua");

	private static DefaultRedisScript<Long> script(String path) {
		DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
		redisScript.setLocation(new ClassPathResource(path));
		redisScript.setResultType(Long.class);
		return redisScript;
	}

	/**
	 * 注册会话：Hash + 用户 Set + 全局 ZSet
	 * @return 注册脚本
	 */
	public DefaultRedisScript<Long> registerScript() {
		return registerScript;
	}

	/**
	 * 终止会话：Hash + 用户 Set + 全局 ZSet
	 * @return 终止脚本
	 */
	public DefaultRedisScript<Long> terminateScript() {
		return terminateScript;
	}

	/**
	 * Hash 已丢失时清理会话 Hash（若存在）与全局 ZSet
	 * @return 孤儿会话清理脚本
	 */
	public DefaultRedisScript<Long> terminateOrphanScript() {
		return terminateOrphanScript;
	}

	/**
	 * 原子旋转 refresh：校验 hash、写入 previous/last 令牌、grace 复用
	 * @return 旋转脚本
	 */
	public DefaultRedisScript<Long> rotateRefreshScript() {
		return rotateRefreshScript;
	}

}
