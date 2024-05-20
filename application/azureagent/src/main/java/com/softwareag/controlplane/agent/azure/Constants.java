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
}
