package com.archmanager_back.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan("com.archmanager_back.model.entity")
@EnableJpaRepositories(basePackages = "com.archmanager_back.repository.jpa")
public class JpaConfig {}