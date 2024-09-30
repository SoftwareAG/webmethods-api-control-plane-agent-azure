/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.common.handlers.heartbeat;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.apimanagement.models.ApiContract;
import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agentsdk.model.Heartbeat;


import java.util.Date;

/**
 * The Heartbeat manager to generate heartbeat for Control Plane.
 */
public final class HeartbeatManager {
    private static HeartbeatManager heartbeatManager;

    private AzureManagersHolder azureManagersHolder;

    private HeartbeatManager() {
        this.azureManagersHolder = AzureManagersHolder.getInstance();
    }

    /**
     * creates instance of class heartbeat manager.
     *
     * @return the instance
     */
    public static HeartbeatManager getInstance() {
        if(heartbeatManager != null) {
            return heartbeatManager;
        }
        heartbeatManager = new HeartbeatManager();
        return heartbeatManager;
    }

    /**
     * Generates heartbeat object for Control plane SDK.
     * Active heartbeat will be sent, If azure api management has atleast one api.
     * InActive heartbet will be sent, If azure api management has zero api.
     *
     * @param runtimeId                the runtime id
     * @param resourceGroup            the resource group
     * @param apiManagementServiceName the api management service name
     * @return the heartbeat
     */
    public Heartbeat generateHeartBeat(String runtimeId, String resourceGroup, String apiManagementServiceName) {
        Heartbeat heartbeat = new Heartbeat.Builder(runtimeId).build();
        heartbeat.setCreated(new Date().getTime());
        PagedIterable<ApiContract> apis = azureManagersHolder.getAzureApiManager().apis().listByService(resourceGroup,
                apiManagementServiceName);
        if(apis.stream().findAny().isPresent()) {
            heartbeat.setActive(1);
        } else {
            heartbeat.setActive(0);
        }

        return heartbeat;
    }
}
