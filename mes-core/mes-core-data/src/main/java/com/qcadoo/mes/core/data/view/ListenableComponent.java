package com.qcadoo.mes.core.data.view;

import java.util.Set;

public interface ListenableComponent {

    Set<String> getListeners();

    void registerListener(String path);

}