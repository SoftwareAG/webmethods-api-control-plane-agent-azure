package com.softwareag.controlplane.agent.azure.functions.retrievers;

import com.softwareag.controlplane.agent.azure.common.handlers.assets.AssetManager;
import com.softwareag.controlplane.agent.azure.functions.utils.DefaultEnvProvider;
import com.softwareag.controlplane.agent.azure.functions.utils.constants.Constants;
import com.softwareag.controlplane.agentsdk.core.handler.SyncAssetsHandler;
import com.softwareag.controlplane.agentsdk.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Assets retriever.
 */
public class AssetsRetrieverImpl implements SyncAssetsHandler.AssetsRetriever{
    /**
     * Gets asset sync actions.
     *
     * @param l  the l
     * @param l1 the l 1
     * @return the asset sync actions
     */
    @Override
    public List<AssetSyncAction<Asset>> getAssetSyncActions(long l, long l1) {
        return convertAPIsToAPISyncAction();
    }

    private List<AssetSyncAction<Asset>> convertAPIsToAPISyncAction() {
        List<AssetSyncAction<Asset>> assetSyncActions = new ArrayList<>();
        List<API> apis = AssetManager.getInstance(DefaultEnvProvider.getEnv(Constants.AZURE_RESOURCE_GROUP), DefaultEnvProvider.getEnv(Constants.AZURE_API_MANAGEMENT_SERVICE_NAME))
                .retrieveAPIs(true, DefaultEnvProvider.getEnv(Constants.AZURE_SUBSCRIPTION_ID), DefaultEnvProvider.getEnv(Constants.APICP_USERNAME));

        for (API api : apis) {
            APISyncAction createAction = new APISyncAction(AssetType.API,
                    AssetSyncAction.SyncType.valueOf(Constants.CREATE), api, System.currentTimeMillis());
            assetSyncActions.add((AssetSyncAction) createAction);
        }
        return assetSyncActions;
    }
}
