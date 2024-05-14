package com.softwareag.controlplane.agent.azure.helpers;

import com.softwareag.controlplane.agentsdk.model.Owner;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mockStatic;

public class AzureAgentUtilTests {

    @Test
    void reduceTimeRangeTest() {
        assertEquals("2024-05-14T13:26:58.853Z", AzureAgentUtil.reduceTimeRange(Long.parseLong("1715695018853"),30));
        assertEquals("2024-05-14T13:46:58.853Z", AzureAgentUtil.reduceTimeRange(Long.parseLong("1715695018853"),10));
        assertEquals("2024-05-14T13:56:58.853Z", AzureAgentUtil.reduceTimeRange(Long.parseLong("1715695018853"),0));
        assertEquals("2024-05-14T12:56:58.853Z", AzureAgentUtil.reduceTimeRange(Long.parseLong("1715695018853"),60));
    }

    @Test
    void convertTagsTest() {
        assert(AzureAgentUtil.convertTags(new HashMap<>()).isEmpty());
        assert(AzureAgentUtil.convertTags(null).isEmpty());

        Map<String, String> tags = new HashMap<>();
        tags.put("tag1","value");
        assertFalse(AzureAgentUtil.convertTags(tags).isEmpty());
        assertEquals("tag1:value",AzureAgentUtil.convertTags(tags).stream().toList().get(0));
    }

    @Test
    void getOwnerInfoTest() {
        Owner owner = AzureAgentUtil.getOwnerInfo(null);
        assertNotNull(owner);
        assertNull(owner.getName());
        assertNull(owner.getId());

        owner = AzureAgentUtil.getOwnerInfo("snow");
        assertEquals(owner.getName(),"snow");
        assertEquals(owner.getId(),null);

    }
}
