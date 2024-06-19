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
        TlsConfig tlsConfig = new TlsConfig
                .Builder(agentProperties.getTrustStorePath(), agentProperties.getTrustStoreType())
                .truststorePassword(agentProperties.getTrustStorePassword())
                .keystorePath(ObjectUtils.isNotEmpty(agentProperties.getKeyStorePath()) ?
                        agentProperties.getKeyStorePath() : null)
                .keystorePassword(ObjectUtils.isNotEmpty(agentProperties.getKeyStorePassword()) ?
                        agentProperties.getKeyStorePassword() : null)
                .keyAlias(ObjectUtils.isNotEmpty(agentProperties.getKeyAlias()) ? agentProperties.getKeyAlias() : null)
                .keyPassword(ObjectUtils.isNotEmpty(agentProperties.getKeyPassword()) ? agentProperties.getKeyPassword() : null)
                .keystoreType(ObjectUtils.isNotEmpty(agentProperties.getKeyStoreType()) ? agentProperties.getKeyStoreType() : null)
                .build();

        Location location = managerHolder.getAzureResourceManager().subscriptions()
                .getById(azureProperties.getSubscriptionId())
                .getLocationByRegion(Region.fromName(managerHolder.getApiService().regionName()));

        AuthConfig authConfig;
        if(ObjectUtils.isNotEmpty(agentProperties.getToken())) {
             authConfig = new AuthConfig
                    .Builder(agentProperties.getToken())
                    .build();
        } else {
             authConfig = new AuthConfig
                    .Builder(agentProperties.getUsername(), agentProperties.getPassword())
                    .build();
        }

        ControlPlaneConfig controlPlaneConfig = new ControlPlaneConfig.Builder()
                .url(agentProperties.getUrl())
                .authConfig(authConfig)
                .tlsConfig(agentProperties.isSslEnabled() && !agentProperties.getTrustStorePath().isEmpty() && !agentProperties.getTrustStorePassword().isEmpty() ? tlsConfig : null)
                .build();

        Capacity capacity = null;
        if(ObjectUtils.isNotEmpty(runtimeProperties.getCapacityValue())) {
            capacity = new Capacity();
            capacity.setUnit(Capacity.TimeUnit.valueOf(runtimeProperties.getCapacityUnit()));
            capacity.setValue(Long.parseLong(runtimeProperties.getCapacityValue()));
        }
        // runtime ID = subscriptionId_serviceName
        String runtimeId =
                azureProperties.getSubscriptionId() + Constants.UNDERSCORE + azureProperties.getApiManagementServiceName();
        RuntimeConfig runtimeConfig = new RuntimeConfig.Builder(runtimeId,
                azureProperties.getApiManagementServiceName(), runtimeProperties.getType(),
                Runtime.DeploymentType.PUBLIC_CLOUD)
                .region(managerHolder.getApiService().regionName())
                .location(location.physicalLocation())
                .tags(AzureAgentUtil.convertTags(managerHolder.getApiService().tags()))
                .capacity(capacity)
                .build();

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

}
