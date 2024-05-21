package com.softwareag.controlplane.agent.azure;

public final class Constants {

    private Constants() {
    }

    public static final String GRAPHQL = "GRAPHQL";

    public static final String SINGLE_QUOTE = "'";

    public static final String FILTER_LE_TIMESTAMP = " and timestamp le datetime";

    public static final String FILTER_GE_TIMESTAMP = "timestamp ge datetime";

    public static final String COLON = ":";
    public static final String UNDERSCORE = "_";

    public static final int BUFFERTIME = 60;

    public static final String INBOUND_TAG="inbound";

    public static final String BACKEND_TAG="backend";

    public static final String OUTBOUND_TAG="outbound";

    public static final String ON_ERROR_TAG="on-error";

    public static final String BASE_TAG="base";
}
