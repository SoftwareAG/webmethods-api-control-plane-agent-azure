package com.softwareag.controlplane.agent.azure.configuration;


import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.models.Location;
import com.softwareag.controlplane.agent.azure.Constants;
import com.softwareag.controlplane.agent.azure.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.helpers.AzureAgentUtil;
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
 * Config manager to create SdkConfig with ControlPlaneProperties, AgentProperties, RuntimeProperties and AWSProperties
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

    public SdkConfig sdkConfig() {
        TlsConfig tlsConfig = new TlsConfig
                .Builder(agentProperties.getTrustStorePath(), agentProperties.getTrustStoreType())
                .truststorePassword(agentProperties.getTrustStorePassword())
                .keystorePath(!ObjectUtils.isEmpty(agentProperties.getKeyStorePath()) ?
                        agentProperties.getKeyStorePath() : null)
                .keystorePassword(!ObjectUtils.isEmpty(agentProperties.getKeyStorePassword()) ?
                        agentProperties.getKeyStorePassword() : null)
                .keyAlias(!ObjectUtils.isEmpty(agentProperties.getKeyAlias()) ? agentProperties.getKeyAlias() : null)
                .keyPassword(!ObjectUtils.isEmpty(agentProperties.getKeyPassword()) ? agentProperties.getKeyPassword() : null)
                .keystoreType(!ObjectUtils.isEmpty(agentProperties.getKeyStoreType()) ? agentProperties.getKeyStoreType() : null)
                .build();

        Location location = managerHolder.getAzureResourceManager().subscriptions()
                .getById(azureProperties.getSubscriptionId())
                .getLocationByRegion(Region.fromName(managerHolder.getApiService().regionName()));
        AuthConfig authConfig = new AuthConfig
                .Builder(agentProperties.getUsername(), agentProperties.getPassword())
                .build();
        ControlPlaneConfig controlPlaneConfig = new ControlPlaneConfig.Builder()
                .url(agentProperties.getUrl())
                .authConfig(authConfig)
                .tlsConfig(agentProperties.isSslEnabled() && !agentProperties.getTrustStorePath().isEmpty() && !agentProperties.getTrustStorePassword().isEmpty() ? tlsConfig : null)
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
                .host(runtimeProperties.getHost())
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
                .logLevel(Level.ERROR)
                .build();
    }

}
