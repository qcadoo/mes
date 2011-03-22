package com.qcadoo.mes.view.xml;

import org.w3c.dom.Node;

public class ViewExtension {

    private final String pluginName;

    private final String viewName;

    private final Node extesionNode;

    public ViewExtension(String pluginName, String viewName, Node extesionNode) {
        super();
        this.pluginName = pluginName;
        this.viewName = viewName;
        this.extesionNode = extesionNode;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getViewName() {
        return viewName;
    }

    public Node getExtesionNode() {
        return extesionNode;
    }

}
