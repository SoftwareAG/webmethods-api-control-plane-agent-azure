/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.common.utils;


import com.softwareag.controlplane.agent.azure.common.constants.Constants;
import com.softwareag.controlplane.agentsdk.model.Owner;
import org.apache.commons.lang3.ObjectUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The class Azure agent util has the methods for re-usability.
 */
public final class AzureAgentUtil {

    private AzureAgentUtil() {

    }

    /**
     * Convert tags set.
     *
     * @param azureTags the azure tags
     * @return the set
     */
    public static Set<String> convertTags(Map<String,String> azureTags) {
        Set<String> tags = new HashSet<>();
        if(ObjectUtils.isNotEmpty(azureTags)) {
            azureTags.keySet().forEach(key -> tags.add(key + Constants.COLON + azureTags.get(key)));
        }
        return tags;
    }

    /**
     * Reduce time range string.
     *
     * @param milliseconds the milliseconds
     * @param bufferTime   the buffer time in seconds
     * @return the reduced milliseconds
     */
    public static long reduceTimeRange(long milliseconds, int bufferTime) {
        long reducedEpochTimeInMilliseconds = milliseconds - bufferTime * 1000L;
        return reducedEpochTimeInMilliseconds;
    }

    /**
     * Converts the epoch timestamp to iso format {@link DateTimeFormatter}
     *
     * @param timeStamp the epochmilliseconds
     * @return the converted timestamp
     */
    public static String filterTimeConversion(long timeStamp){
        Instant instant=Instant.ofEpochMilli(timeStamp);
        return instant.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * Gets owner info.
     *
     * @param userName the user name
     * @return the owner info
     */
    public static Owner getOwnerInfo(String userName) {
        Owner owner = new Owner();
        owner.setName(userName);
        return owner;
    }


    /**
     * Aligns the given epoch timestamp with the specified interval by rounding it down to the nearest interval boundary.
     *
     * @param epochTimestamp The epoch timestamp to align.
     * @param interval The interval, in seconds, to align the timestamp with.
     * @return An Instant representing the epoch timestamp aligned with the specified interval.
     */
    public static long alignTimestampsWithInterval(long epochTimestamp, long interval) {
        long remainder = epochTimestamp % (interval * 1000);
        long alignedEpochTimestampMillis = ((epochTimestamp - remainder));
        return alignedEpochTimestampMillis;
    }

    /**
     * Construct api id string.
     *
     * @param apiName        the api name
     * @param subscriptionId the subscription id
     * @param runtimeName    the runtime name
     * @return the string
     */
    public static String constructAPIId(String apiName, String subscriptionId, String runtimeName) {
       return subscriptionId + Constants.UNDERSCORE + runtimeName
                + Constants.UNDERSCORE + apiName;
    }

    /**
     * Constructs a filter string to retrieve metrics from Azure within a specified time interval.
     *
     * @param startTime  the start time of the interval in ISO 8601 format
     * @param endTime    the end time of the interval in ISO 8601 format
     * @return the filter string formatted for Azure Metrics API requests
     */
    public static String constructFilter(String startTime, String endTime){
        String filter = Constants.FILTER_GE_TIMESTAMP + Constants.SINGLE_QUOTE + startTime + Constants.SINGLE_QUOTE + Constants.FILTER_LE_TIMESTAMP + Constants.SINGLE_QUOTE + endTime + Constants.SINGLE_QUOTE;
        return filter;
    }
}
