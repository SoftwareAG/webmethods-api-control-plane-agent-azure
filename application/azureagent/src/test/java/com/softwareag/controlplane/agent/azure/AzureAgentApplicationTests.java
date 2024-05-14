package com.softwareag.controlplane.agent.azure;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServiceResource;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServices;
import com.softwareag.controlplane.agent.azure.configuration.AzureProperties;
import com.softwareag.controlplane.agent.azure.context.AzureAgentManualContextImpl;
import com.softwareag.controlplane.agent.azure.context.AzureAuthentication;
import com.softwareag.controlplane.agentsdk.api.AgentSDKContextManual;
import com.softwareag.controlplane.agentsdk.api.SdkLogger;
import com.softwareag.controlplane.agentsdk.api.client.http.SdkHttpClient;
import com.softwareag.controlplane.agentsdk.api.config.SdkConfig;
import com.softwareag.controlplane.agentsdk.model.API;
import com.softwareag.controlplane.agentsdk.model.Asset;
import com.softwareag.controlplane.agentsdk.model.AssetSyncAction;
import com.softwareag.controlplane.agentsdk.model.Heartbeat;
import com.softwareag.controlplane.agentsdk.model.Metrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true","apicp.asset-catalog.execute.initializers=false"})
class AzureAgentApplicationTests {

    @Autowired
    AgentSDKContextManual manualContext;

  //  @Test
    void main() {
        SpringApplication.run(AzureAgentApplication.class, new String[]{});
    }

    @TestConfiguration
    static class AzureAgentManualContextImplTestContextConfiguration {
        @Bean
        public AgentSDKContextManual manualContext() {
            return new AgentSDKContextManual() {

                @Override
                public SdkConfig getSdkConfig() {
                    return null;
                }

                @Override
                public SdkLogger getLogger() {
                    return null;
                }

                @Override
                public SdkHttpClient getHttpClient() {
                    return null;
                }

                @Override
                public Heartbeat getHeartbeat() {
                    return null;
                }

                @Override
                public List<API> getAPIs() {
                    return null;
                }

                @Override
                public List<Metrics> getMetrics(long l, long l1, long l2) {
                    return null;
                }

                @Override
                public List<AssetSyncAction<Asset>> getAssetSyncActions(long l) {
                    return null;
                }
            };
            }}




}
