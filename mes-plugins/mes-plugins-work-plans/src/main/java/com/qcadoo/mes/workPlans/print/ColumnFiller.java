package com.qcadoo.mes.workPlans.print;

import java.util.List;
import java.util.Map;

import com.qcadoo.model.api.Entity;

public interface ColumnFiller {

    Map<Entity, Map<String, String>> getValues(List<Entity> orders);
}
