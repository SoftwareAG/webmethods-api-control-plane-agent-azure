/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.common.handlers.Util;

import com.azure.resourcemanager.apimanagement.models.ApiContract;
import com.azure.resourcemanager.apimanagement.models.ApiType;
import com.azure.resourcemanager.apimanagement.models.ReportRecordContract;
import com.azure.resourcemanager.apimanagement.models.RequestReportRecordContract;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetricsMangerUtil {

    public static RequestReportRecordContract requestReportRecordContract(String apiId, double apiTime, double serviceTime,int responseCode,String backendResponseCode){
        RequestReportRecordContract requestReportRecordContract = mock(RequestReportRecordContract.class);
        when(requestReportRecordContract.apiId()).thenReturn(apiId);
        when(requestReportRecordContract.apiTime()).thenReturn(apiTime);
        when(requestReportRecordContract.serviceTime()).thenReturn(serviceTime);
        when(requestReportRecordContract.responseCode()).thenReturn(responseCode);
        when(requestReportRecordContract.backendResponseCode()).thenReturn(backendResponseCode);
        return requestReportRecordContract;
    }

    public static ApiContract apiContract(ApiType apiType, String apiId,String apiName,String displayName,String apiVersionSetId,boolean isCurrent,String description){
        ApiContract apiContract = Mockito.mock(ApiContract.class);
        when(apiContract.apiType()).thenReturn(apiType);
        when(apiContract.id()).thenReturn(apiId);
        when(apiContract.name()).thenReturn(apiName);
        when(apiContract.displayName()).thenReturn(displayName);
        when(apiContract.apiVersionSetId()).thenReturn(apiVersionSetId);
        when(apiContract.isCurrent()).thenReturn(isCurrent);
        when(apiContract.description()).thenReturn(description);

        return apiContract;
    }

    public static ReportRecordContract reportRecordContract(String apiId, int callCountTotal, double apiTimeAvg, double serviceTimeAvg){
        ReportRecordContract reportRecordContract = mock(ReportRecordContract.class);
        when(reportRecordContract.apiId()).thenReturn(apiId);
        when(reportRecordContract.callCountTotal()).thenReturn(callCountTotal);
        when(reportRecordContract.apiTimeAvg()).thenReturn(apiTimeAvg);
        when(reportRecordContract.serviceTimeAvg()).thenReturn(serviceTimeAvg);

        return reportRecordContract;
    }
}
