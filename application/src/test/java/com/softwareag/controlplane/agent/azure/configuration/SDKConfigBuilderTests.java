/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.configuration;

import com.azure.core.management.Region;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServiceResource;
import com.azure.resourcemanager.apimanagement.models.Apis;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.Location;
import com.azure.resourcemanager.resources.models.Subscription;
import com.azure.resourcemanager.resources.models.Subscriptions;
import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agentsdk.api.config.SdkConfig;
import com.softwareag.controlplane.agentsdk.core.validator.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class SDKConfigBuilderTests {

    @Valid
    @Spy
    private AgentProperties agentProperties = new AgentProperties();

    @Valid
    @Spy
    private RuntimeProperties runtimeProperties = new RuntimeProperties();
    @Valid
    @Spy
    private AzureProperties azureProperties = new AzureProperties();

    @Spy
    private AzureManagersHolder azureManagersHolder = spy(AzureManagersHolder.getInstance());

    @InjectMocks
    SDKConfigBuilder sdkConfigBuilder;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        //agent properties
        agentProperties.setPassword("manage");
        agentProperties.setUsername("John Doe");
        agentProperties.setUrl("http://test:8888");
        agentProperties.setSyncAssetsEnabled(true);
        agentProperties.setPublishAssetsEnabled(true);
        agentProperties.setSyncMetricsEnabled(true);
        agentProperties.setSyncAssetsIntervalSeconds(300);
        agentProperties.setSyncHeartbeatIntervalSeconds(60);
        agentProperties.setSyncMetricsIntervalSeconds(500);

        //runtime properties
        runtimeProperties.setCapacityUnit("PER_YEAR");
        runtimeProperties.setType("AzureType");
        runtimeProperties.setCapacityValue("500000");

        //azure properties
        azureProperties.setClientId("abc");
        azureProperties.setClientSecret("ghj");
        azureProperties.setSubscriptionId("qwerty");
        azureProperties.setTenantId("default");
        azureProperties.setResourceGroup("azuregroup");
        azureProperties.setApiManagementServiceName("serviceName");
        azureProperties.setMetricsByRequestsOrInsights("requests");

    }

    @Test
    void testSDKConfigInitialize() {

        Set<ConstraintViolation<AgentProperties>> violations = ValidatorFactory.getValidator().validate(agentProperties);
        assert(violations).isEmpty();

        Set<ConstraintViolation<AzureProperties>> azureViolations =
                ValidatorFactory.getValidator().validate(azureProperties);
        assert(azureViolations).isEmpty();

        Set<ConstraintViolation<RuntimeProperties>> runtimeViolations =
                ValidatorFactory.getValidator().validate(runtimeProperties);
        assert(runtimeViolations).isEmpty();
        assertNotNull(sdkConfigBuilder);
    }

    @Test
    void testSDKConfig() {
        when(azureManagersHolder.getApiService()).thenReturn(Mockito.mock(ApiManagementServiceResource.class));
        when(azureManagersHolder.getApiService().regionName()).thenReturn("East Asia");
        when(azureManagersHolder.getAzureResourceManager()).thenReturn(Mockito.mock(ResourceManager.class));
        when(azureManagersHolder.getAzureResourceManager().subscriptions()).thenReturn(Mockito.mock(Subscriptions.class));
        when(azureManagersHolder.getAzureResourceManager().subscriptions().getById("qwerty")).thenReturn(Mockito.mock(Subscription.class));
        when(azureManagersHolder.getAzureResourceManager().subscriptions().getById("qwerty").getLocationByRegion(Region.ASIA_EAST)).thenReturn(Mockito.mock(Location.class));

        when(azureManagersHolder.getAzureApiManager()).thenReturn(Mockito.mock(ApiManagementManager.class));
        when(azureManagersHolder.getAzureApiManager().apis()).thenReturn(Mockito.mock(Apis.class));
        SdkConfig sdkconfig = sdkConfigBuilder.sdkConfig();
        assertNotNull(sdkconfig);
    }


    @Test
    void testAuthConfig() {
        agentProperties.setPassword(null);
        agentProperties.setUsername(null);
        agentProperties.setToken("abc");
        when(azureManagersHolder.getApiService()).thenReturn(Mockito.mock(ApiManagementServiceResource.class));
        when(azureManagersHolder.getApiService().regionName()).thenReturn("East Asia");
        when(azureManagersHolder.getAzureResourceManager()).thenReturn(Mockito.mock(ResourceManager.class));
        when(azureManagersHolder.getAzureResourceManager().subscriptions()).thenReturn(Mockito.mock(Subscriptions.class));
        when(azureManagersHolder.getAzureResourceManager().subscriptions().getById("qwerty")).thenReturn(Mockito.mock(Subscription.class));
        when(azureManagersHolder.getAzureResourceManager().subscriptions().getById("qwerty").getLocationByRegion(Region.ASIA_EAST)).thenReturn(Mockito.mock(Location.class));

        when(azureManagersHolder.getAzureApiManager()).thenReturn(Mockito.mock(ApiManagementManager.class));
        when(azureManagersHolder.getAzureApiManager().apis()).thenReturn(Mockito.mock(Apis.class));
        SdkConfig sdkconfig = sdkConfigBuilder.sdkConfig();
        assertNotNull(sdkconfig);
        assertNotNull(sdkconfig.getControlPlaneConfig().getAuthConfig());
        assertEquals(sdkconfig.getControlPlaneConfig().getAuthConfig().getToken(),"abc");
    }


    @Test
    void testTLSConfig() {
        agentProperties.setToken("abc");
        agentProperties.setSslEnabled(true);
        agentProperties.setTrustStorePath("C:\\control-plane\\codebase\\controlplane\\control-plane");
        agentProperties.setTrustStorePassword("abc");
        agentProperties.setTrustStoreType("jks");
        agentProperties.setKeyStoreType("jks");
        agentProperties.setKeyAlias("softwareag");
        agentProperties.setKeyPassword("keys");
        agentProperties.setKeyStorePassword("store");
        agentProperties.setKeyStorePath("C:\\control-plane\\codebase\\controlplane\\control-plane");
        agentProperties.setToken("abc");
        when(azureManagersHolder.getApiService()).thenReturn(Mockito.mock(ApiManagementServiceResource.class));
        when(azureManagersHolder.getApiService().regionName()).thenReturn("East Asia");
        when(azureManagersHolder.getAzureResourceManager()).thenReturn(Mockito.mock(ResourceManager.class));
        when(azureManagersHolder.getAzureResourceManager().subscriptions()).thenReturn(Mockito.mock(Subscriptions.class));
        when(azureManagersHolder.getAzureResourceManager().subscriptions().getById("qwerty")).thenReturn(Mockito.mock(Subscription.class));
        when(azureManagersHolder.getAzureResourceManager().subscriptions().getById("qwerty").getLocationByRegion(Region.ASIA_EAST)).thenReturn(Mockito.mock(Location.class));

        when(azureManagersHolder.getAzureApiManager()).thenReturn(Mockito.mock(ApiManagementManager.class));
        when(azureManagersHolder.getAzureApiManager().apis()).thenReturn(Mockito.mock(Apis.class));
        SdkConfig sdkconfig = sdkConfigBuilder.sdkConfig();
        assertNotNull(sdkconfig);
        assertNotNull(sdkconfig.getControlPlaneConfig().getTlsConfig());
        assertEquals(sdkconfig.getControlPlaneConfig().getTlsConfig().getKeyAlias(),"softwareag");
    }
}

