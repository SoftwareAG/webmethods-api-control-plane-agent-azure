package com.softwareag.controlplane.agent.azure.configuration;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


/**
 * Runtime properties read from application.properties or from environment variables
 */
@ConfigurationProperties(prefix = "apicp.runtime")
@Getter
@Setter
@Validated
public class RuntimeProperties {
    private String capacityValue;
    private String capacityUnit;
    @NotBlank
    private String type;
    private String host;

}
