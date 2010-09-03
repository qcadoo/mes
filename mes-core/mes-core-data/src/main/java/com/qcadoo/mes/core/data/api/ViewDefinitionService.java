package com.qcadoo.mes.core.data.api;

import java.util.List;

import com.qcadoo.mes.core.data.definition.ViewDefinition;

public interface ViewDefinitionService {

    ViewDefinition getViewDefinition(String viewName);

    List<ViewDefinition> getAllViews();

}
