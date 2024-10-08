/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.common.constants;


/**
 * The Class Constants holds all variable values won't change once assigned and be used throughout the application.
 */
public final class Constants {



    private Constants() {
    }

    public static final String GRAPHQL = "GRAPHQL";

    public static final String SINGLE_QUOTE = "'";

    public static final String FILTER_LE_TIMESTAMP = " and timestamp le datetime";

    public static final String FILTER_GE_TIMESTAMP = "timestamp ge datetime";

    public static final String COLON = ":";
    public static final String UNDERSCORE = "_";

    public static final String INBOUND_TAG="inbound";

    public static final String BACKEND_TAG="backend";

    public static final String OUTBOUND_TAG="outbound";

    public static final String ON_ERROR_TAG="on-error";

    public static final String BASE_TAG="base";

    public static final String ORIGINAL_VERSION="Original";

    public static final String METRICS_BY_REQUESTS="requests";

    public static final String METRICS_BY_INSIGHTS="insights";

    public static final long SYNC_POLICY_COUNT_DELAY = 900000;

    public static final String SYNC_POLICY_COUNT_THREAD_NAME = "PolicyCountUpdater";
}
