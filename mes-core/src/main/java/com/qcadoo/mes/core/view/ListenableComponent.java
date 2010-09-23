package com.qcadoo.mes.core.view;

import java.util.Set;

public interface ListenableComponent {

    Set<String> getListeners();

    void registerListener(String path);

}