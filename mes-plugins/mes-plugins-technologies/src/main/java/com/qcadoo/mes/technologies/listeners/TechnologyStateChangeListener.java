/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.mes.technologies.states.MessageHolder;
import com.qcadoo.mes.technologies.states.TechnologyStateChangeNotifierService.StateChangeListener;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginAccessor;

@Component
public class TechnologyStateChangeListener implements StateChangeListener {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PluginAccessor pluginAccessor;

    @Override
    public List<MessageHolder> onStateChange(final Entity technology, final TechnologyState newState) {
        List<MessageHolder> resultMessages = Lists.newArrayList();

        if (TechnologyState.OUTDATED.equals(newState) && isTechnologyUsedInActiveOrder(technology)) {
            resultMessages.add(MessageHolder.error("technologies.technology.state.error.orderInProgress"));

        }

        return resultMessages;
    }

    private boolean isTechnologyUsedInActiveOrder(final Entity technology) {
        if (!ordersPluginIsEnabled()) {
            return false;
        }
        SearchCriteriaBuilder searchCriteria = getOrderDataDefinition().find();
        searchCriteria.add(SearchRestrictions.belongsTo("technology", technology));
        searchCriteria.add(SearchRestrictions.in("state",
                Lists.newArrayList("01pending", "02accepted", "03inProgress", "06interrupted")));
        searchCriteria.setMaxResults(1);
        return searchCriteria.uniqueResult() != null;
    }

    private boolean ordersPluginIsEnabled() {
        return pluginAccessor.getPlugin("orders") != null;
    }

    private DataDefinition getOrderDataDefinition() {
        return dataDefinitionService.get("orders", "order");
    }

}
