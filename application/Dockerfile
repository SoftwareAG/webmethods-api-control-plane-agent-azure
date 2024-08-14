#
# Copyright Super iPaaS Integration LLC, an IBM Company 2024
#
FROM openjdk:17-jdk-slim as builder
COPY ./build/libs/application-0.0.1-SNAPSHOT.jar azure-agent.jar
ENTRYPOINT ["java","-jar","/azure-agent.jar"]
