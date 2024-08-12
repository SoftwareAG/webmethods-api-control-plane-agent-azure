/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.functions.retrievers;

import com.softwareag.controlplane.agent.azure.common.handlers.metrics.MetricsManager;
import com.softwareag.controlplane.agent.azure.functions.utils.DefaultEnvProvider;
import com.softwareag.controlplane.agent.azure.functions.utils.constants.Constants;
import com.softwareag.controlplane.agentsdk.core.handler.SendMetricsHandler;
import com.softwareag.controlplane.agentsdk.model.Metrics;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of the MetricsRetriever interface for retrieving metrics.
 * This implementation is intended to be used with the SendMetricsHandler class.
 */
public class MetricsRetrieverImpl implements SendMetricsHandler.MetricsRetriever {

    /**
     * Retrieves runtime metrics within a specified time range and interval.
     * 
     * @param l  start time to fetch metrics
     * @param l1 end time to fetch metrics
     * @param l2 time gap between each fetch of metrics within the specified start
     *           and end times
     * @return Returns a list of {@link Metrics} captured within the specified time
     *         range and intervals
     */
    @Override
    public List<Metrics> getMetrics(long l, long l1, long l2) {
            return MetricsManager
                    .getInstance(DefaultEnvProvider.getEnv(Constants.AZURE_RESOURCE_GROUP),
                            DefaultEnvProvider.getEnv(Constants.AZURE_API_MANAGEMENT_SERVICE_NAME),
                            DefaultEnvProvider.getEnv(Constants.AZURE_SUBSCRIPTION_ID))
                    .metricsTypeHandler(l, l1,
                            Long.parseLong(DefaultEnvProvider.getEnv(Constants.APICP_SYNC_METRICS_INTERVAL_SECONDS)),
                            Integer.parseInt(DefaultEnvProvider.getEnv(Constants.AZURE_METRICS_SYNC_BUFFER_INTERVAL_MINUTES)),
                            DefaultEnvProvider.getEnv(Constants.AZURE_METRICS_BY_REQUESTS_OR_INSIGHTS)
                            );

    }
}
