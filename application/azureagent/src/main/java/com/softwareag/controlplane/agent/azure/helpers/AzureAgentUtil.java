package com.softwareag.controlplane.agent.azure.helpers;


import com.softwareag.controlplane.agent.azure.Constants;
import com.softwareag.controlplane.agentsdk.model.Owner;
import org.apache.commons.lang3.ObjectUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AzureAgentUtil {
    public static Set<String> convertTags(Map<String,String> azureTags) {
        Set<String> tags = new HashSet<>();
        if(ObjectUtils.isNotEmpty(azureTags)) {
            azureTags.keySet().forEach(key -> tags.add(key + Constants.COLON + azureTags.get(key)));
        }
        return tags;
    }


    public static String reduceTimeRange(long milliseconds, int bufferTime) {
        Instant instant = Instant.ofEpochMilli(milliseconds);
        Instant minusFifteenMinutes = instant.minusSeconds(bufferTime * 60); // 15 minutes = 15 * 60 seconds
        String iso8601DateTime = minusFifteenMinutes.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return iso8601DateTime;
    }

    public static Owner getOwnerInfo(String userName) {
        Owner owner = new Owner();
        owner.setName(userName);
        return owner;
    }


    public static String constructAPIId(String apiName, String tenantId, String runtimeName) {
       return tenantId + Constants.UNDERSCORE + runtimeName
                + Constants.UNDERSCORE + apiName;
    }
}
