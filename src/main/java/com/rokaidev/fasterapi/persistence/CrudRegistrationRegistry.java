package com.rokaidev.fasterapi.persistence;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CrudRegistrationRegistry {

    private final List<CrudRegistration> registrations = new ArrayList<>();

    public synchronized void add(CrudRegistration registration) {
        registrations.add(registration);
    }

    public synchronized List<CrudRegistration> getRegistrations() {
        return List.copyOf(registrations);
    }
}
