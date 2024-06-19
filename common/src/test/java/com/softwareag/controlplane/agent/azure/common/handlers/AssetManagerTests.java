package com.softwareag.controlplane.agent.azure.common.handlers;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.Tags;
import com.azure.resourcemanager.apimanagement.models.*;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.Location;
import com.azure.resourcemanager.resources.models.Subscription;
import com.azure.resourcemanager.resources.models.Subscriptions;
import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.common.handlers.assets.AssetManager;
import com.softwareag.controlplane.agent.azure.common.handlers.assets.PolicyRetriever;
import com.softwareag.controlplane.agentsdk.api.client.SdkClientException;
import com.softwareag.controlplane.agentsdk.core.cache.CacheManager;
import com.softwareag.controlplane.agentsdk.core.client.RestControlPlaneClient;
import com.softwareag.controlplane.agentsdk.model.API;
import com.softwareag.controlplane.agentsdk.model.Asset;
import com.softwareag.controlplane.agentsdk.model.AssetSyncAction;
import com.softwareag.controlplane.agentsdk.model.AssetType;
//import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearAllCaches;
import static org.mockito.Mockito.when;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AssetManagerTests {
    @Mock
    AzureManagersHolder azureManagersHolder;

    @Mock
    PolicyRetriever policyRetriever;
    @InjectMocks
    AssetManager assetManager;

    MockedStatic<AzureManagersHolder> mockAzureManagerHolder;

    MockedStatic<PolicyRetriever> mockPolicyRetriever;

    @Test
    @Order(3)
    void retrieveApisTest() {
        List<API> apiList = assetManager.retrieveAPIs(true, "test_subscription", "azure-user");
        assertFalse(apiList.isEmpty());
        assertEquals(apiList.get(0).getType(), API.Type.SOAP);
        assertEquals(apiList.get(0).getName(), "Hello API");
        assertNotNull(CacheManager.getInstance().get(AssetType.API));
    }

    @Test
    @Order(2)
    void getAPIUpdatesTest() {
        List<AssetSyncAction<Asset>> assetSyncActions = assetManager.getAPIUpdates(System.currentTimeMillis(),"test_subscription", "azure-user");
        assert(assetSyncActions.isEmpty());
        assertEquals(assetSyncActions.size(),0);
        assertNotNull(CacheManager.getInstance().get(AssetType.API));
    }

    @Test
    @Order(1)
    void getAPISyncCreateTest() throws SdkClientException {
        List<API> apiList = new ArrayList<>();
        apiList.add(new API.Builder("api1", API.Type.REST).build());
        apiList.add(new API.Builder("api2", API.Type.SOAP).build());
        apiList.add(new API.Builder("arajRuntimeId_arajRuntimeId_hello-api", API.Type.GRAPHQL).build());
        when(azureManagersHolder.getRestControlPlaneClient()).thenReturn(Mockito.mock(RestControlPlaneClient.class));
        when(azureManagersHolder.getRestControlPlaneClient().getAllApis()).thenReturn(apiList);
        List<AssetSyncAction<Asset>> assetSyncActions = assetManager.getAPIUpdates(System.currentTimeMillis(),"test_subscription", "azure-user");
        assertFalse(assetSyncActions.isEmpty());
        assertEquals(assetSyncActions.size(),5);
        assertNotNull(CacheManager.getInstance().get(AssetType.API));
    }

    @BeforeEach
    private void setup() throws SdkClientException {
        MockitoAnnotations.openMocks(this);

        mockAzureManagerHolder = Mockito.mockStatic(AzureManagersHolder.class);
        mockAzureManagerHolder.when(() -> AzureManagersHolder.getInstance())
                .thenReturn(azureManagersHolder);
        mockPolicyRetriever = Mockito.mockStatic(PolicyRetriever.class);
        mockPolicyRetriever.when(() -> PolicyRetriever.getInstance("azure-grp", "service_name"))
                .thenReturn(policyRetriever);
        assetManager = AssetManager.getInstance("azure-grp", "service_name");
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
        when(azureManagersHolder.getAzureApiManager().apis().listByService(anyString(),
                anyString())).thenReturn(Mockito.mock(PagedIterable.class));

        when(azureManagersHolder.getAzureApiManager().apis().listByService(anyString(),
                anyString()).stream()).thenReturn(apiContractsIterator.stream());

        //mocking the policy count for the api
        when(policyRetriever.getPoliciesCount(any())).thenReturn(3);
        when(policyRetriever.getGlobalProductPolicyCount()).thenReturn(4);





    }

    @AfterEach
    public void cleanUp() {
        if(mockAzureManagerHolder.isClosed()) {
            System.out.println("closed");
        }
        mockAzureManagerHolder.close();
        mockPolicyRetriever.close();

    }

    @AfterAll
    public void teardown() {
        Mockito.reset(policyRetriever);
        Mockito.reset(azureManagersHolder);
        clearAllCaches();

    }


}
