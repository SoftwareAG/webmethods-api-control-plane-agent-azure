package com.softwareag.controlplane.agent.azure.common.handlers.assets;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.apimanagement.models.ApiContract;
import com.azure.resourcemanager.apimanagement.models.TagContract;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwareag.controlplane.agent.azure.common.constants.Constants;
import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.common.utils.AzureAgentUtil;
import com.softwareag.controlplane.agentsdk.api.client.SdkClientException;
import com.softwareag.controlplane.agentsdk.core.cache.CacheManager;
import com.softwareag.controlplane.agentsdk.core.log.DefaultAgentLogger;
import com.softwareag.controlplane.agentsdk.model.API;
import com.softwareag.controlplane.agentsdk.model.APISyncAction;
import com.softwareag.controlplane.agentsdk.model.Asset;
import com.softwareag.controlplane.agentsdk.model.AssetSyncAction;
import com.softwareag.controlplane.agentsdk.model.AssetType;
import com.softwareag.controlplane.agentsdk.model.Status;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Asset manager is to query API from Azure and sent to API Control plane
 */
public class AssetManager {

    private AzureManagersHolder azureManagersHolder;
    private PolicyRetriever policyRetriever;
    private String resourceGroup;
    private String apiManagementServiceName;
    private DefaultAgentLogger logger;
    private static AssetManager assetManager;


    private AssetManager(String resourceGroup, String apiManagementResourceName) {
        this.azureManagersHolder = AzureManagersHolder.getInstance();
        this.policyRetriever = PolicyRetriever.getInstance(resourceGroup, apiManagementResourceName);
        this.resourceGroup = resourceGroup;
        this.apiManagementServiceName = apiManagementResourceName;
        this.logger = DefaultAgentLogger.getInstance(this.getClass());
    }

    /**
     * Gets instance.
     *
     * @param resourceGroup             the resource group
     * @param apiManagementResourceName the api management resource name
     * @return the instance
     */
    public static AssetManager getInstance(String resourceGroup, String apiManagementResourceName) {
        if (assetManager != null) {
            return assetManager;
        }
        assetManager = new AssetManager(resourceGroup, apiManagementResourceName);
        return assetManager;
    }


    /**
     * Retrieves all API from Azure using Azure SDK listByService method
     * And Azure APIContract is converted to Control plane SDK API Object.
     * List of APIs is returned to Control Plane. All these API are put in-memory caching.
     *
     * @param toUpdateCache  the to update cache
     * @param subscriptionId the subscription id
     * @param userName       the user name
     * @return the list
     */
    public List<API> retrieveAPIs(boolean toUpdateCache, String subscriptionId, String userName) {
        PagedIterable<ApiContract> apis =
                azureManagersHolder.getAzureApiManager().apis().listByService(this.resourceGroup,
                        this.apiManagementServiceName);

        return convertAsAPIModel(apis, toUpdateCache, subscriptionId, userName);
    }

    private List<API> convertAsAPIModel(PagedIterable<ApiContract> apis, boolean toUpdateCache, String subscriptionId, String userName) {
        List<API> allAPIs = new ArrayList<>();
        if (ObjectUtils.isEmpty(apis)) return allAPIs;

        //fetching the count of global and product policy count
        int globalProductPolicyCount = policyRetriever.getGlobalProductPolicyCount();

        apis.stream().forEach(azureAPI -> {
            String azureAPIId = AzureAgentUtil.constructAPIId(azureAPI.name(),
                    subscriptionId, this.apiManagementServiceName);
            // Azure apiType has values such as SOAP, GRAPHQL , for REST values left to be empty
            String azureAPIType = azureAPI.apiType() == null ? "REST" : azureAPI.apiType().toString().toUpperCase();
            if (validAPICreation(azureAPI, azureAPIType)) {
                int policyCount = policyRetriever.getPoliciesCount(azureAPI.name()) + globalProductPolicyCount;
                String versionSetId = azureAPI.apiVersionSetId() != null ?
                        azureAPI.apiVersionSetId() : null;
                API api = (API) new API.Builder(azureAPIId, API.Type.valueOf(azureAPIType))
                        .version(azureAPI.apiVersion() != null ? azureAPI.apiVersion() : Constants.ORIGINAL_VERSION)
                        .versionSetId(versionSetId)
                        .runtimeAPIId(azureAPI.id())
                        .policiesCount(policyCount)
                        .status(Status.ACTIVE)
                        .owner(AzureAgentUtil.getOwnerInfo(userName))
                        .tags(getAPITags(azureAPI.name()))
                        .description(azureAPI.description())
                        .name(azureAPI.displayName())
                        .build();
                allAPIs.add(api);

                if (toUpdateCache) CacheManager.getInstance().put(AssetType.API, azureAPIId, api);
            }
        });

        return allAPIs;
    }

    /**
     * Convert from Azure API contract to Control Plane SDK API object.
     *
     * @param azureAPI       the azure api
     * @param subscriptionId the subscription id
     * @param userName       the user name
     * @return the api
     */
    public API convertToAPI(ApiContract azureAPI, String subscriptionId, String userName) {
        String azureAPIId = AzureAgentUtil.constructAPIId(azureAPI.name(),
                subscriptionId, this.apiManagementServiceName);
        // Azure apiType has values such as SOAP, GRAPHQL , for REST values left to be empty
        String azureAPIType = azureAPI.apiType() == null ? "REST" : azureAPI.apiType().toString().toUpperCase();
        if (validAPICreation(azureAPI, azureAPIType)) {
            int globalProductPolicyCount = policyRetriever.getGlobalProductPolicyCount();
            int policyCount = policyRetriever.getPoliciesCount(azureAPI.name()) + globalProductPolicyCount;
            String versionSetId = azureAPI.apiVersionSetId() != null ?
                    azureAPI.apiVersionSetId() : null;
            return (API) new API.Builder(azureAPIId, API.Type.valueOf(azureAPIType))
                    .version(azureAPI.apiVersion() != null ? azureAPI.apiVersion() : Constants.ORIGINAL_VERSION)
                    .versionSetId(versionSetId)
                    .runtimeAPIId(azureAPI.id())
                    .policiesCount(policyCount)
                    .status(Status.ACTIVE)
                    .owner(AzureAgentUtil.getOwnerInfo(userName))
                    .tags(getAPITags(azureAPI.name()))
                    .description(azureAPI.description())
                    .name(azureAPI.displayName())
                    .build();

        }
        return null;
    }


    private Set<String> getAPITags(String name) {
        PagedIterable<TagContract> tagContracts =
                azureManagersHolder.getAzureApiManager().tags().listByApi(this.resourceGroup,
                        this.apiManagementServiceName, name);
        if (tagContracts == null)
            return null;
        return tagContracts.stream()
                .map(TagContract::displayName).collect(Collectors.toSet());
    }

    private boolean validAPICreation(ApiContract azureAPI, String azureAPIType) {
        List<String> enumNames = Stream.of(API.Type.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        // Control-plane doesn't support revision concept. So only current revision apis from Azure are published.
        return ObjectUtils.isNotEmpty(azureAPI.isCurrent()) && azureAPI.isCurrent()
                && enumNames.contains(azureAPIType);
    }

    /**
     * Gets all apis from Azure and API updates are identified using internal caching.
     * Once API updates are collected, AssetSyncAction is generated for the API,
     * with corresponding SYNC event such as CREATE, UPDATE, DELETE
     *
     * @param fromTimestamp  the from timestamp
     * @param subscriptionId the subscription id
     * @param userName       the user name
     * @return the api updates
     */
    public List<AssetSyncAction<Asset>> getAPIUpdates(long fromTimestamp, String subscriptionId, String userName) {
        List<AssetSyncAction<Asset>> assetSyncActions = new ArrayList<>();
        // API loaded from Control Plane into cache, in case of agent re-start/down time handling
        List<API> allAPIs = retrieveAPIs(false, subscriptionId, userName);
        populateAPICache();
        for (API api : allAPIs) {
            API cachedAPI = (API) CacheManager.getInstance().get(AssetType.API, api.getId());
            AssetSyncAction apiSyncAction;
            if (cachedAPI == null) {
                apiSyncAction = new APISyncAction(AssetType.API, AssetSyncAction.SyncType.CREATE, api,
                        System.currentTimeMillis());
                assetSyncActions.add(apiSyncAction);
                CacheManager.getInstance().put(AssetType.API, api.getId(), api);
            } else if (cachedAPI.isUpdated(api)) {
                apiSyncAction = new APISyncAction(AssetType.API, AssetSyncAction.SyncType.UPDATE, api,
                        System.currentTimeMillis());
                assetSyncActions.add(apiSyncAction);
                CacheManager.getInstance().put(AssetType.API, api.getId(), api);
            }
        }

        Map<String, Asset> cachedApisMap = CacheManager.getInstance().get(AssetType.API);
        List<String> apisToBeDeleted = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(cachedApisMap)) {
            apisToBeDeleted = new ArrayList(cachedApisMap.keySet());
            List<String> newAPIKeys = allAPIs.stream().map(API::getId).collect(Collectors.toList());
            apisToBeDeleted.removeAll(newAPIKeys);
        }

        apisToBeDeleted.forEach((key) -> {
            AssetSyncAction deleteAction = new APISyncAction(AssetType.API, AssetSyncAction.SyncType.DELETE, key,
                    System.currentTimeMillis());
            assetSyncActions.add(deleteAction);
            CacheManager.getInstance().clear(AssetType.API, key);
        });
        return assetSyncActions;
    }

    private void populateAPICache() {
        if (ObjectUtils.isNotEmpty(CacheManager.getInstance().get(AssetType.API))) return;
        List<API> apis;
        try {
            apis = azureManagersHolder.getRestControlPlaneClient().getAllApis();
            if (apis != null && !apis.isEmpty()) {
                for (API api : apis) {
                    CacheManager.getInstance().put(AssetType.API, api.getId(), api);
                }
            }
        } catch (SdkClientException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *  Retrieves specific API with Id from Azure using Azure SDK getById method
     *  And Azure APIContract is converted to Control plane SDK API Object
     *
     * @param azureApiId     the azure api id
     * @param subscriptionId the subscription id
     * @param userName       the user name
     * @return the api
     */
    public API retrieveAPIwithId(String azureApiId, String subscriptionId, String userName) {
        ApiContract azureApi = AzureManagersHolder.getInstance().getAzureApiManager().apis().getById(azureApiId);
        if (ObjectUtils.isNotEmpty(azureApi)) {
            return convertToAPI(azureApi, subscriptionId, userName);
        }
        return null;
    }

}
