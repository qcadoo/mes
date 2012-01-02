package com.qcadoo.mes.workPlans;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.lowagie.text.DocumentException;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;

public interface WorkPlanService {

    void generateWorkPlanDocuments(final ComponentState state, final Entity workPlan) throws IOException, DocumentException;

    Entity generateWorkPlanEntity(final List<Entity> orders);

    List<Entity> getSelectedOrders(final Set<Long> selectedOrderIds);

    Entity getWorkPlan(final Long workPlanId);
}
