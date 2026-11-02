package com.rokaidev.fasterapi.persistence;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

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
