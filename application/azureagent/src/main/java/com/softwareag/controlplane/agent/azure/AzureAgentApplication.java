package com.softwareag.controlplane.agent.azure;

import com.softwareag.controlplane.agent.azure.configuration.AgentProperties;
import com.softwareag.controlplane.agent.azure.configuration.AzureProperties;
import com.softwareag.controlplane.agent.azure.configuration.RuntimeProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AgentProperties.class, AzureProperties.class,
		 RuntimeProperties.class})
public class AzureAgentApplication implements CommandLineRunner {


	public static void main(String[] args) {
		SpringApplication.run(AzureAgentApplication.class, args);
	}

	@Override
	public void run(String... args)  {
		System.out.println("Running Spring Boot Application console " );
	}

}
