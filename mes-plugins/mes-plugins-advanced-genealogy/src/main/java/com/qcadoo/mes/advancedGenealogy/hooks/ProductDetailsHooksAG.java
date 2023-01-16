/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.advancedGenealogy.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ProductDetailsHooksAG {

    public final void setBatchNumberPatternEnabled(final ViewDefinitionState view) {
        CheckBoxComponent batchEvidence = (CheckBoxComponent) view
                .getComponentByReference(ProductFields.BATCH_EVIDENCE);
        FieldComponent batchNumberPattern = (FieldComponent) view.getComponentByReference(ProductFields.BATCH_NUMBER_PATTERN);

        if (batchEvidence.isChecked()) {
            batchNumberPattern.setEnabled(true);
        } else {
            batchNumberPattern.setEnabled(false);
            batchNumberPattern.setFieldValue(null);
        }
    }
}
