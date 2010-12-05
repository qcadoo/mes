package com.qcadoo.mes.view;

import java.util.Locale;
import java.util.Map;

public interface ComponentPattern {

    boolean initialize();

    ComponentState createComponentState();

    Map<String, Object> prepareView(Locale locale);

    String getName();

    String getPath();

}
