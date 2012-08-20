/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.workPlans;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.lowagie.text.DocumentException;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;

public interface WorkPlansService {

    void generateWorkPlanDocuments(final ComponentState state, final Entity workPlan) throws IOException, DocumentException;

    Entity generateWorkPlanEntity(final List<Entity> orders);

    List<Entity> getSelectedOrders(final Set<Long> selectedOrderIds);

    Entity getWorkPlan(final Long workPlanId);
}
