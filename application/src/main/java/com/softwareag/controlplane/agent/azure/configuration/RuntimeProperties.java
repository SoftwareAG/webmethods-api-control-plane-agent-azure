/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.configuration;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


/**
 * Runtime properties read from application.properties or from environment variables
 *
 */
@ConfigurationProperties(prefix = "apicp.runtime")
@Getter
@Setter
@Validated
public class RuntimeProperties {

    /**
     * The API Management Service transaction capacity is represented as number in here
     */
    private String capacityValue;

    /**
     * unit for transaction is configured such as PER_SECOND , PER_MINUTE , PER_HOUR , PER_DAY , PER_WEEK, PER_MONTH , PER_YEAR
     */
    private String capacityUnit;

    /**
     * Pre-requisite for creating your API Management as Runtime into Control Plane is to create Runtime type in Control Plane.
     */
    @NotBlank
    private String type;

}
