package com.softwareag.controlplane.agent.azure.configuration;

import com.softwareag.controlplane.agent.azure.context.AzureManagersHolder;
import com.softwareag.controlplane.agentsdk.core.validator.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SDKConfigBuilderTests {

    @Valid
    @Autowired
    private AgentProperties agentProperties;

    @Valid
    @Autowired
    private RuntimeProperties runtimeProperties;
    @Valid
    @Autowired
    private AzureProperties azureProperties;

    @Mock
    private AzureManagersHolder managerHolder;

    @BeforeEach
    public void setup() {
        //agent properties
        agentProperties.setPassword("manage");
        agentProperties.setUrl("http://test:8888");
        agentProperties.setUsername("John Doe");
        agentProperties.setSyncAssetsEnabled(true);
        agentProperties.setPublishAssetsEnabled(true);
        agentProperties.setSyncMetricsEnabled(true);
        agentProperties.setSyncAssetsIntervalSeconds(300);
        agentProperties.setSyncHeartbeatIntervalSeconds(60);
        agentProperties.setSyncMetricsIntervalSeconds(500);

        //runtime properties
        runtimeProperties.setCapacityUnit("per year");
        runtimeProperties.setType("AzureType");
        runtimeProperties.setCapacityValue("500000");

        //azure properties
        azureProperties.setClientId("abc");
        azureProperties.setClientSecret("ghj");
        azureProperties.setSubscriptionId("qwerty");
        azureProperties.setTenantId("default");
        azureProperties.setResourceGroup("azuregroup");
        azureProperties.setApiManagementServiceName("serviceName");

    }

    //@Test
    void testSDKConfig() {
        SDKConfigBuilder sdkConfigBuilder = new SDKConfigBuilder(agentProperties, runtimeProperties, azureProperties,
                managerHolder);
        Set<ConstraintViolation<AgentProperties>> violations = ValidatorFactory.getValidator().validate(agentProperties);
        assertThat(violations).isEmpty();

        Set<ConstraintViolation<AzureProperties>> azureViolations =
                ValidatorFactory.getValidator().validate(azureProperties);
        assertThat(azureViolations).isEmpty();

        Set<ConstraintViolation<RuntimeProperties>> runtimeViolations =
                ValidatorFactory.getValidator().validate(runtimeProperties);
        assertThat(runtimeViolations).isEmpty();
        assertThat(sdkConfigBuilder).isNotNull();
    }

}
