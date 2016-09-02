package org.vaadin.guice.security.annotations;

import org.vaadin.guice.security.api.Guard;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GuardedBy {
    Class<? extends Guard>[] guards();
}
