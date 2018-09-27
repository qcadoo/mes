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
package com.qcadoo.mes.techSubcontrForNegot.aop;

import static com.qcadoo.mes.techSubcontrForNegot.constants.RequestForQuotationProductFieldsTSFN.OPERATION;
import static com.qcadoo.mes.techSubcontracting.constants.OperationFieldsTS.OPERATION_COMPANIES;
import static com.qcadoo.mes.techSubcontracting.constants.OperationGroupFieldsTS.OPERATION_GROUP_COMPANIES;
import static com.qcadoo.mes.technologies.constants.OperationFields.OPERATION_GROUP;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.techSubcontrForNegot.constants.TechSubcontrForNegotConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginStateResolver;

@Service
public class NegotiationServiceImplTSFNOverrideUtil {

    @Autowired
    private PluginStateResolver pluginStateResolver;

    public boolean shouldOverride(final Entity negotiationProduct) {
        return pluginStateResolver.isEnabled(TechSubcontrForNegotConstants.PLUGIN_IDENTIFIER)
                && (negotiationProduct.getBelongsToField(OPERATION) != null);
    }

    public List<Entity> getCompaniesForNegotiationProduct(final Entity negotiationProduct) {
        List<Entity> companies = Lists.newArrayList();

        Entity operation = negotiationProduct.getBelongsToField(OPERATION);

        addCompaniesWhichExecutesOperation(companies, operation);

        return companies;
    }

    private void addCompaniesWhichExecutesOperation(final List<Entity> companies, final Entity operation) {
        List<Entity> operationCompanies = operation.getManyToManyField(OPERATION_COMPANIES);

        if (!operationCompanies.isEmpty()) {
            companies.addAll(operationCompanies);
        }

        Entity operationGroup = operation.getBelongsToField(OPERATION_GROUP);

        if (operationGroup != null) {
            List<Entity> operationGroupCompanies = operationGroup.getManyToManyField(OPERATION_GROUP_COMPANIES);

            if (!operationGroupCompanies.isEmpty()) {
                companies.addAll(operationGroupCompanies);
            }
        }
    }

    public void fillRequestForQuotationProductOperation(final Entity negotiationProduct, final Entity requestForQuotationProduct) {
        requestForQuotationProduct.setField(OPERATION, negotiationProduct.getBelongsToField(OPERATION));
    }

}
