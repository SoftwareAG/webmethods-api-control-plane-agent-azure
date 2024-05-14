package com.softwareag.controlplane.agent.azure.helpers;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.apimanagement.models.ApiContract;
import com.softwareag.controlplane.agent.azure.configuration.AzureProperties;
import com.softwareag.controlplane.agent.azure.context.AzureManagersHolder;
import com.softwareag.controlplane.agentsdk.model.Heartbeat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class HeartbeatGenerator {

    @Autowired
    private AzureProperties azureProperties;

    @Autowired
    private AzureManagersHolder azureManagersHolder;

    public Heartbeat generateHeartBeat(String runtimeId) {
        Heartbeat heartbeat = new Heartbeat.Builder(runtimeId).build();
        heartbeat.setCreated(new Date().getTime());
        PagedIterable<ApiContract> apis = azureManagersHolder.getAzureApiManager().apis().listByService(azureProperties.getResourceGroup(),
                azureProperties.getApiManagementServiceName());
        if(apis.stream().findAny().isPresent()) {
            heartbeat.setActive(1);
        } else {
            heartbeat.setActive(0);
        }

        return heartbeat;
    }
}
