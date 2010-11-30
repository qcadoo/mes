package com.qcadoo.mes.newview;

public interface ComponentPattern {

    void initialize(ViewDefinition viewDefinition);

    ComponentState createComponentState();

    String getName();

    String getPathName();

}
