/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class NumberGeneratorService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void generateAndInsertNumber(final ViewDefinitionState state, final String entityName) {
        FieldComponentState number = (FieldComponentState) state.getComponentByReference("number");

        if (!checkIfShouldInsertNumber(state)) {
            return;
        }

        number.setFieldValue(generateNumber(state, entityName));
    }

    public boolean checkIfShouldInsertNumber(final ViewDefinitionState state) {
        FormComponentState form = (FormComponentState) state.getComponentByReference("form");
        FieldComponentState number = (FieldComponentState) state.getComponentByReference("number");
        if (form.getEntityId() != null) {
            // form is already saved
            return false;
        }
        if (StringUtils.hasText((String) number.getFieldValue())) {
            // number is already choosen
            return false;
        }
        if (number.isHasError()) {
            // there is a validation message for that field
            return false;
        }
        return true;
    }

    public String generateNumber(final String entityName) {

        SearchResult results = dataDefinitionService.get("products", entityName).find().withMaxResults(1).orderDescBy("id")
                .list();

        long longValue = 0;

        if (results.getEntities().isEmpty()) {
            longValue++;
        } else {
            longValue = results.getEntities().get(0).getId() + 1;
        }

        return String.format("%06d", longValue);
    }

    public String generateNumber(final String plugin, final String entityName) {

        SearchResult results = dataDefinitionService.get(plugin, entityName).find().withMaxResults(1).orderDescBy("id").list();

        long longValue = 0;

        if (results.getEntities().isEmpty()) {
            longValue++;
        } else {
            longValue = results.getEntities().get(0).getId() + 1;
        }

        return String.format("%06d", longValue);
    }

    public String generateNumber(final ViewDefinitionState state, final String entityName) {

        SearchResult results = dataDefinitionService.get("products", entityName).find().withMaxResults(1).orderDescBy("id")
                .list();

        long longValue = 0;

        if (results.getEntities().isEmpty()) {
            longValue++;
        } else {
            longValue = results.getEntities().get(0).getId() + 1;
        }

        return String.format("%06d", longValue);
    }

    public String generateNumber(final ViewDefinitionState state, final String entityName, int digitsNumber) {

        SearchResult results = dataDefinitionService.get("products", entityName).find().withMaxResults(1).orderDescBy("id")
                .list();

        long longValue = 0;

        if (results.getEntities().isEmpty()) {
            longValue++;
        } else {
            longValue = results.getEntities().get(0).getId() + 1;
        }

        return String.format("%0" + digitsNumber + "d", longValue);
    }

}
