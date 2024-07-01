package com.softwareag.controlplane.agent.azure.functions.retrievers;

import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.common.handlers.assets.AssetManager;
import com.softwareag.controlplane.agent.azure.functions.utils.DefaultEnvProvider;
import com.softwareag.controlplane.agent.azure.functions.utils.Utils;
import com.softwareag.controlplane.agent.azure.functions.utils.constants.Constants;
import com.softwareag.controlplane.agentsdk.core.handler.SyncAssetsHandler;
import com.softwareag.controlplane.agentsdk.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the AssetsRetriever interface for retrieving assets.
 * This implementation is intended to be used with the SyncAssetsHandler class.
 */
public class AssetsRetrieverImpl implements SyncAssetsHandler.AssetsRetriever {

    /**
     * This method is responsible to get the list of create asset sync actions to be
     * published to Control Plane.
     *
     * @param l  - timestamp from which the assets need to be queried. In
     *           milliseconds.
     * @param l1 - timestamp upto which the assets need to be queried. In
     *           milliseconds.
     * @return List of {@link AssetSyncAction}
     */
    @Override
    public List<AssetSyncAction<Asset>> getAssetSyncActions(long l, long l1) {
        return convertAPIsToAPISyncAction();
    }

    // In this method, we fetch all the existing apis from Azure API Management
    // service, and convert them to AssetSyncActions
    // to publish them to controlplane via AssetSyncHandler.
    private List<AssetSyncAction<Asset>> convertAPIsToAPISyncAction() {
        List<AssetSyncAction<Asset>> assetSyncActions = new ArrayList<>();
        /*
         * The Executors are used here to calculate policy count from different levels
         * such as
         * API, Operation, ALL APIS(Global) and Product level policies.
         * To ensure this activity doesn't affect the agent performance,
         * sync of policy count is scheduled at 15 minutes delay.
         * In the meantime, we expect APIs to be published into Control plane.
         */
        Thread policyCountThread = new Thread(() -> AssetManager
                .getInstance(DefaultEnvProvider.getEnv(Constants.AZURE_RESOURCE_GROUP),
                        DefaultEnvProvider.getEnv(Constants.AZURE_API_MANAGEMENT_SERVICE_NAME))
                .apiPolicyCountDispatch(DefaultEnvProvider.getEnv(Constants.AZURE_SUBSCRIPTION_ID),
                        DefaultEnvProvider.getEnv(Constants.APICP_USERNAME),
                        Utils.getControlplaneClient(Utils.getControlPlaneConfig(),
                                Utils.getRuntimeConfig(AzureManagersHolder.getInstance()))));
        policyCountThread.start();

        // cache update is set to false, as azure functions use event subscription for
        // API updates. Policy count is set to 0 on initial publish.
        List<API> apis = AssetManager
                .getInstance(DefaultEnvProvider.getEnv(Constants.AZURE_RESOURCE_GROUP),
                        DefaultEnvProvider.getEnv(Constants.AZURE_API_MANAGEMENT_SERVICE_NAME))
                .retrieveAPIs(false, DefaultEnvProvider.getEnv(Constants.AZURE_SUBSCRIPTION_ID),
                        DefaultEnvProvider.getEnv(Constants.APICP_USERNAME), false);

        // We iterate through all the apis, and generate an assetSyncAction of syncType
        // CREATE.
        for (API api : apis) {
            APISyncAction createAction = new APISyncAction(AssetType.API,
                    AssetSyncAction.SyncType.valueOf(Constants.CREATE), api, System.currentTimeMillis());
            assetSyncActions.add((AssetSyncAction) createAction);
        }
        return assetSyncActions;
    }
}
