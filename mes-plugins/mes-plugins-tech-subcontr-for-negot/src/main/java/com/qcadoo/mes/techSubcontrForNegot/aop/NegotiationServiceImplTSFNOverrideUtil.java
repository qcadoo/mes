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

import com.google.common.collect.Lists;
import com.qcadoo.mes.techSubcontrForNegot.constants.TechSubcontrForNegotConstants;
import com.qcadoo.mes.techSubcontracting.constants.CompanyOperationFields;
import com.qcadoo.mes.techSubcontracting.constants.CompanyOperationGroupFields;
import com.qcadoo.mes.techSubcontracting.constants.OperationFieldsTS;
import com.qcadoo.mes.techSubcontracting.constants.OperationGroupFieldsTS;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginStateResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.qcadoo.mes.techSubcontrForNegot.constants.RequestForQuotationProductFieldsTSFN.OPERATION;
import static com.qcadoo.mes.technologies.constants.OperationFields.OPERATION_GROUP;

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
        List<Entity> operationCompanies = operation.getHasManyField(OperationFieldsTS.COMPANIES);

        if (!operationCompanies.isEmpty()) {
            companies.addAll(operationCompanies.stream().map(e -> e.getBelongsToField(CompanyOperationFields.COMPANY)).collect(Collectors.toList()));
        }

        Entity operationGroup = operation.getBelongsToField(OPERATION_GROUP);

        if (operationGroup != null) {
            List<Entity> operationGroupCompanies = operationGroup.getHasManyField(OperationGroupFieldsTS.COMPANIES);

            if (!operationGroupCompanies.isEmpty()) {
                companies.addAll(operationGroupCompanies.stream().map(e -> e.getBelongsToField(CompanyOperationGroupFields.COMPANY)).collect(Collectors.toList()));
            }
        }
    }

    public void fillRequestForQuotationProductOperation(final Entity negotiationProduct,
                                                        final Entity requestForQuotationProduct) {
        requestForQuotationProduct.setField(OPERATION, negotiationProduct.getBelongsToField(OPERATION));
    }

}
