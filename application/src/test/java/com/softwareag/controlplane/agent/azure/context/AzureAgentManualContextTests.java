/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.context;

import com.azure.core.management.Region;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServiceResource;
import com.azure.resourcemanager.apimanagement.models.Apis;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.Location;
import com.azure.resourcemanager.resources.models.Subscription;
import com.azure.resourcemanager.resources.models.Subscriptions;
import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.configuration.AgentProperties;
import com.softwareag.controlplane.agent.azure.configuration.AzureProperties;
import com.softwareag.controlplane.agent.azure.configuration.SDKConfigBuilder;
import com.softwareag.controlplane.agentsdk.api.SdkLogger;
import com.softwareag.controlplane.agentsdk.api.client.http.SdkHttpClient;
import com.softwareag.controlplane.agentsdk.api.config.RuntimeConfig;
import com.softwareag.controlplane.agentsdk.api.config.SdkConfig;
import com.softwareag.controlplane.agentsdk.model.API;
import com.softwareag.controlplane.agentsdk.model.Asset;
import com.softwareag.controlplane.agentsdk.model.AssetSyncAction;
import com.softwareag.controlplane.agentsdk.model.Heartbeat;
import com.softwareag.controlplane.agentsdk.model.Metrics;
import com.softwareag.controlplane.agent.azure.common.handlers.assets.AssetManager;
import com.softwareag.controlplane.agent.azure.common.handlers.heartbeat.HeartbeatManager;
import com.softwareag.controlplane.agent.azure.common.handlers.metrics.MetricsManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class AzureAgentManualContextTests {
    @Mock
    SDKConfigBuilder sdkConfigBuilder;

    @Mock
    AssetManager assetManager;

    @Mock
    HeartbeatManager heartbeatManager;

    @Mock
    MetricsManager metricsManager;

    @Mock
    AzureProperties azureProperties;

    @Mock
    AgentProperties agentProperties;

    @InjectMocks
    AzureAgentManualContextImpl manualContext;

    @Spy
    private AzureManagersHolder azureManagersHolder = AzureManagersHolder.getInstance();

    @BeforeEach
    private void setup() {
        MockitoAnnotations.openMocks(this);
        when(azureManagersHolder.getApiService()).thenReturn(Mockito.mock(ApiManagementServiceResource.class));
        when(azureManagersHolder.getApiService().regionName()).thenReturn("East Asia");
        when(azureManagersHolder.getAzureResourceManager()).thenReturn(Mockito.mock(ResourceManager.class));
        when(azureManagersHolder.getAzureResourceManager().subscriptions()).thenReturn(Mockito.mock(Subscriptions.class));
        when(azureManagersHolder.getAzureResourceManager().subscriptions().getById("qwerty")).thenReturn(Mockito.mock(Subscription.class));
        when(azureManagersHolder.getAzureResourceManager().subscriptions().getById("qwerty").getLocationByRegion(Region.ASIA_EAST)).thenReturn(Mockito.mock(Location.class));
        when(azureManagersHolder.getAzureApiManager()).thenReturn(Mockito.mock(ApiManagementManager.class));
        when(azureManagersHolder.getAzureApiManager().apis()).thenReturn(Mockito.mock(Apis.class));

        when(sdkConfigBuilder.sdkConfig()).thenReturn(Mockito.mock(SdkConfig.class));
        when(sdkConfigBuilder.sdkConfig().getRuntimeConfig()).thenReturn(Mockito.mock(RuntimeConfig.class));
        when(sdkConfigBuilder.sdkConfig().getRuntimeConfig().getId()).thenReturn("arajRuntimeId");

        when(azureProperties.getResourceGroup()).thenReturn("test-group");
        when(azureProperties.getApiManagementServiceName()).thenReturn("test-service");
        when(azureProperties.getMetricsSyncBufferIntervalMinutes()).thenReturn(60);
        when(azureProperties.getMetricsByRequestsOrInsights()).thenReturn("requests");

        when(agentProperties.getUsername()).thenReturn("Azure-user");

        Heartbeat heartbeat = new Heartbeat.Builder("arajRuntimeId").active(Heartbeat.Status.ACTIVE).build();
        when(heartbeatManager.generateHeartBeat(any(),any(),any())).thenReturn(heartbeat);

        List<API> apiList = new ArrayList<>();
        apiList.add(Mockito.mock(API.class));
        when(assetManager.retrieveAPIs(anyBoolean(), any(), any(), anyBoolean())).thenReturn(apiList);


        List<Metrics> metrics = new ArrayList<>();
        metrics.add(Mockito.mock(Metrics.class));
        when(metricsManager.metricsTypeHandler(anyLong(),anyLong(),anyLong(),anyInt(),any())).thenReturn(metrics);

        List<AssetSyncAction<Asset>> apiListUpdates = new ArrayList<>();
        when(assetManager.getAPIUpdates(anyLong(), any(), any())).thenReturn(apiListUpdates);
    }

    @Test
    void getAPIsTests() {
        List<API> apiList = manualContext.getAPIs();
        assertFalse(apiList.isEmpty());
    }

    @Test
    void getHeartbeatTests() {
        Heartbeat heartbeat = manualContext.getHeartbeat();
        assertEquals(heartbeat.getRuntimeId(), "arajRuntimeId");
        assertEquals(heartbeat.getActive(), 1);
    }

    @Test
    void getMetricsTests() {
        List<Metrics> metrics = manualContext.getMetrics(System.currentTimeMillis(), System.currentTimeMillis(), Long.parseLong("30"));
        assertFalse(metrics.isEmpty());
    }

    @Test
    void getAPIUpdateTests() {
        List<AssetSyncAction<Asset>> apiList = manualContext.getAssetSyncActions(System.currentTimeMillis());
        assert(apiList.isEmpty());
    }

    @Test
    void getSdkConfigTests() {
        SdkConfig heartbeat = manualContext.getSdkConfig();
        assertEquals(heartbeat.getRuntimeConfig().getId(), "arajRuntimeId");
    }

    @Test
    void getLoggerTests() {
        SdkLogger heartbeat = manualContext.getLogger();
        assertEquals(heartbeat, null);
    }

    @Test
    void getHttpClientTests() {
        SdkHttpClient heartbeat = manualContext.getHttpClient();
        assertEquals(heartbeat, null);
    }
}
