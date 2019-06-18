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
package com.qcadoo.mes.advancedGenealogyForOrders.workplan;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.advancedGenealogyForOrders.constants.AdvancedGenealogyForOrdersConstants;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.ProductDirection;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

import static com.qcadoo.model.api.search.SearchRestrictions.idEq;

@Component("batchNumbersOperationProductColumn")
public class BatchNumbersOperationProductColumn implements OperationProductColumn {

    private TranslationService translationService;
    private DataDefinitionService dataDefinitionService;

    @Autowired
    public BatchNumbersOperationProductColumn(TranslationService translationService, DataDefinitionService dataDefinitionService) {
        this.translationService = translationService;
        this.dataDefinitionService = dataDefinitionService;
    }

    @Override
    public String getIdentifier() {
        return "batchNumbersOperationProductColumn";
    }

    @Override
    public String getName(Locale locale, ProductDirection productDirection) {
        return translationService.translate("workPlans.columnForInputProducts.name.value.batchNumbers", locale);
    }

    @Override
    public String getDescription(Locale locale, ProductDirection productDirection) {
        return translationService.translate("workPlans.columnForInputProducts.description.value.batchNumbers", locale);
    }

    @Override
    public String getColumnValue(Entity operationProduct) {
        Entity genealogyProductInComponent = findGenealogyOperationProductInByOperationProductIn(operationProduct);
        List<Entity> batchComps = genealogyProductInComponent.getHasManyField("productInBatches");

        StringBuilder batchNumbers = new StringBuilder();
        for (Entity batchComp : batchComps) {
            if (batchNumbers.length() > 0)
                batchNumbers.append(", ");
            batchNumbers.append(batchComp.getBelongsToField("batch").getStringField("number"));
        }
        return batchNumbers.toString();
    }

    private Entity findGenealogyOperationProductInByOperationProductIn(Entity operationProduct) {
        Long id = operationProduct.getId();
        return advancedGenealogyForOrdersDD()
                .find()
                .createCriteria("productInComponent", "productInComponent_alias", JoinType.INNER).add(idEq(id))
                .uniqueResult();
    }

    @Override
    public ProductDirection[] getDirection() {
        return new ProductDirection[]{ProductDirection.IN};
    }

    private DataDefinition advancedGenealogyForOrdersDD() {
        return dataDefinitionService.get(AdvancedGenealogyForOrdersConstants.PLUGIN_IDENTIFIER,
                AdvancedGenealogyForOrdersConstants.MODEL_PRODUCT_IN_COMPONENT);
    }

}
