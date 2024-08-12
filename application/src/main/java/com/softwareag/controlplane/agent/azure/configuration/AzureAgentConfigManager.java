/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.configuration;

import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.common.handlers.assets.AssetManager;
import com.softwareag.controlplane.agent.azure.common.handlers.assets.PolicyRetriever;
import com.softwareag.controlplane.agent.azure.common.handlers.heartbeat.HeartbeatManager;
import com.softwareag.controlplane.agent.azure.common.handlers.metrics.MetricsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The Class Azure agent config manager to instantiate all the Managers.
 */
@Configuration
public class AzureAgentConfigManager {

    @Autowired
    AzureProperties azureProperties;

    @Autowired
    AgentProperties agentProperties;

    /**
     * This method instantiate the heart beat manager
     *
     * @return the heartbeat manager
     */
    @Bean
    public HeartbeatManager heartbeatManager() {
        return HeartbeatManager.getInstance();
    }

    /**
     *This method instantiate the asset manager
     * @return the asset manager
     */
    @Bean
    public AssetManager assetManager() {
        return AssetManager.getInstance(azureProperties.getResourceGroup(), azureProperties.getApiManagementServiceName());
    }

    /**
     *This method instantiate the metrics manager
     * @return the metrics manager
     */
    @Bean
    public MetricsManager metricsManager() {
        return MetricsManager.getInstance(azureProperties.getResourceGroup(), azureProperties.getApiManagementServiceName(), azureProperties.getSubscriptionId());
    }

    /**
     * Azure managers holder holds instantiate the azure managers holder.
     *
     * @return the azure managers holder
     *
     */
    @Bean
    public AzureManagersHolder azureManagersHolder() {
        return AzureManagersHolder.getInstance();
    }


    @Bean
    public PolicyRetriever policyRetriever(){
        return PolicyRetriever.getInstance(azureProperties.getResourceGroup(),azureProperties.getApiManagementServiceName());
    }
}
