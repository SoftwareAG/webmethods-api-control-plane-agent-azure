package com.softwareag.controlplane.agent.azure.helpers;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.apimanagement.models.ApiContract;
import com.azure.resourcemanager.apimanagement.models.TagContract;
import com.softwareag.controlplane.agent.azure.Constants;
import com.softwareag.controlplane.agent.azure.configuration.AgentProperties;
import com.softwareag.controlplane.agent.azure.configuration.AzureProperties;
import com.softwareag.controlplane.agent.azure.context.AzureManagersHolder;
import com.softwareag.controlplane.agentsdk.api.client.SdkClientException;
import com.softwareag.controlplane.agentsdk.core.cache.CacheManager;
import com.softwareag.controlplane.agentsdk.model.API;
import com.softwareag.controlplane.agentsdk.model.APISyncAction;
import com.softwareag.controlplane.agentsdk.model.Asset;
import com.softwareag.controlplane.agentsdk.model.AssetSyncAction;
import com.softwareag.controlplane.agentsdk.model.AssetType;
import com.softwareag.controlplane.agentsdk.model.Status;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class APIRetriever {
    @Autowired
    private AzureProperties azureProperties;

    @Autowired
    private AgentProperties agentProperties;

    @Autowired
    private AzureManagersHolder azureManagersHolder;


    public List<API> retrieveAPIs(boolean toUpdateCache) {
        PagedIterable<ApiContract> apis =
                azureManagersHolder.getAzureApiManager().apis().listByService( azureProperties.getResourceGroup(),
                azureProperties.getApiManagementServiceName());

        return convertAsAPIModel(apis, toUpdateCache);
    }

    private List<API> convertAsAPIModel(PagedIterable<ApiContract> apis, boolean toUpdateCache) {
        List<API> allAPIs = new ArrayList<>();
        for (ApiContract azureAPI : apis) {
            String azureAPIId = AzureAgentUtil.constructAPIId(azureAPI.name(),
                    azureProperties.getTenantId(), azureProperties.getApiManagementServiceName());
            String azureAPIType = azureAPI.apiType() == null ? "REST" : azureAPI.apiType().toString().toUpperCase();
            if (validAPICreation(azureAPI, azureAPIType)) {
                String versionSetId = azureAPI.apiVersionSetId() != null ?
                        azureAPI.apiVersionSetId() : null;
                API api = (API) new API.Builder(azureAPIId, API.Type.valueOf(azureAPIType))
                        .version(azureAPI.apiVersion())
                        .versionSetId(versionSetId)
                    //    .runtimeAPIId(azureAPI.id())
                        .status(Status.ACTIVE)
                        .owner(AzureAgentUtil.getOwnerInfo(agentProperties.getUsername()))
                        .tags(getAPITags(azureAPI.name()))
                        .description(azureAPI.description())
                        .name(azureAPI.displayName())
                        .build();
                allAPIs.add(api);
                if(toUpdateCache) CacheManager.getInstance().put(AssetType.API, azureAPIId, api);
            }
        }
        return allAPIs;
    }

    private Set<String> getAPITags(String name) {
        Set<String> tags = azureManagersHolder.getAzureApiManager().tags().listByApi(azureProperties.getResourceGroup(),
                        azureProperties.getApiManagementServiceName(), name).stream()
                .map(TagContract::displayName).collect(Collectors.toSet());
        return tags;
    }

    private boolean validAPICreation(ApiContract azureAPI, String azureAPIType) {
        List<String> enumNames = Stream.of(API.Type.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ObjectUtils.isNotEmpty(azureAPI.isCurrent()) && azureAPI.isCurrent()
                && enumNames.contains(azureAPIType);
    }

    public List<AssetSyncAction<Asset>> getAPIUpdates(long fromTimestamp) {
        List<AssetSyncAction<Asset>> assetSyncActions = new ArrayList<>();
        // API loaded from Control Plane into cache
        List<API> allAPIs = retrieveAPIs(false);
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
        if(ObjectUtils.isNotEmpty(CacheManager.getInstance().get(AssetType.API))) return;
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
}
