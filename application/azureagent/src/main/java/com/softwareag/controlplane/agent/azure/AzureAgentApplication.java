package com.softwareag.controlplane.agent.azure;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServiceResource;
import com.azure.resourcemanager.resources.ResourceManager;
import com.softwareag.controlplane.agent.azure.configuration.AgentProperties;
import com.softwareag.controlplane.agent.azure.configuration.AzureProperties;
import com.softwareag.controlplane.agent.azure.configuration.RuntimeProperties;
import com.softwareag.controlplane.agent.azure.context.AzureAgentManualContextImpl;
import com.softwareag.controlplane.agent.azure.context.AzureManagersHolder;
import com.softwareag.controlplane.agentsdk.core.AgentSdk;
import com.softwareag.controlplane.agentsdk.core.InitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EnableConfigurationProperties({AgentProperties.class, AzureProperties.class,
		 RuntimeProperties.class})
public class AzureAgentApplication implements CommandLineRunner {

	@Autowired
	AzureAgentManualContextImpl manualContext;

	@Autowired
	AzureProperties azureProperties;

	@Autowired
	private ApplicationContext context;

	@Autowired
	AzureManagersHolder managerHolder;


	public static void main(String[] args) {
		SpringApplication.run(AzureAgentApplication.class, args);
	}

	@Override
	public void run(String... args)  {
		System.out.println("Running Spring Boot Application console " );
	}

	@EventListener(ApplicationReadyEvent.class)
	public void initializeCPAgent(){
		authenticateAzureSDK();
		try {
			AgentSdk.initialize(manualContext);
		} catch (InitializationException e) {
			e.printStackTrace();
		}
	}

	private void authenticateAzureSDK() {
		AzureProfile profile = new AzureProfile(
				azureProperties.getTenantId(),
				azureProperties.getSubscriptionId(), AzureEnvironment.AZURE);

		ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
				.clientId(azureProperties.getClientId())
				.clientSecret(azureProperties.getClientSecret())
				.tenantId(azureProperties.getTenantId())
				.build();

		initializeAzureManagers(clientSecretCredential, profile);
	}

	private void initializeAzureManagers(ClientSecretCredential clientSecretCredential, AzureProfile profile) {
		ApiManagementManager apiManager = ApiManagementManager
				.authenticate(clientSecretCredential, profile);
		ApiManagementServiceResource apiService = apiManager.apiManagementServices()
				.getByResourceGroup(azureProperties.getResourceGroup(), azureProperties.getApiManagementServiceName());

		if(apiManager == null || apiService == null) {
			int exitCode = SpringApplication.exit(context, () -> 0);
			System.exit(exitCode);
		}

		ResourceManager resourceManager = ResourceManager
				.authenticate(clientSecretCredential, profile).withSubscription(azureProperties.getSubscriptionId());
		managerHolder.setApiService(apiService);
		managerHolder.setAzureProfile(profile);
		managerHolder.setAzureApiManager(apiManager);
		managerHolder.setAzureResourceManager(resourceManager);
	}
}
