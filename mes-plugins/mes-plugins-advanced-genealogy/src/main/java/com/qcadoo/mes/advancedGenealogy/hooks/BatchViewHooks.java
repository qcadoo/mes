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

import com.google.common.collect.Lists;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.advancedGenealogy.states.constants.BatchStateChangeFields;
import com.qcadoo.mes.advancedGenealogy.states.constants.BatchStateStringValues;
import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import static com.qcadoo.mes.states.constants.StateChangeStatus.SUCCESSFUL;

@Service
public final class BatchViewHooks {

    private static final String L_LOGGINGS_GRID = "loggingsGrid";

    private static final String L_SUPPLIER = "supplier";

    @Autowired
    private CompanyService companyService;

    @Autowired
    private StateChangeHistoryService stateChangeHistoryService;

    public void onBeforeRender(final ViewDefinitionState view) {
        disableFormWhenExternalSynchronized(view);
        filterStateChangeHistory(view);
        setIdForMultiUploadField(view);
    }

    private void disableFormWhenExternalSynchronized(final ViewDefinitionState view) {
        FormComponent batchForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long batchId = batchForm.getEntityId();

        if (batchId == null) {
            batchForm.setFormEnabled(true);

            return;
        }

        Entity batch = batchForm.getEntity();

        String externalNumber = batch.getStringField(BatchFields.EXTERNAL_NUMBER);
        String state = batch.getStringField(BatchFields.STATE);

        boolean isEnabled = StringUtils.isEmpty(externalNumber) && BatchStateStringValues.TRACKED.equals(state);

        batchForm.setFormEnabled(isEnabled);
    }

    private void filterStateChangeHistory(final ViewDefinitionState view) {
        GridComponent loggingsGrid = (GridComponent) view.getComponentByReference(L_LOGGINGS_GRID);

        CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService
                .buildStatusRestriction(BatchStateChangeFields.STATUS, Lists.newArrayList(SUCCESSFUL.getStringValue()));

        loggingsGrid.setCustomRestriction(onlySuccessfulRestriction);
    }

    public void setSupplierToOwner(final ViewDefinitionState view) {
        FieldComponent supplierField = (FieldComponent) view.getComponentByReference(L_SUPPLIER);

        if (supplierField.getFieldValue() == null) {
            Entity company = companyService.getCompany();

            if (company != null) {
                supplierField.setFieldValue(company.getId());
                supplierField.requestComponentUpdateState();
            }
        }
    }

    public void setIdForMultiUploadField(final ViewDefinitionState view) {
        FormComponent batchForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent batchIdForMultiUpload = (FieldComponent) view.getComponentByReference("batchIdForMultiUpload");
        FieldComponent locale = (FieldComponent) view.getComponentByReference("locale");

        Long id = batchForm.getEntityId();

        if (Objects.nonNull(id)) {
            batchIdForMultiUpload.setFieldValue(id);
            batchIdForMultiUpload.requestComponentUpdateState();
        } else {
            batchIdForMultiUpload.setFieldValue("");
            batchIdForMultiUpload.requestComponentUpdateState();
        }

        locale.setFieldValue(LocaleContextHolder.getLocale());
        locale.requestComponentUpdateState();
    }

}
