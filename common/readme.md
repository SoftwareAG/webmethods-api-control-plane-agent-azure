<!--
  Copyright Super iPaaS Integration LLC, an IBM Company 2024
-->
## Customize the Implementation Logic for Retrieval of Heartbeats, APIs, and Metrics from the Azure API Management Service

The **Common** directory contains the shared code utilized by both the Spring Boot application and Lambda functions.

Within this directory, the **Manager** classes are implemented to facilitate the retrieval of heartbeats, APIs, and metrics from the Azure API Management Service. To customize the code according to your requirement, navigate to the respective *Manager* class directory and modify the relevant implementation files as required.

[AzureManagersHolder](../common/src/main/java/com/softwareag/controlplane/agent/azure/common/context)


| Class Name          | Description                                                  |
| ------------------- | ------------------------------------------------------------ |
| [AzureManagersHolder](../common/src/main/java/com/softwareag/controlplane/agent/azure/common/context/AzureManagersHolder.java) | Contains code to store the instance of all API Management Services, such as ApiManagementManager, ApiManagementServiceResource, and ResourceManager. Azure agent utilizes these managers to invoke *Azure SDK APIs* for retrieving assets (APIs, Analytics). |


[Assets](../common/src/main/java/com/softwareag/controlplane/agent/azure/common/handlers/assets)

The Assets directory contains the following files:

| Class Name | Description                                                       |
|-------------|-------------------------------------------------------------------|
| [AssetManager](../common/src/main/java/com/softwareag/controlplane/agent/azure/common/handlers/assets/AssetManager.java) | Contains code required for retrieving all the assets (APIs) from Azure API Management Service. When the Azure API Management Service is first registered with the API Control Plane, the Azure agent retrieves the assets from the AWS API Management Service and sends them to the API Control Plane when the *Publish Assets* use case is performed. For subsequent updates, the *Sync Assets* use case is performed. This class determines the API updates and sends it to API Control Plane. |
| [PolicyRetriever](../common/src/main/java/com/softwareag/controlplane/agent/azure/common/handlers/assets/PolicyRetriever.java) | Contains code required for retrieving the policies of APIs at different levels (product, API, All APIs (Global), operation, etc.) from Azure API Management Service. The policies are parsed to determine the total count and then loaded into the API model.|

[Heartbeat](../common/src/main/java/com/softwareag/controlplane/agent/azure/common/handlers/heartbeat/)

The Heartbeat directory contains the following file:

| Class Name | Description                                                       |
|-------------|-------------------------------------------------------------------|
| [HeartbeatManager](../common/src/main/java/com/softwareag/controlplane/agent/azure/common/handlers/heartbeat/HeartbeatManager.java) | Contains code required for retrieving the heartbeats from Azure API Management Service and sending it to API Control Plane. The *heartbeats* are shown as *Runtime status* in API Control Plane UI. |

[Metrics](../common/src/main/java/com/softwareag/controlplane/agent/azure/common/handlers/metrics/)

The Metrics directory contains the following file:

| Class Name | Description                                                       |
|-------------|-------------------------------------------------------------------|
| [MetricsManager](../common/src/main/java/com/softwareag/controlplane/agent/azure/common/handlers/metrics/MetricsManager.java) | Contains code required for retrieving the metrics from the Azure API Management Service.|
