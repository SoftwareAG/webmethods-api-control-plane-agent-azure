/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.common.handlers.metrics;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.apimanagement.models.ApiContract;
import com.azure.resourcemanager.apimanagement.models.ReportRecordContract;
import com.azure.resourcemanager.apimanagement.models.RequestReportRecordContract;
import com.softwareag.controlplane.agent.azure.common.constants.Constants;
import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agentsdk.core.log.DefaultAgentLogger;
import com.softwareag.controlplane.agentsdk.model.APIMetrics;
import com.softwareag.controlplane.agentsdk.model.APITransactionMetrics;
import com.softwareag.controlplane.agentsdk.model.Metrics;
import com.softwareag.controlplane.agentsdk.model.RuntimeTransactionMetrics;
import com.softwareag.controlplane.agent.azure.common.utils.AzureAgentUtil;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The  Metrics manager is to query Metrics from azure and sent to API Control Plane.
 */
public final class MetricsManager {

    private static MetricsManager metricsManager;
    private AzureManagersHolder azureManagersHolder;
    private String resourceGroup;
    private String apiManagementServiceName;
    private String subscriptionId;

    private DefaultAgentLogger logger;

    private MetricsManager(String resourceGroup, String apiManagementServiceName, String subscriptionId) {
        this.azureManagersHolder = AzureManagersHolder.getInstance();
        this.resourceGroup = resourceGroup;
        this.apiManagementServiceName = apiManagementServiceName;
        this.subscriptionId = subscriptionId;
        this.logger = DefaultAgentLogger.getInstance(this.getClass());
    }

    /**
     * Gets instance method creates instance of metrics manager
     *
     * @param resourceGroup            the resource group
     * @param apiManagementServiceName the api management service name
     * @param subscriptionId           the subscription id
     * @return the instance
     */
    public static MetricsManager getInstance(String resourceGroup, String apiManagementServiceName, String subscriptionId) {
        if(metricsManager != null) {
            return metricsManager;
        }
        metricsManager = new MetricsManager(resourceGroup, apiManagementServiceName, subscriptionId);
        return metricsManager;
    }

    private Map<String, List<RequestReportRecordContract>> statusCodeMapAzure ;

    private Map<String, Map<String, List<RequestReportRecordContract>>> statusCodeByApiAzure ;

    public List<Metrics> metricsTypeHandler(long fromTimestamp, long toTimestamp, long interval, int bufferIntervalMinutes, String metricsByRequestOrInsights){
        List<Metrics> metrics = new ArrayList<>();
        long currentStart = fromTimestamp;
        while(currentStart<toTimestamp){
            long currentEnd =  currentStart + (interval*1000);
            if(currentEnd>toTimestamp) currentEnd =toTimestamp;

            if(metricsByRequestOrInsights.equals("requests")){
                metrics.add(metricsRetrieverByRequests(currentStart,currentEnd,bufferIntervalMinutes));
            }
            else {
                metrics.add(metricsRetrieverByInsights(currentStart,currentEnd,interval,bufferIntervalMinutes));
            }
            currentStart=currentEnd;
        }
        return metrics;
    }
    /**
     * Control plane Metrics retriever means analytics of api management service.
     * This method queries the azure SDK for all request using listByRequest method.
     * Converts to Report contracts to SDK Metrics object.
     *
     * @param fromTimestamp             the from timestamp
     * @param toTimestamp               the to timestamp
     * @param bufferIntervalMinutes     the metrics sync buffer interval in minutes
     * @return the list
     */
    public Metrics metricsRetrieverByRequests(long fromTimestamp, long toTimestamp,int bufferIntervalMinutes) {
        int bufferIntervalSeconds = bufferIntervalMinutes*60;
        String startTime = AzureAgentUtil.filterTimeConversion(AzureAgentUtil.reduceTimeRange(fromTimestamp, bufferIntervalSeconds));
        String endTime = AzureAgentUtil.filterTimeConversion(AzureAgentUtil.reduceTimeRange(toTimestamp-1, bufferIntervalSeconds));

        String filter = AzureAgentUtil.constructFilter(startTime,endTime);
        PagedIterable<RequestReportRecordContract> azureMetricsByRequests = azureManagersHolder.getAzureApiManager().reports().listByRequest(this.resourceGroup, this.apiManagementServiceName, filter);

        List<APITransactionMetrics> apiTransactionMetrics = new ArrayList<>();
        RuntimeTransactionMetrics runtimeTransactionMetrics =null;
        if(azureMetricsByRequests.stream().findAny().isPresent()){
            statusMapPopulate(azureMetricsByRequests);
            runtimeTransactionMetrics = convertAsRuntimeMetrics();
            apiTransactionMetrics = convertAsAPITransMetrics();
        }

        return convertAsMetrics(runtimeTransactionMetrics, apiTransactionMetrics, toTimestamp);
    }

     /**
     * Metrics retriever list using aggregated report from azure.
      * This method queries the azure SDK for analytics by using listByTime and listByApi method.
      *  Converts to Report contracts to SDK Metrics object.
      *
     *
     * @param fromTimestamp             the from timestamp
     * @param toTimestamp               the to timestamp
     * @param interval                  the metrics sync interval in seconds
     * @param bufferIntervalMinutes     the metrics sync buffer interval in minutes
     * @return the list
     */
    public Metrics metricsRetrieverByInsights(long fromTimestamp, long toTimestamp, long interval,int bufferIntervalMinutes){
        List<APITransactionMetrics> apiTransactionMetrics = new ArrayList<>();
        RuntimeTransactionMetrics runtimeTransactionMetrics =null;

        // If the time range is less than the configured interval, the time range will be ignored, and empty metrics object will be returned.
        long intervalInMillis = interval * 1000;
        long timeRange = toTimestamp - fromTimestamp;
        if (timeRange < intervalInMillis) {
            return convertAsMetrics(runtimeTransactionMetrics, apiTransactionMetrics, toTimestamp);
        }

        int bufferIntervalSeconds = bufferIntervalMinutes*60;
        long bufferedFromTimestamp = AzureAgentUtil.reduceTimeRange(fromTimestamp, bufferIntervalSeconds);
        long bufferedToTimestamp = AzureAgentUtil.reduceTimeRange(toTimestamp, bufferIntervalSeconds);

        String startTime = AzureAgentUtil.filterTimeConversion(AzureAgentUtil.alignTimestampsWithInterval(bufferedFromTimestamp,bufferIntervalSeconds));
        String endTime =AzureAgentUtil.filterTimeConversion(AzureAgentUtil.alignTimestampsWithInterval(bufferedToTimestamp,bufferIntervalSeconds)-1);

        String filter =AzureAgentUtil.constructFilter(startTime,endTime);
        PagedIterable<ReportRecordContract> azureMetricsByTime = azureManagersHolder.getAzureApiManager().reports().listByTime(this.resourceGroup, this.apiManagementServiceName, filter, Duration.ofSeconds(interval));

        PagedIterable<ReportRecordContract> azureMetricsByAPI = azureManagersHolder.getAzureApiManager().reports().listByApi(this.resourceGroup, this.apiManagementServiceName, filter);

        if(azureMetricsByTime.stream().findAny().isPresent() && azureMetricsByAPI.stream().findAny().isPresent()){
            apiTransactionMetrics =aggregateReportToAPITransMetrics(azureMetricsByAPI);
            runtimeTransactionMetrics=aggregateReportToRuntimeMetrics(azureMetricsByTime);
        }
        return convertAsMetrics(runtimeTransactionMetrics, apiTransactionMetrics, toTimestamp);
    }

    private void statusMapPopulate(PagedIterable<RequestReportRecordContract> azureMetricsByRequests){
        statusCodeMapAzure = new HashMap<>();
        statusCodeByApiAzure = new HashMap<>();
        azureMetricsByRequests.forEach(met -> {
            String responseCode = null;

            if(met.responseCode() >= 200 && met.responseCode() < 300) {
                responseCode = "2xx";
            } else if(met.responseCode() >= 300 && met.responseCode() < 400) {
                responseCode = "3xx";
            } else if(met.responseCode() >= 400 && met.responseCode() < 500) {
                responseCode = "4xx";
            }
            else if(met.responseCode()<600){
                responseCode= "5xx";
            }
            List<RequestReportRecordContract> requestReportRecordContracts = statusCodeMapAzure.getOrDefault(responseCode,new ArrayList<>());
            requestReportRecordContracts.add(met);
            statusCodeMapAzure.put(responseCode, requestReportRecordContracts);

            Map<String, List<RequestReportRecordContract>> requestRecordContractsByApi = statusCodeByApiAzure.get(met.apiId());
            List<RequestReportRecordContract> apiRecordByStatus = requestRecordContractsByApi ==null? new ArrayList<>():requestRecordContractsByApi.getOrDefault(responseCode,new ArrayList<>());
            if (requestRecordContractsByApi == null) {
                requestRecordContractsByApi = new HashMap<>();
            }
            apiRecordByStatus.add(met);
            requestRecordContractsByApi.put(responseCode, apiRecordByStatus);
            statusCodeByApiAzure.put(met.apiId(), requestRecordContractsByApi);
        });
    }
    private RuntimeTransactionMetrics convertAsRuntimeMetrics() {
        double[] totalApiTime = {0.0D};
        double[] totalServiceTime = {0.0D};
        long[] totalApiCallCount ={0};

        Map<String, APIMetrics> metricsByStatusCode = new HashMap<>();
        statusCodeMapAzure.forEach((key, contracts)-> aggregateMetrics(key,contracts,totalApiTime, totalServiceTime, totalApiCallCount,metricsByStatusCode));
        APIMetrics apiMetrics = createAPIMetrics(totalApiCallCount[0], totalApiTime[0]/totalApiCallCount[0],
                totalServiceTime[0]/totalApiCallCount[0]);

        return (RuntimeTransactionMetrics) new RuntimeTransactionMetrics
                    .Builder(apiMetrics)
                    .metricsByStatusCode(metricsByStatusCode)
                    .build();
    }

    private List<APITransactionMetrics> convertAsAPITransMetrics() {

        List<APITransactionMetrics> apiTransMetricsList = new ArrayList<>();

        statusCodeByApiAzure.forEach((apiId, contractList) -> {
            String constructAPIId = AzureAgentUtil.constructAPIId(apiId.substring(6), this.subscriptionId, this.apiManagementServiceName);
            Map<String, APIMetrics> metricsByStatusCode = new HashMap<>();

            double[] totalApiTime = {0.0D};
            double[] totalServiceTime = {0.0D};
            long[] totalApiCallCount = {0};

            if (contractList != null) {

                contractList.keySet().forEach(code -> {
                    List<RequestReportRecordContract> contracts = contractList.get(code);
                    aggregateMetrics(code,contracts, totalApiTime, totalServiceTime, totalApiCallCount,metricsByStatusCode);
                });

                APIMetrics apiMetrics = createAPIMetrics(totalApiCallCount[0], totalApiTime[0] / totalApiCallCount[0], totalServiceTime[0] / totalApiCallCount[0]);
                //API details will be retrieved from the Azure Portal. If the API is not found, the metric details for that API will be skipped.
                try{
                    ApiContract apiVersionContract = azureManagersHolder.getAzureApiManager().apis().get(this.resourceGroup,
                            this.apiManagementServiceName,
                            apiId.substring(6));
                    APITransactionMetrics apiTransMetrics = (APITransactionMetrics) new APITransactionMetrics
                            .Builder(apiMetrics, constructAPIId, apiVersionContract.name(),
                            apiVersionContract.apiVersion() == null ? Constants.ORIGINAL_VERSION : apiVersionContract.apiVersion())
                            .metricsByStatusCode(metricsByStatusCode)
                            .build();
                    apiTransMetricsList.add(apiTransMetrics);
                }catch(ResourceNotFoundException e){
                    logger.info("Exception occurred during API transaction retrieval");
                }

            }

        });

        return apiTransMetricsList;
    }

    private RuntimeTransactionMetrics aggregateReportToRuntimeMetrics(PagedIterable<ReportRecordContract> azureMetricsByTime){
        Map<String, APIMetrics> metricsByStatusCode = new HashMap<>();
        ReportRecordContract metricByTime = azureMetricsByTime.stream().findAny().get();
        APIMetrics apiMetrics = createAPIMetrics(metricByTime.callCountTotal(), metricByTime.apiTimeAvg(),
                metricByTime.serviceTimeAvg());
        return (RuntimeTransactionMetrics) new RuntimeTransactionMetrics
                .Builder(apiMetrics)
                .metricsByStatusCode(metricsByStatusCode)
                .build();
    }

    private List<APITransactionMetrics> aggregateReportToAPITransMetrics(PagedIterable<ReportRecordContract> azureMetricsByAPI){
        List<APITransactionMetrics> apiTransMetricsList = new ArrayList<>();
        azureMetricsByAPI.forEach(metricByApi->{
            if(metricByApi.callCountTotal()>0){
                String constructAPIId = AzureAgentUtil.constructAPIId(metricByApi.apiId().substring(6),this.subscriptionId,this.apiManagementServiceName);

                APIMetrics apiMetrics = createAPIMetrics(metricByApi.callCountTotal(), metricByApi.apiTimeAvg(), metricByApi.serviceTimeAvg());

                Map<String, APIMetrics> metricsByStatusCode = new HashMap<>();
                try{
                    ApiContract apiVersionContract = azureManagersHolder.getAzureApiManager().apis().get(this.resourceGroup, this.apiManagementServiceName, metricByApi.apiId().substring(6));
                    APITransactionMetrics apiTransMetrics = (APITransactionMetrics) new APITransactionMetrics
                            .Builder(apiMetrics, constructAPIId, apiVersionContract.name(),
                            apiVersionContract.apiVersion() == null ? Constants.ORIGINAL_VERSION : apiVersionContract.apiVersion())
                            .metricsByStatusCode(metricsByStatusCode)
                            .build();
                    apiTransMetricsList.add(apiTransMetrics);
                }
                catch (ResourceNotFoundException e){
                   logger.info("Exception occurred during API transaction retrieval");
                }
            }
        });
        return apiTransMetricsList;
    }

    private Metrics convertAsMetrics(RuntimeTransactionMetrics runtimeTransactionMetrics,List<APITransactionMetrics> transactionMetrics, long timestamp) {
        return new Metrics.Builder()
                .runtimeTransactionMetrics(runtimeTransactionMetrics)
                .apiTransactionMetricsList(transactionMetrics)
                .timestamp(timestamp)
                .build();
    }

    private void aggregateMetrics(String statusCode,List<RequestReportRecordContract> contracts, double[] totalApiTime, double[] totalServiceTime, long[] totalApiCallCount, Map<String, APIMetrics>metricsByStatusCode){
        Double apiTimeavg = 0.0D;
        Double serviceTime = 0.0D;
        for (RequestReportRecordContract recordContract : contracts) {
            apiTimeavg += recordContract.apiTime();
            serviceTime += recordContract.serviceTime();
        }
        totalApiCallCount[0] += contracts.size();
        totalApiTime[0] += apiTimeavg;
        totalServiceTime[0] += serviceTime;
        metricsByStatusCode.put(statusCode, createAPIMetrics(contracts.size(), apiTimeavg / contracts.size(),
                serviceTime / contracts.size()));
    }

    private APIMetrics createAPIMetrics(long callCountTotal, Double apiTimeAvg, Double serviceTimeAvg) {
        return new APIMetrics.Builder(callCountTotal)
                .averageGatewayLatency((float) (apiTimeAvg - serviceTimeAvg))
                .averageBackendLatency(serviceTimeAvg.floatValue())
                .averageTotalLatency(apiTimeAvg.floatValue()).build();
    }
}
