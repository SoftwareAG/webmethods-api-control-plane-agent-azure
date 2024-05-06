package com.softwareag.controlplane.agent.azure.helpers;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.apimanagement.models.ReportRecordContract;
import com.azure.resourcemanager.apimanagement.models.RequestReportRecordContract;
import com.softwareag.controlplane.agent.azure.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.Constants;
import com.softwareag.controlplane.agent.azure.configuration.AgentProperties;
import com.softwareag.controlplane.agent.azure.configuration.AzureProperties;
import com.softwareag.controlplane.agentsdk.model.APIMetrics;
import com.softwareag.controlplane.agentsdk.model.APITransactionMetrics;
import com.softwareag.controlplane.agentsdk.model.Metrics;
import com.softwareag.controlplane.agentsdk.model.RuntimeTransactionMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class MetricsRetriever {
    @Autowired
    AzureProperties azureProperties;

    @Autowired
    AgentProperties agentProperties;

    @Autowired
    AzureManagersHolder azureManagersHolder;

    public List<Metrics> metricsRetriever(long fromTimestamp, long toTimestamp, long interval) {
        List<Metrics> metrics = new ArrayList<>();
        String filter = Constants.FILTER_GE_TIMESTAMP + Constants.SINGLE_QUOTE
                + Instant.ofEpochMilli(fromTimestamp) + Constants.SINGLE_QUOTE + Constants.FILTER_LE_TIMESTAMP
                + Constants.SINGLE_QUOTE + Instant.ofEpochMilli(toTimestamp) + Constants.SINGLE_QUOTE;
        PagedIterable<ReportRecordContract> azureMetricsByTime =
                azureManagersHolder.getAzureApiManager().reports().listByTime(azureProperties.getResourceGroup(),
                        azureProperties.getApiManagementServiceName(), filter,
                        Duration.ofSeconds(900));
        azureMetricsByTime.forEach(met -> {
            metrics.add(convertAsMetrics(met, fromTimestamp, toTimestamp));
        });
        PagedIterable<RequestReportRecordContract> azureMetricsByRequests = azureManagersHolder.getAzureApiManager()
                .reports().listByRequest(azureProperties.getResourceGroup(),
                azureProperties.getApiManagementServiceName(), filter);
        azureMetricsByRequests.forEach(met -> {

        });

        PagedIterable<ReportRecordContract> azureMetricsByAPI = azureManagersHolder.getAzureApiManager().reports().listByApi(azureProperties.getResourceGroup(),
                azureProperties.getApiManagementServiceName(), filter);
        azureMetricsByAPI.forEach(met -> {

        });
        return metrics;
    }

    private RuntimeTransactionMetrics convertAsRuntimeMetrics(ReportRecordContract met, long fromTimestamp, long toTimestamp) {
        APIMetrics apiMetrics = new APIMetrics.Builder(Long.valueOf(met.callCountTotal()))
                .averageLatency((float) (met.apiTimeAvg() - met.serviceTimeAvg()))
                .averageBackendResponseTime(met.serviceTimeAvg().floatValue())
                .averageResponseTime(met.apiTimeAvg().floatValue()).build();
        RuntimeTransactionMetrics transactionMetrics = new RuntimeTransactionMetrics.Builder(apiMetrics).build();
        return transactionMetrics;
    }

    private Metrics convertAsMetrics(ReportRecordContract recordContract, long fromTimestamp, long toTimestamp) {
        Metrics metrics = new Metrics.Builder()
                .apiTransactionMetricsList(convertAsAPITransMetrics(recordContract, fromTimestamp, toTimestamp))
                .runtimeTransactionMetrics(convertAsRuntimeMetrics(recordContract, fromTimestamp, toTimestamp))
                .timestamp(toTimestamp).build();
        return metrics;
    }

    private List<APITransactionMetrics> convertAsAPITransMetrics(ReportRecordContract recordContract, long fromTimestamp, long toTimestamp) {
        List<APITransactionMetrics> apiTransMetricsList = new ArrayList<>();
        APIMetrics apiMetrics = new APIMetrics.Builder(fromTimestamp).build();
        APITransactionMetrics apiTransMetrics = new APITransactionMetrics.Builder(apiMetrics, "", "", "").build();
        apiTransMetricsList.add(apiTransMetrics);
        return apiTransMetricsList;
    }
}
