package org.vaadin.guice.security.cdi;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import com.vaadin.guice.annotation.GuiceView;
import com.vaadin.guice.server.UsesReflections;
import com.vaadin.navigator.View;

import org.reflections.Reflections;
import org.vaadin.guice.security.annotations.PermissionDeniedView;
import org.vaadin.guice.security.api.ComponentVisibilityEvaluator;
import org.vaadin.guice.security.api.Guard;
import org.vaadin.guice.security.api.PermissionEvaluator;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.inject.name.Names.named;

public class SecurityModule extends AbstractModule implements UsesReflections {

    private Reflections reflections;

    protected Class<? extends PermissionEvaluator> getPermissionEvaluatorClass(){
        return new PermissionEvaluator(){

            public boolean hasPermission(String permission) {
                throw new UnsupportedOperationException("in order to use @NeedsPermission, SecurityModule.getPermissionEvaluatorClass() needs to be overwritten");
            }
        }.getClass();
    }

    protected void configure() {

        bind(PermissionEvaluator.class).to(checkNotNull(getPermissionEvaluatorClass()));
        bindPermissionDeniedView();

        final Multibinder<Guard> guardMultibinder = Multibinder.newSetBinder(binder(), Guard.class);

        for (Class<? extends Guard> guardClass : reflections.getSubTypesOf(Guard.class)) {
            guardMultibinder.addBinding().to(guardClass);
        }

        bind(ComponentVisibilityEvaluator.class).to(NavigationAccessChecker.class);
    }

    private void bindPermissionDeniedView() {
        final Set<Class<?>> permissionDeniedViews = reflections.getTypesAnnotatedWith(PermissionDeniedView.class);

        switch (permissionDeniedViews.size()){
            case 0:
                bind(String.class).annotatedWith(named("guice_security_permission_denied_view")).toInstance("permissionDeniedGuiceDefault");
                break;
            case 1:
                final Class<?> permissionDeniedView = Iterables.getOnlyElement(permissionDeniedViews);

                checkArgument(
                        View.class.isAssignableFrom(permissionDeniedView),
                        "% is annotated with PermissionDeniedView, but is not a view",
                        permissionDeniedView
                );

                final GuiceView guiceView = permissionDeniedView.getAnnotation(GuiceView.class);

                checkArgument(
                        guiceView != null,
                        "% is annotated with PermissionDeniedView, but has no GuiceView annotation",
                        permissionDeniedView
                );

                bind(String.class)
                        .annotatedWith(named("guice_security_permission_denied_view"))
                        .toInstance(guiceView.name());

                break;
            default:
                final String names = Joiner.on(",").join(permissionDeniedViews);
                throw new IllegalStateException("multiple classes have PermissionDeniedView annotation: " + names);
        }
    }

    public void setReflections(Reflections reflections) {
        this.reflections = reflections;
        reflections.merge(new Reflections("org.vaadin.guice.security"));
    }
}
