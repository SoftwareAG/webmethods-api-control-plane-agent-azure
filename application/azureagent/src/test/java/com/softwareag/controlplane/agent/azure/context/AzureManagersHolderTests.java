package com.softwareag.controlplane.agent.azure.context;

import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServiceResource;
import com.azure.resourcemanager.resources.ResourceManager;
import com.softwareag.controlplane.agentsdk.core.client.RestControlPlaneClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;


public class AzureManagersHolderTests {

    AzureManagersHolder azureManagersHolder = new AzureManagersHolder();


    @Test
    void profileHolderTest() {
        azureManagersHolder.setAzureProfile(Mockito.mock(AzureProfile.class));
        assertNotNull(azureManagersHolder.getAzureProfile());
    }

    @Test
    void apiManagerHolderTest() {
        azureManagersHolder.setAzureApiManager(Mockito.mock(ApiManagementManager.class));
        assertNotNull(azureManagersHolder.getAzureApiManager());
    }

    @Test
    void azureResourceManagerTest() {
        azureManagersHolder.setAzureResourceManager(Mockito.mock(ResourceManager.class));
        assertNotNull(azureManagersHolder.getAzureResourceManager());
    }

    @Test
    void restControlPlaneClientTest() {
        azureManagersHolder.setRestControlPlaneClient(Mockito.mock(RestControlPlaneClient.class));
        assertNotNull(azureManagersHolder.getRestControlPlaneClient());
    }

    @Test
    void apiServiceHolderTest() {
        azureManagersHolder.setApiService(Mockito.mock(ApiManagementServiceResource.class));
        assertNotNull(azureManagersHolder.getApiService());
    }

}
