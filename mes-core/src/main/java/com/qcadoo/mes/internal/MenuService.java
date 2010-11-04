package com.qcadoo.mes.internal;

import java.util.Locale;

import com.qcadoo.mes.view.menu.MenuDefinition;

public interface MenuService {

    void updateViewDefinitionDatabase();

    MenuDefinition getMenu(final Locale locale);

}
