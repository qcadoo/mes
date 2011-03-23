package com.qcadoo.mes.internal;

public interface InternalMenuService extends MenuService {

    void createViewIfNotExists(String pluginIdentifier, String viewName, String view, String url);

    void enableView(String pluginIdentifier, String viewName);

    void disableView(String pluginIdentifier, String viewName);

    void createCategoryIfNotExists(String pluginIdentifier, String categoryName);

    void createItemIfNotExists(String pluginIdentifier, String name, String category, String viewPluginIdentifier, String viewName);

    void enableItem(String pluginIdentifier, String name);

    void disableItem(String pluginIdentifier, String name);

}
