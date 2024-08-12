/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.functions.utils;

import com.softwareag.controlplane.agent.azure.functions.utils.constants.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides default values of Environment variables for FAAS implementation.
 */
public class DefaultEnvProvider {
    private DefaultEnvProvider(){}

    private static final Map<String, String> DEFAULT_VALUES = new HashMap<>();

    static {
        DEFAULT_VALUES.put(Constants.AZURE_METRICS_SYNC_BUFFER_INTERVAL_MINUTES,"15");
        DEFAULT_VALUES.put(Constants.AZURE_METRICS_BY_REQUESTS_OR_INSIGHTS,"requests");
        DEFAULT_VALUES.put(Constants.APICP_SYNC_ASSETS_INTERVAL_SECONDS,"300");
        DEFAULT_VALUES.put(Constants.APICP_SYNC_HEARTBEAT_INTERVAL_SECONDS,"60");
        DEFAULT_VALUES.put(Constants.APICP_SYNC_METRICS_INTERVAL_SECONDS,"300");
        DEFAULT_VALUES.put(Constants.APICP_RUNTIME_CAPACITY_VALUE,"500000");
        DEFAULT_VALUES.put(Constants.APICP_RUNTIME_CAPACITY_UNIT,"PER_YEAR");
        DEFAULT_VALUES.put(Constants.APICP_LOG_LEVEL,"INFO");
    }

    /**
     * Retrieves the value of the specified environment variable.
     *
     * @param variableName The name of the environment variable to retrieve.
     * @return The value of the specified environment variable, or a default value if not found.
     */
    public static String getEnv(String variableName) {
        String value = System.getenv(variableName);
        return value != null ? value : DEFAULT_VALUES.get(variableName);
    }
}
