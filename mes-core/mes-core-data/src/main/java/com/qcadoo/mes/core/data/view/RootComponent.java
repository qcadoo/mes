package com.qcadoo.mes.core.data.view;

import java.util.Set;

public interface RootComponent extends ContainerComponent<Object> {

    public abstract void initialize();

    public abstract Set<String> getListenersForPath(final String path);

    public abstract CastableComponent<?> getComponentForPath(final String path);

}