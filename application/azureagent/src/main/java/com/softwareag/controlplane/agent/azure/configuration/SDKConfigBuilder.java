package com.softwareag.controlplane.agent.azure.configuration;


import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.models.Location;
import com.softwareag.controlplane.agent.azure.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.Constants;
import com.softwareag.controlplane.agent.azure.helpers.AzureAgentUtil;
import com.softwareag.controlplane.agentsdk.api.config.AuthConfig;
import com.softwareag.controlplane.agentsdk.api.config.ControlPlaneConfig;
import com.softwareag.controlplane.agentsdk.api.config.RuntimeConfig;
import com.softwareag.controlplane.agentsdk.api.config.SdkConfig;
import com.softwareag.controlplane.agentsdk.model.AssetSyncMethod;
import com.softwareag.controlplane.agentsdk.model.Capacity;
import com.softwareag.controlplane.agentsdk.model.Runtime;
import jakarta.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Component;

/**
 * Config manager to create SdkConfig with ControlPlaneProperties, AgentProperties, RuntimeProperties and AWSProperties
 */
@Component
public class SDKConfigBuilder {
    @Valid
    private final AgentProperties agentProperties;

    @Valid
    private final RuntimeProperties runtimeProperties;
    @Valid
    private final AzureProperties azureProperties;

    private final AzureManagersHolder managerHolder;

    public SDKConfigBuilder(AgentProperties agentProperties, RuntimeProperties runtimeProperties, AzureProperties azureProperties, AzureManagersHolder managerHolder) {
        this.agentProperties = agentProperties;
        this.runtimeProperties = runtimeProperties;
        this.azureProperties = azureProperties;
        this.managerHolder = managerHolder;
    }

    public SdkConfig sdkConfig() {
        Location location = managerHolder.getAzureResourceManager().subscriptions()
                .getById(azureProperties.getSubscriptionId())
                .getLocationByRegion(Region.fromName(managerHolder.getApiService().regionName()));
        AuthConfig authConfig = new AuthConfig
                .Builder(agentProperties.getUsername(), agentProperties.getPassword())
                .build();
        ControlPlaneConfig controlPlaneConfig = new ControlPlaneConfig.Builder()
                .url(agentProperties.getUrl())
                .authConfig(authConfig)
                .build();

        Capacity capacity = null;
        if(ObjectUtils.isNotEmpty(runtimeProperties.getCapacityValue())) {
            capacity = new Capacity();
            capacity.setUnit(Capacity.TimeUnit.PER_YEAR); //TODO this needs to be read from properties
            capacity.setValue(Long.parseLong(runtimeProperties.getCapacityValue()));
        }
        // runtime ID = tenantId_serviceName
        String runtimeId =
                azureProperties.getTenantId() + Constants.UNDERSCORE + azureProperties.getApiManagementServiceName();
        RuntimeConfig runtimeConfig = new RuntimeConfig.Builder(runtimeId,
                azureProperties.getApiManagementServiceName(), runtimeProperties.getType(),
                Runtime.DeploymentType.PUBLIC_CLOUD)
                .region(managerHolder.getApiService().regionName())
                .location(location.physicalLocation())
                //.host(runtimeProperties.getHost())
                .tags(AzureAgentUtil.convertTags(managerHolder.getApiService().tags()))
                .capacity(capacity)
                .build();

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

}
