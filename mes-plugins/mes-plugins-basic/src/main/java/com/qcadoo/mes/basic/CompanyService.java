/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class CompanyService {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private ParameterService parameterService;

    /**
     * Returns basic company entity id for current user
     * 
     * @return company entity id
     * 
     */
    public Long getCompanyId() {
        return getCompany().getId();
    }

    /**
     * Returns basic comapny entity for current user. If company does not exist the new company entity will be created, saved and
     * returned.
     * 
     * @return company entity
     * 
     */
    @Transactional
    public Entity getCompany() {
        Entity parameter = parameterService.getParameter();
        return parameter.getBelongsToField(ParameterFields.COMPANY);

    }

    public final Boolean getOwning(final FormComponent form) {

        if (form.getEntityId() == null) {
            return Boolean.FALSE;
        }

        Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).get(
                form.getEntityId());

        if (company == null) {
            return Boolean.FALSE;
        }
        Entity parameter = parameterService.getParameter();
        Entity owner = parameter.getBelongsToField(ParameterFields.COMPANY);
        return company.getId().equals(owner.getId());
    }

    public void disableCompanyFormForOwner(final ViewDefinitionState state) {
        FormComponent form = (FormComponent) state.getComponentByReference(L_FORM);
        Boolean owner = getOwning(form);

        if (owner.booleanValue()) {
            form.setFormEnabled(false);
        }
    }

    public void disabledGridWhenCompanyIsAnOwner(final ViewDefinitionState state, String... references) {

        FormComponent form = (FormComponent) state.getComponentByReference(L_FORM);
        Boolean owner = getOwning(form);

        if (owner.booleanValue()) {
            disableGridComponents(state, references);
        }
    }

    private void disableGridComponents(final ViewDefinitionState state, String... references) {
        for (String reference : references) {
            ComponentState component = state.getComponentByReference(reference);
            if (component instanceof GridComponent) {
                ((GridComponent) component).setEditable(false);
            }
        }
    }

    public void generateCompanyNumber(final ViewDefinitionState state) {
        numberGeneratorService.generateAndInsertNumber(state, BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY,
                L_FORM, "number");
    }

}
