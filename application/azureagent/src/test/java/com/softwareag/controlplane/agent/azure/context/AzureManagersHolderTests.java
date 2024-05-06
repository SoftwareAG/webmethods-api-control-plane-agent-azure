package com.softwareag.controlplane.agent.azure.context;

import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServiceResource;
import com.softwareag.controlplane.agent.azure.context.AzureManagersHolder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;


public class AzureManagersHolderTests {

    AzureManagersHolder azureManagersHolder = new AzureManagersHolder();


    @Test
    void profileHolderTest() {
        azureManagersHolder.setAzureProfile(Mockito.mock(AzureProfile.class));
        assertThat(azureManagersHolder.getAzureProfile()).isNotNull();
    }

    @Test
    void apiManagerHolderTest() {
        azureManagersHolder.setAzureApiManager(Mockito.mock(ApiManagementManager.class));
        assertThat(azureManagersHolder.getAzureApiManager()).isNotNull();
    }

    @Test
    void apiServiceHolderTest() {
        azureManagersHolder.setApiService(Mockito.mock(ApiManagementServiceResource.class));
        assertThat(azureManagersHolder.getApiService()).isNotNull();
    }

}
