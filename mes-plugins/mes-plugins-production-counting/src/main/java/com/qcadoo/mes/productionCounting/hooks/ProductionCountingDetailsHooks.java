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
package com.qcadoo.mes.productionCounting.hooks;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ProductionCountingDetailsHooks {

    private static final List<String> L_PRODUCTION_COUNTING_FIELD_NAMES = Lists.newArrayList(ProductionCountingFields.ORDER,
            ProductionCountingFields.NAME, ProductionCountingFields.DESCRIPTION);

    @Autowired
    private ProductionCountingService productionCountingService;

    public void fillProductionRecordsGrid(final ViewDefinitionState view) {
        productionCountingService.fillProductionRecordsGrid(view);
    }

    public void disableFieldsWhenGenerated(final ViewDefinitionState view) {
        boolean isEnabled = false;

        FieldComponent generatedField = (FieldComponent) view.getComponentByReference(ProductionCountingFields.GENERATED);
        String generated = (String) generatedField.getFieldValue();

        if (StringUtils.isEmpty(generated) || "0".equals(generated)) {
            isEnabled = true;
        }

        productionCountingService.setComponentsState(view, L_PRODUCTION_COUNTING_FIELD_NAMES, isEnabled, true);
    }

}
