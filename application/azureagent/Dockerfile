FROM openjdk:17-jdk-slim as builder
COPY ./build/libs/azureagent-0.0.1-SNAPSHOT.jar azure-agent.jar
ENTRYPOINT ["java","-jar","/azure-agent.jar"]
