package com.softwareag.controlplane.agent.azure.context;

import com.softwareag.controlplane.agent.azure.configuration.SDKConfigBuilder;
import com.softwareag.controlplane.agent.azure.helpers.APIRetriever;
import com.softwareag.controlplane.agent.azure.helpers.HeartbeatGenerator;
import com.softwareag.controlplane.agent.azure.helpers.MetricsRetriever;
import com.softwareag.controlplane.agentsdk.api.AgentSDKContextManual;
import com.softwareag.controlplane.agentsdk.api.SdkLogger;
import com.softwareag.controlplane.agentsdk.api.client.http.SdkHttpClient;
import com.softwareag.controlplane.agentsdk.api.config.SdkConfig;
import com.softwareag.controlplane.agentsdk.model.API;
import com.softwareag.controlplane.agentsdk.model.Asset;
import com.softwareag.controlplane.agentsdk.model.AssetSyncAction;
import com.softwareag.controlplane.agentsdk.model.Heartbeat;
import com.softwareag.controlplane.agentsdk.model.Metrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class AzureAgentManualContextImpl implements AgentSDKContextManual {

    @Autowired
    SDKConfigBuilder sdkConfigBuilder;

    @Autowired
    HeartbeatGenerator heartbeatHelper;

    @Autowired
    APIRetriever apiRetriever;

    @Autowired
    MetricsRetriever metricsRetriever;

    @Override
    public Heartbeat getHeartbeat() {
        return heartbeatHelper.generateHeartBeat(sdkConfigBuilder.sdkConfig().getRuntimeConfig().getId());
    }

    @Override
    public List<API> getAPIs() {
        return apiRetriever.retrieveAPIs();
    }

    @Override
    public List<Metrics> getMetrics(long fromTimestamp, long toTimestamp, long interval) {
        return metricsRetriever.metricsRetriever(fromTimestamp,toTimestamp,interval);
    }

    @Override
    public List<AssetSyncAction<Asset>> getAssetSyncActions(long fromTimestamp) {
        return null;
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
