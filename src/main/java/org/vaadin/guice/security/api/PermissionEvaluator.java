package org.vaadin.guice.security.api;

public interface PermissionEvaluator {
    boolean hasPermission(String permission);
}
