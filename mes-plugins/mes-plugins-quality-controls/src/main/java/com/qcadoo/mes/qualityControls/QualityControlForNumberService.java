/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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

import static com.qcadoo.model.api.search.SearchRestrictions.eq;
import static com.qcadoo.model.api.search.SearchRestrictions.idNe;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.qualityControls.constants.QualityControlFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class QualityControlForNumberService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean checkUniqueNumber(final DataDefinition dataDefinition, final Entity entity) {
        String qualityControlType = entity.getStringField(QualityControlFields.QUALITY_CONTROL_TYPE);
        String number = entity.getStringField(QualityControlFields.NUMBER);

        SearchCriteriaBuilder searchCriteriaBuilder = dataDefinition.find();
        searchCriteriaBuilder.add(eq(QualityControlFields.NUMBER, number));
        searchCriteriaBuilder.add(eq(QualityControlFields.QUALITY_CONTROL_TYPE, qualityControlType));

        if (entity.getId() != null) {
            searchCriteriaBuilder.add(idNe(entity.getId()));
        }

        if (searchCriteriaBuilder.list().getTotalNumberOfEntities() > 0) {
            entity.addError(dataDefinition.getField("number"), "qualityControls.quality.control.validate.global.error.number");
            return false;
        }

        return true;
    }

    public String generateNumber(final String plugin, final String model, final int digitsNumber, final String qualityControlType) {
        long longValue = 0;
        SearchResult searchResult = dataDefinitionService.get(plugin, model).find()
                .add(eq("qualityControlType", qualityControlType)).addOrder(SearchOrders.desc("id")).setMaxResults(1).list();
        if (searchResult == null || searchResult.getEntities().isEmpty()) {
            longValue++;
        } else {
            List<Entity> entityList = searchResult.getEntities();
            Entity entity = entityList.get(0);
            longValue = entity.getId() + 1;
        }
        return String.format("%0" + digitsNumber + "d", longValue);
    }
}
