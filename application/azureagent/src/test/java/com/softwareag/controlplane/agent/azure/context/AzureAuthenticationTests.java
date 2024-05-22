package com.softwareag.controlplane.agent.azure.context;

import com.azure.core.management.Region;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServiceResource;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServices;
import com.azure.resourcemanager.apimanagement.models.Apis;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.Location;
import com.azure.resourcemanager.resources.models.Subscription;
import com.azure.resourcemanager.resources.models.Subscriptions;
import com.softwareag.controlplane.agent.azure.configuration.AzureProperties;
import com.softwareag.controlplane.agent.azure.configuration.SDKConfigBuilder;
import com.softwareag.controlplane.agentsdk.api.config.RuntimeConfig;
import com.softwareag.controlplane.agentsdk.api.config.SdkConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class AzureAuthenticationTests {

    @Mock
    SDKConfigBuilder sdkConfigBuilder;

    @Mock
    AzureAgentManualContextImpl manualContext;

    @Mock
    AzureProperties azureProperties;

    @Mock
    private ApplicationContext context;

    @Mock
    AzureManagersHolder azureManagersHolder;

    @InjectMocks
    AzureAuthentication azureAuthentication;

    @Mock
    ApiManagementManager apiManagementManager;

    @Mock
    ApiManagementServices apiManagementServices;

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

        when(azureProperties.getTenantId()).thenReturn("arajRuntimeId");
        when(azureProperties.getClientId()).thenReturn("arajRuntimeId");
        when(azureProperties.getClientSecret()).thenReturn("arajRuntimeId");
        when(azureProperties.getSubscriptionId()).thenReturn("arajRuntimeId");
        when(azureProperties.getResourceGroup()).thenReturn("arajRuntimeId");
        when(azureProperties.getApiManagementServiceName()).thenReturn("arajRuntimeId");

    }

}
