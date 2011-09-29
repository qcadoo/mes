/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.6
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
package com.qcadoo.mes.materialFlow;

import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants.MODEL_TRANSFER;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class MaterialFlowService {

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;


    public BigDecimal calculateShouldBe(final String stockAreas, final String product, final String forDate) {

        BigDecimal countProductIn = BigDecimal.ZERO;
        BigDecimal countProductOut = BigDecimal.ZERO;
        BigDecimal quantity = BigDecimal.ZERO;
        BigDecimal countProduct = BigDecimal.ZERO;
        Date lastCorrectionDate = null;

        DataDefinition transferDataCorrection = dataDefinitionService.get("materialFlow", "stockCorrection");
        DataDefinition transferTo = dataDefinitionService.get("materialFlow", "transfer");
        DataDefinition transferFrom = dataDefinitionService.get("materialFlow", "transfer");

        SearchResult resultDataCorrection = transferDataCorrection.find(
                "where stockAreas = " + stockAreas + " order by stockCorrectionDate asc").list();

        for (Entity e : resultDataCorrection.getEntities()) {
            lastCorrectionDate = (Date) e.getField("stockCorrectionDate");
            quantity = (BigDecimal) e.getField("found");
            countProduct = quantity;
        }

        SearchResult resultTo = null;
        SearchResult resultFrom = null;

        if (lastCorrectionDate == null) {
            resultTo = transferTo.find(
                    "where stockAreasTo = '" + stockAreas + "' and product = '" + product + "' and date <= '" + forDate + "'")
                    .list();

            resultFrom = transferFrom.find(
                    "where stockAreasFrom = '" + stockAreas + "' and product = '" + product + "' and date <= '" + forDate + "'")
                    .list();

        } else {
            resultTo = transferTo.find(
                    "where stockAreasTo = '" + stockAreas + "' and product = '" + product + "' and date <= '" + forDate
                            + "' and date > '" + lastCorrectionDate + "'").list();

            resultFrom = transferFrom.find(
                    "where stockAreasFrom = '" + stockAreas + "' and product = '" + product + "' and date <= '" + forDate
                            + "' and date > '" + lastCorrectionDate + "'").list();
        }

        for (Entity e : resultTo.getEntities()) {
            quantity = (BigDecimal) e.getField("quantity");
            countProductIn = countProductIn.add(quantity);
        }

        for (Entity e : resultFrom.getEntities()) {
            quantity = (BigDecimal) e.getField("quantity");
            countProductOut = countProductOut.add(quantity);
        }

        if (lastCorrectionDate == null) {

            countProductIn = countProductIn.subtract(countProductOut);
        } else {
            countProductIn = countProductIn.add(countProduct);
            countProductIn = countProductIn.subtract(countProductOut);
        }

        if (countProductIn.compareTo(BigDecimal.ZERO) == -1)
            countProductIn = BigDecimal.ZERO;
        return countProductIn;
    }

    public void refreshShouldBe(final ViewDefinitionState state, final ComponentState componentState, final String[] args) {
        refreshShouldBe(state);
    }

    public void refreshShouldBe(final ViewDefinitionState state) {
        FieldComponent stockAreas = (FieldComponent) state.getComponentByReference("stockAreas");
        FieldComponent product = (FieldComponent) state.getComponentByReference("product");
        FieldComponent date = (FieldComponent) state.getComponentByReference("stockCorrectionDate");
        FieldComponent should = (FieldComponent) state.getComponentByReference("shouldBe");

        if (stockAreas != null && product != null && date != null) {
            if (stockAreas.getFieldValue() != null && product.getFieldValue() != null
                    && !date.getFieldValue().toString().equals("")) {
                String stockAreasNumber = stockAreas.getFieldValue().toString();
                String productNumber = product.getFieldValue().toString();
                String forDate = date.getFieldValue().toString();

                BigDecimal shouldBe = calculateShouldBe(stockAreasNumber, productNumber, forDate);

                if (shouldBe != null && shouldBe != BigDecimal.ZERO) {
                    should.setFieldValue(shouldBe);
                } else {
                    should.setFieldValue(BigDecimal.ZERO);
                }

            }
        }
    }

    public boolean validateTransfer(final DataDefinition dataDefinition, final Entity entity) {

        Entity stockAreasFrom = (Entity) (entity.getField("stockAreasFrom") != null ? entity.getField("stockAreasFrom") : null);
        Entity stockAreasTo = (Entity) (entity.getField("stockAreasTo") != null ? entity.getField("stockAreasTo") : null);

        if (stockAreasFrom == null && stockAreasTo == null) {
            entity.addError(dataDefinition.getField("stockAreasFrom"), "materialFlow.validate.global.error.fillAtLeastOneStockAreas");
            entity.addError(dataDefinition.getField("stockAreasTo"), "materialFlow.validate.global.error.fillAtLeastOneStockAreas");
            return false;
        }
        return true;

    }

    public void generateTransferNumber(final ViewDefinitionState state, final ComponentState componentState, final String[] args) {
        if (!(componentState instanceof FieldComponent)) {
            throw new IllegalStateException("component is not FieldComponentState");
        }
        FieldComponent number = (FieldComponent) state.getComponentByReference("number");
        FieldComponent productState = (FieldComponent) componentState;

        if (!numberGeneratorService.checkIfShouldInsertNumber(state, "form", "number")) {
            return;
        }
        if (productState.getFieldValue() != null) {
            Entity product = getAreaById((Long) productState.getFieldValue());
            if (product != null) {
                String numberValue = product.getField("number") + "-" +
                        numberGeneratorService.generateNumber("materialFlow", "transfer", 3);
                number.setFieldValue(numberValue);
            }
        }
    }
    
    private Entity getAreaById(final Long productId) {
        DataDefinition instructionDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);

        SearchCriteriaBuilder searchCriteria = instructionDD.find().setMaxResults(1).isIdEq(productId);

        SearchResult searchResult = searchCriteria.list();
        if (searchResult.getTotalNumberOfEntities() == 1) {
            return searchResult.getEntities().get(0);
        }
        return null;
    }
       
    public void fillNumberFieldValue(final ViewDefinitionState view) {
        if (view.getComponentByReference("number").getFieldValue() != null) {
            return;
        }
        numberGeneratorService.generateAndInsertNumber(view, MaterialFlowConstants.PLUGIN_IDENTIFIER, MODEL_TRANSFER, "form",
                "number");
    }

    public void fillUnitFieldValue(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Long productId = (Long) view.getComponentByReference("product").getFieldValue();
        if (productId == null) {
            return;
        }
        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCT).get(productId);
        FieldComponent unitField = null;
        String unit = product.getField("unit").toString();
        for (String referenceName : Sets.newHashSet("quantityUNIT", "shouldBeUNIT", "foundUNIT")) {
            unitField = (FieldComponent) view.getComponentByReference(referenceName);
            if (unitField == null) {
                continue;
            }
            unitField.setFieldValue(unit);
            unitField.requestComponentUpdateState();
        }
    }

}
