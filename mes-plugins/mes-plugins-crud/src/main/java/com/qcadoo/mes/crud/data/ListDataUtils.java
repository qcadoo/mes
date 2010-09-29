package com.qcadoo.mes.crud.data;

import java.util.LinkedList;
import java.util.List;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.search.SearchResult;
import com.qcadoo.mes.core.view.elements.GridComponent;
import com.qcadoo.mes.core.view.elements.grid.ColumnDefinition;
import com.qcadoo.mes.core.view.elements.grid.ListData;

public final class ListDataUtils {

    private ListDataUtils() {
    }

    public static ListData generateListData(final SearchResult rs, final GridComponent gridDefinition) {
        List<Entity> entities = rs.getEntities();
        List<Entity> gridEntities = new LinkedList<Entity>();

        for (Entity entity : entities) {
            Entity gridEntity = entity.copy();
            for (ColumnDefinition column : gridDefinition.getColumns()) {
                gridEntity.setField(column.getName(), column.getValue(entity));
            }
            gridEntities.add(gridEntity);
        }

        int totalNumberOfEntities = rs.getTotalNumberOfEntities();
        return new ListData(totalNumberOfEntities, gridEntities);
    }
}
