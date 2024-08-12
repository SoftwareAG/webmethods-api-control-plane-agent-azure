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
 * Agent properties read from application.properties or from environment variables
 * These properties is authorize agent application with Control Plane.
 * Also, properties for SDK to schedule jobs to Sync Assets into Control Plane.
 */
@ConfigurationProperties(prefix = "apicp")
@Getter
@Setter
@Validated
public class AgentProperties {

    /**
     * To publish all the APIs to Control Plane, this property has to be set to true
     */
    private boolean publishAssetsEnabled;

    /**
     * To send API updates synchornized to Control Plane, this property has to be set to true
     */
    private boolean syncAssetsEnabled;

    /**
     * To send the azure analytical data to Control plane, this property has to set to true
     */
    private boolean syncMetricsEnabled;

    /**
     * Interval in which SDK send heartbeat data to Control plane
     */
    private int syncHeartbeatIntervalSeconds;

    /**
     * Interval in which Azure metric reports queried and synced to Control Plane
     */
    private int syncMetricsIntervalSeconds;

    /**
     * Interval in which API updates from azure are queried and sent to API Control Plane
     */
    private int syncAssetsIntervalSeconds;

    /**
     * The URL where Control plane is deployed
     */
    @NotBlank
    private String url;

    /**
     * The username and password to authorize Control Plane instance. This represents basic authentication.
     */
    private String username;

    /**
     * The username and password to authorize Control Plane instance. This represents basic authentication.
     */
    private String password;

    /**
     * The token is to authorize Control Plane instance. This represents token based authorization. Either one of these authentication is mandatory for agent to run.
     */
    private String token;

    /**
     *  Agent Log level as Configured such as ALL , ERROR , INFO , TRACE
     */
    private String logLevel;

    /**
     * Set your SSL enabled to true, and provide with all the below configurations, to connect to Control Plane using HTTPS.
     */
    private boolean sslEnabled;

    /**
     * The trust Store Path
     */
    private String trustStorePath;

    /**
     * The trust Store Password
     */
    private String trustStorePassword;

    /**
     * The trust Store Type. Example : jks
     */
    private String trustStoreType;

    /**
     * The key Store Path.
     */
    private String keyStorePath;

    /**
     * The key Store Password.
     */
    private String keyStorePassword;

    /**
     * The key Store Type. Example : jks
     */
    private String keyStoreType;

    /**
     * The key Alias
     */
    private String keyAlias;

    /**
     * The key Password.
     */
    private String keyPassword;
}
