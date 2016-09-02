package org.vaadin.guice.security.cdi;

import com.google.inject.BindingAnnotation;

import org.vaadin.guice.security.api.Guard;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@BindingAnnotation
@interface AllGuards {
}
