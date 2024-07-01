package com.softwareag.controlplane.agent.azure.configuration;


import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.models.Location;
import com.softwareag.controlplane.agent.azure.common.constants.Constants;
import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.common.utils.AzureAgentUtil;
import com.softwareag.controlplane.agentsdk.api.client.ControlPlaneClient;
import com.softwareag.controlplane.agentsdk.api.client.http.SdkHttpClient;
import com.softwareag.controlplane.agentsdk.api.config.AuthConfig;
import com.softwareag.controlplane.agentsdk.api.config.ControlPlaneConfig;
import com.softwareag.controlplane.agentsdk.api.config.RuntimeConfig;
import com.softwareag.controlplane.agentsdk.api.config.SdkConfig;
import com.softwareag.controlplane.agentsdk.api.config.TlsConfig;
import com.softwareag.controlplane.agentsdk.core.client.DefaultHttpClient;
import com.softwareag.controlplane.agentsdk.core.client.RestControlPlaneClient;
import com.softwareag.controlplane.agentsdk.model.AssetSyncMethod;
import com.softwareag.controlplane.agentsdk.model.Capacity;
import com.softwareag.controlplane.agentsdk.model.Runtime;
import jakarta.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Config manager to create SdkConfig with ControlPlaneProperties, AgentProperties, RuntimeProperties and AzureProperties
 */
@Component
public class SDKConfigBuilder {
    @Valid
    @Autowired
    private AgentProperties agentProperties;

    @Valid
    @Autowired
    private RuntimeProperties runtimeProperties;
    @Valid
    @Autowired
    private  AzureProperties azureProperties;

    @Autowired
    private AzureManagersHolder managerHolder;

    /**
     * Sdk config is built here with the all the properties configured.
     *
     * @return the sdk config
     */
    public SdkConfig sdkConfig() {
        ControlPlaneConfig controlPlaneConfig = SDKConfigUtil.controlPlaneConfig(agentProperties);

        RuntimeConfig runtimeConfig = SDKConfigUtil.runtimeConfig(azureProperties,runtimeProperties,managerHolder);

        SdkHttpClient httpClient = new DefaultHttpClient.Builder()
                    .tlsConfig(controlPlaneConfig.getTlsConfig())
                    .connectionConfig(controlPlaneConfig.getConnectionConfig())
                    .build();
        ControlPlaneClient controlPlaneClient = new RestControlPlaneClient.Builder()
                .runtimeConfig(runtimeConfig)
                .controlPlaneConfig(controlPlaneConfig)
                .httpClient(httpClient)
                .build();
        managerHolder.setRestControlPlaneClient(controlPlaneClient);

        return new SdkConfig.Builder(controlPlaneConfig, runtimeConfig)
                .publishAssets(agentProperties.isPublishAssetsEnabled())
                .syncAssets(agentProperties.isSyncAssetsEnabled())
                .assetSyncMethod(AssetSyncMethod.POLLING)
                .sendMetrics(agentProperties.isSyncMetricsEnabled())
                .heartbeatInterval(agentProperties.getSyncHeartbeatIntervalSeconds())
                .assetsSyncInterval(agentProperties.getSyncAssetsIntervalSeconds())
                .metricsSendInterval(agentProperties.getSyncMetricsIntervalSeconds())
                .logLevel(Level.valueOf(ObjectUtils.isEmpty(agentProperties.getLogLevel()) ?
                        "ALL" : agentProperties.getLogLevel()))
                .build();
    }

    public ControlPlaneClient controlPlaneClient(){
        ControlPlaneConfig controlPlaneConfig = SDKConfigUtil.controlPlaneConfig(agentProperties);

        RuntimeConfig runtimeConfig = SDKConfigUtil.runtimeConfig(azureProperties,runtimeProperties,managerHolder);

        SdkHttpClient httpClient = new DefaultHttpClient.Builder()
                .tlsConfig(controlPlaneConfig.getTlsConfig())
                .connectionConfig(controlPlaneConfig.getConnectionConfig())
                .build();
        return new RestControlPlaneClient.Builder()
                .runtimeConfig(runtimeConfig)
                .controlPlaneConfig(controlPlaneConfig)
                .httpClient(httpClient)
                .build();
    }

}
