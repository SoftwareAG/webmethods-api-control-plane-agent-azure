/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.context;


import com.softwareag.controlplane.agent.azure.common.handlers.assets.PolicyRetriever;
import com.softwareag.controlplane.agent.azure.configuration.AgentProperties;
import com.softwareag.controlplane.agent.azure.configuration.AzureProperties;
import com.softwareag.controlplane.agent.azure.configuration.SDKConfigBuilder;
import com.softwareag.controlplane.agentsdk.api.AgentSDKContextManual;
import com.softwareag.controlplane.agentsdk.api.SdkLogger;
import com.softwareag.controlplane.agentsdk.api.client.http.SdkHttpClient;
import com.softwareag.controlplane.agentsdk.api.config.SdkConfig;
import com.softwareag.controlplane.agentsdk.model.API;
import com.softwareag.controlplane.agentsdk.model.Asset;
import com.softwareag.controlplane.agentsdk.model.AssetSyncAction;
import com.softwareag.controlplane.agentsdk.model.Heartbeat;
import com.softwareag.controlplane.agentsdk.model.Metrics;
import com.softwareag.controlplane.agent.azure.common.handlers.assets.AssetManager;
import com.softwareag.controlplane.agent.azure.common.handlers.heartbeat.HeartbeatManager;
import com.softwareag.controlplane.agent.azure.common.handlers.metrics.MetricsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;



/**
 * This is the implementation that retrieves the Runtime resources. This take care of everything for retrieving the data from the API Runtime.
 */
@Component
public class AzureAgentManualContextImpl implements AgentSDKContextManual {

    @Autowired
    private SDKConfigBuilder sdkConfigBuilder;

    @Autowired
    private AzureProperties azureProperties;

    @Autowired
    private AgentProperties agentProperties;

    @Autowired
    private AssetManager assetManager;

    @Autowired
    private MetricsManager metricsManager;

    @Autowired
    private HeartbeatManager heartbeatManager;

    @Autowired
    private PolicyRetriever policyRetriever;

    @Override
    public Heartbeat getHeartbeat() {
        return heartbeatManager.generateHeartBeat(sdkConfigBuilder.sdkConfig().getRuntimeConfig().getId(),
                azureProperties.getResourceGroup(), azureProperties.getApiManagementServiceName());
    }

    @Override
    public List<API> getAPIs() {
        /* The Executors are used here to calculate policy count from different levels such as
         API, Operation, ALL APIS(Global) and Product level policies.
            To ensure this activity doesn't affect the agent performance,
            sync of policy count is scheduled at 15 minutes delay.
            In the meantime, we expect APIs to be published into Control plane.
         */
        Thread policyCountThread = new Thread(() -> assetManager.apiPolicyCountDispatch(azureProperties.getSubscriptionId(), agentProperties.getUsername(), sdkConfigBuilder.controlPlaneClient()));
        policyCountThread.start();

        return assetManager.retrieveAPIs(true, azureProperties.getSubscriptionId(), agentProperties.getUsername(), false);
    }


    @Override
    public List<Metrics> getMetrics(long fromTimestamp, long toTimestamp, long interval) {
        return metricsManager.metricsTypeHandler(fromTimestamp,toTimestamp,interval,azureProperties.getMetricsSyncBufferIntervalMinutes(),azureProperties.getMetricsByRequestsOrInsights());
    }

    @Override
    public List<AssetSyncAction<Asset>> getAssetSyncActions(long fromTimestamp) {
        return assetManager.getAPIUpdates(fromTimestamp, azureProperties.getSubscriptionId(), agentProperties.getUsername());
    }
    @Override
    public SdkConfig getSdkConfig() {
        return sdkConfigBuilder.sdkConfig();
    }

    @Override
    public SdkLogger getLogger() {
        return null;
    }

    @Override
    public SdkHttpClient getHttpClient() {
        return null;
    }
}
