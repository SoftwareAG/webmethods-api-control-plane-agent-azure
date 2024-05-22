package com.softwareag.controlplane.agent.azure.configuration;

import com.softwareag.controlplane.agentsdk.core.validator.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;


public class AgentPropertiesTests {

    private AgentProperties agentProperties = new AgentProperties();

    @Test
    void testValidAgentProperties() {
        agentProperties.setPassword("manage");
        agentProperties.setUrl("http://test:8888");
        agentProperties.setUsername("John Doe");
        agentProperties.setSyncAssetsEnabled(true);
        agentProperties.setPublishAssetsEnabled(true);
        agentProperties.setSyncMetricsEnabled(true);
        agentProperties.setSyncAssetsIntervalSeconds(300);
        agentProperties.setSyncHeartbeatIntervalSeconds(60);
        agentProperties.setSyncMetricsIntervalSeconds(500);
        Set<ConstraintViolation<AgentProperties>> violations = ValidatorFactory.getValidator().validate(agentProperties);
        assert(violations).isEmpty();
    }

    @Test
    void testInValidAgentProperties() {
        agentProperties.setPassword(null);
        agentProperties.setUrl("http://test:8888");
        agentProperties.setUsername("John Doe");
        agentProperties.setSyncAssetsEnabled(true);
        agentProperties.setPublishAssetsEnabled(true);
        agentProperties.setSyncMetricsEnabled(true);
        agentProperties.setSyncAssetsIntervalSeconds(300);
        agentProperties.setSyncHeartbeatIntervalSeconds(60);
        agentProperties.setSyncMetricsIntervalSeconds(500);
        Set<ConstraintViolation<AgentProperties>> violations = ValidatorFactory.getValidator().validate(agentProperties);
        assertFalse(violations.isEmpty());
    }

}
