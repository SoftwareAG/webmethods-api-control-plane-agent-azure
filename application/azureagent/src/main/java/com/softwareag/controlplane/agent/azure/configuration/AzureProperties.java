package com.softwareag.controlplane.agent.azure.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "azure")
@Getter
@Setter
@Validated
public class AzureProperties {
    @NotBlank
    private String subscriptionId;
    @NotBlank
    private String resourceGroup;
    @NotBlank
    private String tenantId;
    @NotBlank
    private String clientId;
    @NotBlank
    private String clientSecret;
    @NotBlank
    private String apiManagementServiceName;

    @PositiveOrZero
    private int metricsSyncBufferIntervalMinutes;
}
