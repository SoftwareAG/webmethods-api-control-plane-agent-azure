package com.softwareag.controlplane.agent.azure.common.context;

import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServiceResource;
import com.azure.resourcemanager.resources.ResourceManager;
import com.softwareag.controlplane.agentsdk.core.client.RestControlPlaneClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AzureManagersHolderTests {

    AzureManagersHolder azureManagersHolder = AzureManagersHolder.getInstance();


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
