package com.softwareag.controlplane.agent.azure.common.handlers;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiContract;
import com.azure.resourcemanager.apimanagement.models.Apis;
import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.common.handlers.heartbeat.HeartbeatManager;
import com.softwareag.controlplane.agentsdk.model.Heartbeat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.*;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HeartbeatManagerTests {

    @Mock
    AzureManagersHolder azureManagersHolder;

    @Mock
    private ApiContract apiContract;

    @InjectMocks
    HeartbeatManager heartbeatManager;

    MockedStatic<AzureManagersHolder> mockAzureManagerHolder;

    @BeforeEach
    private void setup() {
        MockitoAnnotations.openMocks(this);
        mockAzureManagerHolder = Mockito.mockStatic(AzureManagersHolder.class);
        mockAzureManagerHolder.when(() -> AzureManagersHolder.getInstance())
                .thenReturn(azureManagersHolder);

        heartbeatManager = HeartbeatManager.getInstance();
    }

    @AfterEach
    public void cleanUp() {
        mockAzureManagerHolder.close();
    }


    @Test
    void generateValidHeartBeat() {
        ApiManagementManager apiManagementManager = Mockito.mock(ApiManagementManager.class);
        when(azureManagersHolder.getAzureApiManager()).thenReturn(apiManagementManager);
        when(apiManagementManager.apis()).thenReturn(Mockito.mock(Apis.class));

        Iterator mockIterator = Mockito.mock(Iterator.class);
        when(mockIterator.hasNext()).thenReturn(true, false);
        when(mockIterator.next()).thenReturn(apiContract);
        PagedIterable mockPagedTableEntities = Mockito.mock(PagedIterable.class);
        when(azureManagersHolder.getAzureApiManager().apis()
                .listByService(anyString(), anyString())).thenReturn(mockPagedTableEntities);
        Heartbeat heartbeat = heartbeatManager.generateHeartBeat("arajgw","azuregroup","serviceName");
        assertEquals("arajgw", heartbeat.getRuntimeId());
        assertEquals(heartbeat.getActive(), 0);
    }
}
