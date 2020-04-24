package com.example.sample.config;

import net.sunshow.toolkit.core.qbean.helper.repository.BaseRepositoryFactoryBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * author: sunshow.
 */
@Configuration
@EnableJpaRepositories(repositoryFactoryBeanClass = BaseRepositoryFactoryBean.class, basePackages = {"com.example.sample.dao"})
public class JpaConfig {
}
