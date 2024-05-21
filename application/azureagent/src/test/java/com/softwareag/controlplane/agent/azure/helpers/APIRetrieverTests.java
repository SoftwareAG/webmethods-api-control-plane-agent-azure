package com.softwareag.controlplane.agent.azure.helpers;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiContract;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServiceResource;
import com.azure.resourcemanager.apimanagement.models.ApiType;
import com.azure.resourcemanager.apimanagement.models.Apis;
import com.azure.resourcemanager.apimanagement.models.Tags;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.Location;
import com.azure.resourcemanager.resources.models.Subscription;
import com.azure.resourcemanager.resources.models.Subscriptions;
import com.softwareag.controlplane.agent.azure.configuration.AgentProperties;
import com.softwareag.controlplane.agent.azure.configuration.AzureProperties;
import com.softwareag.controlplane.agent.azure.context.AzureManagersHolder;
import com.softwareag.controlplane.agentsdk.core.cache.CacheManager;
import com.softwareag.controlplane.agentsdk.core.client.RestControlPlaneClient;
import com.softwareag.controlplane.agentsdk.model.API;
import com.softwareag.controlplane.agentsdk.model.Asset;
import com.softwareag.controlplane.agentsdk.model.AssetSyncAction;
import com.softwareag.controlplane.agentsdk.model.AssetType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class APIRetrieverTests {
    @Spy
    private AzureProperties azureProperties;

    @Spy
    private AgentProperties agentProperties;

    @Spy
    private AzureManagersHolder azureManagersHolder;


    @Mock
    PolicyRetriever policyRetriever;

    @InjectMocks
    APIRetriever apiRetriever;

    @Test
    @Order(3)
    void retrieveApisTest() {
        List<API> apiList = apiRetriever.retrieveAPIs(true);
        assertFalse(apiList.isEmpty());
        assertEquals(apiList.get(0).getType(), API.Type.SOAP);
        assertEquals(apiList.get(0).getName(), "Hello API");
        assertNotNull(CacheManager.getInstance().get(AssetType.API));
    }

    @Test
    @Order(2)
    void getAPIUpdatesTest() {
        List<AssetSyncAction<Asset>> assetSyncActions = apiRetriever.getAPIUpdates(System.currentTimeMillis());
        assert(assetSyncActions.isEmpty());
        assertEquals(assetSyncActions.size(),0);
        assertNotNull(CacheManager.getInstance().get(AssetType.API));
    }

    @SneakyThrows
    @Test
    @Order(1)
    void getAPISyncCreateTest() {
        List<API> apiList = new ArrayList<>();
        apiList.add(new API.Builder("api1", API.Type.REST).build());
        apiList.add(new API.Builder("api2", API.Type.SOAP).build());
        apiList.add(new API.Builder("arajRuntimeId_arajRuntimeId_hello-api", API.Type.GRAPHQL).build());
        when(azureManagersHolder.getRestControlPlaneClient()).thenReturn(Mockito.mock(RestControlPlaneClient.class));
        when(azureManagersHolder.getRestControlPlaneClient().getAllApis()).thenReturn(apiList);
        List<AssetSyncAction<Asset>> assetSyncActions = apiRetriever.getAPIUpdates(System.currentTimeMillis());
        assertFalse(assetSyncActions.isEmpty());
        assertEquals(assetSyncActions.size(),4);
        assertNotNull(CacheManager.getInstance().get(AssetType.API));
    }

    @BeforeEach
    @SneakyThrows
    private void setup() {
        MockitoAnnotations.openMocks(this);

        when(azureProperties.getTenantId()).thenReturn("arajRuntimeId");
        when(azureProperties.getClientId()).thenReturn("arajRuntimeId");
        when(azureProperties.getClientSecret()).thenReturn("arajRuntimeId");
        when(azureProperties.getSubscriptionId()).thenReturn("arajRuntimeId");
        when(azureProperties.getResourceGroup()).thenReturn("arajRuntimeId");
        when(azureProperties.getApiManagementServiceName()).thenReturn("arajRuntimeId");

        List<API> apiList = new ArrayList<>();
        apiList.add(new API.Builder("api1", API.Type.REST).build());
        apiList.add(new API.Builder("api2", API.Type.SOAP).build());
        when(azureManagersHolder.getRestControlPlaneClient()).thenReturn(Mockito.mock(RestControlPlaneClient.class));
        when(azureManagersHolder.getRestControlPlaneClient().getAllApis()).thenReturn(apiList);

        when(azureManagersHolder.getApiService()).thenReturn(Mockito.mock(ApiManagementServiceResource.class));
        when(azureManagersHolder.getApiService().regionName()).thenReturn("East Asia");
        when(azureManagersHolder.getAzureResourceManager()).thenReturn(Mockito.mock(ResourceManager.class));
        when(azureManagersHolder.getAzureResourceManager().subscriptions()).thenReturn(Mockito.mock(Subscriptions.class));
        when(azureManagersHolder.getAzureResourceManager().subscriptions().getById("qwerty")).thenReturn(Mockito.mock(Subscription.class));
        when(azureManagersHolder.getAzureResourceManager().subscriptions().getById("qwerty").getLocationByRegion(Region.ASIA_EAST)).thenReturn(Mockito.mock(Location.class));
        when(azureManagersHolder.getAzureApiManager()).thenReturn(Mockito.mock(ApiManagementManager.class));
        when(azureManagersHolder.getAzureApiManager().apis()).thenReturn(Mockito.mock(Apis.class));

        when(azureManagersHolder.getAzureApiManager().tags()).thenReturn(Mockito.mock(Tags.class));
        when(azureManagersHolder.getAzureApiManager().tags().listByApi(any(),any(),any())).thenReturn(Mockito.mock(PagedIterable.class));

        ApiContract apiContract = Mockito.mock(ApiContract.class);
        ApiContract apiContract1 = Mockito.mock(ApiContract.class);
        List<ApiContract> apiContractsIterator = new ArrayList<>();
        apiContractsIterator.add(apiContract);
        when(apiContract.apiType()).thenReturn(ApiType.SOAP);
        when(apiContract.id()).thenReturn("/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.ApiManagement/service/apimService1/apis/a1");
        when(apiContract.name()).thenReturn("hello-api");
        when(apiContract.displayName()).thenReturn("Hello API");
        when(apiContract.apiVersionSetId()).thenReturn("c48f96c9-1385-4e2d-b410-5ab591ce0fc4");
        when(apiContract.isCurrent()).thenReturn(true);
        when(apiContract.description()).thenReturn("This is hello world description");

        when(apiContract1.apiType()).thenReturn(ApiType.GRAPHQL);
        when(apiContract1.id()).thenReturn("/subscriptions/abc/resourceGroups/rg1/providers/Microsoft" +
                ".ApiManagement/service/apimService1/apis/a1");
        when(apiContract1.name()).thenReturn("echo-api");
        when(apiContract1.displayName()).thenReturn("Echo API");
        when(apiContract1.apiVersionSetId()).thenReturn("c48f96c9-1355-4e2d-b410-5ab591ce0fc4");
        when(apiContract1.isCurrent()).thenReturn(true);
        when(apiContract1.description()).thenReturn("This is echo api description");
        apiContractsIterator.add(apiContract1);
        when(azureManagersHolder.getAzureApiManager().apis().listByService(azureProperties.getResourceGroup(),
                azureProperties.getApiManagementServiceName())).thenReturn(Mockito.mock(PagedIterable.class));

        when(azureManagersHolder.getAzureApiManager().apis().listByService(azureProperties.getResourceGroup(),
                azureProperties.getApiManagementServiceName()).stream()).thenReturn(apiContractsIterator.stream());

        //mocking the policy count for the api
        when(policyRetriever.getPoliciesCount(any())).thenReturn(3);
        when(policyRetriever.getGlobalProductPolicyCount()).thenReturn(4);

    }


}
