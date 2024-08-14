/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.functions.utils;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiManagementServiceResource;
import com.azure.resourcemanager.resources.ResourceManager;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.common.utils.AzureAgentUtil;
import com.softwareag.controlplane.agent.azure.functions.utils.constants.Constants;
import com.softwareag.controlplane.agentsdk.api.client.ControlPlaneClient;
import com.softwareag.controlplane.agentsdk.api.client.SdkClientException;
import com.softwareag.controlplane.agentsdk.api.client.http.SdkHttpClient;
import com.softwareag.controlplane.agentsdk.api.config.*;
import com.softwareag.controlplane.agentsdk.core.client.DefaultHttpClient;
import com.softwareag.controlplane.agentsdk.core.client.RestControlPlaneClient;
import com.softwareag.controlplane.agentsdk.core.handler.RuntimeRegistrationHandler;
import com.softwareag.controlplane.agentsdk.core.model.RuntimeReregistrationReport;
import com.softwareag.controlplane.agentsdk.model.AssetSyncMethod;
import com.softwareag.controlplane.agentsdk.model.Capacity;
import com.softwareag.controlplane.agentsdk.model.Runtime;
import org.apache.commons.lang3.ObjectUtils;
import com.azure.resourcemanager.resources.models.Location;

import java.io.IOException;

/**
 * Utilities class for FAAS implementation.
 */
public class Utils {
    private Utils(){}
    private static TlsConfig getTlsConfig() {
        String isSSLEnabled = DefaultEnvProvider.getEnv(Constants.APICP_SSL_ENABLED);
        String trustStorePath = DefaultEnvProvider.getEnv(Constants.APICP_TRUST_STORE_PATH);
        String trustStoreType = DefaultEnvProvider.getEnv(Constants.APICP_TRUST_STORE_TYPE);
        String trustStorePassword = DefaultEnvProvider.getEnv(Constants.APICP_TRUST_STORE_PASSWORD);

        if(isSSLEnabled.equals("true") && ObjectUtils.isNotEmpty(trustStorePath) && ObjectUtils.isNotEmpty(trustStorePassword)) {
            return new TlsConfig.Builder(trustStorePath, trustStoreType)
                    .truststorePassword(trustStorePassword)
                    .keystorePath(ObjectUtils.isNotEmpty(DefaultEnvProvider.getEnv(Constants.APICP_KEY_STORE_PATH)) ?
                            DefaultEnvProvider.getEnv(Constants.APICP_KEY_STORE_PATH): null)
                    .keystorePassword(ObjectUtils.isNotEmpty(DefaultEnvProvider.getEnv(Constants.APICP_KEY_STORE_PASSWORD)) ?
                            DefaultEnvProvider.getEnv(Constants.APICP_KEY_STORE_PASSWORD) : null)
                    .keyAlias(ObjectUtils.isNotEmpty(DefaultEnvProvider.getEnv(Constants.APICP_KEY_ALIAS)) ? DefaultEnvProvider.getEnv(Constants.APICP_KEY_ALIAS) : null)
                    .keyPassword(ObjectUtils.isNotEmpty(DefaultEnvProvider.getEnv(Constants.APICP_KEY_PASSWORD)) ? DefaultEnvProvider.getEnv(Constants.APICP_KEY_PASSWORD) : null)
                    .keystoreType(ObjectUtils.isNotEmpty(DefaultEnvProvider.getEnv(Constants.APICP_KEY_STORE_TYPE)) ? DefaultEnvProvider.getEnv(Constants.APICP_KEY_STORE_TYPE) : null)
                    .build();
        }
        return null;
    }

    /**
     * Creates and configures ControlPlane configuration.
     *
     * @return An instance of {@link ControlPlaneConfig}
     */
    public static ControlPlaneConfig getControlPlaneConfig() {
        AuthConfig authConfig;
        if(ObjectUtils.isNotEmpty(DefaultEnvProvider.getEnv(Constants.APICP_TOKEN))) {
            authConfig = new AuthConfig
                    .Builder(DefaultEnvProvider.getEnv(Constants.APICP_TOKEN))
                    .build();
        } else {
            authConfig = new AuthConfig
                    .Builder(DefaultEnvProvider.getEnv(Constants.APICP_USERNAME), DefaultEnvProvider.getEnv(Constants.APICP_PASSWORD))
                    .build();
        }
        return new ControlPlaneConfig.Builder()
                .url(DefaultEnvProvider.getEnv(Constants.APICP_URL))
                .authConfig(authConfig)
                .tlsConfig(getTlsConfig())
                .build();
    }

    /**
     * Creates and configures Runtime configuration.
     *
     * @param azureManagersHolder the azure managers holder
     * @return An instance of {@link RuntimeConfig}.
     */
    public static RuntimeConfig getRuntimeConfig(AzureManagersHolder azureManagersHolder) {
        Location location = azureManagersHolder.getAzureResourceManager().subscriptions()
                .getById(DefaultEnvProvider.getEnv(Constants.AZURE_SUBSCRIPTION_ID))
                .getLocationByRegion(Region.fromName(azureManagersHolder.getApiService().regionName()));

        return new RuntimeConfig.Builder(getRuntimeId(),
                DefaultEnvProvider.getEnv(Constants.AZURE_API_MANAGEMENT_SERVICE_NAME), DefaultEnvProvider.getEnv(Constants.APICP_RUNTIME_TYPE),
                Runtime.DeploymentType.PUBLIC_CLOUD)
                .region(azureManagersHolder.getApiService().regionName())
                .location(location.physicalLocation())
                .tags(AzureAgentUtil.convertTags(azureManagersHolder.getApiService().tags()))
                .capacity(getCapacity())
                .build();
    }

    /**
     * Creates and configures a SdkConfig object using the provided ControlPlane configuration and runtime configuration.
     *
     * @param controlPlaneConfig The {@link ControlPlaneConfig} object.
     * @param runtimeConfig      The runtime configuration object.
     * @return An {@link SdkConfig} initialized with the provided configurations.
     */
    public static SdkConfig getSdkConfig(ControlPlaneConfig controlPlaneConfig, RuntimeConfig runtimeConfig) {
        return  new SdkConfig.Builder(controlPlaneConfig, runtimeConfig)
                .assetSyncMethod(AssetSyncMethod.POLLING)
                .heartbeatInterval(Integer.parseInt(DefaultEnvProvider.getEnv(Constants.APICP_SYNC_HEARTBEAT_INTERVAL_SECONDS)))
                .assetsSyncInterval(Integer.parseInt(DefaultEnvProvider.getEnv(Constants.APICP_SYNC_ASSETS_INTERVAL_SECONDS)))
                .metricsSendInterval(Integer.parseInt(DefaultEnvProvider.getEnv(Constants.APICP_SYNC_METRICS_INTERVAL_SECONDS)))
                .build();

    }

    /**
     * Creates and configures a ControlPlaneClient Object.
     *
     * @param controlPlaneConfig The {@link ControlPlaneConfig} object.
     * @param runtimeConfig      The {@link RuntimeConfig} object.
     * @return {@link ControlPlaneClient} A client which communicates to Control Plane APIs.
     */
    public static ControlPlaneClient getControlplaneClient(ControlPlaneConfig controlPlaneConfig, RuntimeConfig runtimeConfig) {
        SdkHttpClient httpClient = new DefaultHttpClient.Builder()
                .tlsConfig(controlPlaneConfig.getTlsConfig())
                .connectionConfig(controlPlaneConfig.getConnectionConfig())
                .build();

        return new RestControlPlaneClient.Builder()
                .runtimeConfig(runtimeConfig)
                .controlPlaneConfig(controlPlaneConfig)
                .httpClient(httpClient)
                .build();
    }

    /**
     * Gets runtime id.
     *
     * @return the runtime id
     */
    public static String getRuntimeId() {
        return DefaultEnvProvider.getEnv(Constants.AZURE_SUBSCRIPTION_ID) +
                Constants.UNDERSCORE + DefaultEnvProvider.getEnv(Constants.AZURE_API_MANAGEMENT_SERVICE_NAME);
    }

    /**
     * Gets capacity.
     *
     * @return the capacity
     */
    public static Capacity getCapacity() {
        Capacity capacity = null;
        if(ObjectUtils.isNotEmpty(DefaultEnvProvider.getEnv(Constants.APICP_RUNTIME_CAPACITY_VALUE))) {
            capacity = new Capacity();
            capacity.setUnit(Capacity.TimeUnit.valueOf(DefaultEnvProvider.getEnv(Constants.APICP_RUNTIME_CAPACITY_UNIT)));
            capacity.setValue(Long.parseLong(DefaultEnvProvider.getEnv(Constants.APICP_RUNTIME_CAPACITY_VALUE)));
        }
        return capacity;
    }

    /**
     * Gets runtime registration handler.
     *
     * @param controlPlaneClient the control plane client
     * @param sdkConfig          the sdk config
     * @return the runtime registration handler
     */
    public static RuntimeRegistrationHandler getRuntimeRegistrationHandler(ControlPlaneClient controlPlaneClient, SdkConfig sdkConfig) {
        return new RuntimeRegistrationHandler.Builder(
                controlPlaneClient,
                sdkConfig.getRuntimeConfig(),
                sdkConfig.getHeartbeatInterval())
                .build();
    }

    /**
     * Authenticates to Azure portal.
     *
     * @param azureManagersHolder The holder object {@link AzureManagersHolder} for Azure managers that will be initialized.
     */
    public static void authenticate(AzureManagersHolder azureManagersHolder) {
        AzureProfile profile = new AzureProfile(
                DefaultEnvProvider.getEnv(Constants.AZURE_TENANT_ID),
                DefaultEnvProvider.getEnv(Constants.AZURE_SUBSCRIPTION_ID), AzureEnvironment.AZURE);

        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(DefaultEnvProvider.getEnv(Constants.AZURE_CLIENT_ID))
                .clientSecret(DefaultEnvProvider.getEnv(Constants.AZURE_CLIENT_SECRET))
                .tenantId(DefaultEnvProvider.getEnv(Constants.AZURE_TENANT_ID))
                .build();

        initializeAzureManagers(clientSecretCredential, profile, azureManagersHolder);
    }

    private static void initializeAzureManagers(ClientSecretCredential clientSecretCredential, AzureProfile profile, AzureManagersHolder azureManagersHolder) {
        ApiManagementManager apiManager = ApiManagementManager
                .authenticate(clientSecretCredential, profile);

        if(apiManager == null) {
            throw  new RuntimeException("API management service doesn't exist");
        }

        ApiManagementServiceResource apiService = apiManager.apiManagementServices()
                .getByResourceGroup(DefaultEnvProvider.getEnv(Constants.AZURE_RESOURCE_GROUP), DefaultEnvProvider.getEnv(Constants.AZURE_API_MANAGEMENT_SERVICE_NAME));

        if(apiService == null) {
            System.exit(1);
        }

        ResourceManager resourceManager = ResourceManager
                .authenticate(clientSecretCredential, profile).withSubscription(DefaultEnvProvider.getEnv(Constants.AZURE_SUBSCRIPTION_ID));
        azureManagersHolder.setApiService(apiService);
        azureManagersHolder.setAzureProfile(profile);
        azureManagersHolder.setAzureApiManager(apiManager);
        azureManagersHolder.setAzureResourceManager(resourceManager);
    }

    /**
     * Checks if the ControlPlane is active and healthy.
     *
     * @param controlPlaneClient The ControlPlaneClient object which communicates to Control Plane APIs.
     * @return {@code true} if the ControlPlane is active and healthy; {@code false} otherwise.
     */
    public static boolean isControlplaneActive(ControlPlaneClient controlPlaneClient) {
        try {
            controlPlaneClient.checkHealth();
        } catch (SdkClientException e) {
            return false;
        }
        return true;
    }


    /**
     * Retrieves the timestamp of the last synchronization action from ControlPlane.
     *
     * @param responseObject The responseObject returned while registering runtime.
     * @param actionType     The type of action for which to retrieve the sync time.
     * @return The timestamp of the last synchronization action.
     * @throws IOException If an error occurs during JSON deserialization.
     */
    public static Long getLastActionSyncTime(Object responseObject, String actionType) throws IOException {
        if (responseObject instanceof String response) {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            RuntimeReregistrationReport reregistrationReport = mapper.readValue(response, RuntimeReregistrationReport.class);
            switch (actionType){
                case Constants.SYNC_ASSET_ACTION -> {
                    return reregistrationReport.getLastAssetSyncTime();
                }
                case Constants.SEND_HEARTBEAT_ACTION -> {
                    return reregistrationReport.getLastHeartbeatTime();
                }
                case Constants.SEND_METRIC_ACTION -> {
                    return reregistrationReport.getLastMetricsTime();
                }
                default -> {
                    return null;
                }
            }
        }
        return null;
    }
}
