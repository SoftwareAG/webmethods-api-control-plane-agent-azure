package com.softwareag.controlplane.agent.azure.functions.utils.constants;

/**
 * The Class Constants holds all variable values won't change once assigned and be used throughout the application.
 */
public class Constants {
    public static final String AZURE_SUBSCRIPTION_ID = "AZURE_SUBSCRIPTION_ID";
    public static final String AZURE_RESOURCE_GROUP = "AZURE_RESOURCE_GROUP";
    public static final String AZURE_TENANT_ID ="AZURE_TENANT_ID";
    public static final String AZURE_CLIENT_ID = "AZURE_CLIENT_ID";
    public static final String AZURE_CLIENT_SECRET = "AZURE_CLIENT_SECRET";
    public static final String AZURE_API_MANAGEMENT_SERVICE_NAME = "AZURE_API_MANAGEMENT_SERVICE_NAME";
    public static final String AZURE_METRICS_SYNC_BUFFER_INTERVAL_MINUTES = "AZURE_METRICS_SYNC_BUFFER_INTERVAL_MINUTES";
    public static final String AZURE_METRICS_BY_REQUESTS_OR_INSIGHTS = "AZURE_METRICS_BY_REQUESTS_OR_INSIGHTS";

    public static final String APICP_URL = "APICP_URL";
    public static final String APICP_USERNAME = "APICP_USERNAME";
    public static final String APICP_PASSWORD = "APICP_PASSWORD";
    public static final String APICP_TOKEN = "APICP_TOKEN";
    public static final String APICP_SYNC_HEARTBEAT_INTERVAL_SECONDS = "APICP_SYNC_HEARTBEAT_INTERVAL_SECONDS";
    public static final String APICP_SYNC_METRICS_INTERVAL_SECONDS = "APICP_SYNC_METRICS_INTERVAL_SECONDS";
    public static final String APICP_SYNC_ASSETS_INTERVAL_SECONDS = "APICP_SYNC_ASSETS_INTERVAL_SECONDS";
    public static final String APICP_RUNTIME_CAPACITY_VALUE = "APICP_RUNTIME_CAPACITY_VALUE";
    public static final String APICP_RUNTIME_CAPACITY_UNIT = "APICP_RUNTIME_CAPACITY_UNIT";
    public static final String APICP_RUNTIME_TYPE = "APICP_RUNTIME_TYPE";
    public static final String APICP_LOG_LEVEL = "APICP_LOG_LEVEL";


    public static final String APICP_SSL_ENABLED = "APICP_SSL_ENABLED";
    public static final String APICP_TRUST_STORE_PATH = "APICP_TRUST_STORE_PATH";
    public static final String APICP_TRUST_STORE_PASSWORD = "APICP_TRUST_STORE_PASSWORD";
    public static final String APICP_TRUST_STORE_TYPE = "APICP_TRUST_STORE_TYPE";
    public static final String APICP_KEY_STORE_PATH = "APICP_KEY_STORE_PATH";
    public static final String APICP_KEY_STORE_PASSWORD = "APICP_KEY_STORE_PASSWORD";
    public static final String APICP_KEY_STORE_TYPE = "APICP_KEY_STORE_TYPE";
    public static final String APICP_KEY_ALIAS = "APICP_KEY_ALIAS";
    public static final String APICP_KEY_PASSWORD = "APICP_KEY_PASSWORD";

    public static final String UNDERSCORE = "_";
    public static final String CREATE = "CREATE";
    public static final String SYNC_ASSET_ACTION = "SYNC_ASSET_ACTION";
    public static final String SEND_HEARTBEAT_ACTION = "SEND_HEARTBEAT_ACTION";
    public static final String SEND_METRIC_ACTION = "SEND_METRIC_ACTION";
    public static final String REQUESTS = "requests";
    public static final String INSIGHTS = "insights";
}
