package com.softwareag.controlplane.agent.azure.functions.handlers;

import com.azure.resourcemanager.apimanagement.models.ApiContract;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.common.handlers.assets.AssetManager;
import com.softwareag.controlplane.agent.azure.common.utils.AzureAgentUtil;
import com.softwareag.controlplane.agent.azure.functions.retrievers.AssetsRetrieverImpl;
import com.softwareag.controlplane.agent.azure.functions.utils.DefaultEnvProvider;
import com.softwareag.controlplane.agent.azure.functions.utils.Utils;
import com.softwareag.controlplane.agent.azure.functions.utils.constants.Constants;
import com.softwareag.controlplane.agentsdk.api.client.ControlPlaneClient;
import com.softwareag.controlplane.agentsdk.api.client.SdkClientException;
import com.softwareag.controlplane.agentsdk.api.config.ControlPlaneConfig;
import com.softwareag.controlplane.agentsdk.api.config.RuntimeConfig;
import com.softwareag.controlplane.agentsdk.api.config.SdkConfig;
import com.softwareag.controlplane.agentsdk.core.assetsync.dispatcher.AssetSyncDispatcherImpl;
import com.softwareag.controlplane.agentsdk.core.assetsync.dispatcher.AssetSyncDispatcherProvider;
import com.softwareag.controlplane.agentsdk.core.handler.RuntimeRegistrationHandler;
import com.softwareag.controlplane.agentsdk.core.handler.SyncAssetsHandler;
import com.softwareag.controlplane.agentsdk.core.log.DefaultAgentLogger;
import com.softwareag.controlplane.agentsdk.model.*;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * The type Assets handler.
 */
public class AssetsHandler {
    private SyncAssetsHandler syncAssetsHandler;
    private AzureManagersHolder managerHolder;
    private ControlPlaneClient controlPlaneClient;
    private DefaultAgentLogger logger;
    private AssetManager assetManager;

    /**
     * Instantiates a new Assets handler.
     *
     * @throws SdkClientException the sdk client exception
     * @throws IOException        the io exception
     */
    public AssetsHandler() throws SdkClientException, IOException {
        init();
    }

    /**
     * This function will be invoked periodically according to the specified schedule.
     *
     * @param content the content
     * @param context the context
     * @throws SdkClientException the sdk client exception
     */
    @FunctionName("AssetsHandler")
    public void run(
            @EventGridTrigger(name = "event") String content,
            final ExecutionContext context
    ) throws SdkClientException {
        if(Utils.isControlplaneActive(controlPlaneClient)) {
            context.getLogger().info("Started asset handler invocation");

            //Syncing assets via AssetSync Dispatcher
            AssetSyncDispatcherImpl assetSyncDispatcherProvider = AssetSyncDispatcherProvider.createAssetSyncDispatcher(this.controlPlaneClient, logger);
            AssetSyncAction assetSyncAction = generateAssetSyncAction(content, DefaultEnvProvider.getEnv(Constants.AZURE_SUBSCRIPTION_ID),
                    DefaultEnvProvider.getEnv(Constants.AZURE_API_MANAGEMENT_SERVICE_NAME),DefaultEnvProvider.getEnv(Constants.APICP_USERNAME));
            if(ObjectUtils.isNotEmpty(assetSyncAction))
                assetSyncDispatcherProvider.dispatch(assetSyncAction);

            context.getLogger().info("Finished asset handler invocation");
        }
        else {
            context.getLogger().log(Level.INFO, "ControlPlane is not available");
        }
    }

    private void init() throws SdkClientException, IOException {

        // Initialize Configurations
        this.managerHolder = AzureManagersHolder.getInstance();
        this.assetManager = AssetManager.getInstance(DefaultEnvProvider.getEnv(Constants.AZURE_RESOURCE_GROUP),
                DefaultEnvProvider.getEnv(Constants.AZURE_API_MANAGEMENT_SERVICE_NAME));
        Utils.authenticate(managerHolder);
        ControlPlaneConfig controlPlaneConfig = Utils.getControlPlaneConfig();
        RuntimeConfig runtimeConfig = Utils.getRuntimeConfig(this.managerHolder);
        controlPlaneClient = Utils.getControlplaneClient(controlPlaneConfig,runtimeConfig);
        managerHolder.setRestControlPlaneClient(controlPlaneClient);
        SdkConfig sdkConfig = Utils.getSdkConfig(controlPlaneConfig, runtimeConfig);
        logger = DefaultAgentLogger.getInstance(getClass());

        // Runtime registration
        logger.info("Registering Runtime");
        RuntimeRegistrationHandler registrationHandler = Utils.getRuntimeRegistrationHandler(controlPlaneClient, sdkConfig);
        Object response = registrationHandler.handle();


        // Publish assets only if the lastAssetSyncTime is null (as it could be the first time registration).
        Long lastAssetSyncTime = Utils.getLastActionSyncTime(response, Constants.SYNC_ASSET_ACTION);
        if(ObjectUtils.isEmpty(lastAssetSyncTime)){
            // This handler will be used to publish assets for the first time.
            this.syncAssetsHandler = new SyncAssetsHandler.Builder(new AssetsRetrieverImpl(),
                    Long.parseLong(DefaultEnvProvider.getEnv(Constants.APICP_SYNC_ASSETS_INTERVAL_SECONDS)))
                    .assetSyncDispatcher(AssetSyncDispatcherProvider.createAssetSyncDispatcher(controlPlaneClient, logger))
                    .build();
            this.syncAssetsHandler.handle();
        }
    }


    /**
     * Generate asset sync action asset sync action.
     *
     * @param event                    the event
     * @param subscriptionId           the subscription id
     * @param apiManagementServiceName the api management service name
     * @param userName                 the user name
     * @return the asset sync action
     */
    public AssetSyncAction generateAssetSyncAction(String event, String subscriptionId, String apiManagementServiceName, String userName) {
        Map<String, String> apiInfo = parseWebhookEvent(event);
        String eventType = getSyncEventType(apiInfo.get("eventType"));
        if(eventType!=null && eventType.equals(com.softwareag.controlplane.agent.azure.common.constants.Constants.DELETE)) {
            String apiId = AzureAgentUtil.constructAPIId(apiInfo.get("apiName"),
                    subscriptionId, apiManagementServiceName);
            return new APISyncAction(AssetType.API,
                    AssetSyncAction.SyncType.valueOf(eventType), apiId, System.currentTimeMillis());
        }

        API api = assetManager.retrieveAPIwithId(apiInfo.get("azureApiId"), subscriptionId, userName);
        if(ObjectUtils.isNotEmpty(api)){
            return new APISyncAction(AssetType.API,
                    AssetSyncAction.SyncType.valueOf(eventType), api, System.currentTimeMillis());
        }
        return null;
    }

    private String getSyncEventType(String eventType) {
        switch (eventType){
            case "APICreated":
                return com.softwareag.controlplane.agent.azure.common.constants.Constants.CREATE;
            case "APIUpdated":
                return com.softwareag.controlplane.agent.azure.common.constants.Constants.UPDATE;
            case "APIDeleted":
                return com.softwareag.controlplane.agent.azure.common.constants.Constants.DELETE;
        }
        return null;
    }

    private Map<String,String> parseWebhookEvent(String event) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String,String> apiInfo = new HashMap<>();
        try {
            JsonNode jsonNode = objectMapper.readTree(event);

            String eventType = jsonNode.get("eventType").asText();
            String eventTypeSuffix = eventType.substring(eventType.lastIndexOf('.')+1);
            String apiId = jsonNode.get("data").get("resourceUri").asText();
            String apiName = apiId.split("/apis/")[1].split(";")[0];

            apiInfo.put("azureApiId", apiId);
            apiInfo.put("apiName", apiName);
            apiInfo.put("eventType", eventTypeSuffix);

            return apiInfo;
        } catch (IOException e) {
            logger.info("Couldn't parse the Webhook event");
        }
        return null;
    }
}

