package com.softwareag.controlplane.agent.azure.context;

import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServiceResource;
import com.azure.resourcemanager.resources.ResourceManager;
import com.softwareag.controlplane.agentsdk.api.client.ControlPlaneClient;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class AzureManagersHolder {
    static ThreadLocal<AzureProfile> profile = new InheritableThreadLocal<>();

    public void setAzureProfile(AzureProfile azureProfile) {
        profile.set(azureProfile);
    }

    public AzureProfile getAzureProfile() {
        return profile.get();
    }

    static ThreadLocal<ApiManagementManager> azureAPIManager = new InheritableThreadLocal<>();

    public void setAzureApiManager(ApiManagementManager apiManager) {
        azureAPIManager.set(apiManager);
    }

    public ApiManagementManager getAzureApiManager() {
        return azureAPIManager.get();
    }

    static ThreadLocal<ApiManagementServiceResource> apiServiceResource = new InheritableThreadLocal<>();

    public void setApiService(ApiManagementServiceResource apiManager) {
        apiServiceResource.set(apiManager);
    }

    public ApiManagementServiceResource getApiService() {
        return apiServiceResource.get();
    }

    static ThreadLocal<ResourceManager> resourceManager = new InheritableThreadLocal<>();

    public void setAzureResourceManager(ResourceManager azureResourceManager) { resourceManager.set(azureResourceManager); }

    public ResourceManager getAzureResourceManager() {
        return resourceManager.get();
    }

    static ThreadLocal<ControlPlaneClient> restControlPlaneClient = new InheritableThreadLocal<>();

    public void setRestControlPlaneClient(ControlPlaneClient azureResourceManager) { restControlPlaneClient.set(azureResourceManager); }

    public ControlPlaneClient getRestControlPlaneClient() {
        return restControlPlaneClient.get();
    }
}
