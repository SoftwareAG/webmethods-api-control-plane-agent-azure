package com.softwareag.controlplane.agent.azure.helpers;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.*;
import com.softwareag.controlplane.agent.azure.configuration.AzureProperties;
import com.softwareag.controlplane.agent.azure.context.AzureManagersHolder;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PolicyRetrieverTests {
    @Spy
    private AzureProperties azureProperties;

    @Spy
    private AzureManagersHolder azureManagersHolder;

    @InjectMocks
    PolicyRetriever policyRetriever;

    @BeforeEach
    public void setup(){
        MockitoAnnotations.openMocks(this);

        when(azureManagersHolder.getAzureApiManager()).thenReturn(mock(ApiManagementManager.class));
        when(azureManagersHolder.getAzureApiManager().apis()).thenReturn(mock(Apis.class));

        List<PolicyContract> policyContractList = new ArrayList<>();
        PolicyContract policyContract = mock(PolicyContract.class);
        PolicyCollection policyCollection = mock(PolicyCollection.class);


        when(policyContract.id()).thenReturn("/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.ApiManagement/service/apimService1/apis/a1");
        when(policyContract.value()).thenReturn("<!--\n" +
                "    IMPORTANT:\n" +
                "    - Policy elements can appear only within the <inbound>, <outbound>, <backend> section elements.\n" +
                "    - Only the <forward-request> policy element can appear within the <backend> section element.\n" +
                "    - To apply a policy to the incoming request (before it is forwarded to the backend service), place a corresponding policy element within the <inbound> section element.\n" +
                "    - To apply a policy to the outgoing response (before it is sent back to the caller), place a corresponding policy element within the <outbound> section element.\n" +
                "    - To add a policy position the cursor at the desired insertion point and click on the round button associated with the policy.\n" +
                "    - To remove a policy, delete the corresponding policy statement from the policy document.\n" +
                "    - Policies are applied in the order of their appearance, from the top down.\n" +
                "-->\n" +
                "<policies>\n" +
                "  <inbound>\n" +
                "    <rate-limit-by-key calls=\"1000\" renewal-period=\"60\" counter-key=\"@(context.Subscription?.Key ?? &quot;anonymous&quot;)\" />\n" +
                "    <mock-response status-code=\"101\" content-type=\"application/json\" />\n" +
                "  </inbound>\n" +
                "  <backend>\n" +
                "    <forward-request />\n" +
                "  </backend>\n" +
                "  <outbound />\n" +
                "  <on-error />\n" +
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
        when(azureManagersHolder.getAzureApiManager().productPolicies().listByProduct(azureProperties.getResourceGroup(),
                azureProperties.getApiManagementServiceName(),"unlimited")).thenReturn(policyCollection);

        //Global Policy Mock
        when(azureManagersHolder.getAzureApiManager().policies()).thenReturn(mock(Policies.class));
        when(azureManagersHolder.getAzureApiManager().policies().listByService(azureProperties.getResourceGroup(),
                azureProperties.getApiManagementServiceName())).thenReturn(policyCollection);


        //API Policy Mock
        when(azureManagersHolder.getAzureApiManager().apiPolicies()).thenReturn(mock(ApiPolicies.class));
        when(azureManagersHolder.getAzureApiManager().apiPolicies().listByApi(azureProperties.getResourceGroup(), azureProperties.getApiManagementServiceName(), "api_id")).thenReturn(policyCollection);
        when(azureManagersHolder.getAzureApiManager().apiPolicies().listByApi(azureProperties.getResourceGroup(), azureProperties.getApiManagementServiceName(), "api_id_1")).thenReturn(policyCollection1);

        //Operation Policy Mock
        when(azureManagersHolder.getAzureApiManager().apiOperations()).thenReturn(mock(ApiOperations.class));
        when(azureManagersHolder.getAzureApiManager().apiOperationPolicies()).thenReturn(mock(ApiOperationPolicies.class));
        when(azureManagersHolder.getAzureApiManager().apiOperationPolicies().listByOperation(azureProperties.getResourceGroup(), azureProperties.getApiManagementServiceName(), "api_id", "addnumberget")).thenReturn(policyCollection);
        when(azureManagersHolder.getAzureApiManager().apiOperationPolicies().listByOperation(azureProperties.getResourceGroup(), azureProperties.getApiManagementServiceName(), "api_id_1", "addnumberget")).thenReturn(policyCollection);


    }
    @Test
    @Order(1)
    void retrieveGlobalandProductPolicy(){
        int count = policyRetriever.getGlobalProductPolicyCount();
        assertEquals(count,6);
    }

    @Test
    @Order(2)
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

        when(azureManagersHolder.getAzureApiManager().apiOperations().listByApi(azureProperties.getResourceGroup(), azureProperties.getApiManagementServiceName(), "api_id")).thenReturn(mockApiOperations);



        int count = policyRetriever.getPoliciesCount("api_id");
        assertEquals(count,6);
    }

    @Test
    @Order(3)
    void retrieveApiOperationPolicyCountInnerTag(){
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

        when(azureManagersHolder.getAzureApiManager().apiOperations().listByApi(azureProperties.getResourceGroup(), azureProperties.getApiManagementServiceName(), "api_id_1")).thenReturn(mockApiOperations);



        int count = policyRetriever.getPoliciesCount("api_id_1");
        assertEquals(count,9);
    }
}
