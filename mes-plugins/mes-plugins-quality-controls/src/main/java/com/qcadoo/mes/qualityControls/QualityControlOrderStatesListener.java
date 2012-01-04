/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.1
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
package com.qcadoo.mes.qualityControls;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.orders.states.ChangeOrderStateMessage;
import com.qcadoo.mes.orders.states.OrderStateListener;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class QualityControlOrderStatesListener extends OrderStateListener {

    @Autowired
    TranslationService translationService;

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Override
    public List<ChangeOrderStateMessage> onCompleted(final Entity newEntity) {

        checkArgument(newEntity != null, "entity is null");
        List<ChangeOrderStateMessage> listOfMessage = new ArrayList<ChangeOrderStateMessage>();
        if (isQualityControlAutoCheckEnabled() && !checkIfAllQualityControlsAreClosed(newEntity)) {
            listOfMessage.add(ChangeOrderStateMessage.error(translationService.translate(
                    "qualityControls.qualityControls.not.closed", LocaleContextHolder.getLocale())));
        }
        return listOfMessage;

    }

    private boolean isQualityControlAutoCheckEnabled() {
        SearchResult searchResult = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER)
                .find().setMaxResults(1).list();

        Entity parameter = null;
        if (searchResult.getEntities().size() > 0) {
            parameter = searchResult.getEntities().get(0);
        }

        if (parameter == null) {
            return false;
        } else {
            return (Boolean) parameter.getField("checkDoneOrderForQuality");
        }
    }

    private boolean checkIfAllQualityControlsAreClosed(final Entity order) {
        if (order.getBelongsToField("technology") == null) {
            return true;
        }

        Object controlTypeField = order.getBelongsToField("technology").getField("qualityControlType");

        if (controlTypeField == null) {
            return false;
        } else {

            DataDefinition qualityControlDD = null;

            qualityControlDD = dataDefinitionService.get("qualityControls", "qualityControl");

            if (qualityControlDD == null) {
                return false;
            } else {
                SearchResult searchResult = qualityControlDD.find().add(SearchRestrictions.belongsTo("order", order))
                        .add(SearchRestrictions.eq("closed", false)).list();
                return (searchResult.getTotalNumberOfEntities() <= 0);
            }
        }
    }
}
