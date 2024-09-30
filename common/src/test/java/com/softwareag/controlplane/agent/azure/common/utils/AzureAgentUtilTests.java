/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.common.utils;

import com.softwareag.controlplane.agentsdk.model.Owner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AzureAgentUtilTests {

    @Test
    void reduceTimeRangeTest() {
        /*
        1715695018853 :14 May 2024, 19:26:58
        1715694118000L :14 May 2024, 19:11:58
        bufferInSeconds:900s(15min)
        */
        assertEquals(1715694118853L, AzureAgentUtil.reduceTimeRange(Long.parseLong("1715695018853"),900));

        /*
        1715695018853 :14 May 2024, 19:26:58
        1715694118000L :14 May 2024, 19:06:58
        bufferInSeconds:1200s(20min)
        */
        assertEquals(1715693818853L, AzureAgentUtil.reduceTimeRange(Long.parseLong("1715695018853"),1200));
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

    @Test
    void filterTimeConversionTest(){
       assertEquals("2024-05-14T13:56:58.853Z",AzureAgentUtil.filterTimeConversion(1715695018853L));
       assertEquals("2024-06-07T11:57:45.216Z",AzureAgentUtil.filterTimeConversion(1717761465216L));
    }

    @Test
    void alignTimestampsWithIntervalTest(){
        /*
        1717761465216L :7 Jun 2024, 17:27:45
        1717760700000L :7 Jun 2024, 17:15:00
         */
        assertEquals(1717760700000L,AzureAgentUtil.alignTimestampsWithInterval(1717761465216L,900));
        /*
        1717761465216L :7 Jun 2024, 17:27:45
        1717760400000L :7 Jun 2024, 17:10:00
         */
        assertEquals(1717760400000L,AzureAgentUtil.alignTimestampsWithInterval(1717761465216L,1200));
    }

    @Test
    void constructFilter(){
        String mockFilterString1="timestamp ge datetime'2024-06-12T06:05:56.927Z' and timestamp le datetime'2024-06-12T06:07:56.925Z'";
        String mockFilterString2="timestamp ge datetime'2024-06-12T05:57:56.926Z' and timestamp le datetime'2024-06-12T05:59:56.926Z'";
        assertEquals(mockFilterString1,AzureAgentUtil.constructFilter("2024-06-12T06:05:56.927Z","2024-06-12T06:07:56.925Z"));
        assertEquals(mockFilterString2,AzureAgentUtil.constructFilter("2024-06-12T05:57:56.926Z","2024-06-12T05:59:56.926Z"));


    }
}
