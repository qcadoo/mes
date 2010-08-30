package com.qcadoo.mes.plugins.products.data;

import java.util.LinkedList;
import java.util.List;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.core.data.search.ResultSet;

public class ListDataUtils {

    public static ListData generateListData(ResultSet rs, GridDefinition gridDefinition) {
        List<Entity> entities = rs.getResults();
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
