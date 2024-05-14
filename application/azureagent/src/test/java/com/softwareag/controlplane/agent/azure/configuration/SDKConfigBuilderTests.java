package com.softwareag.controlplane.agent.azure.configuration;

import com.azure.core.management.Region;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServiceResource;
import com.azure.resourcemanager.apimanagement.models.Apis;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.Location;
import com.azure.resourcemanager.resources.models.Subscription;
import com.azure.resourcemanager.resources.models.Subscriptions;
import com.softwareag.controlplane.agent.azure.context.AzureManagersHolder;
import com.softwareag.controlplane.agentsdk.api.config.SdkConfig;
import com.softwareag.controlplane.agentsdk.core.validator.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class SDKConfigBuilderTests {

    @Valid
    @Spy
    private AgentProperties agentProperties = new AgentProperties();

    @Valid
    @Spy
    private RuntimeProperties runtimeProperties = new RuntimeProperties();
    @Valid
    @Spy
    private AzureProperties azureProperties = new AzureProperties();

    @Spy
    private AzureManagersHolder azureManagersHolder = spy(new AzureManagersHolder());

    @InjectMocks
    SDKConfigBuilder sdkConfigBuilder;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
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

//        sdkConfigBuilder = new SDKConfigBuilder(agentProperties, runtimeProperties, azureProperties,
//                azureManagersHolder);
     //   sdkConfigBuilder = new SDKConfigBuilder();

    }

    @Test
    void testSDKConfigInitialize() {

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

    @Test
    void testSDKConfig() {
        when(azureManagersHolder.getApiService()).thenReturn(Mockito.mock(ApiManagementServiceResource.class));
        when(azureManagersHolder.getApiService().regionName()).thenReturn("East Asia");
        when(azureManagersHolder.getAzureResourceManager()).thenReturn(Mockito.mock(ResourceManager.class));
        when(azureManagersHolder.getAzureResourceManager().subscriptions()).thenReturn(Mockito.mock(Subscriptions.class));
        when(azureManagersHolder.getAzureResourceManager().subscriptions().getById("qwerty")).thenReturn(Mockito.mock(Subscription.class));
        when(azureManagersHolder.getAzureResourceManager().subscriptions().getById("qwerty").getLocationByRegion(Region.ASIA_EAST)).thenReturn(Mockito.mock(Location.class));

        when(azureManagersHolder.getAzureApiManager()).thenReturn(Mockito.mock(ApiManagementManager.class));
        when(azureManagersHolder.getAzureApiManager().apis()).thenReturn(Mockito.mock(Apis.class));
        SdkConfig sdkconfig = sdkConfigBuilder.sdkConfig();
        assertThat(sdkconfig).isNotNull();
    }

}

