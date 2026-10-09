package com.rokaidev.fasterapi.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "fasterapi")
public class FasterApiProperties {

    private String basePackage;
    private List<String> basePackages = new ArrayList<>();
    private List<String> modelPackages = new ArrayList<>();
    private List<String> dtoPackages = new ArrayList<>();

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public List<String> getBasePackages() {
        return basePackages;
    }

    public void setBasePackages(List<String> basePackages) {
        this.basePackages = basePackages == null ? new ArrayList<>() : basePackages;
    }

    public List<String> getModelPackages() {
        return modelPackages;
    }

    public void setModelPackages(List<String> modelPackages) {
        this.modelPackages = modelPackages == null ? new ArrayList<>() : modelPackages;
    }

    public List<String> getDtoPackages() {
        return dtoPackages;
    }

    public void setDtoPackages(List<String> dtoPackages) {
        this.dtoPackages = dtoPackages == null ? new ArrayList<>() : dtoPackages;
    }

    public List<String> getScanPackages() {
        List<String> packages = new ArrayList<>();
        packages.addAll(splitPackages(basePackage));
        addConfiguredPackages(packages, basePackages);
        addConfiguredPackages(packages, modelPackages);
        addConfiguredPackages(packages, dtoPackages);
        return packages.stream().distinct().toList();
    }

    private void addConfiguredPackages(List<String> packages, List<String> configuredPackages) {
        for (String configuredPackage : configuredPackages) {
            packages.addAll(splitPackages(configuredPackage));
        }
    }

    private List<String> splitPackages(String packages) {
        if (packages == null || packages.isBlank()) {
            return Collections.emptyList();
        }
        return List.of(packages.split(",")).stream()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
