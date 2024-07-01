## Core Implementation Logic 
Let’s understand the logic of how the agent for Azure API Management Service is implemented for different use cases.

1. **Registering Azure API Management Service with API Control Plane**.

    1. The agent reads Azure API Management Service and API Control Plane configurations from the environment properties.
	2. The agent validates whether the Azure API management service specified in the environment properties is operational. During this process, the agent retrieves the  location and the region details of where the API management service is hosted.
	3. If the API management service is operational, the agent registers the Azure API Management Service with API Control Plane.<br><br>

2. **Retrieving Azure API Management Service’s health status and sending it to API Control Plane**.

    1. The agent verifies if at least one API is present in the Azure API Management Service.
	2. If an API is present in Azure API Management Service, the agent sends the health status (heartbeat) of the runtime (Azure API Management Service) as *active* to API Control Plane. Else, the agent sends the runtime health status as *inactive*.<br><br>

3. **Publishing Azure API Management Service’s assets to API Control Plane**. 

    1. The agent retrieves all REST APIs at various levels (product, API, operation, etc.) from the Azure API Management Service.
    2. The agent publishes all REST APIs to the API Control Plane.<br>
    
    The policies associated with the REST APIs are not published in this step due to high compute time, which may lead to performance issues in the Azure agent.

4. **Synchronizing assets between Azure API Management Service and API Control Plane**.<br>
     The implementation logic for synchronizing assets between Azure API Management Service and API Control Plane varies for Spring Boot Stand-alone Application and 	  Azure Functions.

    **Spring Boot Application**<br>
	The agent categorizes the retrieved assets based on CREATE, UPDATE, and DELETE actions, and synchronizes the updates with API Control Plane periodically, 	  according to the configured synchronization values in the Environment properties. The policies of the APIs are computed after the publish activity and then 		synchronized with API Control Plane.
  
	**Azure Functions**<br>
	The agent synchronizes assets based on the event subscription of the API management service. The policies of the APIs are computed after the publish activity 	      and then synchronized with API Control Plane.
 
5. **Retrieving metrics from Azure API Management Service to API Control Plane**.

    1. The agent retrieves metrics using the following methods from Azure SDK: *Request* or *Insights*.
	2. **Request**: The agent uses the *reports().listByRequest* method to retrieve all requests for the specified time interval. The agent builds API Control Plane compatible metrics such as *transactionsCount*, *averageLatency*, and *averageResponseTime*, categorizing them according to the status code *2xx*, *3xx*, *4xx*, and *5xx* before sending them to API Control Plane. For details, see [Azure documentation](https://learn.microsoft.com/en-us/rest/api/apimanagement/reports/list-by-request?view=rest-apimanagement-2022-08-01&tabs=HTTP).
	3. **Insights**: The agent uses *reports().listByTime* or *reports().listByApi* method to retrieve aggregated metrics data in *15* minute time intervals (15, 30, 45). However, this method does not categorize the data according to the status codes.<br>
	You can choose to specify the method in which the metrics must be retrieved using the environment properties.
	


