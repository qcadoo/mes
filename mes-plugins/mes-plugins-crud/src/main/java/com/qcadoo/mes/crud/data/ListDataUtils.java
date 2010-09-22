package com.qcadoo.mes.crud.data;

import java.util.LinkedList;
import java.util.List;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.search.SearchResult;
import com.qcadoo.mes.core.data.view.elements.GridComponent;
import com.qcadoo.mes.core.data.view.elements.grid.ColumnDefinition;

public class ListDataUtils {

    private ListDataUtils() {

    }

    public static ListData generateListData(SearchResult rs, GridComponent gridDefinition) {
        List<Entity> entities = rs.getEntities();
        List<Entity> gridEntities = new LinkedList<Entity>();

        for (Entity entity : entities) {
            Entity gridEntity = new Entity(entity.getId());
            for (ColumnDefinition column : gridDefinition.getColumns()) {
                gridEntity.setField(column.getName(), column.getValue(entity));
            }
            gridEntities.add(gridEntity);
        }

        int totalNumberOfEntities = rs.getTotalNumberOfEntities();
        return new ListData(totalNumberOfEntities, gridEntities);
    }
}
