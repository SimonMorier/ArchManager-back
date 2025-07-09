package com.archmanager_back.config;

import com.archmanager_back.config.constant.AppProperties;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;

import lombok.AllArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@AllArgsConstructor
public class DockerConfig {

private final AppProperties props;    

@Bean
public DockerClient dockerClient() {
    DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
        .withDockerHost(props.getDocker().getHost()) 
        .build();

    ZerodepDockerHttpClient httpClient = new ZerodepDockerHttpClient.Builder()
        .dockerHost(config.getDockerHost())
        .build();

    return DockerClientImpl.getInstance(config, httpClient);
  }
}
