/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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

import static com.qcadoo.mes.basic.constants.ParameterFields.COMPANY;

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

@Service
public class CompanyService {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    /**
     * Returns basic company entity id for current user
     * 
     * @return company entity id
     * 
     */
    public Long getCompanyId() {
        if (getCompany() == null) {
            return null;
        } else {
            return getCompany().getId();
        }
    }

    /**
     * Returns basic company entity for current user.
     * 
     * @return company entity
     * 
     */
    @Transactional
    public Entity getCompany() {
        Entity parameter = parameterService.getParameter();

        return parameter.getBelongsToField(ParameterFields.COMPANY);
    }

    public final Boolean isCompanyOwner(final Entity company) {
        if (company.getId() == null) {
            return Boolean.FALSE;
        }

        Entity companyFromDB = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).get(
                company.getId());

        if (companyFromDB == null) {
            return Boolean.FALSE;
        }

        Entity parameter = parameterService.getParameter();
        Entity owner = parameter.getBelongsToField(COMPANY);

        return companyFromDB.getId().equals(owner.getId());
    }

    public void disabledGridWhenCompanyIsOwner(final ViewDefinitionState view, final String... references) {
        FormComponent companyForm = (FormComponent) view.getComponentByReference(L_FORM);
        Boolean isOwner = isCompanyOwner(companyForm.getEntity());

        disableGridComponents(view, !isOwner, references);
    }

    private void disableGridComponents(final ViewDefinitionState view, final Boolean enable, final String... references) {
        for (String reference : references) {
            ComponentState component = view.getComponentByReference(reference);
            if (component instanceof GridComponent) {
                ((GridComponent) component).setEditable(enable);
            }
        }
    }

}
