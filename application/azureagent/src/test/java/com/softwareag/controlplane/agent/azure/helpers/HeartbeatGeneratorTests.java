package com.softwareag.controlplane.agent.azure.helpers;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiContract;
import com.azure.resourcemanager.apimanagement.models.Apis;
import com.softwareag.controlplane.agent.azure.configuration.AzureProperties;
import com.softwareag.controlplane.agent.azure.context.AzureManagersHolder;
import com.softwareag.controlplane.agentsdk.model.Heartbeat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestPropertySource(locations = "azure")
public class HeartbeatGeneratorTests {

    private AzureProperties properties = new AzureProperties();


    AzureManagersHolder azureManagersHolder = spy(new AzureManagersHolder());

    private ApiContract apiContract;

    HeartbeatGenerator heartbeatGenerator = spy(new HeartbeatGenerator(properties, azureManagersHolder));

    @BeforeEach
    private void setup() {
        properties.setClientId("abc");
        properties.setClientSecret("ghj");
        properties.setSubscriptionId("qwerty");
        properties.setTenantId("default");
        properties.setResourceGroup("azuregroup");
        properties.setApiManagementServiceName("serviceName");
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateValidHeartBeat() {
        when(azureManagersHolder.getAzureApiManager()).thenReturn(Mockito.mock(ApiManagementManager.class));
        when(azureManagersHolder.getAzureApiManager().apis()).thenReturn(Mockito.mock(Apis.class));

        Iterator mockIterator = Mockito.mock(Iterator.class);
        when(mockIterator.hasNext()).thenReturn(true, false);
        when(mockIterator.next()).thenReturn(apiContract);
        PagedIterable mockPagedTableEntities = Mockito.mock(PagedIterable.class);
        when(azureManagersHolder.getAzureApiManager().apis()
                .listByService(anyString(), anyString())).thenReturn(mockPagedTableEntities);
        Heartbeat heartbeat = heartbeatGenerator.generateHeartBeat("arajgw");
        verify(heartbeatGenerator, times(1)).generateHeartBeat("arajgw");
        assertEquals(heartbeatGenerator.generateHeartBeat("arajgw").getRuntimeId(), heartbeat.getRuntimeId());
    }
}
