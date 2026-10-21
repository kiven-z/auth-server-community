package com.auth.common.ip.region;

import cn.hutool.core.text.CharSequenceUtil;
import org.lionsoul.ip2region.xdb.Searcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * 用 ip2region {@link Searcher} 查区域
 *
 * @author Bunny
 */
public record Ip2RegionSearcherRegionResolver(Searcher searcher) implements RegionResolver {

	private static final Logger log = LoggerFactory.getLogger(Ip2RegionSearcherRegionResolver.class);

	@Override
	public Optional<String> resolveRegion(String ip) {
		if (CharSequenceUtil.isBlank(ip)) {
			return Optional.empty();
		}

		try {
			String region = searcher.search(ip);
			if (CharSequenceUtil.isBlank(region)) {
				return Optional.empty();
			}

			return Optional.of(region);
		}
		catch (Exception e) {
			log.debug("Failed to resolve ip region for ip={}", ip, e);
			return Optional.empty();
		}
	}

}
