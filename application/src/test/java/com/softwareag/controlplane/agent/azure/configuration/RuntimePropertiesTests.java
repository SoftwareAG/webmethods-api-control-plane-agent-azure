/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.configuration;

import com.softwareag.controlplane.agentsdk.core.validator.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;


public class RuntimePropertiesTests {

    RuntimeProperties properties = new RuntimeProperties();

    @Test
    void testValidRuntimeProperties() {
        properties.setCapacityUnit("per year");
        properties.setType("AzureType");
        properties.setCapacityValue("500000");
        Set<ConstraintViolation<RuntimeProperties>> violations = ValidatorFactory.getValidator().validate(properties);
        assert(violations).isEmpty();
    }

    @Test
    void testInValidRuntimeProperties() {
        properties.setCapacityUnit("per year");
        properties.setType(null);
        properties.setCapacityValue("500000");
        Set<ConstraintViolation<RuntimeProperties>> violations = ValidatorFactory.getValidator().validate(properties);
        assertFalse(violations.isEmpty());
    }
}
