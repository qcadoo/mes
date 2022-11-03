/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.basic;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CompanyService {

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

    /**
     * Returns company entity
     * 
     * @param companyId
     *            companyId
     * 
     * @return company
     */
    @Transactional
    public Entity getCompany(final Long companyId) {
        return getCompanyDD().get(companyId);
    }

    private DataDefinition getCompanyDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY);
    }

    public final Boolean isCompanyOwner(final Entity company) {
        Long companyId = company.getId();

        if (companyId == null) {
            return Boolean.FALSE;
        }

        Entity companyFromDB = getCompanyDD().get(companyId);

        if (companyFromDB == null) {
            return Boolean.FALSE;
        }

        Entity parameter = parameterService.getParameter();
        Entity owner = parameter.getBelongsToField(ParameterFields.COMPANY);

        if (owner == null) {
            return Boolean.FALSE;
        }

        return companyFromDB.getId().equals(owner.getId());
    }

    public void disabledGridWhenCompanyIsOwner(final ViewDefinitionState view, final String... references) {
        FormComponent companyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity company = companyForm.getEntity();

        Boolean isOwner = isCompanyOwner(company);

        disableGridComponents(view, !isOwner, references);
    }

    private void disableGridComponents(final ViewDefinitionState view, final Boolean isEditable, final String... references) {
        for (String reference : references) {
            ComponentState componentState = view.getComponentByReference(reference);

            if (componentState instanceof GridComponent) {
                ((GridComponent) componentState).setEditable(isEditable);
            }
        }
    }

    public void disableButton(final ViewDefinitionState view, final String ribbonGroupName, final String ribbonActionItemName,
            final boolean isEnabled, final String message) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        RibbonGroup ribbonGroup = window.getRibbon().getGroupByName(ribbonGroupName);
        RibbonActionItem ribbonActionItem = ribbonGroup.getItemByName(ribbonActionItemName);

        ribbonActionItem.setEnabled(isEnabled);

        if (isEnabled) {
            ribbonActionItem.setMessage(null);
        } else {
            ribbonActionItem.setMessage(message);
        }

        ribbonActionItem.requestUpdate(true);
    }

    public Optional<Entity> findCompanyByNumber(final String number) {
        return Optional.ofNullable(getCompanyDD().find().add(SearchRestrictions.eq(CompanyFields.NUMBER, number))
                .setMaxResults(1).uniqueResult());
    }

    public Optional<Entity> findCompanyByTax(final String tax, boolean useLike) {
        if(useLike) {
            return Optional.ofNullable(getCompanyDD().find().add(SearchRestrictions.ilike(CompanyFields.TAX, tax))
                    .setMaxResults(1).uniqueResult());
        } else {
            return Optional.ofNullable(getCompanyDD().find().add(SearchRestrictions.eq(CompanyFields.TAX, tax))
                    .setMaxResults(1).uniqueResult());
        }
    }
}
