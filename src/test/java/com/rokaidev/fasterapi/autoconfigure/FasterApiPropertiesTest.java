package com.rokaidev.fasterapi.autoconfigure;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FasterApiPropertiesTest {

    @Test
    void combinesLegacyBaseAndDedicatedModelAndDtoPackages() {
        FasterApiProperties properties = new FasterApiProperties();
        properties.setBasePackage("legacy.models, legacy.shared");
        properties.setBasePackages(List.of("legacy.shared", "common"));
        properties.setModelPackages(List.of("domain.models"));
        properties.setDtoPackages(List.of("api.dto", "common"));

        assertEquals(List.of(
                "legacy.models",
                "legacy.shared",
                "common",
                "domain.models",
                "api.dto"), properties.getScanPackages());
    }

    @Test
    void returnsNoPackagesWhenNothingIsConfigured() {
        assertEquals(List.of(), new FasterApiProperties().getScanPackages());
    }
}
