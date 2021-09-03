/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.basic.services;

import static com.qcadoo.model.api.search.SearchRestrictions.eq;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.DashboardButtonFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.constants.MenuItemFields;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class DashboardButtonService {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardButtonService.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    public void addButton(String identifier, String icon, String itemPluginIdentifier, String itemName) {
        if (isDashboardButtonExists(identifier)) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Dashboard buttons for coverages will be populated ...");
        }

        addDashboardButton(identifier, icon, getViewItem(itemPluginIdentifier, itemName));
    }

    public void deleteButton(String identifier) {
        if (!isDashboardButtonExists(identifier)) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Dashboard buttons for coverages will be unpopulated ...");
        }

        deleteDashboardButton(identifier);
    }

    private Entity addDashboardButton(String identifier, String icon, Entity item) {
        Entity button = getDashboardButtonDD().create();

        button.setField(DashboardButtonFields.PARAMETER, parameterService.getParameter());
        button.setField(DashboardButtonFields.IDENTIFIER, identifier);
        button.setField(DashboardButtonFields.ITEM, item);
        button.setField(DashboardButtonFields.ICON, icon);
        button.setField(DashboardButtonFields.ACTIVE, false);

        button = button.getDataDefinition().save(button);
        return button;
    }

    private void deleteDashboardButton(String identifier) {
        final List<Entity> buttons = getDashboardButtonDD().find()
                .add(SearchRestrictions.eq(DashboardButtonFields.IDENTIFIER, identifier)).list().getEntities();

        for (Entity button : buttons) {
            button.getDataDefinition().delete(button.getId());
        }
    }

    private boolean isDashboardButtonExists(String identifier) {
        return getDashboardButtonDD().find().add(SearchRestrictions.eq(DashboardButtonFields.IDENTIFIER, identifier)).list()
                .getTotalNumberOfEntities() > 0;
    }

    private Entity getViewItem(String pluginIdentifier, String name) {
        return getViewDD().find().add(eq(MenuItemFields.NAME, name)).add(eq(MenuItemFields.PLUGIN_IDENTIFIER, pluginIdentifier))
                .setMaxResults(1).uniqueResult();
    }

    private DataDefinition getDashboardButtonDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_DASHBOARD_BUTTON);
    }

    private DataDefinition getViewDD() {
        return dataDefinitionService.get(QcadooViewConstants.PLUGIN_IDENTIFIER, QcadooViewConstants.MODEL_ITEM);
    }
}
