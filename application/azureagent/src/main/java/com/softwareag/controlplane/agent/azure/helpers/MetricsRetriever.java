package com.softwareag.controlplane.agent.azure.helpers;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.apimanagement.models.ApiContract;
import com.azure.resourcemanager.apimanagement.models.ReportRecordContract;
import com.azure.resourcemanager.apimanagement.models.RequestReportRecordContract;
import com.softwareag.controlplane.agent.azure.Constants;
import com.softwareag.controlplane.agent.azure.configuration.AgentProperties;
import com.softwareag.controlplane.agent.azure.configuration.AzureProperties;
import com.softwareag.controlplane.agent.azure.context.AzureManagersHolder;
import com.softwareag.controlplane.agentsdk.model.APIMetrics;
import com.softwareag.controlplane.agentsdk.model.APITransactionMetrics;
import com.softwareag.controlplane.agentsdk.model.Metrics;
import com.softwareag.controlplane.agentsdk.model.RuntimeTransactionMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                + AzureAgentUtil.reduceTimeRange(fromTimestamp, Constants.BUFFERTIME) + Constants.SINGLE_QUOTE + Constants.FILTER_LE_TIMESTAMP
                + Constants.SINGLE_QUOTE + AzureAgentUtil.reduceTimeRange(toTimestamp, Constants.BUFFERTIME) + Constants.SINGLE_QUOTE;
        PagedIterable<ReportRecordContract> azureMetricsByTime =
                azureManagersHolder.getAzureApiManager().reports().listByTime(azureProperties.getResourceGroup(),
                        azureProperties.getApiManagementServiceName(), filter,
                        Duration.ofSeconds(interval));

        PagedIterable<RequestReportRecordContract> azureMetricsByRequests = azureManagersHolder.getAzureApiManager()
                .reports().listByRequest(azureProperties.getResourceGroup(),
                        azureProperties.getApiManagementServiceName(), filter);
        Map<String, List<RequestReportRecordContract>> statusCodeMapAzure = new HashMap<>();
        Map<String, Map<String, List<RequestReportRecordContract>>> statusCodeByApiAzure = new HashMap<>();
        azureMetricsByRequests.forEach(met -> {
            String responseCode = null;
            if(met.responseCode() >= 200 && met.responseCode() < 300) {
                responseCode = "2xx";
            } else if(met.responseCode() >= 300 && met.responseCode() < 400) {
                responseCode = "3xx";
            } else if(met.responseCode() >= 400 && met.responseCode() < 500) {
                responseCode = "4xx";
            }
            // first map one with status code,list<report>
            List<RequestReportRecordContract> requestReportRecordContracts =
                    statusCodeMapAzure.get(responseCode);
            if (requestReportRecordContracts == null) {
                requestReportRecordContracts = new ArrayList<>();
                requestReportRecordContracts.add(met);
            } else {
                requestReportRecordContracts.add(met);
            }

            statusCodeMapAzure.put(responseCode, requestReportRecordContracts);

            //second map with api-id, list<report>
            Map<String, List<RequestReportRecordContract>> requestRecordContractsByApi =
                    statusCodeByApiAzure.get(AzureAgentUtil.constructAPIId(met.apiId().substring(6),
                            azureProperties.getTenantId(), azureProperties.getApiManagementServiceName()));

            if (requestRecordContractsByApi == null) {
                requestRecordContractsByApi = new HashMap<>();
                List<RequestReportRecordContract> apiRecordBycode =
                        requestRecordContractsByApi.get(responseCode);
                if (apiRecordBycode == null) {
                    apiRecordBycode = new ArrayList<>();
                    apiRecordBycode.add(met);
                } else {
                    apiRecordBycode.add(met);
                }
                requestRecordContractsByApi.put(met.responseCode().toString(), apiRecordBycode);
            } else {
                List<RequestReportRecordContract> apiRecordBycode =
                        requestRecordContractsByApi.get(responseCode);
                if (apiRecordBycode == null) {
                    apiRecordBycode = new ArrayList<>();
                    apiRecordBycode.add(met);
                } else {
                    apiRecordBycode.add(met);
                }
                requestRecordContractsByApi.put(responseCode, apiRecordBycode);
            }

            statusCodeByApiAzure.put(AzureAgentUtil.constructAPIId(met.apiId().substring(6),
                            azureProperties.getTenantId(), azureProperties.getApiManagementServiceName()),
                    requestRecordContractsByApi);
        });
        RuntimeTransactionMetrics runtimeTransactionMetrics = convertAsRuntimeMetrics(azureMetricsByTime,
                statusCodeMapAzure);

        PagedIterable<ReportRecordContract> azureMetricsByAPI = azureManagersHolder.getAzureApiManager().reports()
                .listByApi(azureProperties.getResourceGroup(),
                        azureProperties.getApiManagementServiceName(), filter);
        List<APITransactionMetrics> apiTransactionMetrics = convertAsAPITransMetrics(azureMetricsByAPI, statusCodeByApiAzure);

        metrics.add(convertAsMetrics(runtimeTransactionMetrics, apiTransactionMetrics, toTimestamp));
        return metrics;
    }

    private RuntimeTransactionMetrics convertAsRuntimeMetrics(PagedIterable<ReportRecordContract> metricsByTime, Map<String,
            List<RequestReportRecordContract>> statusCodeMapAzure) {
        if (metricsByTime.stream().findAny().isPresent()) {
            ReportRecordContract metricByTime = metricsByTime.stream().findAny().get();
            APIMetrics apiMetrics = createAPIMetrics(metricByTime.callCountTotal(), metricByTime.apiTimeAvg(),
                    metricByTime.serviceTimeAvg());

            Map<String, APIMetrics> metricsByStatusCode = new HashMap<>();
            statusCodeMapAzure.keySet().forEach(key -> {
                List<RequestReportRecordContract> contracts = statusCodeMapAzure.get(key);
                Double apiTimeavg = 0.0D;
                Double serviceTime = 0.0D;
                for (RequestReportRecordContract recordContract : contracts) {
                    apiTimeavg += recordContract.apiTime();
                    serviceTime += recordContract.serviceTime();
                }
                metricsByStatusCode.put(key, createAPIMetrics(contracts.size(), apiTimeavg / contracts.size(),
                        serviceTime / contracts.size()));

            });

            RuntimeTransactionMetrics transactionMetrics = (RuntimeTransactionMetrics) new RuntimeTransactionMetrics
                    .Builder(apiMetrics)
                //    .metricsByStatusCode(new HashMap<>())
                    .metricsByStatusCode(metricsByStatusCode)
                    .build();
            return transactionMetrics;
        }
        return null;
    }

    private APIMetrics createAPIMetrics(Integer callCountTotal, Double apiTimeAvg, Double serviceTimeAvg) {
        APIMetrics apiMetrics = new APIMetrics.Builder(Long.valueOf(callCountTotal))
                .averageLatency((float) (apiTimeAvg - serviceTimeAvg))
                .averageBackendResponseTime(serviceTimeAvg.floatValue())
                .averageResponseTime(apiTimeAvg.floatValue()).build();
        return apiMetrics;
    }

    private Metrics convertAsMetrics(RuntimeTransactionMetrics runtimeTransactionMetrics,
                                     List<APITransactionMetrics> transactionMetrics, long timestamp) {
        Metrics metrics = new Metrics.Builder()
                .runtimeTransactionMetrics(runtimeTransactionMetrics)
                .apiTransactionMetricsList(transactionMetrics)
                .timestamp(timestamp)
                .build();
        return metrics;
    }

    private List<APITransactionMetrics> convertAsAPITransMetrics
            (PagedIterable<ReportRecordContract> recordContracts,
             Map<String, Map<String, List<RequestReportRecordContract>>> statusCodeByApiAzure) {
        List<APITransactionMetrics> apiTransMetricsList = new ArrayList<>();

        recordContracts.forEach(metricByApi -> {
            APIMetrics apiMetrics = createAPIMetrics(metricByApi.callCountTotal(),
                    metricByApi.apiTimeAvg(), metricByApi.serviceTimeAvg());

            Map<String, APIMetrics> metricsByStatusCode = new HashMap<>();

            Map<String, List<RequestReportRecordContract>> contractList =
                    statusCodeByApiAzure.get(AzureAgentUtil.constructAPIId(metricByApi.apiId().substring(6),
                            azureProperties.getTenantId(), azureProperties.getApiManagementServiceName()));

            if (contractList != null) {
                contractList.keySet().forEach(code -> {
                    Double apiTimeavg = 0.0D;
                    Double serviceTime = 0.0D;
                    List<RequestReportRecordContract> contracts = contractList.get(code);

                    for (RequestReportRecordContract recordContract : contracts) {
                        apiTimeavg += recordContract.apiTime();
                        serviceTime += recordContract.serviceTime();
                    }
                    metricsByStatusCode.put(code, createAPIMetrics(contracts.size(), apiTimeavg / contracts.size(),
                            serviceTime / contracts.size()));
                });

                ApiContract apiVersionContract =
                        azureManagersHolder.getAzureApiManager().apis().get(azureProperties.getResourceGroup(),
                                azureProperties.getApiManagementServiceName(),
                                metricByApi.apiId().substring(6));
                APITransactionMetrics apiTransMetrics = (APITransactionMetrics) new APITransactionMetrics
                        .Builder(apiMetrics, AzureAgentUtil.constructAPIId(metricByApi.apiId().substring(6),
                        azureProperties.getTenantId(), azureProperties.getApiManagementServiceName()), metricByApi.name(),
                        apiVersionContract.apiVersion() ==  null ? "" : apiVersionContract.apiVersion())
                    //    .metricsByStatusCode(new HashMap<>())
                       .metricsByStatusCode(metricsByStatusCode)
                        .build();
                apiTransMetricsList.add(apiTransMetrics);
            }

        });

        return apiTransMetricsList;
    }
}
