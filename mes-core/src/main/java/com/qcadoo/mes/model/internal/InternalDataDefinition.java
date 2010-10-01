package com.qcadoo.mes.model.internal;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.search.SearchCriteria;
import com.qcadoo.mes.model.search.SearchResult;

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

    boolean isCreatable();

    boolean isUpdatable();

}
