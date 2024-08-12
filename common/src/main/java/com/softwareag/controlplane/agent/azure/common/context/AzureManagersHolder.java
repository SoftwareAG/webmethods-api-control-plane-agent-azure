/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.common.context;

import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServiceResource;
import com.azure.resourcemanager.resources.ResourceManager;
import com.softwareag.controlplane.agentsdk.api.client.ControlPlaneClient;


/**
 * The Azure managers holder holds instantiates the azure managers holder.
 */
public class AzureManagersHolder {

    private static AzureManagersHolder azureManagersHolder;
    private AzureManagersHolder(){
    }

    /**
     * Gets instance of Azure Manager Holder class.
     *
     * @return the instance
     */
    public static AzureManagersHolder getInstance() {
        if(azureManagersHolder != null) {
            return azureManagersHolder;
        }
        azureManagersHolder = new AzureManagersHolder();
        return azureManagersHolder;
    }

    /**
     * The Profile instance.
     */
    static ThreadLocal<AzureProfile> profile = new InheritableThreadLocal<>();

    /**
     * Sets azure profile instance.
     *
     * @param azureProfile the azure profile instance
     */
    public void setAzureProfile(AzureProfile azureProfile) {
        profile.set(azureProfile);
    }

    /**
     * Gets azure profile instance.
     *
     * @return the azure profile instance
     */
    public AzureProfile getAzureProfile() {
        return profile.get();
    }

    /**
     * The Azure api manager.
     */
    static ThreadLocal<ApiManagementManager> azureAPIManager = new InheritableThreadLocal<>();

    /**
     * Sets azure api manager instance.
     *
     * @param apiManager the api manager instance
     */
    public void setAzureApiManager(ApiManagementManager apiManager) {
        azureAPIManager.set(apiManager);
    }

    /**
     * Gets azure api manager instance.
     *
     * @return the azure api manager instance
     */
    public ApiManagementManager getAzureApiManager() {
        return azureAPIManager.get();
    }

    /**
     * The Api service resource instance.
     */
    static ThreadLocal<ApiManagementServiceResource> apiServiceResource = new InheritableThreadLocal<>();

    /**
     * Sets api service instance.
     *
     * @param apiManager the api manager instance
     */
    public void setApiService(ApiManagementServiceResource apiManager) {
        apiServiceResource.set(apiManager);
    }

    /**
     * Gets api service instance.
     *
     * @return the api service instance
     */
    public ApiManagementServiceResource getApiService() {
        return apiServiceResource.get();
    }

    /**
     * The Resource manager instance.
     */
    static ThreadLocal<ResourceManager> resourceManager = new InheritableThreadLocal<>();

    /**
     * Sets azure resource manager instance.
     *
     * @param azureResourceManager the azure resource manager instance
     */
    public void setAzureResourceManager(ResourceManager azureResourceManager) { resourceManager.set(azureResourceManager); }

    /**
     * Gets azure resource manager instance.
     *
     * @return the azure resource manager instance
     */
    public ResourceManager getAzureResourceManager() {
        return resourceManager.get();
    }

    /**
     * The Rest control plane client instance.
     */
    static ThreadLocal<ControlPlaneClient> restControlPlaneClient = new InheritableThreadLocal<>();

    /**
     * Sets rest control plane client instance.
     *
     * @param azureResourceManager the azure resource manager instance.
     */
    public void setRestControlPlaneClient(ControlPlaneClient azureResourceManager) { restControlPlaneClient.set(azureResourceManager); }

    /**
     * Gets rest control plane client instance.
     *
     * @return the rest control plane client instance
     */
    public ControlPlaneClient getRestControlPlaneClient() {
        return restControlPlaneClient.get();
    }
}
