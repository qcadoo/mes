/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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

    private static final String TRANSFORMATIONS_CONSUMPTION_L = "transformationsConsumption";

    private static final String STOCK_AREAS_TO_L = "stockAreasTo";

    private static final String TRANSFORMATIONS_PRODUCTION_L = "transformationsProduction";

    private static final String TYPE_L = "type";

    private static final String STAFF_L = "staff";

    private static final String STOCK_AREAS_FROM_L = "stockAreasFrom";

    private static final String SHOULD_BE_L = "shouldBe";

    private static final String STOCK_CORRECTION_DATE_L = "stockCorrectionDate";

    private static final String PRODUCT_L = "product";

    private static final String STOCK_AREAS_L = "stockAreas";

    private static final String NUMBER_L = "number";

    private static final String TIME_L = "time";

    private static final String QUANTITY_L = "quantity";

    private static final String PRODUCT_ID_L = "product.id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TimeConverterService timeConverterService;

    public BigDecimal calculateShouldBeInStockArea(final Long stockAreas, final String product, final Date forDate) {

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

        Long productId = Long.valueOf(product);

        Entity resultDataCorrection = transferDataCorrection.find().add(SearchRestrictions.eq("stockAreas.id", stockAreas))
                .add(SearchRestrictions.eq(PRODUCT_ID_L, productId)).addOrder(SearchOrders.desc(STOCK_CORRECTION_DATE_L))
                .setMaxResults(1).uniqueResult();

        if (resultDataCorrection != null) {
            lastCorrectionDate = (Date) resultDataCorrection.getField(STOCK_CORRECTION_DATE_L);
            countProduct = (BigDecimal) resultDataCorrection.getField("found");
        }

        SearchResult resultTo = null;
        SearchResult resultFrom = null;

        if (lastCorrectionDate == null) {

            resultTo = transferTo.find().add(SearchRestrictions.eq("stockAreasTo.id", stockAreas))
                    .add(SearchRestrictions.eq(PRODUCT_ID_L, productId)).add(SearchRestrictions.le(TIME_L, forDate)).list();

            resultFrom = transferFrom.find().add(SearchRestrictions.eq("stockAreasFrom.id", stockAreas))
                    .add(SearchRestrictions.eq(PRODUCT_ID_L, productId)).add(SearchRestrictions.le(TIME_L, forDate)).list();

        } else {
            resultTo = transferTo.find().add(SearchRestrictions.eq("stockAreasTo.id", stockAreas))
                    .add(SearchRestrictions.eq(PRODUCT_ID_L, productId)).add(SearchRestrictions.le(TIME_L, forDate))
                    .add(SearchRestrictions.gt(TIME_L, lastCorrectionDate)).list();

            resultFrom = transferFrom.find().add(SearchRestrictions.eq("stockAreasFrom.id", stockAreas))
                    .add(SearchRestrictions.eq(PRODUCT_ID_L, productId)).add(SearchRestrictions.le(TIME_L, forDate))
                    .add(SearchRestrictions.gt(TIME_L, lastCorrectionDate)).list();
        }

        for (Entity e : resultTo.getEntities()) {
            BigDecimal quantity = (BigDecimal) e.getField(QUANTITY_L);
            countProductIn = countProductIn.add(quantity, numberService.getMathContext());
        }

        for (Entity e : resultFrom.getEntities()) {
            BigDecimal quantity = (BigDecimal) e.getField(QUANTITY_L);
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
        FieldComponent stockAreas = (FieldComponent) state.getComponentByReference(STOCK_AREAS_L);
        FieldComponent product = (FieldComponent) state.getComponentByReference(PRODUCT_L);
        FieldComponent date = (FieldComponent) state.getComponentByReference(STOCK_CORRECTION_DATE_L);
        FieldComponent should = (FieldComponent) state.getComponentByReference(SHOULD_BE_L);

        if ((stockAreas != null) && (product != null) && (date != null) && (stockAreas.getFieldValue() != null)
                && (product.getFieldValue() != null) && !date.getFieldValue().toString().equals("")) {
            Long stockAreasNumber = (Long) stockAreas.getFieldValue();
            String productNumber = product.getFieldValue().toString();

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
        if (view.getComponentByReference(NUMBER_L).getFieldValue() != null) {
            return;
        }
        numberGeneratorService.generateAndInsertNumber(view, MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_TRANSFORMATIONS, "form", NUMBER_L);
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
        FieldComponent number = (FieldComponent) state.getComponentByReference(NUMBER_L);
        FieldComponent productState = (FieldComponent) componentState;

        if (!numberGeneratorService.checkIfShouldInsertNumber(state, "form", NUMBER_L)) {
            return;
        }
        if (productState.getFieldValue() != null) {
            Entity product = getAreaById((Long) productState.getFieldValue());
            if (product != null) {
                String numberValue = product.getField(NUMBER_L)
                        + "-"
                        + numberGeneratorService
                                .generateNumber(MaterialFlowConstants.PLUGIN_IDENTIFIER, tableToGenerateNumber, 3);
                number.setFieldValue(numberValue);
            }
        }
        number.requestComponentUpdateState();
    }

    private Entity getAreaById(final Long productId) {
        DataDefinition instructionDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);

        SearchCriteriaBuilder searchCriteria = instructionDD.find().add(SearchRestrictions.eq("id", productId)).setMaxResults(1);

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
        Long productId = (Long) view.getComponentByReference(PRODUCT_L).getFieldValue();
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

    public Map<Entity, BigDecimal> calculateMaterialQuantitiesInStockArea(final Entity materialsInStockAreas) {
        List<Entity> stockAreas = new ArrayList<Entity>(materialsInStockAreas.getHasManyField(STOCK_AREAS_L));
        Map<Entity, BigDecimal> reportData = new HashMap<Entity, BigDecimal>();

        for (Entity component : stockAreas) {
            Entity stockArea = (Entity) component.getField(STOCK_AREAS_L);

            List<Entity> products = getProductsSeenInStockArea(stockArea.getStringField(NUMBER_L));

            Date forDate = ((Date) materialsInStockAreas.getField("materialFlowForDate"));

            for (Entity product : products) {
                BigDecimal quantity = calculateShouldBeInStockArea(stockArea.getId(), product.getStringField(NUMBER_L), forDate);

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

        Long id = dataDefStockAreas.find().add(SearchRestrictions.eq(NUMBER_L, stockAreaNumber)).uniqueResult().getId();

        DataDefinition dataDefProduct = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER_BASIC,
                MaterialFlowConstants.MODEL_PRODUCT);

        List<Entity> productsFromTransfers = dataDefProduct.find().createAlias("transfer", "t")
                .addOrder(SearchOrders.asc("t.product.id"))
                .setProjection(SearchProjections.distinct(SearchProjections.field("t.product")))
                .add(SearchRestrictions.eqField("t.product.id", "id")).add(SearchRestrictions.eq("t.stockAreasTo.id", id)).list()
                .getEntities();

        List<Entity> productsFromStockCorrections = dataDefProduct.find().createAlias("stockCorrection", "sc")
                .addOrder(SearchOrders.asc("sc.product.id"))
                .setProjection(SearchProjections.distinct(SearchProjections.field("sc.product")))
                .add(SearchRestrictions.eqField("sc.product.id", "id")).add(SearchRestrictions.eq("sc.stockAreas.id", id)).list()
                .getEntities();

        for (Entity product : productsFromStockCorrections) {
            if (!productsFromTransfers.contains(product)) {
                productsFromTransfers.add(product);
            }
        }

        return productsFromTransfers;
    }

    public void fillTransferConsumptionDataFromTransformation(final ViewDefinitionState state,
            final ComponentState componentState, final String[] args) {
        String number = state.getComponentByReference(NUMBER_L).getFieldValue().toString();
        componentState.performEvent(state, "save", new String[0]);

        DataDefinition transferDataDefinition = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_TRANSFER);
        Long id = transferDataDefinition.find().add(SearchRestrictions.eq(NUMBER_L, number)).uniqueResult().getId();
        if (id == null) {
            return;
        }

        Entity transferCopy = transferDataDefinition.get(id);

        Entity transformation = transferCopy.getBelongsToField(TRANSFORMATIONS_CONSUMPTION_L);
        transferCopy.setField(TYPE_L, "Consumption");
        transferCopy.setField(TIME_L, transformation.getField(TIME_L));
        transferCopy.setField(STOCK_AREAS_FROM_L, transformation.getField(STOCK_AREAS_FROM_L));
        transferCopy.setField(STAFF_L, transformation.getField(STAFF_L));

        transferDataDefinition.save(transferCopy);
    }

    public void fillTransferProductionDataFromTransformation(final ViewDefinitionState state,
            final ComponentState componentState, final String[] args) {
        String number = state.getComponentByReference(NUMBER_L).getFieldValue().toString();
        componentState.performEvent(state, "save", new String[0]);

        DataDefinition transferDataDefinition = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_TRANSFER);
        Long id = transferDataDefinition.find().add(SearchRestrictions.eq(NUMBER_L, number)).uniqueResult().getId();

        Entity transferCopy = transferDataDefinition.get(id);

        Entity transformation = transferCopy.getBelongsToField(TRANSFORMATIONS_PRODUCTION_L);
        transferCopy.setField(TYPE_L, "Production");
        transferCopy.setField(TIME_L, transformation.getField(TIME_L));
        transferCopy.setField(STOCK_AREAS_TO_L, transformation.getField(STOCK_AREAS_TO_L));
        transferCopy.setField(STAFF_L, transformation.getField(STAFF_L));

        transferDataDefinition.save(transferCopy);
    }

    public void disableStockAreaFieldForParticularTransferType(final ViewDefinitionState state,
            final ComponentState componentState, final String[] args) {
        disableStockAreaFieldForParticularTransferType(state);
    }

    public void disableStockAreaFieldForParticularTransferType(final ViewDefinitionState state) {
        if (state.getComponentByReference(TYPE_L).getFieldValue() == null) {
            return;
        }

        String type = state.getComponentByReference(TYPE_L).getFieldValue().toString();
        FieldComponent toStockArea = (FieldComponent) state.getComponentByReference(STOCK_AREAS_TO_L);
        FieldComponent fromStockArea = (FieldComponent) state.getComponentByReference(STOCK_AREAS_FROM_L);

        if (type.compareTo("Consumption") == 0) {
            toStockArea.setEnabled(false);
            toStockArea.setFieldValue("");
            fromStockArea.setEnabled(true);
            toStockArea.setRequired(false);
        } else if (type.compareTo("Production") == 0) {
            fromStockArea.setEnabled(false);
            fromStockArea.setFieldValue("");
            toStockArea.setEnabled(true);
            toStockArea.setRequired(true);
        } else {
            toStockArea.setEnabled(true);
            fromStockArea.setEnabled(true);
        }
        toStockArea.requestComponentUpdateState();
        fromStockArea.requestComponentUpdateState();
    }

    public void fillDefaultStockAreaToFieldInTransformations(final ViewDefinitionState state,
            final ComponentState componentState, final String[] args) {
        FieldComponent stockAreaTo = (FieldComponent) state.getComponentByReference(STOCK_AREAS_TO_L);

        if (stockAreaTo.getFieldValue() == null) {
            FieldComponent stockAreaFrom = (FieldComponent) state.getComponentByReference(STOCK_AREAS_FROM_L);
            stockAreaTo.setFieldValue(stockAreaFrom.getFieldValue());
        }
        stockAreaTo.requestComponentUpdateState();
    }

    public boolean validateTransfer(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getField(TRANSFORMATIONS_CONSUMPTION_L) == null && entity.getField(TRANSFORMATIONS_PRODUCTION_L) == null) {
            Entity stockAreasFrom = (Entity) (entity.getField(STOCK_AREAS_FROM_L) == null ? null : entity
                    .getField(STOCK_AREAS_FROM_L));
            Entity stockAreasTo = (Entity) (entity.getField(STOCK_AREAS_TO_L) == null ? null : entity.getField(STOCK_AREAS_TO_L));
            Date date = (Date) (entity.getField(TIME_L) == null ? null : entity.getField(TIME_L));
            String type = (entity.getStringField(TYPE_L) == null ? null : entity.getStringField(TYPE_L));

            boolean validate = true;
            if (stockAreasFrom == null && stockAreasTo == null) {
                entity.addError(dataDefinition.getField(STOCK_AREAS_FROM_L),
                        "materialFlow.validate.global.error.fillAtLeastOneStockAreas");
                entity.addError(dataDefinition.getField(STOCK_AREAS_TO_L),
                        "materialFlow.validate.global.error.fillAtLeastOneStockAreas");
                validate = false;
            }
            if (type == null) {
                entity.addError(dataDefinition.getField(TYPE_L), "materialFlow.validate.global.error.fillType");
                validate = false;
            }
            if (date == null) {
                entity.addError(dataDefinition.getField(TIME_L), "materialFlow.validate.global.error.fillDate");
                validate = false;
            }
            return validate;
        }
        return true;
    }

    public void checkIfTransferHasTransformation(final ViewDefinitionState state) {
        String number = (String) state.getComponentByReference(NUMBER_L).getFieldValue();

        if (number == null) {
            return;
        }

        Entity transfer = dataDefinitionService
                .get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_TRANSFER).find()
                .add(SearchRestrictions.eq(NUMBER_L, number)).uniqueResult();

        if (transfer == null) {
            return;
        }

        if (transfer.getBelongsToField(TRANSFORMATIONS_CONSUMPTION_L) != null
                || transfer.getBelongsToField(TRANSFORMATIONS_PRODUCTION_L) != null) {
            FieldComponent type = (FieldComponent) state.getComponentByReference(TYPE_L);
            FieldComponent date = (FieldComponent) state.getComponentByReference(TIME_L);
            FieldComponent stockAreasTo = (FieldComponent) state.getComponentByReference(STOCK_AREAS_TO_L);
            FieldComponent stockAreasFrom = (FieldComponent) state.getComponentByReference(STOCK_AREAS_FROM_L);
            FieldComponent staff = (FieldComponent) state.getComponentByReference(STAFF_L);
            type.setEnabled(false);
            date.setEnabled(false);
            stockAreasTo.setEnabled(false);
            stockAreasFrom.setEnabled(false);
            staff.setEnabled(false);
        }
    }
}
