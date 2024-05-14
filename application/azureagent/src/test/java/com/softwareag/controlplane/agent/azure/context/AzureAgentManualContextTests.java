package com.softwareag.controlplane.agent.azure.context;

import com.azure.core.management.Region;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServiceResource;
import com.azure.resourcemanager.apimanagement.models.Apis;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.Location;
import com.azure.resourcemanager.resources.models.Subscription;
import com.azure.resourcemanager.resources.models.Subscriptions;
import com.softwareag.controlplane.agent.azure.configuration.SDKConfigBuilder;
import com.softwareag.controlplane.agent.azure.helpers.APIRetriever;
import com.softwareag.controlplane.agent.azure.helpers.HeartbeatGenerator;
import com.softwareag.controlplane.agent.azure.helpers.MetricsRetriever;
import com.softwareag.controlplane.agentsdk.api.SdkLogger;
import com.softwareag.controlplane.agentsdk.api.client.http.SdkHttpClient;
import com.softwareag.controlplane.agentsdk.api.config.RuntimeConfig;
import com.softwareag.controlplane.agentsdk.api.config.SdkConfig;
import com.softwareag.controlplane.agentsdk.model.API;
import com.softwareag.controlplane.agentsdk.model.Asset;
import com.softwareag.controlplane.agentsdk.model.AssetSyncAction;
import com.softwareag.controlplane.agentsdk.model.Heartbeat;
import com.softwareag.controlplane.agentsdk.model.Metrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AzureAgentManualContextTests {
    @Mock
    SDKConfigBuilder sdkConfigBuilder;

    @Mock
    HeartbeatGenerator heartbeatHelper;

    @Mock
    APIRetriever apiRetriever;

    @Mock
    MetricsRetriever metricsRetriever;

    @InjectMocks
    AzureAgentManualContextImpl manualContext;

    @Spy
    private AzureManagersHolder azureManagersHolder = new AzureManagersHolder();

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

        Heartbeat heartbeat = new Heartbeat.Builder("arajRuntimeId").active(Heartbeat.Status.ACTIVE).build();
        when(heartbeatHelper.generateHeartBeat(any())).thenReturn(heartbeat);

        List<API> apiList = new ArrayList<>();
        apiList.add(Mockito.mock(API.class));
        when(apiRetriever.retrieveAPIs(true)).thenReturn(apiList);


        List<Metrics> metrics = new ArrayList<>();
        metrics.add(Mockito.mock(Metrics.class));
        when(metricsRetriever.metricsRetriever(anyLong(),anyLong(),anyLong())).thenReturn(metrics);

        List<AssetSyncAction<Asset>> apiListUpdates = new ArrayList<>();
        when(apiRetriever.getAPIUpdates(anyLong())).thenReturn(apiListUpdates);
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
