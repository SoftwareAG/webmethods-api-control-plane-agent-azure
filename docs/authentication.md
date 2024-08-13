<!--
  Copyright Super iPaaS Integration LLC, an IBM Company 2024
-->

## Authentication


This section details how to assign a Service principle for API Management service.

The agent implementation for Azure API Gateway supports various implementations of **AzureAuthentication** interface provided by **Azure SDK**. 

Ensure that you have created an Service principle in Azure with the following roles assigned:

- **Cloud Application Administrator** (for running standalone spring boot application and authenticating azure SDK )
- **Website Contributor role** (for configuring Azure function application in Azure portal and deploying azure package to functions app)

You can assign credential details by using the environment variable, **AZURE_CLIENT_ID** and **AZURE_CLIENT_SECRET**. 

Azure Service Principal can be created with following steps. https://learn.microsoft.com/en-us/entra/identity-platform/howto-create-service-principal-portal