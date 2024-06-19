package com.softwareag.controlplane.agent.azure.functions.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.*;
import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.functions.retrievers.HeartbeatRetrieverImpl;
import com.softwareag.controlplane.agent.azure.functions.utils.DefaultEnvProvider;
import com.softwareag.controlplane.agent.azure.functions.utils.Utils;
import com.softwareag.controlplane.agent.azure.functions.utils.constants.Constants;
import com.softwareag.controlplane.agentsdk.api.client.ControlPlaneClient;
import com.softwareag.controlplane.agentsdk.api.client.SdkClientException;
import com.softwareag.controlplane.agentsdk.api.config.*;
import com.softwareag.controlplane.agentsdk.core.handler.RuntimeRegistrationHandler;
import com.softwareag.controlplane.agentsdk.core.handler.SendHeartbeatHandler;
import com.softwareag.controlplane.agentsdk.core.log.DefaultAgentLogger;
import com.softwareag.controlplane.agentsdk.model.Heartbeat;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Azure Functions with Timer trigger.
 */
public class HeartbeatHandler {
    private SendHeartbeatHandler sendHeartbeatHandler;
    private AzureManagersHolder managerHolder;
    private ControlPlaneClient controlPlaneClient;
    private RuntimeConfig runtimeConfig;
    private SdkConfig sdkConfig;
    private DefaultAgentLogger logger;

    /**
     * Instantiates a new Heartbeat handler.
     *
     * @throws SdkClientException the sdk client exception
     * @throws IOException        the io exception
     */
    public HeartbeatHandler() throws SdkClientException, IOException {
        init();
    }

    /**
     * This function will be invoked periodically according to the specified schedule.
     *
     * @param timerInfo the timer info
     * @param context   the context
     */
    @FunctionName("HeartbeatHandler")
    public void run(
        @TimerTrigger(name = "timerInfo", schedule = "%APICP_SYNC_HEARTBEAT_INTERVAL_CRON%") String timerInfo,
        final ExecutionContext context
    ) {
        if(Utils.isControlplaneActive(controlPlaneClient)) {
            context.getLogger().info("Started heartbeat handler invocation");
            this.sendHeartbeatHandler.handle();
        }
        else
            context.getLogger().log(Level.INFO,"ControlPlane is not available");

        context.getLogger().info("Finished heartbeat handler invocation");
    }

    private void init() throws SdkClientException, IOException {

        // Initialize Configurations
        this.managerHolder = AzureManagersHolder.getInstance();
        Utils.authenticate(managerHolder);
        ControlPlaneConfig controlPlaneConfig = Utils.getControlPlaneConfig();
        runtimeConfig = Utils.getRuntimeConfig(this.managerHolder);
        controlPlaneClient = Utils.getControlplaneClient(controlPlaneConfig,runtimeConfig);
        managerHolder.setRestControlPlaneClient(controlPlaneClient);
        sdkConfig = Utils.getSdkConfig(controlPlaneConfig, runtimeConfig);
        this.logger = DefaultAgentLogger.getInstance(getClass());

        this.sendHeartbeatHandler = new SendHeartbeatHandler.Builder(
                controlPlaneClient,
                new HeartbeatRetrieverImpl())
                .build();

        // Runtime registration
        logger.info("Registering Runtime");
        RuntimeRegistrationHandler registrationHandler = Utils.getRuntimeRegistrationHandler(controlPlaneClient, sdkConfig);
        Object response = registrationHandler.handle();

        Long lastHeartbeatSyncTime = Utils.getLastActionSyncTime(response, Constants.SEND_HEARTBEAT_ACTION);
        if(ObjectUtils.isNotEmpty(lastHeartbeatSyncTime)) {
            Long currentTime = System.currentTimeMillis();
            // If currentTime - interval is far greater than the lastHeartbeatSyncTime, we send inactive heartbeats for the missed intervals.
            if(lastHeartbeatSyncTime < (currentTime - Long.parseLong(DefaultEnvProvider.getEnv(Constants.APICP_SYNC_HEARTBEAT_INTERVAL_SECONDS))*2))
                sendMissingHeartbeats(lastHeartbeatSyncTime, currentTime);
        }

    }

    private void sendMissingHeartbeats(Long lastSyncTime, Long currentTime) {
        List<Heartbeat> heartbeats = getInactiveHeartbeats(lastSyncTime, currentTime,
                this.sdkConfig.getHeartbeatInterval());
        try {
            this.controlPlaneClient.sendHeartbeats(heartbeats);
        } catch (SdkClientException e) {
            this.logger.error("Error occurred while sending missing heartbeats " + e.getMessage(), e);
        }
    }

    private List<Heartbeat> getInactiveHeartbeats(long fromTime, long toTime, long syncInterval) {
        List<Heartbeat> heartbeats = new ArrayList<>();

        long currentTime = fromTime;
        while (currentTime < toTime) {
            Heartbeat heartbeat = new Heartbeat.Builder(this.runtimeConfig.getId())
                    .active(Heartbeat.Status.INACTIVE)
                    .created(currentTime)
                    .build();
            heartbeats.add(heartbeat);
            currentTime += (syncInterval*1000);
        }
        return heartbeats;
    }

}
