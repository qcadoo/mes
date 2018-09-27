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
package com.qcadoo.mes.advancedGenealogy.hooks;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class UsedBatchSimpleViewHooks {

    private static final String L_PRODUCT = "product";

    private static final String L_UNIT = "unit";

    private static final String L_BATCH_LOOKUP = "batchLookup";

    private static final String L_WORKER = "worker";

    private static final String L_DATE_AND_TIME = "dateAndTime";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    public final void fillDateAndWorkerField(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        fillDateAndWorkerField(view);
    }

    public final void fillDateAndWorkerField(final ViewDefinitionState view) {
        FieldComponent dateAndTimeField = getFieldComponent(view, L_DATE_AND_TIME);
        FieldComponent workerField = getFieldComponent(view, L_WORKER);

        final Date dateAndTime = new Date();
        final String worker = securityService.getCurrentUserName();

        dateAndTimeField.setFieldValue(setDateToField(dateAndTime));
        workerField.setFieldValue(worker);
    }

    public final void fillUnitField(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        fillUnitField(view);
    }

    public final void fillUnitField(final ViewDefinitionState view) {
        FieldComponent batchLookup = getFieldComponent(view, L_BATCH_LOOKUP);
        FieldComponent unitField = getFieldComponent(view, L_UNIT);

        if (batchLookup.getFieldValue() == null) {
            unitField.setFieldValue(null);
            return;
        }

        Long batchId = Long.valueOf(batchLookup.getFieldValue().toString());
        Entity batch = getDataDef(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER, AdvancedGenealogyConstants.MODEL_BATCH).get(
                batchId);
        if (batch == null) {
            unitField.setFieldValue(null);
            return;
        }

        Entity product = batch.getBelongsToField(L_PRODUCT);
        unitField.setFieldValue(product.getField(L_UNIT));
    }

    private FieldComponent getFieldComponent(final ViewDefinitionState view, final String name) {
        return (FieldComponent) view.getComponentByReference(name);
    }

    public Object setDateToField(final Date date) {
        return new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault()).format(date);
    }

    private DataDefinition getDataDef(final String pluginName, final String modelName) {
        return dataDefinitionService.get(pluginName, modelName);
    }
}
