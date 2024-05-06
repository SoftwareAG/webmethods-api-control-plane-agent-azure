# **webMethods API Control Plane Agent for Azure API Gateway**

This repository holds a sample stand-alone agent implementation Gradle project for connecting Azure API Gateway with API Control Plane, utilizing the Agent SDK. Key functionalities include:

1. Registering Azure API Gateway with API Control Plane.
2. Retrieving Azure API Gateway’s health status and sending it to API Control Plane.
3. Publishing Azure API Gateway’s assets to API Control Plane. 
4. Synchronizing assets between Azure API Gateway and API Control Plane.

Retrieving metrics from Azure API Gateway to API Control Plane.

## **Implementation overview**

The implementation utilizes the *Manual* approach, leveraging the Azure SDK for connection management and authentication. For details about the approaches, see [Agent SDK ](https://docs.webmethods.io/apicontrolplane/agent_sdk/chapter2wco/#gsc.tab=0) documentation.

**Note:**  The agent implementation is compatible with API Control Plane version **11.0.3**.

### **How to build the Gradle project?**

##### **Pre-requisites :** Ensure that you have:

·    Access to Agent SDK Jars in Empower portal. For details, see *How to access Agent SDK Jars*.

·    Cloned **webmethods-api-control-plane-agent-azure** Git Hub repository using any GIT client.

##### **To build the Gradle project**

Let’s look at a sample scenario through which you can build a Gradle project using Visual Studio Code editor.

1. Unzip Agent SDK folder downloaded from the Empower portal and place the (api, core, and model) Jars under *lib* folder in the cloned repository.

2. Open Visual Studio Code editor.

3. Go to File > Add Folder to Workspace and select the cloned **webmethods-api-control-plane-agent-azure** repository.

4. Run the following command in the Visual Studio Terminal to build the project:

```groovy
./gradlew build
```

### Authentication

 *Build Successful* message appears, and the jars for the spring boot application is created at the following locations for application / azureagent / build / libs

#### **How to deploy a Spring boot application in Docker**

Let’s look at a sample scenario through which you can deploy a spring boot application in Docker using Visual Studio Code editor.

##### **Pre-requisites:**

Ensure that you have:

·    Started the Docker client.

·    API Control Plane 11.0.3 subscription.

·    Verified if Azure API Gateway and API Control Plane for which you want to establish connectivity using the Agent are up and running.

·    Created Runtime Type in API Control Plane to represent Azure API Gateway. For details, see [Runtime Type Management REST API](https://github.com/SoftwareAG/webmethods-api-control-plane/blob/main/apis/openapi-specifications/runtime-type.yaml). 

##### To build a Docker image

1. Go to *webmethods-api-control-plane-agent-azure\application\azureagent* using the following command in the Visual Studio Terminal:

   ```groovy
   cd application\azureagent
   ```


2. Run the following command to build the Docker image

```dockerfile
docker build . --tag=<image-name>	
```

​	For example: docker build . --tag=azure-agent-appln

3. *Verify if azure-agent-appln image* is listed in the docker client.

#####  **How to run the Spring boot application in Docker**

Let’s look at a sample scenario through which you can run the spring boot application in Docker using Visual Studio Code editor.

1. Run the following command:

```dockerfile
docker-compose up -d
```

### 

