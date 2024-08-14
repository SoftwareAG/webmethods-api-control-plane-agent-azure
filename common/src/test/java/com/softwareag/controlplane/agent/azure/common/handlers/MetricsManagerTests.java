/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.common.handlers;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.*;
import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.common.handlers.Util.MetricsMangerUtil;
import com.softwareag.controlplane.agent.azure.common.handlers.metrics.MetricsManager;
import com.softwareag.controlplane.agentsdk.model.Metrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MetricsManagerTests {

    @Mock
    AzureManagersHolder azureManagersHolder;

    @InjectMocks
    MetricsManager metricsManager;

    MockedStatic<AzureManagersHolder> mockAzureManagerHolder;

    @BeforeEach
    private void setup() {
        MockitoAnnotations.openMocks(this);
        mockAzureManagerHolder = Mockito.mockStatic(AzureManagersHolder.class);
        mockAzureManagerHolder.when(() -> AzureManagersHolder.getInstance())
                .thenReturn(azureManagersHolder);

        metricsManager = MetricsManager.getInstance("demo","arajgw","12323-3454-34874");
    }



    @Test
    public void metricsRetrieverByDataTest() {
        Metrics metrics;

        RequestReportRecordContract requestReportRecordContract1 = MetricsMangerUtil.requestReportRecordContract("api-id-1", 2.0, 1.0, 200, "200");
        RequestReportRecordContract requestReportRecordContract2 = MetricsMangerUtil.requestReportRecordContract("api-id-2", 4.0, 2.0, 500, "500");
        ApiContract apiContract = MetricsMangerUtil.apiContract(ApiType.SOAP, "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.ApiManagement/service/apimService1/apis/a1", "hello-api", "Hello API", "c48f96c9-1385-4e2d-b410-5ab591ce0fc4", true, "This is hello world description");

        List<RequestReportRecordContract> mockedResponse = List.of(requestReportRecordContract1, requestReportRecordContract2);

        PagedIterable<RequestReportRecordContract> pagedIterableMock = mock(PagedIterable.class);
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Consumer<RequestReportRecordContract> consumer = invocation.getArgument(0);
                mockedResponse.forEach(consumer);
                return null;
            }
        }).when(pagedIterableMock).forEach(any());

        when(azureManagersHolder.getAzureApiManager()).thenReturn(mock(ApiManagementManager.class));
        when(azureManagersHolder.getAzureApiManager().reports()).thenReturn(mock(Reports.class));
        when(azureManagersHolder.getAzureApiManager().reports().listByRequest(any(), any(), any())).thenReturn(pagedIterableMock);
        when(azureManagersHolder.getAzureApiManager().reports().listByRequest(any(), any(), any()).stream()).thenReturn(mockedResponse.stream());
        when(azureManagersHolder.getAzureApiManager().apis()).thenReturn(mock(Apis.class));
        when(azureManagersHolder.getAzureApiManager().apis().get(any(), any(), any())).thenReturn(apiContract);



        metrics = metricsManager.metricsRetrieverByRequests(1717561359658L, 1717561359659L, 15);

        float averageBackendLatency = (float) ((requestReportRecordContract1.serviceTime() + requestReportRecordContract2.serviceTime()) / 2);
        float averageTotalLatency = (float) ((requestReportRecordContract1.apiTime() + requestReportRecordContract2.apiTime()) / 2);
        float averageGatewayLatency = averageTotalLatency - averageBackendLatency;

        assertEquals("12323-3454-34874_arajgw_-1", metrics.getApiTransactionMetricsList().get(0).getApiId());
        assertEquals(1, metrics.getApiTransactionMetricsList().get(0).getMetricsByStatusCode().get("2xx").getTransactionCount());
        assertEquals(1, metrics.getApiTransactionMetricsList().get(1).getMetricsByStatusCode().get("5xx").getTransactionCount());
        assertEquals(metrics.getApiTransactionMetricsList().get(0).getApiMetrics().getAverageBackendLatency(),requestReportRecordContract1.serviceTime().floatValue());
        assertEquals(averageBackendLatency,metrics.getRuntimeTransactionMetrics().getApiMetrics().getAverageBackendLatency());
        assertEquals(averageTotalLatency,metrics.getRuntimeTransactionMetrics().getApiMetrics().getAverageTotalLatency());
        assertEquals(averageGatewayLatency,metrics.getRuntimeTransactionMetrics().getApiMetrics().getAverageGatewayLatency());

    }


    @Test
    public void metricsTypeHandler() {
        List<Metrics> metrics = new ArrayList<>();

        RequestReportRecordContract requestReportRecordContract1 = MetricsMangerUtil.requestReportRecordContract("api-id-1", 2.0, 1.0, 200, "200");
        RequestReportRecordContract requestReportRecordContract2 = MetricsMangerUtil.requestReportRecordContract("api-id-2", 4.0, 2.0, 500, "500");
        ApiContract apiContract = MetricsMangerUtil.apiContract(ApiType.SOAP, "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.ApiManagement/service/apimService1/apis/a1", "hello-api", "Hello API", "c48f96c9-1385-4e2d-b410-5ab591ce0fc4", true, "This is hello world description");

        List<RequestReportRecordContract> mockedResponse = List.of(requestReportRecordContract1, requestReportRecordContract2);

        PagedIterable<RequestReportRecordContract> pagedIterableMock = mock(PagedIterable.class);
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Consumer<RequestReportRecordContract> consumer = invocation.getArgument(0);
                mockedResponse.forEach(consumer);
                return null;
            }
        }).when(pagedIterableMock).forEach(any());

        when(azureManagersHolder.getAzureApiManager()).thenReturn(mock(ApiManagementManager.class));
        when(azureManagersHolder.getAzureApiManager().reports()).thenReturn(mock(Reports.class));
        when(azureManagersHolder.getAzureApiManager().reports().listByRequest(any(), any(), any())).thenReturn(pagedIterableMock);
        when(azureManagersHolder.getAzureApiManager().reports().listByRequest(any(), any(), any()).stream()).thenReturn(mockedResponse.stream());
        when(azureManagersHolder.getAzureApiManager().apis()).thenReturn(mock(Apis.class));
        when(azureManagersHolder.getAzureApiManager().apis().get(any(), any(), any())).thenReturn(apiContract);



         metrics=metricsManager.metricsTypeHandler(1717560459000L, 1717561359000L, 900,15,"requests");

        float averageBackendLatency = (float) ((requestReportRecordContract1.serviceTime() + requestReportRecordContract2.serviceTime()) / 2);
        float averageTotalLatency = (float) ((requestReportRecordContract1.apiTime() + requestReportRecordContract2.apiTime()) / 2);
        float averageGatewayLatency = averageTotalLatency - averageBackendLatency;

        assertEquals("12323-3454-34874_arajgw_-1", metrics.get(0).getApiTransactionMetricsList().get(0).getApiId());
        assertEquals(1, metrics.get(0).getApiTransactionMetricsList().get(0).getMetricsByStatusCode().get("2xx").getTransactionCount());
        assertEquals(1, metrics.get(0).getApiTransactionMetricsList().get(1).getMetricsByStatusCode().get("5xx").getTransactionCount());
        assertEquals(metrics.get(0).getApiTransactionMetricsList().get(0).getApiMetrics().getAverageBackendLatency(),requestReportRecordContract1.serviceTime().floatValue());
        assertEquals(averageBackendLatency,metrics.get(0).getRuntimeTransactionMetrics().getApiMetrics().getAverageBackendLatency());
        assertEquals(averageTotalLatency,metrics.get(0).getRuntimeTransactionMetrics().getApiMetrics().getAverageTotalLatency());
        assertEquals(averageGatewayLatency,metrics.get(0).getRuntimeTransactionMetrics().getApiMetrics().getAverageGatewayLatency());

    }


    @Test
    public void metricsRetrieverByDataTestMultipleTransaction(){
        Metrics metrics;

        //mock for the same api-id id
        RequestReportRecordContract requestReportRecordContract1 = MetricsMangerUtil.requestReportRecordContract("api-id-1",2.0,1.0,200,"200");
        RequestReportRecordContract requestReportRecordContract3 = MetricsMangerUtil.requestReportRecordContract("api-id-1",3.0,3.0,500,"500");
        RequestReportRecordContract requestReportRecordContract4 = MetricsMangerUtil.requestReportRecordContract("api-id-1",4.0,2.0,400,"400");
        RequestReportRecordContract requestReportRecordContract5 = MetricsMangerUtil.requestReportRecordContract("api-id-1",5.1,4.0,401,"401");

        RequestReportRecordContract requestReportRecordContract2 = MetricsMangerUtil.requestReportRecordContract("api-id-2",4.0,2.0,500,"500");
        ApiContract apiContract = MetricsMangerUtil.apiContract(ApiType.SOAP,"/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.ApiManagement/service/apimService1/apis/a1","hello-api","Hello API","c48f96c9-1385-4e2d-b410-5ab591ce0fc4",true,"This is hello world description");

        List<RequestReportRecordContract> mockedResponse= List.of(requestReportRecordContract1,requestReportRecordContract2,requestReportRecordContract3,requestReportRecordContract4,requestReportRecordContract5);

        PagedIterable<RequestReportRecordContract> pagedIterableMock = mock(PagedIterable.class);
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Consumer<RequestReportRecordContract> consumer = invocation.getArgument(0);
                mockedResponse.forEach(consumer);
                return null;
            }
        }).when(pagedIterableMock).forEach(any());

        when(azureManagersHolder.getAzureApiManager()).thenReturn(mock(ApiManagementManager.class));
        when(azureManagersHolder.getAzureApiManager().reports()).thenReturn(mock(Reports.class));
        when(azureManagersHolder.getAzureApiManager().reports().listByRequest(any(),any(),any())).thenReturn(pagedIterableMock);
        when(azureManagersHolder.getAzureApiManager().reports().listByRequest(any(),any(),any()).stream()).thenReturn(mockedResponse.stream());
        when(azureManagersHolder.getAzureApiManager().apis()).thenReturn(mock(Apis.class));
        when(azureManagersHolder.getAzureApiManager().apis().get(any(),any(),any())).thenReturn(apiContract);

        metrics=metricsManager.metricsRetrieverByRequests(1717561359658L,1717561359659L,15);


        float averageBackendLatency = (float) ((requestReportRecordContract1.serviceTime()+requestReportRecordContract2.serviceTime()+requestReportRecordContract3.serviceTime()+requestReportRecordContract4.serviceTime()+requestReportRecordContract5.serviceTime())/5);
        float averageTotalLatency = (float) ((requestReportRecordContract1.apiTime()+requestReportRecordContract2.apiTime()+requestReportRecordContract3.apiTime()+requestReportRecordContract4.apiTime()+requestReportRecordContract5.apiTime())/5);
        float apiAverageBackendLatency = ((requestReportRecordContract1.serviceTime().floatValue()+requestReportRecordContract3.serviceTime().floatValue()+requestReportRecordContract4.serviceTime().floatValue()+requestReportRecordContract5.serviceTime().floatValue())/4);

        assertEquals("12323-3454-34874_arajgw_-1", metrics.getApiTransactionMetricsList().get(0).getApiId());
        assertEquals(2, metrics.getApiTransactionMetricsList().get(0).getMetricsByStatusCode().get("4xx").getTransactionCount());
        assertEquals(1, metrics.getApiTransactionMetricsList().get(1).getMetricsByStatusCode().get("5xx").getTransactionCount());
        assertEquals(apiAverageBackendLatency,metrics.getApiTransactionMetricsList().get(0).getApiMetrics().getAverageBackendLatency());
        assertEquals(averageBackendLatency,metrics.getRuntimeTransactionMetrics().getApiMetrics().getAverageBackendLatency());
        assertEquals(averageTotalLatency,metrics.getRuntimeTransactionMetrics().getApiMetrics().getAverageTotalLatency());
   }

    @Test
    public void metricsRetrieverByStatisticsTest(){
        Metrics metrics;

        //mock for metricsByApi
        ReportRecordContract reportRecordContract1 = MetricsMangerUtil.reportRecordContract("api-Id-1",3,2.0,1.0);
        ReportRecordContract reportRecordContract2 = MetricsMangerUtil.reportRecordContract("api-Id-2",4,2.0,1.0);
        //mock for metricsByTime
        ReportRecordContract reportRecordContract3 = MetricsMangerUtil.reportRecordContract(null,40,2.0,1.0);
        //mock for apiContract
        ApiContract apiContract = MetricsMangerUtil.apiContract(ApiType.SOAP,"/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.ApiManagement/service/apimService1/apis/a1","hello-api","Hello API","c48f96c9-1385-4e2d-b410-5ab591ce0fc4",true,"This is hello world description");

        List<ReportRecordContract> mockedListByApi= List.of(reportRecordContract1,reportRecordContract2);
        List<ReportRecordContract> mockedListByTime= List.of(reportRecordContract3);

        PagedIterable<ReportRecordContract> pagedIterableMockByApi = mock(PagedIterable.class);
        PagedIterable<ReportRecordContract> pagedIterableMockByTime = mock(PagedIterable.class);


        when(azureManagersHolder.getAzureApiManager()).thenReturn(mock(ApiManagementManager.class));
        when(azureManagersHolder.getAzureApiManager().reports()).thenReturn(mock(Reports.class));
        when(azureManagersHolder.getAzureApiManager().reports().listByApi(any(),any(),any())).thenReturn(pagedIterableMockByApi);
        when(azureManagersHolder.getAzureApiManager().reports().listByApi(any(),any(),any()).stream()).thenReturn(mockedListByApi.stream());
        when(azureManagersHolder.getAzureApiManager().reports().listByTime(any(),any(),any(),any())).thenReturn(pagedIterableMockByTime);
        when(azureManagersHolder.getAzureApiManager().reports().listByTime(any(),any(),any(),any()).stream()).thenReturn(mockedListByTime.stream()).thenReturn(mockedListByTime.stream());
        when(azureManagersHolder.getAzureApiManager().apis()).thenReturn(mock(Apis.class));
        when(azureManagersHolder.getAzureApiManager().apis().get(any(),any(),any())).thenReturn(apiContract);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Consumer<ReportRecordContract> consumer = invocation.getArgument(0);
                mockedListByApi.forEach(consumer);
                return null;
            }
        }).when(pagedIterableMockByApi).forEach(any());

         doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Consumer<ReportRecordContract> consumer = invocation.getArgument(0);
                mockedListByTime.forEach(consumer);
                return null;
            }
        }).when(pagedIterableMockByTime).forEach(any());


        metrics=metricsManager.metricsRetrieverByInsights(1717560459659L,1717561359659L,15,15);

        assertEquals("12323-3454-34874_arajgw_-1", metrics.getApiTransactionMetricsList().get(0).getApiId());
        float averageGatewayLatency= (float) (reportRecordContract1.apiTimeAvg() -reportRecordContract1.serviceTimeAvg());
        assertEquals(averageGatewayLatency,metrics.getApiTransactionMetricsList().get(0).getApiMetrics().getAverageGatewayLatency());
        assertEquals(reportRecordContract1.serviceTimeAvg().floatValue(),metrics.getApiTransactionMetricsList().get(0).getApiMetrics().getAverageBackendLatency());
        assertEquals(reportRecordContract3.apiTimeAvg().floatValue(),metrics.getRuntimeTransactionMetrics().getApiMetrics().getAverageTotalLatency());
        assertTrue(metrics.getApiTransactionMetricsList().get(0).getMetricsByStatusCode().isEmpty());
        assertTrue(metrics.getRuntimeTransactionMetrics().getMetricsByStatusCode().isEmpty());
    }

    @AfterEach
    public void cleanUp() {
        if(mockAzureManagerHolder.isClosed()) {
            System.out.println("closed");
        }
        mockAzureManagerHolder.close();


    }

    @AfterAll
    public void teardown() {
        Mockito.reset(azureManagersHolder);
        clearAllCaches();

    }
}
