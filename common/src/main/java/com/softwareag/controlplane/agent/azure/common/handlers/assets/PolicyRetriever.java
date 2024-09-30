/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.common.handlers.assets;


import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.apimanagement.models.OperationContract;
import com.azure.resourcemanager.apimanagement.models.PolicyCollection;
import com.softwareag.controlplane.agent.azure.common.constants.Constants;
import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agentsdk.core.log.DefaultAgentLogger;
import org.apache.commons.lang3.ObjectUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * The type Policy retriever.
 */
public final class PolicyRetriever {

    private AzureManagersHolder azureManagersHolder;
    private String resourceGroup;
    private String apiManagementServiceName;
    private static PolicyRetriever policyRetriever;

    private DefaultAgentLogger logger;

    private PolicyRetriever(String resourceGroup, String apiManagementServiceName) {
        this.resourceGroup = resourceGroup;
        this.apiManagementServiceName = apiManagementServiceName;
        this.azureManagersHolder = AzureManagersHolder.getInstance();
        this.logger = DefaultAgentLogger.getInstance(this.getClass());
    }

    /**
     * Gets instance.
     *
     * @param resourceGroup            the resource group
     * @param apiManagementServiceName the api management service name
     * @return the instance
     */
    public static PolicyRetriever getInstance(String resourceGroup, String apiManagementServiceName) {
        if(policyRetriever != null) {
            return policyRetriever;
        }
        policyRetriever = new PolicyRetriever(resourceGroup, apiManagementServiceName);
        return policyRetriever;
    }

    /**
     * Retrieves the combined count of Global policies and Product policies.
     *
     * @return the total count of Global policies and Product policies.
     */
    public int getGlobalProductPolicyCount(){
        //Global Policy and Product Policy will be common for all the APIs
        PolicyCollection productPolicies = azureManagersHolder.getAzureApiManager().productPolicies().listByProduct(this.resourceGroup, this.apiManagementServiceName, "unlimited");
        int productPolicyCount = parsePolicies(productPolicies);
        PolicyCollection globalPolicy =azureManagersHolder.getAzureApiManager().policies().listByService(this.resourceGroup,this.apiManagementServiceName);
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
        try {
            int apiOperationPolicyCount=0;
            // Retrieves each method within the specified API, such as GET, PUT, etc
            PagedIterable<OperationContract> apiOperations = azureManagersHolder.getAzureApiManager().apiOperations().listByApi(this.resourceGroup, this.apiManagementServiceName, apiId);
            //Retrieves Policy collection of given API.
            PolicyCollection apiPolicies = azureManagersHolder.getAzureApiManager().apiPolicies().listByApi(this.resourceGroup,this.apiManagementServiceName,apiId);
            int apiPolicyCount = parsePolicies(apiPolicies);

            // Iterates through each method within the API to calculate the policy count for each one.
            if(ObjectUtils.isNotEmpty(apiOperations)) {
                for (OperationContract operation : apiOperations) {
                    PolicyCollection apiOperationPolicies = azureManagersHolder.getAzureApiManager().apiOperationPolicies().listByOperation(this.resourceGroup, this.apiManagementServiceName, apiId, operation.name());
                    apiOperationPolicyCount += parsePolicies(apiOperationPolicies);
                }
            }
            return apiPolicyCount + apiOperationPolicyCount;
        }
        catch (ResourceNotFoundException e){
            logger.info("Exception occured during API Operation retrieval");
            return 0;
        }
    }

    private int parsePolicies(PolicyCollection policies) {
        return policies.value().stream().mapToInt(policy -> {
            try {
                String xmlPolicy = policy.value();
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(new InputSource(new StringReader(xmlPolicy)));

                int inboundCount = getCount(doc, "inbound");
                int backendCount = getCount(doc, "backend");
                int outboundCount = getCount(doc, "outbound");
                int onerrorCount = getCount(doc, "on-error");

                return inboundCount+backendCount+outboundCount+onerrorCount;
            } catch (IOException | ParserConfigurationException | SAXException e) {
                return 0;
            }

        }).sum();
    }

    private int getCount(Document doc, String tagName) {
        NodeList nodeParent = doc.getElementsByTagName(tagName);
        int count = 0;
        int len = nodeParent.getLength();
        for (int index = 0; index < len; index++) {
            Node item = nodeParent.item(index);
            if (item != null && Node.ELEMENT_NODE == item.getNodeType()) {
                NodeList innerList = item.getChildNodes();
                int innerLen = innerList.getLength();
                for (int innerIndex = 0; innerIndex < innerLen; innerIndex++) {
                    Node innerItem = innerList.item(innerIndex);
                    if (innerItem != null && Node.ELEMENT_NODE == innerItem.getNodeType() && (!innerItem.getNodeName().equals(Constants.BASE_TAG))) {
                        count += 1;
                    }
                }
            }
        }
        return count;
    }

}
