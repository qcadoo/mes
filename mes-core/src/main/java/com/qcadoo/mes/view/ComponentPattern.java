package com.qcadoo.mes.view;

public interface ComponentPattern {

    void initialize(ViewDefinition viewDefinition);

    ComponentState createComponentState();

    String getName();

    String getPathName();

    String getJspFilePath();

    String getJavaScriptFilePath();

    String getJavaScriptObjectName();

}
