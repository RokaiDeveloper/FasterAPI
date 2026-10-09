package com.rokaidev.fasterapi.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "fasterapi")
public class FasterApiProperties {

    private String basePackage;
    private List<String> basePackages = new ArrayList<>();

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

    public List<String> getScanPackages() {
        List<String> packages = new ArrayList<>();
        if (basePackage != null) {
            packages.addAll(splitPackages(basePackage));
        }
        for (String configuredPackage : basePackages) {
            packages.addAll(splitPackages(configuredPackage));
        }
        if (packages.isEmpty()) {
            return List.of("com.exemplo.entidades");
        }
        return packages.stream().distinct().toList();
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
