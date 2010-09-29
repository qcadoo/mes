package com.qcadoo.mes.core.internal.model;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.model.DataDefinition;
import com.qcadoo.mes.core.search.SearchCriteria;
import com.qcadoo.mes.core.search.SearchResult;

public interface InternalDataDefinition extends DataDefinition {

    SearchResult find(final SearchCriteria searchCriteria);

    String getFullyQualifiedClassName();

    boolean isVirtualTable();

    boolean isCoreTable();

    boolean isPluginTable();

    void callCreateHook(final Entity entity);

    void callUpdateHook(final Entity entity);

    Class<?> getClassForEntity();

    Object getInstanceForEntity();

    boolean isDeletable();

}
