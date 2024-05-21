package com.softwareag.controlplane.agent.azure.configuration;

import com.softwareag.controlplane.agentsdk.core.validator.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(locations = "apicp")
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
        assertThat(violations).isEmpty();
    }

    @Test
    void testInValidAgentProperties() {
        agentProperties.setPassword(null);
        agentProperties.setUsername("John Doe");
        agentProperties.setPassword("John Doe");
        agentProperties.setSyncAssetsEnabled(true);
        agentProperties.setPublishAssetsEnabled(true);
        agentProperties.setSyncMetricsEnabled(true);
        agentProperties.setSyncAssetsIntervalSeconds(300);
        agentProperties.setSyncHeartbeatIntervalSeconds(60);
        agentProperties.setSyncMetricsIntervalSeconds(500);
        Set<ConstraintViolation<AgentProperties>> violations = ValidatorFactory.getValidator().validate(agentProperties);
        assertThat(violations).isNotEmpty();
    }

}
