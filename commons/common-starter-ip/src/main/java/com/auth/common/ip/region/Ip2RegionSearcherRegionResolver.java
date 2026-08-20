package com.auth.common.ip.region;

import cn.hutool.core.text.CharSequenceUtil;
import org.lionsoul.ip2region.xdb.Searcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * ip2region数据库搜索器区域解析器
 *
 * @author Bunny
 */
public record Ip2RegionSearcherRegionResolver(Searcher searcher) implements RegionResolver {

	private static final Logger log = LoggerFactory.getLogger(Ip2RegionSearcherRegionResolver.class);

	/**
	 * 解析IP地址的区域
	 * @param ip IP地址
	 * @return 区域
	 */
	@Override
	public Optional<String> resolveRegion(String ip) {
		// 如果IP地址为空，则返回空
		if (CharSequenceUtil.isBlank(ip)) {
			return Optional.empty();
		}

		try {
			// 搜索IP地址的区域
			String region = searcher.search(ip);
			// 如果区域为空，则返回空
			if (CharSequenceUtil.isBlank(region)) {
				return Optional.empty();
			}

			// 返回区域
			return Optional.of(region);
		}
		catch (Exception e) {
			log.debug("Failed to resolve ip region for ip={}", ip, e);
			return Optional.empty();
		}
	}

}
