/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.workPlans.pdf.document.operation.product.column;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.ProductDirection;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("effectiveQuantityOperationProductColumn")
public class EffectiveQuantityOperationProductColumn extends AbstractOperationProductColumn {

    @Autowired
    public EffectiveQuantityOperationProductColumn(TranslationService translationService) {
        super(translationService);
    }

    @Override
    public String getIdentifier() {
        return "effectiveQuantityOperationProductColumn";
    }

    @Override
    public String getColumnValue(Entity operationProduct) {
        return "";
    }

    @Override
    public ProductDirection[] getDirection() {
        return ProductDirection.values();
    }
}
