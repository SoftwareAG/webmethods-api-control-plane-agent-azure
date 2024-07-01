package com.softwareag.controlplane.agent.azure.configuration;

import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.models.Location;
import com.softwareag.controlplane.agent.azure.common.constants.Constants;
import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.common.utils.AzureAgentUtil;
import com.softwareag.controlplane.agentsdk.api.config.AuthConfig;
import com.softwareag.controlplane.agentsdk.api.config.ControlPlaneConfig;
import com.softwareag.controlplane.agentsdk.api.config.RuntimeConfig;
import com.softwareag.controlplane.agentsdk.api.config.TlsConfig;
import com.softwareag.controlplane.agentsdk.model.Capacity;
import com.softwareag.controlplane.agentsdk.model.Runtime;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

@Component
public class SDKConfigUtil {

    public static ControlPlaneConfig controlPlaneConfig(AgentProperties agentProperties){
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

        return new ControlPlaneConfig.Builder()
                .url(agentProperties.getUrl())
                .authConfig(authConfig)
                .tlsConfig(agentProperties.isSslEnabled() && !agentProperties.getTrustStorePath().isEmpty() && !agentProperties.getTrustStorePassword().isEmpty() ? tlsConfig : null)
                .build();
    }

    public static RuntimeConfig runtimeConfig(AzureProperties azureProperties,RuntimeProperties runtimeProperties, AzureManagersHolder managerHolder){

        Location location = managerHolder.getAzureResourceManager().subscriptions()
                .getById(azureProperties.getSubscriptionId())
                .getLocationByRegion(Region.fromName(managerHolder.getApiService().regionName()));

        Capacity capacity = null;
        if(ObjectUtils.isNotEmpty(runtimeProperties.getCapacityValue())) {
            capacity = new Capacity();
            capacity.setUnit(Capacity.TimeUnit.valueOf(runtimeProperties.getCapacityUnit()));
            capacity.setValue(Long.parseLong(runtimeProperties.getCapacityValue()));
        }
        // runtime ID = subscriptionId_serviceName
        String runtimeId =
                azureProperties.getSubscriptionId() + Constants.UNDERSCORE + azureProperties.getApiManagementServiceName();
        return new RuntimeConfig.Builder(runtimeId,
                azureProperties.getApiManagementServiceName(), runtimeProperties.getType(),
                Runtime.DeploymentType.PUBLIC_CLOUD)
                .region(managerHolder.getApiService().regionName())
                .location(location.physicalLocation())
                .tags(AzureAgentUtil.convertTags(managerHolder.getApiService().tags()))
                .capacity(capacity)
                .build();
    }

}
