/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static com.qcadoo.mes.materialFlow.constants.StockCorrectionFields.FOUND;
import static com.qcadoo.mes.materialFlow.constants.StockCorrectionFields.PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.StockCorrectionFields.SHOULD_BE;
import static com.qcadoo.mes.materialFlow.constants.StockCorrectionFields.STOCK_AREAS;
import static com.qcadoo.mes.materialFlow.constants.StockCorrectionFields.STOCK_CORRECTION_DATE;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.NUMBER;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.QUANTITY;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STOCK_AREAS_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STOCK_AREAS_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;
import static com.qcadoo.mes.materialFlow.constants.TransferType.CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransferType.PRODUCTION;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public class MaterialFlowService {

    private static final String L_FORM = "form";

    private static final String L_ID = "id";

    private static final String L_T = "t";

    private static final String L_T_PRODUCT = "t.product";

    private static final String L_T_PRODUCT_ID = "t.product.id";

    private static final String L_T_STOCK_AREAS_TO_ID = "t.stockAreasTo.id";

    private static final String L_SC = "sc";

    private static final String L_SC_PRODUCT = "sc.product";

    private static final String L_SC_PRODUCT_ID = "sc.product.id";

    private static final String L_SC_STOCK_AREAS_ID = "sc.stockAreas.id";

    private static final String L_STOCK_AREAS_ID = "stockAreas.id";

    private static final String L_STOCK_AREAS_TO_ID = "stockAreasTo.id";

    private static final String L_STOCK_AREAS_FROM_ID = "stockAreasFrom.id";

    private static final String L_PRODUCT_ID = "product.id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TimeConverterService timeConverterService;

    public BigDecimal calculateShouldBeInStockArea(final Long stockAreasId, final Long productId, final Date forDate) {

        BigDecimal countProductIn = BigDecimal.ZERO;
        BigDecimal countProductOut = BigDecimal.ZERO;
        BigDecimal countProduct = BigDecimal.ZERO;
        Date lastCorrectionDate = null;
        DataDefinition transferDataCorrection = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_STOCK_CORRECTION);
        DataDefinition transferTo = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_TRANSFER);
        DataDefinition transferFrom = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_TRANSFER);

        Entity resultDataCorrection = transferDataCorrection.find().add(SearchRestrictions.eq(L_STOCK_AREAS_ID, stockAreasId))
                .add(SearchRestrictions.eq(L_PRODUCT_ID, productId)).addOrder(SearchOrders.desc(STOCK_CORRECTION_DATE))
                .setMaxResults(1).uniqueResult();

        if (resultDataCorrection != null) {
            lastCorrectionDate = (Date) resultDataCorrection.getField(STOCK_CORRECTION_DATE);
            countProduct = (BigDecimal) resultDataCorrection.getField(FOUND);
        }

        SearchResult resultTo = null;
        SearchResult resultFrom = null;

        if (lastCorrectionDate == null) {

            resultTo = transferTo.find().add(SearchRestrictions.eq(L_STOCK_AREAS_TO_ID, stockAreasId))
                    .add(SearchRestrictions.eq(L_PRODUCT_ID, productId)).add(SearchRestrictions.le(TIME, forDate)).list();

            resultFrom = transferFrom.find().add(SearchRestrictions.eq(L_STOCK_AREAS_FROM_ID, stockAreasId))
                    .add(SearchRestrictions.eq(L_PRODUCT_ID, productId)).add(SearchRestrictions.le(TIME, forDate)).list();

        } else {
            resultTo = transferTo.find().add(SearchRestrictions.eq(L_STOCK_AREAS_TO_ID, stockAreasId))
                    .add(SearchRestrictions.eq(L_PRODUCT_ID, productId)).add(SearchRestrictions.le(TIME, forDate))
                    .add(SearchRestrictions.gt(TIME, lastCorrectionDate)).list();

            resultFrom = transferFrom.find().add(SearchRestrictions.eq(L_STOCK_AREAS_FROM_ID, stockAreasId))
                    .add(SearchRestrictions.eq(L_PRODUCT_ID, productId)).add(SearchRestrictions.le(TIME, forDate))
                    .add(SearchRestrictions.gt(TIME, lastCorrectionDate)).list();
        }

        for (Entity e : resultTo.getEntities()) {
            BigDecimal quantity = (BigDecimal) e.getField(QUANTITY);
            countProductIn = countProductIn.add(quantity, numberService.getMathContext());
        }

        for (Entity e : resultFrom.getEntities()) {
            BigDecimal quantity = (BigDecimal) e.getField(QUANTITY);
            countProductOut = countProductOut.add(quantity, numberService.getMathContext());
        }

        if (lastCorrectionDate == null) {
            countProductIn = countProductIn.subtract(countProductOut, numberService.getMathContext());
        } else {
            countProductIn = countProductIn.add(countProduct, numberService.getMathContext());
            countProductIn = countProductIn.subtract(countProductOut, numberService.getMathContext());
        }

        if (countProductIn.compareTo(BigDecimal.ZERO) == -1) {
            countProductIn = BigDecimal.ZERO;
        }

        return countProductIn;
    }

    public void refreshShouldBeInStockCorrectionDetail(final ViewDefinitionState state, final ComponentState componentState,
            final String[] args) {
        refreshShouldBeInStockCorrectionDetail(state);
    }

    public void refreshShouldBeInStockCorrectionDetail(final ViewDefinitionState state) {
        FieldComponent stockAreas = (FieldComponent) state.getComponentByReference(STOCK_AREAS);
        FieldComponent product = (FieldComponent) state.getComponentByReference(PRODUCT);
        FieldComponent date = (FieldComponent) state.getComponentByReference(STOCK_CORRECTION_DATE);
        FieldComponent should = (FieldComponent) state.getComponentByReference(SHOULD_BE);

        if ((stockAreas != null) && (product != null) && (date != null) && (stockAreas.getFieldValue() != null)
                && (product.getFieldValue() != null) && !date.getFieldValue().toString().equals("")) {
            Long stockAreasNumber = (Long) stockAreas.getFieldValue();
            Long productNumber = (Long) product.getFieldValue();

            Date forDate = timeConverterService.getDateFromField(date.getFieldValue());

            BigDecimal shouldBe = calculateShouldBeInStockArea(stockAreasNumber, productNumber, forDate);

            if (shouldBe == null || shouldBe == BigDecimal.ZERO) {
                should.setFieldValue(BigDecimal.ZERO);
            } else {
                should.setFieldValue(shouldBe);
            }
        }

        should.requestComponentUpdateState();
    }

    public void fillNumberFieldValue(final ViewDefinitionState view) {
        if (view.getComponentByReference(NUMBER).getFieldValue() != null) {
            return;
        }
        numberGeneratorService.generateAndInsertNumber(view, MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_TRANSFORMATIONS, L_FORM, NUMBER);
    }

    public void generateNumberForTransfer(final ViewDefinitionState state, final ComponentState componentState,
            final String[] args) {
        generateNumberAfterSelectingProduct(state, componentState, MaterialFlowConstants.MODEL_TRANSFER);
    }

    public void generateNumberForStockCorrection(final ViewDefinitionState state, final ComponentState componentState,
            final String[] args) {
        generateNumberAfterSelectingProduct(state, componentState, MaterialFlowConstants.MODEL_STOCK_CORRECTION);
    }

    public void generateNumberAfterSelectingProduct(final ViewDefinitionState state, final ComponentState componentState,
            final String tableToGenerateNumber) {
        if (!(componentState instanceof FieldComponent)) {
            throw new IllegalStateException("component is not FieldComponentState");
        }
        FieldComponent number = (FieldComponent) state.getComponentByReference(NUMBER);
        FieldComponent productState = (FieldComponent) componentState;

        if (!numberGeneratorService.checkIfShouldInsertNumber(state, L_FORM, NUMBER)) {
            return;
        }
        if (productState.getFieldValue() != null) {
            Entity product = getAreaById((Long) productState.getFieldValue());
            number.setFieldValue(generateNumberFromProduct(product, tableToGenerateNumber));
        }
        number.requestComponentUpdateState();
    }

    public String generateNumberFromProduct(final Entity product, final String model) {
        String number = "";

        if (product != null) {
            number = product.getField(NUMBER) + "-"
                    + numberGeneratorService.generateNumber(MaterialFlowConstants.PLUGIN_IDENTIFIER, model, 3);
        }

        return number;
    }

    public List<Entity> getStockAreasFromDB() {
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_STOCK_AREAS).find()
                .list().getEntities();
    }

    private Entity getAreaById(final Long productId) {
        DataDefinition instructionDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);

        SearchCriteriaBuilder searchCriteria = instructionDD.find().add(SearchRestrictions.eq(L_ID, productId)).setMaxResults(1);

        SearchResult searchResult = searchCriteria.list();
        if (searchResult.getTotalNumberOfEntities() == 1) {
            return searchResult.getEntities().get(0);
        }
        return null;
    }

    public void fillUnitFieldValue(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        fillUnitFieldValue(view);
    }

    public void fillUnitFieldValue(final ViewDefinitionState view) {
        Long productId = (Long) view.getComponentByReference(PRODUCT).getFieldValue();
        if (productId == null) {
            return;
        }
        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCT).get(productId);
        FieldComponent unitField = null;
        String unit = product.getStringField(UNIT);
        for (String referenceName : Sets.newHashSet("quantityUNIT", "shouldBeUNIT", "foundUNIT")) {
            unitField = (FieldComponent) view.getComponentByReference(referenceName);
            if (unitField == null) {
                continue;
            }
            unitField.setFieldValue(unit);
            unitField.requestComponentUpdateState();
        }
    }

    public Map<Entity, BigDecimal> calculateMaterialQuantitiesInStockArea(final Entity materialsInStockAreas) {
        List<Entity> stockAreas = new ArrayList<Entity>(materialsInStockAreas.getHasManyField(STOCK_AREAS));
        Map<Entity, BigDecimal> reportData = new HashMap<Entity, BigDecimal>();

        for (Entity component : stockAreas) {
            Entity stockArea = component.getBelongsToField(STOCK_AREAS);

            List<Entity> products = getProductsSeenInStockArea(stockArea.getStringField(NUMBER));

            Date forDate = ((Date) materialsInStockAreas.getField("materialFlowForDate"));

            for (Entity product : products) {
                BigDecimal quantity = calculateShouldBeInStockArea(stockArea.getId(), product.getId(), forDate);

                if (reportData.containsKey(product)) {
                    reportData.put(product, reportData.get(product).add(quantity, numberService.getMathContext()));
                } else {
                    reportData.put(product, quantity);
                }
            }
        }
        return reportData;
    }

    public List<Entity> getProductsSeenInStockArea(final String stockAreaNumber) {
        DataDefinition dataDefStockAreas = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_STOCK_AREAS);

        Long id = dataDefStockAreas.find().add(SearchRestrictions.eq(NUMBER, stockAreaNumber)).uniqueResult().getId();

        DataDefinition dataDefProduct = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);

        List<Entity> productsFromTransfers = dataDefProduct.find().createAlias(MaterialFlowConstants.MODEL_TRANSFER, L_T)
                .addOrder(SearchOrders.asc(L_T_PRODUCT_ID))
                .setProjection(SearchProjections.distinct(SearchProjections.field(L_T_PRODUCT)))
                .add(SearchRestrictions.eqField(L_T_PRODUCT_ID, L_ID)).add(SearchRestrictions.eq(L_T_STOCK_AREAS_TO_ID, id))
                .list().getEntities();

        List<Entity> productsFromStockCorrections = dataDefProduct.find()
                .createAlias(MaterialFlowConstants.MODEL_STOCK_CORRECTION, L_SC).addOrder(SearchOrders.asc(L_SC_PRODUCT_ID))
                .setProjection(SearchProjections.distinct(SearchProjections.field(L_SC_PRODUCT)))
                .add(SearchRestrictions.eqField(L_SC_PRODUCT_ID, L_ID)).add(SearchRestrictions.eq(L_SC_STOCK_AREAS_ID, id))
                .list().getEntities();

        for (Entity product : productsFromStockCorrections) {
            if (!productsFromTransfers.contains(product)) {
                productsFromTransfers.add(product);
            }
        }

        return productsFromTransfers;
    }

    public void disableStockAreaFieldForParticularTransferType(final ViewDefinitionState view,
            final ComponentState componentState, final String[] args) {
        disableStockAreaFieldForParticularTransferType(view);
    }

    public void disableStockAreaFieldForParticularTransferType(final ViewDefinitionState view) {
        if (view.getComponentByReference(TYPE).getFieldValue() == null) {
            return;
        }

        String type = view.getComponentByReference(TYPE).getFieldValue().toString();
        FieldComponent toStockArea = (FieldComponent) view.getComponentByReference(STOCK_AREAS_TO);
        FieldComponent fromStockArea = (FieldComponent) view.getComponentByReference(STOCK_AREAS_FROM);

        if (CONSUMPTION.getStringValue().equals(type)) {
            fromStockArea.setEnabled(true);
            fromStockArea.setRequired(true);
            toStockArea.setRequired(false);
            toStockArea.setEnabled(false);
            toStockArea.setFieldValue("");
        } else if (PRODUCTION.getStringValue().equals(type)) {
            toStockArea.setEnabled(true);
            toStockArea.setRequired(true);
            fromStockArea.setRequired(false);
            fromStockArea.setEnabled(false);
            fromStockArea.setFieldValue("");
        } else {
            toStockArea.setEnabled(true);
            toStockArea.setRequired(true);
            fromStockArea.setRequired(true);
            fromStockArea.setEnabled(true);
        }

        toStockArea.requestComponentUpdateState();
        fromStockArea.requestComponentUpdateState();
    }

    public void fillDefaultStockAreaToFieldInTransformations(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FieldComponent stockAreaTo = (FieldComponent) view.getComponentByReference(STOCK_AREAS_TO);

        if (stockAreaTo.getFieldValue() == null) {
            FieldComponent stockAreaFrom = (FieldComponent) view.getComponentByReference(STOCK_AREAS_FROM);
            stockAreaTo.setFieldValue(stockAreaFrom.getFieldValue());
        }
        stockAreaTo.requestComponentUpdateState();
    }

}
