# **webMethods API Control Plane Agent for Azure API Management Service**

![Version 1.0.0](https://img.shields.io/badge/Version-1.0.0-blue)
![Agent SDK Version 1.0.0](https://img.shields.io/badge/Agent_SDK-1.0.0-green)
![API Control Plane Version 11.0.4](https://img.shields.io/badge/API_Control_Plane-11.0.4-purple)
![Azure API Management Service](https://img.shields.io/badge/Azure-API_Management_Service-blue)
![Azure Functions](https://img.shields.io/badge/Azure-Functions-red)
![Azure Subscriptions](https://img.shields.io/badge/Azure-Subscriptions-yellow)
![Azure API Management Service SDK](https://img.shields.io/badge/Azure-API_Management_Service_SDK-purple)
<br>

![Java 17](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java&logoColor=white)
![Gradle 7.4.2](https://img.shields.io/badge/Gradle-7.4.2-DD0031?style=for-the-badge&logo=java&logoColor=white)
![Spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

This repository holds an agent implementation Java project for connecting Azure API Management Service with API Control Plane, utilizing the **Agent SDK**. The key functionalities include:

1.  Registering Azure API Management Service with API Control Plane.
2.	Retrieving Azure API Management Service’s health status and sending it to API Control Plane.
3.	Publishing Azure API Management Service’s assets to API Control Plane.
4.	Synchronizing assets between Azure API Management Service and API Control Plane.
5.	Retrieving metrics from Azure API Management Service to API Control Plane.

This project is developed using **Java 17** and **Gradle 7.4.2**<br>
If you plan to upgrade *Gradle*, ensure that you also upgrade the supported *Java* version accordingly. For details about the compatibility between *Java* and *Gradle* versions, see [Compatibility Matrix](https://docs.gradle.org/current/userguide/compatibility.html).


## Table of Contents
- [Implementation Overview](#implementation-overview)
  - [Core Implementation Logic](docs/core-logic.md)
  - [Customize the Core Implementation Logic](common/)
- [How is this Repository Structured?](docs/repo-structure.md)
- [Co-relation Between Azure API Management Service and API Control Plane Terminologies](docs/corelation.md)
- [How to Build the Gradle Project?](devops/)
- [How to Deploy and Run the Azure Agent as a Spring Boot application in Docker?](application/)
  - [How to Create the Runtime Type in API Control Plane?](docs/runtime_service_mgmt_api.md)
- [How to Deploy and Run the Azure Agent in Azure Functions?](functions/)
- [Best Practices](docs/best-practices.md)



## **Implementation Overview**

The implementation utilizes the **Manual** approach of the *Agent SDK*, providing options for deploying the Azure agent as a stand-alone application. For details about the Agent SDK implementation approaches and deployment modes, see [Agent SDK ](https://docs.webmethods.io/apicontrolplane/agent_sdk/chapter2wco/#gsc.tab=0) documentation. 
The implementation leverages **Azure SDK** for **API Management Service** to:
- Manage connections and authentication for Azure API Management Service
- Retrieve APIs and metrics from  Azure API Management Service

**Note:**  The agent implementation is compatible with API Control Plane version, **11.0.4** and currently supports only the *REST*, *SOAP*, and *Graph QL* APIs of Azure API Management Service.

The Azure agent can be deployed in the following ways:

- **Spring Boot application**
- **Azure Functions**<br>

For a detailed understanding of how the agent for Azure API Management service is implemented, see [Core Implementation Logic](docs/core-logic.md).

The Azure agent developer can utilize this repository in the following ways:
 
- Use the repository directly to build and deploy the Azure agent.
- Fork the repository, customize the code as required, and then build and deploy the Azure agent.



## How is this Repository Structured?

This section outlines the Git repository's structure, highlighting the purpose of each directory. For details, see [Repository structure](docs/repo-structure.md).



## Co-relation Between Azure API Management service and API Control Plane Terminologies

This section details the relationship and equivalence between the terminologies used in *Azure API Management service* and the *API Control Plane*.  For details, see [Co-relation](docs/corelation.md).


## How to Build the Gradle Project?

For details, see [How to build?](devops/)


## How to Deploy and Run the Azure Agent as a Spring Boot Application in Docker?

This section details how to deploy the Azure agent as a stand-alone application. For details, see [How to deploy?](application/)


## How to Deploy and Run the Azure Agent in Azure Functions?

Azure Functions is a cloud service provided by Azure that lets you deploy and run your application without provisioning or managing servers. You are responsible only for the agent application code that you provide to Functions and the conﬁguration of how the Function runs that code on your behalf. For details about Azure Functions, see [Azure documentation]( https://learn.microsoft.com/en-us/azure/azure-functions/).

Deploying an Azure agent using Azure Functions lets you run the agent virtually without the need for administration of the underlying infrastructure. 
Azure Functions support various tools for creating and deploying Functions in Java. For details, see [Azure documentation](https://learn.microsoft.com/en-us/azure/azure-functions/create-first-function-vs-code-java). This tutorial specifically covers the creation and deployment of Azure Functions in Java using the following tools:
- **Visual Studio Code**
- **Azure CLI**

For details about how to deploy and run the Azure agent in Azure Functions, see [How to deploy?](functions/)

## Best Practices
This section outlines the essential best practices for using this implementation and deploying the Azure agent. For details, see [Best Practices](docs/best-practices.md).

## References
- To learn how to create the *runtime type* in the API Control Plane, see [Runtime Type Management Service API](docs/runtime_service_mgmt_api.md).
