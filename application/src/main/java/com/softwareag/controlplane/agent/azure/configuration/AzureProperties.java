/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.configuration;

import com.softwareag.controlplane.agent.azure.common.constants.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 *  Azure properties read from application.properties or from environment variables
 *  These properties is to authorize azure SDK and agent application.
 */
@ConfigurationProperties(prefix = "azure")
@Getter
@Setter
@Validated
public class AzureProperties {

    /**
     * The subscription identifier of your Azure service resources are grouped
     */
    @NotBlank
    private String subscriptionId;

    /**
     * The container that holds related resources for an Azure api management solution
     */
    @NotBlank
    private String resourceGroup;

    /**
     * your azure account id / organization id
     */
    @NotBlank
    private String tenantId;

    /**
     * to authorize the azure sdk connection
     */
    @NotBlank
    private String clientId;

    /**
     * to authorize the azure sdk connection
     */
    @NotBlank
    private String clientSecret;

    /**
     * A unique name that identifies API Management instance.
     */
    @NotBlank
    private String apiManagementServiceName;

    /**
     * The metrics can be obtained either by collecting data from each individual request made during the interval or by using the aggregated report provided by Azure.
     */
    @NotNull
    @Pattern(regexp = Constants.METRICS_BY_INSIGHTS+"|"+Constants.METRICS_BY_REQUESTS, message = "Invalid value. Must be " + Constants.METRICS_BY_REQUESTS + " or "+Constants.METRICS_BY_INSIGHTS+".")
    private String metricsByRequestsOrInsights;

     /**
     * As per azure documentation Real-time analytical data may be delayed 15 minutes
     * or longer depending on the current service load.
     * The same is configured as buffer time.
     */
    @PositiveOrZero
    private int metricsSyncBufferIntervalMinutes;
}
