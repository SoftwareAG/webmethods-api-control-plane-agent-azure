/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.common.handlers;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiOperationPolicies;
import com.azure.resourcemanager.apimanagement.models.ApiOperations;
import com.azure.resourcemanager.apimanagement.models.ApiPolicies;
import com.azure.resourcemanager.apimanagement.models.Apis;
import com.azure.resourcemanager.apimanagement.models.OperationContract;
import com.azure.resourcemanager.apimanagement.models.Policies;
import com.azure.resourcemanager.apimanagement.models.PolicyCollection;
import com.azure.resourcemanager.apimanagement.models.PolicyContentFormat;
import com.azure.resourcemanager.apimanagement.models.PolicyContract;
import com.azure.resourcemanager.apimanagement.models.ProductPolicies;
import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.common.handlers.assets.PolicyRetriever;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class APIPolicyRetrieverTests {

    @Mock
    private AzureManagersHolder azureManagersHolder;

    PolicyRetriever policyRetriever;

    MockedStatic<AzureManagersHolder> mockAzureManagerHolder;

    @BeforeEach
    public void setup(){
        MockitoAnnotations.openMocks(this);
        mockAzureManagerHolder = Mockito.mockStatic(AzureManagersHolder.class);
        mockAzureManagerHolder.when(() -> AzureManagersHolder.getInstance())
                .thenReturn(azureManagersHolder);

        policyRetriever = PolicyRetriever.getInstance("azure_grp1", "service_name1");

        when(azureManagersHolder.getAzureApiManager()).thenReturn(mock(ApiManagementManager.class));
        when(azureManagersHolder.getAzureApiManager().apis()).thenReturn(mock(Apis.class));

        List<PolicyContract> policyContractList = new ArrayList<>();
        PolicyContract policyContract = mock(PolicyContract.class);
        PolicyCollection policyCollection = mock(PolicyCollection.class);

        when(policyContract.id()).thenReturn("/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.ApiManagement/service/apimService1/apis/a1");
        when(policyContract.value()).thenReturn("<!--\n" +
                "    IMPORTANT:\n" +
                "    - Policy elements can appear only within the <inbound>, <outbound>, <backend> section elements.\n" +
                "    - To apply a policy to the incoming request (before it is forwarded to the backend service), place a corresponding policy element within the <inbound> section element.\n" +
                "    - To apply a policy to the outgoing response (before it is sent back to the caller), place a corresponding policy element within the <outbound> section element.\n" +
                "    - To add a policy, place the cursor at the desired insertion point and select a policy from the sidebar.\n" +
                "    - To remove a policy, delete the corresponding policy statement from the policy document.\n" +
                "    - Position the <base> element within a section element to inherit all policies from the corresponding section element in the enclosing scope.\n" +
                "    - Remove the <base> element to prevent inheriting policies from the corresponding section element in the enclosing scope.\n" +
                "    - Policies are applied in the order of their appearance, from the top down.\n" +
                "    - Comments within policy elements are not supported and may disappear. Place your comments between policy elements or at a higher level scope.\n" +
                "-->\n" +
                "<policies>\n" +
                "    <inbound>\n" +
                "        <cors allow-credentials=\"true\">\n" +
                "            <allowed-origins>\n" +
                "                <origin>https://poclanos.developer.azure-api.net</origin>\n" +
                "            </allowed-origins>\n" +
                "            <allowed-methods preflight-result-max-age=\"300\">\n" +
                "                <method>*</method>\n" +
                "            </allowed-methods>\n" +
                "            <allowed-headers>\n" +
                "                <header>*</header>\n" +
                "            </allowed-headers>\n" +
                "            <expose-headers>\n" +
                "                <header>*</header>\n" +
                "            </expose-headers>\n" +
                "        </cors>\n" +
                "    </inbound>\n" +
                "    <backend>\n" +
                "        <forward-request />\n" +
                "    </backend>\n" +
                "    <outbound />\n" +
                "    <on-error />\n" +
                "</policies>");
        when(policyContract.name()).thenReturn("policy");
        when(policyContract.format()).thenReturn(PolicyContentFormat.XML);
        policyContractList.add(policyContract);
        when(policyCollection.value()).thenReturn(policyContractList);
        when(policyCollection.count()).thenReturn(1L);

        List<PolicyContract> policyContractList1 = new ArrayList<>();
        PolicyContract policyContract1 = mock(PolicyContract.class);
        PolicyCollection policyCollection1=mock(PolicyCollection.class);
        when(policyContract1.id()).thenReturn("/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.ApiManagement/service/apimService1/apis/a1");

        //here the policy count is 6 (the inner tag should not be considered)
        when(policyContract1.value()).thenReturn("<policies>\n" +
                "  <inbound>\n" +
                "    <base />\n" +
                "    <rate-limit-by-key calls=\"1000\" renewal-period=\"60\" counter-key=\"@(context.Subscription?.Key ?? &quot;anonymous&quot;)\" />\n" +
                "    <ip-filter action=\"allow\">\n" +
                "      <address-range from=\"10.10.20.30\" to=\"10.10.20.80\">\n" +
                "\t\t<open>\n" +
                "\t\t\t<test1>\n" +
                "\t\t\t</test1>\n" +
                "\t\t</open>\n" +
                "\t  </address-range>\n" +
                "    </ip-filter>\n" +
                "  </inbound>\n" +
                "  <backend>\n" +
                "    <base />\n" +
                "\t<rate-limit-by-key calls=\"1000\" renewal-period=\"60\" counter-key=\"@(context.Subscription?.Key ?? &quot;anonymous&quot;)\" />\n" +
                "    <ip-filter action=\"allow\">\n" +
                "      <address-range from=\"10.10.20.30\" to=\"10.10.20.80\">\n" +
                "\t\t<open>\n" +
                "\t\t\t<test1>\n" +
                "\t\t\t</test1>\n" +
                "\t\t</open>\n" +
                "\t  </address-range>\n" +
                "    </ip-filter>\n" +
                "  </backend>\n" +
                "  <outbound>\n" +
                "    <base />\n" +
                "\t<ip-filter action=\"allow\">\n" +
                "      <address-range from=\"10.10.20.30\" to=\"10.10.20.80\">\n" +
                "\t\t<open>\n" +
                "\t\t\t<test1>\n" +
                "\t\t\t</test1>\n" +
                "\t\t</open>\n" +
                "\t  </address-range>\n" +
                "    </ip-filter>\n" +
                "  </outbound>\n" +
                "  <on-error>\n" +
                "    <base />\n" +
                "\t<ip-filter action=\"allow\">\n" +
                "      <address-range from=\"10.10.20.30\" to=\"10.10.20.80\">\n" +
                "\t\t<open>\n" +
                "\t\t\t<test1>\n" +
                "\t\t\t</test1>\n" +
                "\t\t</open>\n" +
                "\t  </address-range>\n" +
                "    </ip-filter>\n" +
                "  </on-error>\n" +
                "</policies>");
        when(policyContract1.name()).thenReturn("policy");
        when(policyContract1.format()).thenReturn(PolicyContentFormat.XML);
        policyContractList1.add(policyContract1);
        when(policyCollection1.value()).thenReturn(policyContractList1);
        when(policyCollection1.count()).thenReturn(1L);

        //Product Policy Mock
        when(azureManagersHolder.getAzureApiManager().productPolicies()).thenReturn(mock(ProductPolicies.class));
        when(azureManagersHolder.getAzureApiManager().productPolicies().listByProduct(anyString(),
                anyString(),eq("unlimited"))).thenReturn(policyCollection);

        //Global Policy Mock
        when(azureManagersHolder.getAzureApiManager().policies()).thenReturn(mock(Policies.class));
        when(azureManagersHolder.getAzureApiManager().policies().listByService(anyString(),
                anyString())).thenReturn(policyCollection);


        //API Policy Mock
        when(azureManagersHolder.getAzureApiManager().apiPolicies()).thenReturn(mock(ApiPolicies.class));
        when(azureManagersHolder.getAzureApiManager().apiPolicies().listByApi(anyString(), anyString(), eq("api_id"))).thenReturn(policyCollection);
        when(azureManagersHolder.getAzureApiManager().apiPolicies().listByApi(anyString(), anyString(), eq("api_id_1"))).thenReturn(policyCollection1);

        //Operation Policy Mock
        when(azureManagersHolder.getAzureApiManager().apiOperations()).thenReturn(mock(ApiOperations.class));
        when(azureManagersHolder.getAzureApiManager().apiOperationPolicies()).thenReturn(mock(ApiOperationPolicies.class));
        when(azureManagersHolder.getAzureApiManager().apiOperationPolicies().listByOperation(anyString(), anyString(), eq("api_id"), eq("addnumberget"))).thenReturn(policyCollection);
        when(azureManagersHolder.getAzureApiManager().apiOperationPolicies().listByOperation(anyString(), anyString(), eq("api_id_1"), eq("addnumberget"))).thenReturn(policyCollection);


    }

    @AfterEach
    public void cleanUp() {
        mockAzureManagerHolder.close();
    }

    @Test
    @Order(3)
    void retrieveGlobalandProductPolicy(){
        int count = policyRetriever.getGlobalProductPolicyCount();
        assertEquals(count,4);
    }

    @Test
    @Order(1)
    void retrieveApiOperationPolicyCount(){
        List<OperationContract> operationContractList= new ArrayList<>();

        OperationContract operationContract = mock(OperationContract.class);
        when(operationContract.id()).thenReturn("/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.ApiManagement/service/apimService1/apis/a1");
        when(operationContract.name()).thenReturn("addnumberget");
        operationContractList.add(operationContract);

        PagedResponse<OperationContract> mockPagedResponse = mock(PagedResponse.class);
        when(mockPagedResponse.getValue()).thenReturn(operationContractList);

        // Mock PagedIterable to return an iterator over the PagedResponse
        PagedIterable<OperationContract> mockApiOperations = mock(PagedIterable.class);
        when(mockApiOperations.iterator()).thenReturn(operationContractList.iterator());
        when(mockApiOperations.iterableByPage()).thenReturn(Arrays.asList(mockPagedResponse));

        when(azureManagersHolder.getAzureApiManager().apiOperations().listByApi(anyString(), anyString(), eq("api_id"))).thenReturn(mockApiOperations);

        int count = policyRetriever.getPoliciesCount("api_id");
        assertEquals(count,4);
    }

    @Test
    @Order(2)
    void apiOperationPolicyCountInnerTag(){
        List<OperationContract> operationContractList= new ArrayList<>();

        OperationContract operationContract = mock(OperationContract.class);
        when(operationContract.id()).thenReturn("/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.ApiManagement/service/apimService1/apis/a1");
        when(operationContract.name()).thenReturn("addnumberget");
        operationContractList.add(operationContract);

        PagedResponse<OperationContract> mockPagedResponse = mock(PagedResponse.class);
        when(mockPagedResponse.getValue()).thenReturn(operationContractList);

        // Mock PagedIterable to return an iterator over the PagedResponse
        PagedIterable<OperationContract> mockApiOperations = mock(PagedIterable.class);
        when(mockApiOperations.iterator()).thenReturn(operationContractList.iterator());
        when(mockApiOperations.iterableByPage()).thenReturn(Arrays.asList(mockPagedResponse));

        when(azureManagersHolder.getAzureApiManager().apiOperations().listByApi(anyString(), anyString(), eq("api_id_1"))).thenReturn(mockApiOperations);

        int count = policyRetriever.getPoliciesCount("api_id_1");
        assertEquals(count,6); //TO DO : PMR count adapted for junit success
    }
}