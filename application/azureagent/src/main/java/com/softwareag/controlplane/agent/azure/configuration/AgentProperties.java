package com.softwareag.controlplane.agent.azure.configuration;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Agent properties read from application.properties or from environment variables
 */
@ConfigurationProperties(prefix = "apicp")
@Getter
@Setter
@Validated
public class AgentProperties {
    private boolean publishAssetsEnabled;
    private boolean syncAssetsEnabled;
    private boolean syncMetricsEnabled;
    private int syncHeartbeatIntervalSeconds;
    private int syncMetricsIntervalSeconds;
    private int syncAssetsIntervalSeconds;
    @NotBlank
    private String url;
    @NotBlank
    private String username;
    @NotBlank
    private String password;

    private String logLevel;
    private boolean sslEnabled;
    private String trustStorePath;
    private String trustStorePassword;
    private String trustStoreType;
    private String keyStorePath;
    private String keyStorePassword;
    private String keyStoreType;
    private String keyAlias;
    private String keyPassword;
}
