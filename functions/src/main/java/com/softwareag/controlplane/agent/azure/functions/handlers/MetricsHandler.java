package com.softwareag.controlplane.agent.azure.functions.handlers;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.functions.retrievers.AssetsRetrieverImpl;
import com.softwareag.controlplane.agent.azure.functions.retrievers.MetricsRetrieverImpl;
import com.softwareag.controlplane.agent.azure.functions.utils.DefaultEnvProvider;
import com.softwareag.controlplane.agent.azure.functions.utils.Utils;
import com.softwareag.controlplane.agent.azure.functions.utils.constants.Constants;
import com.softwareag.controlplane.agentsdk.api.client.ControlPlaneClient;
import com.softwareag.controlplane.agentsdk.api.client.SdkClientException;
import com.softwareag.controlplane.agentsdk.api.config.ControlPlaneConfig;
import com.softwareag.controlplane.agentsdk.api.config.RuntimeConfig;
import com.softwareag.controlplane.agentsdk.api.config.SdkConfig;
import com.softwareag.controlplane.agentsdk.core.assetsync.dispatcher.AssetSyncDispatcherProvider;
import com.softwareag.controlplane.agentsdk.core.handler.RuntimeRegistrationHandler;
import com.softwareag.controlplane.agentsdk.core.handler.SendMetricsHandler;
import com.softwareag.controlplane.agentsdk.core.handler.SyncAssetsHandler;
import com.softwareag.controlplane.agentsdk.core.log.DefaultAgentLogger;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.logging.Level;

/**
 * This MetricsHandler class serves the Asset sync action for the FAAS of
 * implementation.
 * This class contains the method that will be invoked by Azure Function.
 */
public class MetricsHandler {
    private SendMetricsHandler sendMetricsHandler;
    private AzureManagersHolder managerHolder;
    private ControlPlaneClient controlPlaneClient;
    private DefaultAgentLogger logger;

    /**
     * Instantiates a new Metrics handler.
     *
     * @throws SdkClientException if there is an error in the AgentSDK client.
     * @throws IOException        if an I/O error occurs during initialization.
     */
    public MetricsHandler() throws SdkClientException, IOException {
        init();
    }

    /**
     * This function will be invoked periodically according to the specified
     * schedule.
     *
     * @param timerInfo the timer info
     * @param context   the context
     */
    @FunctionName("MetricsHandler")
    public void run(
            @TimerTrigger(name = "timerInfo", schedule = "%APICP_SYNC_METRICS_INTERVAL_CRON%") String timerInfo,
            final ExecutionContext context) {
        if (Utils.isControlplaneActive(controlPlaneClient)) {
            context.getLogger().info("Started metric handler invocation");
            this.sendMetricsHandler.handle();
        } else
            context.getLogger().log(Level.INFO, "ControlPlane is not available");

        context.getLogger().info("Finished metric handler invocation");
    }

    private void init() throws SdkClientException, IOException {

        // Initialize Configurations
        this.managerHolder = AzureManagersHolder.getInstance();
        Utils.authenticate(managerHolder);
        ControlPlaneConfig controlPlaneConfig = Utils.getControlPlaneConfig();
        RuntimeConfig runtimeConfig = Utils.getRuntimeConfig(this.managerHolder);
        controlPlaneClient = Utils.getControlplaneClient(controlPlaneConfig, runtimeConfig);
        managerHolder.setRestControlPlaneClient(controlPlaneClient);
        SdkConfig sdkConfig = Utils.getSdkConfig(controlPlaneConfig, runtimeConfig);
        logger = DefaultAgentLogger.getInstance(getClass());

        // Runtime registration
        logger.info("Registering Runtime");
        RuntimeRegistrationHandler registrationHandler = Utils.getRuntimeRegistrationHandler(controlPlaneClient,
                sdkConfig);
        Object response = registrationHandler.handle();

        Long lastMetricSyncTime = Utils.getLastActionSyncTime(response, Constants.SEND_METRIC_ACTION);
        Long lastAssetSyncTime = Utils.getLastActionSyncTime(response, Constants.SYNC_ASSET_ACTION);
        // if the lastAssetSyncTime is null, which means the assets have not yet
        // published to the controlplane.
        if (ObjectUtils.isEmpty(lastAssetSyncTime)) {
            // Create a syncAssetHandler and pass the respective AssetsRetriever
            SyncAssetsHandler syncAssetsHandler = new SyncAssetsHandler.Builder(new AssetsRetrieverImpl(),
                    Long.parseLong(DefaultEnvProvider.getEnv(Constants.APICP_SYNC_ASSETS_INTERVAL_SECONDS)))
                    .assetSyncDispatcher(
                            AssetSyncDispatcherProvider.createAssetSyncDispatcher(controlPlaneClient, logger))
                    .build();
            // This handle method publishes the assets to the controlplane.
            syncAssetsHandler.handle();
        }

        // if the lastMetricSyncTime is not null, create a sendMetricsHandler with the
        // fromTime as the lastMetricSyncTime.
        if (!ObjectUtils.isEmpty(lastMetricSyncTime)) {
            this.sendMetricsHandler = new SendMetricsHandler.Builder(this.controlPlaneClient,
                    new MetricsRetrieverImpl(),
                    Utils.getRuntimeId(),
                    Long.parseLong(DefaultEnvProvider.getEnv(Constants.APICP_SYNC_METRICS_INTERVAL_SECONDS)))
                    .fromTime(lastMetricSyncTime)
                    .build();
        } else {
            // if the lastMetricSyncTime is null, which means metric sync is happening for
            // the first so do not pass the fromTime.
            this.sendMetricsHandler = new SendMetricsHandler.Builder(this.controlPlaneClient,
                    new MetricsRetrieverImpl(),
                    Utils.getRuntimeId(),
                    Long.parseLong(DefaultEnvProvider.getEnv(Constants.APICP_SYNC_METRICS_INTERVAL_SECONDS)))
                    .build();
        }
    }
}
