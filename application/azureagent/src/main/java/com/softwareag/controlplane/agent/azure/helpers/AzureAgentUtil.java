package com.softwareag.controlplane.agent.azure.helpers;


import com.softwareag.controlplane.agent.azure.Constants;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AzureAgentUtil {
    public static Set<String> convertTags(Map<String,String> azureTags) {
        Set<String> tags = new HashSet<>();
        azureTags.keySet().forEach(key -> tags.add(key + Constants.COLON + azureTags.get(key)));
        return tags;
    }
}
