package com.softwareag.controlplane.agent.azure.context;

import com.softwareag.controlplane.agent.azure.common.constants.Constants;
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
import java.util.Objects;

/**
 * This is the implementation that retrieves the Runtime resources. This take care of everything for retrieving the data from the API Runtime.
 */
@Component
public class AzureAgentManualContextImpl implements AgentSDKContextManual {

    @Autowired
    SDKConfigBuilder sdkConfigBuilder;

    @Autowired
    AzureProperties azureProperties;

    @Autowired
    AgentProperties agentProperties;

    @Autowired
    AssetManager assetManager;

    @Autowired
    MetricsManager metricsManager;

    @Autowired
    HeartbeatManager heartbeatManager;

    @Override
    public Heartbeat getHeartbeat() {
        return heartbeatManager.generateHeartBeat(sdkConfigBuilder.sdkConfig().getRuntimeConfig().getId(),
                azureProperties.getResourceGroup(), azureProperties.getApiManagementServiceName());
    }

    @Override
    public List<API> getAPIs() {
        return assetManager.retrieveAPIs(true,azureProperties.getSubscriptionId(), agentProperties.getUsername());
    }

    @Override
    public List<Metrics> getMetrics(long fromTimestamp, long toTimestamp, long interval) {
        if(Objects.equals(azureProperties.getMetricsByRequestsOrInsights(), Constants.METRICS_BY_REQUESTS)) return metricsManager.metricsRetrieverByRequests(fromTimestamp, toTimestamp, azureProperties.getMetricsSyncBufferIntervalMinutes());
        return metricsManager.metricsRetrieverByInsights(fromTimestamp,toTimestamp,interval,azureProperties.getMetricsSyncBufferIntervalMinutes());
    }

    @Override
    public List<AssetSyncAction<Asset>> getAssetSyncActions(long fromTimestamp) {
        return assetManager.getAPIUpdates(fromTimestamp,azureProperties.getSubscriptionId(), agentProperties.getUsername());
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
