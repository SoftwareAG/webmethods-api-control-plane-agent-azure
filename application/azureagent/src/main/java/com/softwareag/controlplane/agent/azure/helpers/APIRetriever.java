package com.softwareag.controlplane.agent.azure.helpers;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.apimanagement.models.ApiContract;
import com.azure.resourcemanager.apimanagement.models.TagContract;
import com.softwareag.controlplane.agent.azure.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.Constants;
import com.softwareag.controlplane.agent.azure.configuration.AgentProperties;
import com.softwareag.controlplane.agent.azure.configuration.AzureProperties;
import com.softwareag.controlplane.agentsdk.model.API;
import com.softwareag.controlplane.agentsdk.model.Owner;
import com.softwareag.controlplane.agentsdk.model.Status;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class APIRetriever {
    @Autowired
    AzureProperties azureProperties;

    @Autowired
    AgentProperties agentProperties;

    @Autowired
    AzureManagersHolder azureManagersHolder;

    public List<API> retrieveAPIs() {
        PagedIterable<ApiContract> apis = azureManagersHolder.getAzureApiManager().apis().listByService(azureProperties.getResourceGroup(),
                azureProperties.getApiManagementServiceName());

        return convertAsAPIModel(apis);
    }

    private List<API> convertAsAPIModel(PagedIterable<ApiContract> apis) {
        List<API> allAPIs = new ArrayList<>();
        Owner owner = new Owner();
        owner.setName(agentProperties.getUsername());
        for (ApiContract azureAPI : apis) {

            String azureAPIType = azureAPI.apiType() == null ? "REST" : azureAPI.apiType().toString().toUpperCase();
            if (validAPICreation(azureAPI, azureAPIType)) {
                String versionSetId = azureAPI.apiVersionSetId() != null ?
                        azureAPI.apiVersionSetId() : null;
                API api = (API) new API.Builder(azureAPI.id(), API.Type.valueOf(azureAPIType))
                        .version(azureAPI.apiVersion())
                        .versionSetId(versionSetId)
                        .status(Status.ACTIVE)
                        .owner(owner)
                        .tags(getAPITags(azureAPI.name()))
                        .description(azureAPI.description())
                        .name(azureAPI.name())
                        .build();
                allAPIs.add(api);
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
        enumNames.remove(Constants.GRAPHQL);
        return ObjectUtils.isNotEmpty(azureAPI.isCurrent()) && azureAPI.isCurrent()
                && enumNames.contains(azureAPIType);
    }
}
