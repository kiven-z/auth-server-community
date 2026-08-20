package com.auth.common.ip.config;

import com.auth.common.ip.DefaultIpAddressServiceImpl;
import com.auth.common.ip.IpAddressService;
import com.auth.common.ip.format.DefaultRegionFormatter;
import com.auth.common.ip.format.RegionFormatter;
import com.auth.common.ip.inner.CompositeInnerIpChecker;
import com.auth.common.ip.inner.InnerIpChecker;
import com.auth.common.ip.inner.Ipv4InnerIpChecker;
import com.auth.common.ip.inner.Ipv6InnerIpChecker;
import com.auth.common.ip.normalize.DefaultIpNormalizer;
import com.auth.common.ip.normalize.IpNormalizer;
import com.auth.common.ip.region.Ip2RegionSearcherRegionResolver;
import com.auth.common.ip.region.NoopRegionResolver;
import com.auth.common.ip.region.RegionResolver;
import com.auth.common.ip.region.RoutingRegionResolver;
import com.auth.common.ip.resolver.*;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.LongByteArray;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Version;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * IP工具模块的自动配置
 *
 * @author Bunny
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(IpToolProperties.class)
public class IpToolAutoConfiguration {

	/**
	 * 警告并返回noop区域解析器
	 * @param type ip类型
	 * @param path ip2region数据库路径
	 * @param noop noop区域解析器
	 * @return 警告并返回noop区域解析器
	 */
	private static RegionResolver warnAndReturnNoop(String type, String path, RegionResolver noop) {
		log.warn("{} ip2region database is not available at path={}, region resolving will be disabled.", type, path);
		return noop;
	}

	/**
	 * 创建ip2region数据库搜索器
	 * @param dbPath ip2region数据库路径
	 * @param version ip版本
	 * @return ip2region数据库搜索器
	 */
	private static Optional<Searcher> createSearcher(String dbPath, Version version) {
		// 如果数据库路径为空，则返回空
		if (dbPath == null) {
			return Optional.empty();
		}

		try {
			// 将数据库路径转换为Path对象
			Path path = Path.of(dbPath);
			// 如果数据库路径不存在，则返回空
			if (!Files.exists(path) || !Files.isRegularFile(path) || !Files.isReadable(path)) {
				return Optional.empty();
			}

			// 加载数据库内容
			LongByteArray content = Searcher.loadContentFromFile(dbPath);
			// 创建ip2region数据库搜索器
			return Optional.of(Searcher.newWithBuffer(version, content));
		}
		catch (Exception e) {
			// 如果加载数据库内容失败，则返回空
			log.warn("{} ip2region database failed to load, region resolving will be disabled.", version.name, e);
			return Optional.empty();
		}
	}

	/**
	 * 创建ip正常化器
	 * @return ip正常化器
	 */
	@ConditionalOnMissingBean
	@Bean
	public IpNormalizer ipNormalizer() {
		return new DefaultIpNormalizer();
	}

	/**
	 * 创建区域格式化器
	 * @return 区域格式化器
	 */
	@ConditionalOnMissingBean
	@Bean
	public RegionFormatter regionFormatter() {
		return new DefaultRegionFormatter();
	}

	/**
	 * 创建客户端ip解析器
	 * @return 客户端ip解析器
	 */
	@ConditionalOnMissingBean
	@Bean
	public ClientIpResolver clientIpResolver() {
		return new CompositeClientIpResolver(List.of(new XForwardedForResolver(), new ProxyClientIpResolver(),
				new WLProxyClientIpResolver(), new HttpClientIpResolver(), new HttpXForwardedForResolver(),
				new XRealIpResolver(), new RemoteAddrResolver()));
	}

	/**
	 * 创建内部ip检查器
	 * @return 内部ip检查器
	 */
	@ConditionalOnMissingBean
	@Bean
	public InnerIpChecker innerIpChecker() {
		Ipv4InnerIpChecker ipv4Checker = new Ipv4InnerIpChecker();
		Ipv6InnerIpChecker ipv6Checker = new Ipv6InnerIpChecker(ipv4Checker);
		return new CompositeInnerIpChecker(List.of(ipv4Checker, ipv6Checker));
	}

	/**
	 * 创建区域解析器
	 * @param properties ip工具属性
	 * @return 区域解析器
	 */
	@ConditionalOnMissingBean
	@Bean
	public RegionResolver regionResolver(IpToolProperties properties) {
		if (!properties.isResolveExternalEnabled()) {
			return new NoopRegionResolver();
		}

		RegionResolver noop = new NoopRegionResolver();
		RegionResolver ipv4 = createSearcher(properties.getIpv4DbPath(), Version.IPv4)
			.<RegionResolver>map(Ip2RegionSearcherRegionResolver::new)
			.orElseGet(() -> warnAndReturnNoop("IPv4", properties.getIpv4DbPath(), noop));

		RegionResolver ipv6;
		if (!properties.isResolveIpv6Enabled()) {
			ipv6 = noop;
		}
		else {
			ipv6 = createSearcher(properties.getIpv6DbPath(), Version.IPv6)
				.<RegionResolver>map(Ip2RegionSearcherRegionResolver::new)
				.orElseGet(() -> warnAndReturnNoop("IPv6", properties.getIpv6DbPath(), noop));
		}

		return new RoutingRegionResolver(ipv4, ipv6, noop);
	}

	/**
	 * 创建ip地址服务
	 * @param clientIpResolver 客户端ip解析器
	 * @param ipNormalizer ip正常化器
	 * @param innerIpChecker 内部ip检查器
	 * @param regionResolver 区域解析器
	 * @param regionFormatter 区域格式化器
	 * @return ip地址服务
	 */
	@ConditionalOnMissingBean(IpAddressService.class)
	@Bean(name = "ipAddressService")
	public IpAddressService ipAddressService(ClientIpResolver clientIpResolver, IpNormalizer ipNormalizer,
			InnerIpChecker innerIpChecker, RegionResolver regionResolver, RegionFormatter regionFormatter) {
		return new DefaultIpAddressServiceImpl(clientIpResolver, ipNormalizer, innerIpChecker, regionResolver,
				regionFormatter);
	}

}
