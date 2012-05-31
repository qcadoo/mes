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
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public class MaterialFlowService {

    private static final String L_TRANSFORMATIONS_CONSUMPTION = "transformationsConsumption";

    private static final String L_STOCK_AREAS_TO = "stockAreasTo";

    private static final String L_TRANSFORMATIONS_PRODUCTION = "transformationsProduction";

    private static final String L_TYPE = "type";

    private static final String L_STAFF = "staff";

    private static final String L_STOCK_AREAS_FROM = "stockAreasFrom";

    private static final String L_SHOULD_BE = "shouldBe";

    private static final String L_STOCK_CORRECTION_DATE = "stockCorrectionDate";

    private static final String L_PRODUCT = "product";

    private static final String L_STOCK_AREAS = "stockAreas";

    private static final String L_NUMBER = "number";

    private static final String L_TIME = "time";

    private static final String L_QUANTITY = "quantity";

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

        Entity resultDataCorrection = transferDataCorrection.find().add(SearchRestrictions.eq("stockAreas.id", stockAreasId))
                .add(SearchRestrictions.eq(L_PRODUCT_ID, productId)).addOrder(SearchOrders.desc(L_STOCK_CORRECTION_DATE))
                .setMaxResults(1).uniqueResult();

        if (resultDataCorrection != null) {
            lastCorrectionDate = (Date) resultDataCorrection.getField(L_STOCK_CORRECTION_DATE);
            countProduct = (BigDecimal) resultDataCorrection.getField("found");
        }

        SearchResult resultTo = null;
        SearchResult resultFrom = null;

        if (lastCorrectionDate == null) {

            resultTo = transferTo.find().add(SearchRestrictions.eq("stockAreasTo.id", stockAreasId))
                    .add(SearchRestrictions.eq(L_PRODUCT_ID, productId)).add(SearchRestrictions.le(L_TIME, forDate)).list();

            resultFrom = transferFrom.find().add(SearchRestrictions.eq("stockAreasFrom.id", stockAreasId))
                    .add(SearchRestrictions.eq(L_PRODUCT_ID, productId)).add(SearchRestrictions.le(L_TIME, forDate)).list();

        } else {
            resultTo = transferTo.find().add(SearchRestrictions.eq("stockAreasTo.id", stockAreasId))
                    .add(SearchRestrictions.eq(L_PRODUCT_ID, productId)).add(SearchRestrictions.le(L_TIME, forDate))
                    .add(SearchRestrictions.gt(L_TIME, lastCorrectionDate)).list();

            resultFrom = transferFrom.find().add(SearchRestrictions.eq("stockAreasFrom.id", stockAreasId))
                    .add(SearchRestrictions.eq(L_PRODUCT_ID, productId)).add(SearchRestrictions.le(L_TIME, forDate))
                    .add(SearchRestrictions.gt(L_TIME, lastCorrectionDate)).list();
        }

        for (Entity e : resultTo.getEntities()) {
            BigDecimal quantity = (BigDecimal) e.getField(L_QUANTITY);
            countProductIn = countProductIn.add(quantity, numberService.getMathContext());
        }

        for (Entity e : resultFrom.getEntities()) {
            BigDecimal quantity = (BigDecimal) e.getField(L_QUANTITY);
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
        FieldComponent stockAreas = (FieldComponent) state.getComponentByReference(L_STOCK_AREAS);
        FieldComponent product = (FieldComponent) state.getComponentByReference(L_PRODUCT);
        FieldComponent date = (FieldComponent) state.getComponentByReference(L_STOCK_CORRECTION_DATE);
        FieldComponent should = (FieldComponent) state.getComponentByReference(L_SHOULD_BE);

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
        if (view.getComponentByReference(L_NUMBER).getFieldValue() != null) {
            return;
        }
        numberGeneratorService.generateAndInsertNumber(view, MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_TRANSFORMATIONS, "form", L_NUMBER);
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
        FieldComponent number = (FieldComponent) state.getComponentByReference(L_NUMBER);
        FieldComponent productState = (FieldComponent) componentState;

        if (!numberGeneratorService.checkIfShouldInsertNumber(state, "form", L_NUMBER)) {
            return;
        }
        if (productState.getFieldValue() != null) {
            Entity product = getAreaById((Long) productState.getFieldValue());
            if (product != null) {
                String numberValue = product.getField(L_NUMBER)
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
        Long productId = (Long) view.getComponentByReference(L_PRODUCT).getFieldValue();
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
        List<Entity> stockAreas = new ArrayList<Entity>(materialsInStockAreas.getHasManyField(L_STOCK_AREAS));
        Map<Entity, BigDecimal> reportData = new HashMap<Entity, BigDecimal>();

        for (Entity component : stockAreas) {
            Entity stockArea = component.getBelongsToField(L_STOCK_AREAS);

            List<Entity> products = getProductsSeenInStockArea(stockArea.getStringField(L_NUMBER));

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

        Long id = dataDefStockAreas.find().add(SearchRestrictions.eq(L_NUMBER, stockAreaNumber)).uniqueResult().getId();

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
        String number = state.getComponentByReference(L_NUMBER).getFieldValue().toString();
        componentState.performEvent(state, "save", new String[0]);

        DataDefinition transferDataDefinition = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_TRANSFER);
        Long id = transferDataDefinition.find().add(SearchRestrictions.eq(L_NUMBER, number)).uniqueResult().getId();
        if (id == null) {
            return;
        }

        Entity transferCopy = transferDataDefinition.get(id);

        Entity transformation = transferCopy.getBelongsToField(L_TRANSFORMATIONS_CONSUMPTION);
        transferCopy.setField(L_TYPE, "Consumption");
        transferCopy.setField(L_TIME, transformation.getField(L_TIME));
        transferCopy.setField(L_STOCK_AREAS_FROM, transformation.getField(L_STOCK_AREAS_FROM));
        transferCopy.setField(L_STAFF, transformation.getField(L_STAFF));

        transferDataDefinition.save(transferCopy);
    }

    public void fillTransferProductionDataFromTransformation(final ViewDefinitionState state,
            final ComponentState componentState, final String[] args) {
        String number = state.getComponentByReference(L_NUMBER).getFieldValue().toString();
        componentState.performEvent(state, "save", new String[0]);

        DataDefinition transferDataDefinition = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_TRANSFER);
        Long id = transferDataDefinition.find().add(SearchRestrictions.eq(L_NUMBER, number)).uniqueResult().getId();

        Entity transferCopy = transferDataDefinition.get(id);

        Entity transformation = transferCopy.getBelongsToField(L_TRANSFORMATIONS_PRODUCTION);
        transferCopy.setField(L_TYPE, "Production");
        transferCopy.setField(L_TIME, transformation.getField(L_TIME));
        transferCopy.setField(L_STOCK_AREAS_TO, transformation.getField(L_STOCK_AREAS_TO));
        transferCopy.setField(L_STAFF, transformation.getField(L_STAFF));

        transferDataDefinition.save(transferCopy);
    }

    public void disableStockAreaFieldForParticularTransferType(final ViewDefinitionState state,
            final ComponentState componentState, final String[] args) {
        disableStockAreaFieldForParticularTransferType(state);
    }

    public void disableStockAreaFieldForParticularTransferType(final ViewDefinitionState state) {
        if (state.getComponentByReference(L_TYPE).getFieldValue() == null) {
            return;
        }

        String type = state.getComponentByReference(L_TYPE).getFieldValue().toString();
        FieldComponent toStockArea = (FieldComponent) state.getComponentByReference(L_STOCK_AREAS_TO);
        FieldComponent fromStockArea = (FieldComponent) state.getComponentByReference(L_STOCK_AREAS_FROM);

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
        FieldComponent stockAreaTo = (FieldComponent) state.getComponentByReference(L_STOCK_AREAS_TO);

        if (stockAreaTo.getFieldValue() == null) {
            FieldComponent stockAreaFrom = (FieldComponent) state.getComponentByReference(L_STOCK_AREAS_FROM);
            stockAreaTo.setFieldValue(stockAreaFrom.getFieldValue());
        }
        stockAreaTo.requestComponentUpdateState();
    }

    public boolean validateTransfer(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getField(L_TRANSFORMATIONS_CONSUMPTION) == null && entity.getField(L_TRANSFORMATIONS_PRODUCTION) == null) {
            Entity stockAreasFrom = (Entity) (entity.getField(L_STOCK_AREAS_FROM) == null ? null : entity
                    .getField(L_STOCK_AREAS_FROM));
            Entity stockAreasTo = (Entity) (entity.getField(L_STOCK_AREAS_TO) == null ? null : entity.getField(L_STOCK_AREAS_TO));
            Date date = (Date) (entity.getField(L_TIME) == null ? null : entity.getField(L_TIME));
            String type = (entity.getStringField(L_TYPE) == null ? null : entity.getStringField(L_TYPE));

            boolean validate = true;
            if (stockAreasFrom == null && stockAreasTo == null) {
                entity.addError(dataDefinition.getField(L_STOCK_AREAS_FROM),
                        "materialFlow.validate.global.error.fillAtLeastOneStockAreas");
                entity.addError(dataDefinition.getField(L_STOCK_AREAS_TO),
                        "materialFlow.validate.global.error.fillAtLeastOneStockAreas");
                validate = false;
            }
            if (type == null) {
                entity.addError(dataDefinition.getField(L_TYPE), "materialFlow.validate.global.error.fillType");
                validate = false;
            }
            if (date == null) {
                entity.addError(dataDefinition.getField(L_TIME), "materialFlow.validate.global.error.fillDate");
                validate = false;
            }
            return validate;
        }
        return true;
    }

    public void checkIfTransferHasTransformation(final ViewDefinitionState state) {
        String number = (String) state.getComponentByReference(L_NUMBER).getFieldValue();

        if (number == null) {
            return;
        }

        Entity transfer = dataDefinitionService
                .get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_TRANSFER).find()
                .add(SearchRestrictions.eq(L_NUMBER, number)).uniqueResult();

        if (transfer == null) {
            return;
        }

        if (transfer.getBelongsToField(L_TRANSFORMATIONS_CONSUMPTION) != null
                || transfer.getBelongsToField(L_TRANSFORMATIONS_PRODUCTION) != null) {
            FieldComponent type = (FieldComponent) state.getComponentByReference(L_TYPE);
            FieldComponent date = (FieldComponent) state.getComponentByReference(L_TIME);
            FieldComponent stockAreasTo = (FieldComponent) state.getComponentByReference(L_STOCK_AREAS_TO);
            FieldComponent stockAreasFrom = (FieldComponent) state.getComponentByReference(L_STOCK_AREAS_FROM);
            FieldComponent staff = (FieldComponent) state.getComponentByReference(L_STAFF);
            type.setEnabled(false);
            date.setEnabled(false);
            stockAreasTo.setEnabled(false);
            stockAreasFrom.setEnabled(false);
            staff.setEnabled(false);
        }
    }

    public void fillUnitsInADL(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view.getComponentByReference("products");
        List<FormComponent> formComponents = adlc.getFormComponents();

        for (FormComponent formComponent : formComponents) {
            Entity productQuantity = formComponent.getEntity();
            formComponent.setEntity(productQuantity);
        }

        // if (productId == null) {
        // return;
        // }
        // Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCT).get(productId);
        // FieldComponent unitField = null;
        // String unit = product.getField("unit").toString();
        // for (String referenceName : Sets.newHashSet("quantityUNIT", "shouldBeUNIT", "foundUNIT")) {
        // unitField = (FieldComponent) view.getComponentByReference(referenceName);
        // if (unitField == null) {
        // continue;
        // }
        // unitField.setFieldValue(unit);
        // unitField.requestComponentUpdateState();
        // }
    }
}
