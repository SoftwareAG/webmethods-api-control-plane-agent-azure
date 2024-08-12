<!--
  Copyright Super iPaaS Integration LLC, an IBM Company 2024
-->

## Best practices

**Retrieving metrics from Azure API Management Service**:

There are two primary methods for retrieving metrics from Azure API Management Service using Azure SDK: **Requests** and **Insights**. Each method has its own advantages and limitations. Choose the method as per your requirement.

| Method | Description | Limitations |
|--------------------|-------------------|-------------------|
| **Requests** |	The agent uses the *reports().listByRequest* method to retrieve all requests for the specified time interval. <br>The agent builds API Control Plane compatible metrics such as *transactionsCount*, *averageLatency*, and *averageResponseTime*, categorizing them according to the status code *2xx*, *3xx*, *4xx*, and *5xx* before sending them to API Control Plane. | If high volume of transactions occur within the specified time interval, retrieving the data may lead to performance issues in the agent.| 
| **Insights** | The agent uses *reports().listByTime* or *reports().listByApi* method to retrieve aggregated metrics data in *15* minute time intervals (15, 30, 45).| This method does not categorize the data according to the status code.|




