package com.softwareag.controlplane.agent.azure.context;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServiceResource;
import com.azure.resourcemanager.resources.ResourceManager;
import com.softwareag.controlplane.agent.azure.configuration.AzureProperties;
import com.softwareag.controlplane.agentsdk.core.AgentSdk;
import com.softwareag.controlplane.agentsdk.core.InitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class AzureAuthentication implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    AzureAgentManualContextImpl manualContext;

    @Autowired
    AzureProperties azureProperties;

    @Autowired
    private ApplicationContext context;

    @Autowired
    AzureManagersHolder managerHolder;


    private void authenticate() {
        AzureProfile profile = new AzureProfile(
                azureProperties.getTenantId(),
                azureProperties.getSubscriptionId(), AzureEnvironment.AZURE);

        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(azureProperties.getClientId())
                .clientSecret(azureProperties.getClientSecret())
                .tenantId(azureProperties.getTenantId())
                .build();

        initializeAzureManagers(clientSecretCredential, profile);
    }
    private void initializeAzureManagers(ClientSecretCredential clientSecretCredential, AzureProfile profile) {
        ApiManagementManager apiManager = ApiManagementManager
                .authenticate(clientSecretCredential, profile);

        if(apiManager == null) {
        throw  new RuntimeException("API management service doesn't exist");
        }

        ApiManagementServiceResource apiService = apiManager.apiManagementServices()
                .getByResourceGroup(azureProperties.getResourceGroup(), azureProperties.getApiManagementServiceName());

        if(apiService == null) {
            int exitCode = SpringApplication.exit(context, () -> 0);
            System.exit(exitCode);
        }

        ResourceManager resourceManager = ResourceManager
                .authenticate(clientSecretCredential, profile).withSubscription(azureProperties.getSubscriptionId());
        managerHolder.setApiService(apiService);
        managerHolder.setAzureProfile(profile);
        managerHolder.setAzureApiManager(apiManager);
        managerHolder.setAzureResourceManager(resourceManager);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        authenticate();
        try {
            AgentSdk.initialize(manualContext);
        } catch (InitializationException e) {
            e.printStackTrace();
        }
    }
}
