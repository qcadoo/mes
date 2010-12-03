package com.qcadoo.mes.view;

import org.json.JSONObject;

public interface ComponentPattern {

    boolean initialize(ViewDefinition viewDefinition);

    JSONObject getStaticJavaScriptOptions();

    ComponentState createComponentState();

    String getName();

    String getPathName();

    String getJspFilePath();

    String getJavaScriptFilePath();

    String getJavaScriptObjectName();

}
