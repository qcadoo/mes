/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.5
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
package com.qcadoo.mes.technologies.listeners;

import static com.qcadoo.mes.technologies.constants.TechnologyFields.OPERATION_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.TechnologyState.ACCEPTED;
import static com.qcadoo.mes.technologies.constants.TechnologyState.CHECKED;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.mes.technologies.states.TechnologyStateBeforeChangeNotifierService.BeforeStateChangeListener;
import com.qcadoo.mes.technologies.tree.TechnologyTreeValidationService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;

@Component
public class TechnologyBeforeStateChangeListener implements BeforeStateChangeListener {

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private TechnologyTreeValidationService technologyTreeValidationService;

    @Override
    public boolean canChange(final ComponentState gridOrForm, final Entity technology, final TechnologyState newState) {
        boolean result = true;
        if ((TechnologyState.OUTDATED.equals(newState) && technologyService.isTechnologyUsedInActiveOrder(technology))
                || (TechnologyState.DECLINED.equals(newState) && technologyService.isTechnologyUsedInActiveOrder(technology))) {
            gridOrForm.addMessage("technologies.technology.state.error.orderInProgress", MessageType.FAILURE);

            result = false;
        }

        if (ACCEPTED == newState || CHECKED == newState) {
            checkConsumingManyProductsFromOneSubOp(gridOrForm, technology);
        }

        return result;
    }

    private void checkConsumingManyProductsFromOneSubOp(final ComponentState gridOrForm, final Entity technology) {
        final Entity existingTechnology = technology.getDataDefinition().get(technology.getId());
        final Map<String, Set<String>> parentChildNodeNums = technologyTreeValidationService
                .checkConsumingManyProductsFromOneSubOp(existingTechnology.getTreeField(OPERATION_COMPONENTS));

        for (Map.Entry<String, Set<String>> parentChildNodeNum : parentChildNodeNums.entrySet()) {
            for (String childNodeNum : parentChildNodeNum.getValue()) {
                gridOrForm.addMessage("technologies.technology.validate.global.info.consumingManyProductsFromOneSubOperations",
                        MessageType.INFO, parentChildNodeNum.getKey(), childNodeNum);
            }
        }
    }
}
