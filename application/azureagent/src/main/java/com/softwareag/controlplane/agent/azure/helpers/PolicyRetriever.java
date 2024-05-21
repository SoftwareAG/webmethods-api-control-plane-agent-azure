package com.softwareag.controlplane.agent.azure.helpers;


import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.apimanagement.models.OperationContract;
import com.azure.resourcemanager.apimanagement.models.PolicyCollection;
import com.softwareag.controlplane.agent.azure.configuration.AzureProperties;
import com.softwareag.controlplane.agent.azure.context.AzureManagersHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;

@Component
public class PolicyRetriever {

    @Autowired
    private AzureProperties azureProperties;

    @Autowired
    private AzureManagersHolder azureManagersHolder;

    /**
     * Retrieves the combined count of Global policies and Product policies.
     *
     * @return the total count of Global policies and Product policies.
     */
    public int getGlobalProductPolicyCount(){
        //Global Policy and Product Policy will be common for all the APIs
        PolicyCollection productPolicies = azureManagersHolder.getAzureApiManager().productPolicies().listByProduct(azureProperties.getResourceGroup(), azureProperties.getApiManagementServiceName(), "unlimited");
        int productPolicyCount = parsePolicies(productPolicies);
        PolicyCollection globalPolicy =azureManagersHolder.getAzureApiManager().policies().listByService(azureProperties.getResourceGroup(),azureProperties.getApiManagementServiceName());
        int globalPolicyCount = parsePolicies(globalPolicy);

        return productPolicyCount+globalPolicyCount;
    }

    /**
     * Retrieves the combined count of API policies and API Operation policies.
     *
     * @param apiId the identifier of the API for which the policy count is to be fetched
     * @return the total count of API policies and API Operation policies
     */
    public int getPoliciesCount(String apiId)  {
        int apiOperationPolicyCount=0;
        // Retrieves each method within the specified API, such as GET, PUT, etc.
        PagedIterable<OperationContract> apiOperations = azureManagersHolder.getAzureApiManager().apiOperations().listByApi(azureProperties.getResourceGroup(), azureProperties.getApiManagementServiceName(), apiId);
        //Retrieves Policy collection of given API.
        PolicyCollection apiPolicies = azureManagersHolder.getAzureApiManager().apiPolicies().listByApi(azureProperties.getResourceGroup(),azureProperties.getApiManagementServiceName(),apiId);
        int apiPolicyCount = parsePolicies(apiPolicies);

        // Iterates through each method within the API to calculate the policy count for each one.
        for (OperationContract operation : apiOperations) {
            PolicyCollection apiOperationPolicies = azureManagersHolder.getAzureApiManager().apiOperationPolicies().listByOperation(azureProperties.getResourceGroup(), azureProperties.getApiManagementServiceName(), apiId, operation.name());
            apiOperationPolicyCount += parsePolicies(apiOperationPolicies);
        }
        return apiPolicyCount + apiOperationPolicyCount;
    }

    private int parsePolicies(PolicyCollection policies) {
        // Uses PolicySAXParser to traverse the policy array and count the policies from the XML format.
        return policies.value().stream().mapToInt(policy -> {
            try {
                String xmlPolicy = policy.value();
                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXParser saxParser = spf.newSAXParser();
                PolicySAXParser policySAXParser = new PolicySAXParser();
                saxParser.parse(new InputSource(new StringReader(xmlPolicy)), policySAXParser);
                return policySAXParser.getPolicyCount();
            } catch (Exception e) {
                return 0;
            }
        }).sum();
    }

}
