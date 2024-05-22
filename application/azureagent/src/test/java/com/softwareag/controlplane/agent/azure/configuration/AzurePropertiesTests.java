package com.softwareag.controlplane.agent.azure.configuration;

import com.softwareag.controlplane.agentsdk.core.validator.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;


public class AzurePropertiesTests {

    private AzureProperties properties = new AzureProperties();

    @Test
    void testValidAzureProperties() {
        properties.setClientId("abc");
        properties.setClientSecret("ghj");
        properties.setSubscriptionId("qwerty");
        properties.setTenantId("default");
        properties.setResourceGroup("azuregroup");
        properties.setApiManagementServiceName("serviceName");
        Set<ConstraintViolation<AzureProperties>> violations = ValidatorFactory.getValidator().validate(properties);
        assert(violations).isEmpty();
    }

    @Test
    void testInValidAzureProperties() {
        properties.setClientId(null);
        properties.setClientSecret("ghj");
        properties.setSubscriptionId("qwerty");
        properties.setTenantId("default");
        properties.setResourceGroup("azuregroup");
        properties.setApiManagementServiceName("serviceName");
        Set<ConstraintViolation<AzureProperties>> violations = ValidatorFactory.getValidator().validate(properties);
        assertFalse(violations.isEmpty());
    }
}
