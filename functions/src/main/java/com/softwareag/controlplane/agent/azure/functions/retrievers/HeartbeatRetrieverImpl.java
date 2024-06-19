package com.softwareag.controlplane.agent.azure.functions.retrievers;

import com.softwareag.controlplane.agent.azure.common.handlers.heartbeat.HeartbeatManager;
import com.softwareag.controlplane.agent.azure.functions.utils.DefaultEnvProvider;
import com.softwareag.controlplane.agent.azure.functions.utils.Utils;
import com.softwareag.controlplane.agent.azure.functions.utils.constants.Constants;
import com.softwareag.controlplane.agentsdk.core.handler.SendHeartbeatHandler;
import com.softwareag.controlplane.agentsdk.model.Heartbeat;

/**
 * The type Heartbeat retriever.
 */
public final class HeartbeatRetrieverImpl implements SendHeartbeatHandler.HeartbeatRetriever {
    /**
     * Retrieves Heartbeat from the API runtime.
     *
     * @return {@link Heartbeat}
     */
    @Override
    public Heartbeat getHeartbeat() {
        return HeartbeatManager.getInstance().generateHeartBeat(Utils.getRuntimeId(), DefaultEnvProvider.getEnv(Constants.AZURE_RESOURCE_GROUP),
                DefaultEnvProvider.getEnv(Constants.AZURE_API_MANAGEMENT_SERVICE_NAME));
    }
}
