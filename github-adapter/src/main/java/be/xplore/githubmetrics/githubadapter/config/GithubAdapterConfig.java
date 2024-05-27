package be.xplore.githubmetrics.githubadapter.config;

import be.xplore.githubmetrics.githubadapter.cacheevicting.CacheEvictionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({GithubProperties.class, CacheEvictionProperties.class})
public class GithubAdapterConfig {
}
