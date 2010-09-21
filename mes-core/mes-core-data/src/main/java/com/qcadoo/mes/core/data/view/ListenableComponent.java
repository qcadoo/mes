package com.qcadoo.mes.core.data.view;

import java.util.Set;

public interface ListenableComponent {

    public abstract Set<String> getListeners();

    public abstract void registerListener(String path);

}