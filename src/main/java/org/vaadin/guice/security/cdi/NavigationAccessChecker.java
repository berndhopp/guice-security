package org.vaadin.guice.security.cdi;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import com.vaadin.guice.annotation.GuiceViewChangeListener;
import com.vaadin.guice.annotation.UIScope;
import com.vaadin.guice.server.NavigatorProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;

import org.vaadin.guice.security.annotations.GuardedBy;
import org.vaadin.guice.security.annotations.NeedsPermission;
import org.vaadin.guice.security.api.ComponentVisibilityEvaluator;
import org.vaadin.guice.security.api.Guard;
import org.vaadin.guice.security.api.PermissionEvaluator;

import java.util.Set;

@GuiceViewChangeListener
@UIScope
class NavigationAccessChecker implements ViewChangeListener, ComponentVisibilityEvaluator{

    @Inject
    private PermissionEvaluator permissionEvaluator;

    @Inject
    @Named("guice_security_permission_denied_view")
    private String permissionDeniedView;

    @Inject
    private NavigatorProvider navigatorProvider;

    @Inject
    @AllGuards
    private Set<Guard> allGuards;

    @Inject
    @AllRestrictedComponents
    private Set<Component> restrictedComponents;

    public boolean beforeViewChange(ViewChangeEvent viewChangeEvent) {

        final Class<? extends View> newViewClass = viewChangeEvent.getNewView().getClass();

        boolean isPermitted = isPermitted(newViewClass);

        if(!isPermitted){
            navigateToPermissionDeniedView();
        }

        return isPermitted;
    }

    private boolean isPermitted(Class<?> clazz) {
        NeedsPermission needsPermission = clazz.getAnnotation(NeedsPermission.class);

        if(needsPermission != null){
            if(!permissionEvaluator.hasPermission(needsPermission.value())){
                return false;
            }
        }

        GuardedBy guardedBy = clazz.getAnnotation(GuardedBy.class);

        if(guardedBy != null){
            for (Class<? extends Guard> guardClass : guardedBy.value()) {
                for (Guard guard : allGuards) {
                    if(guard.getClass().equals(guardClass)){
                        if(!guard.hasAccess()){
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private void navigateToPermissionDeniedView(){
        navigatorProvider.get().navigateTo(permissionDeniedView);
    }

    public void afterViewChange(ViewChangeEvent viewChangeEvent) {
    }

    public void evaluate() {
        final View currentView = navigatorProvider.get().getCurrentView();

        if(currentView != null){
            final Class<? extends View> viewClass = currentView.getClass();

            if(!isPermitted(viewClass)){
                navigateToPermissionDeniedView();
            }
        }

        for (Component restrictedComponent : restrictedComponents) {
            Class<? extends Component> restrictedComponentClass = restrictedComponent.getClass();

            restrictedComponent.setVisible(isPermitted(restrictedComponentClass));
        }
    }
}
