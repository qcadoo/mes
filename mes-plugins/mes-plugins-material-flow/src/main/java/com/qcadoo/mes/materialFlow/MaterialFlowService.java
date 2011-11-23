/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class MaterialFlowService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public BigDecimal calculateShouldBeInStockArea(final Long stockAreas, final String product, final String forDate) {

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

        Long stockAreasId = stockAreas;
        Long productId = Long.valueOf(product);

        Entity resultDataCorrection = transferDataCorrection.find().add(SearchRestrictions.eq("stockAreas.id", stockAreas))
                .add(SearchRestrictions.eq("product.id", productId)).addOrder(SearchOrders.desc("stockCorrectionDate"))
                .setMaxResults(1).uniqueResult();

        if (resultDataCorrection != null) {
            lastCorrectionDate = (Date) resultDataCorrection.getField("stockCorrectionDate");
            countProduct = (BigDecimal) resultDataCorrection.getField("found");
        }

        SearchResult resultTo = null;
        SearchResult resultFrom = null;

        if (lastCorrectionDate == null) {
            resultTo = transferTo.find(
                    "where stockAreasTo = '" + stockAreasId + "' and product = '" + product + "' and time <= '" + forDate + "'")
                    .list();

            resultFrom = transferFrom
                    .find("where stockAreasFrom = '" + stockAreasId + "' and product = '" + product + "' and time <= '" + forDate
                            + "'").list();

        } else {
            resultTo = transferTo.find(
                    "where stockAreasTo = '" + stockAreasId + "' and product = '" + product + "' and time <= '" + forDate
                            + "' and time > '" + lastCorrectionDate + "'").list();

            resultFrom = transferFrom.find(
                    "where stockAreasFrom = '" + stockAreasId + "' and product = '" + product + "' and time <= '" + forDate
                            + "' and time > '" + lastCorrectionDate + "'").list();
        }

        for (Entity e : resultTo.getEntities()) {
            BigDecimal quantity = (BigDecimal) e.getField("quantity");
            countProductIn = countProductIn.add(quantity);
        }

        for (Entity e : resultFrom.getEntities()) {
            BigDecimal quantity = (BigDecimal) e.getField("quantity");
            countProductOut = countProductOut.add(quantity);
        }

        if (lastCorrectionDate == null) {
            countProductIn = countProductIn.subtract(countProductOut);
        } else {
            countProductIn = countProductIn.add(countProduct);
            countProductIn = countProductIn.subtract(countProductOut);
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
        FieldComponent stockAreas = (FieldComponent) state.getComponentByReference("stockAreas");
        FieldComponent product = (FieldComponent) state.getComponentByReference("product");
        FieldComponent date = (FieldComponent) state.getComponentByReference("stockCorrectionDate");
        FieldComponent should = (FieldComponent) state.getComponentByReference("shouldBe");

        if (stockAreas != null && product != null && date != null) {
            if (stockAreas.getFieldValue() != null && product.getFieldValue() != null
                    && !date.getFieldValue().toString().equals("")) {
                Long stockAreasNumber = (Long) stockAreas.getFieldValue();
                String productNumber = product.getFieldValue().toString();
                String forDate = date.getFieldValue().toString();

                BigDecimal shouldBe = calculateShouldBeInStockArea(stockAreasNumber, productNumber, forDate);

                if (shouldBe != null && shouldBe != BigDecimal.ZERO) {
                    should.setFieldValue(shouldBe);
                } else {
                    should.setFieldValue(BigDecimal.ZERO);
                }

            }
        }
        should.requestComponentUpdateState();
    }

    public void fillNumberFieldValue(final ViewDefinitionState view) {
        if (view.getComponentByReference("number").getFieldValue() != null) {
            return;
        }
        numberGeneratorService.generateAndInsertNumber(view, MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_TRANSFORMATIONS, "form", "number");
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
            String tableToGenerateNumber) {
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
                String numberValue = product.getField("number")
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

        @SuppressWarnings("deprecation")
        SearchCriteriaBuilder searchCriteria = instructionDD.find().setMaxResults(1).isIdEq(productId);

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

    public Map<Entity, BigDecimal> calculateMaterialQuantitiesInStockArea(Entity materialsInStockAreas) {
        List<Entity> stockAreas = new ArrayList<Entity>(materialsInStockAreas.getHasManyField("stockAreas"));
        Map<Entity, BigDecimal> reportData = new HashMap<Entity, BigDecimal>();

        for (Entity component : stockAreas) {
            Entity stockArea = (Entity) component.getField("stockAreas");
            Long stockAreaNumber = (Long) stockArea.getField("number");

            List<Entity> products = getProductsSeenInStockArea(stockAreaNumber.toString());

            String forDate = ((Date) materialsInStockAreas.getField("materialFlowForDate")).toString();
            for (Entity product : products) {
                BigDecimal quantity = calculateShouldBeInStockArea(stockAreaNumber, product.getStringField("number"), forDate);

                if (reportData.containsKey(product)) {
                    reportData.put(product, reportData.get(product).add(quantity));
                } else {
                    reportData.put(product, quantity);
                }
            }
        }
        return reportData;
    }

    public List<Entity> getProductsSeenInStockArea(String stockAreaNumber) {
        DataDefinition dataDefStockAreas = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_STOCK_AREAS);

        Long id = dataDefStockAreas.find("where number = '" + stockAreaNumber + "'").uniqueResult().getId();

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
        String number = state.getComponentByReference("number").getFieldValue().toString();
        componentState.performEvent(state, "save", new String[0]);

        DataDefinition transferDataDefinition = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_TRANSFER);
        Long id = transferDataDefinition.find("where number = '" + number + "'").uniqueResult().getId();

        Entity transferCopy = transferDataDefinition.get(id);

        Entity transformation = transferCopy.getBelongsToField("transformationsConsumption");
        transferCopy.setField("type", "Consumption");
        transferCopy.setField("time", transformation.getField("time"));
        transferCopy.setField("stockAreasFrom", transformation.getField("stockAreasFrom"));
        transferCopy.setField("staff", transformation.getField("staff"));

        transferDataDefinition.save(transferCopy);
    }

    public void fillTransferProductionDataFromTransformation(final ViewDefinitionState state,
            final ComponentState componentState, final String[] args) {
        String number = state.getComponentByReference("number").getFieldValue().toString();
        componentState.performEvent(state, "save", new String[0]);

        DataDefinition transferDataDefinition = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_TRANSFER);
        Long id = transferDataDefinition.find("where number = '" + number + "'").uniqueResult().getId();

        Entity transferCopy = transferDataDefinition.get(id);

        Entity transformation = transferCopy.getBelongsToField("transformationsProduction");
        transferCopy.setField("type", "Production");
        transferCopy.setField("time", transformation.getField("time"));
        transferCopy.setField("stockAreasTo", transformation.getField("stockAreasTo"));
        transferCopy.setField("staff", transformation.getField("staff"));

        transferDataDefinition.save(transferCopy);
    }

    public void disableStockAreaFieldForParticularTransferType(final ViewDefinitionState state,
            final ComponentState componentState, final String[] args) {
        disableStockAreaFieldForParticularTransferType(state);
    }

    public void disableStockAreaFieldForParticularTransferType(final ViewDefinitionState state) {
        if (state.getComponentByReference("type").getFieldValue() == null) {
            return;
        }

        String type = state.getComponentByReference("type").getFieldValue().toString();
        FieldComponent toStockArea = (FieldComponent) state.getComponentByReference("stockAreasTo");
        FieldComponent fromStockArea = (FieldComponent) state.getComponentByReference("stockAreasFrom");

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
        FieldComponent stockAreaTo = (FieldComponent) state.getComponentByReference("stockAreasTo");

        if (stockAreaTo.getFieldValue() == null) {
            FieldComponent stockAreaFrom = (FieldComponent) state.getComponentByReference("stockAreasFrom");
            stockAreaTo.setFieldValue(stockAreaFrom.getFieldValue());
        }
        stockAreaTo.requestComponentUpdateState();
    }

    public boolean validateTransfer(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getField("transformationsConsumption") == null && entity.getField("transformationsProduction") == null) {
            Entity stockAreasFrom = (Entity) (entity.getField("stockAreasFrom") != null ? entity.getField("stockAreasFrom")
                    : null);
            Entity stockAreasTo = (Entity) (entity.getField("stockAreasTo") != null ? entity.getField("stockAreasTo") : null);
            Date date = (Date) (entity.getField("time") != null ? entity.getField("time") : null);
            String type = (entity.getStringField("type") != null ? entity.getStringField("type") : null);

            boolean validate = true;
            if (stockAreasFrom == null && stockAreasTo == null) {
                entity.addError(dataDefinition.getField("stockAreasFrom"),
                        "materialFlow.validate.global.error.fillAtLeastOneStockAreas");
                entity.addError(dataDefinition.getField("stockAreasTo"),
                        "materialFlow.validate.global.error.fillAtLeastOneStockAreas");
                validate = false;
            }
            if (type == null) {
                entity.addError(dataDefinition.getField("type"), "materialFlow.validate.global.error.fillType");
                validate = false;
            }
            if (date == null) {
                entity.addError(dataDefinition.getField("time"), "materialFlow.validate.global.error.fillDate");
                validate = false;
            }
            return validate;
        }
        return true;
    }

    public void checkIfTransferHasTransformation(final ViewDefinitionState state) {
        String number = (String) state.getComponentByReference("number").getFieldValue();

        if (number == null) {
            return;
        }

        Entity transfer = dataDefinitionService
                .get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_TRANSFER)
                .find("where number = '" + number + "'").uniqueResult();

        if (transfer == null) {
            return;
        }

        if (transfer.getBelongsToField("transformationsConsumption") != null
                || transfer.getBelongsToField("transformationsProduction") != null) {
            FieldComponent type = (FieldComponent) state.getComponentByReference("type");
            FieldComponent date = (FieldComponent) state.getComponentByReference("time");
            FieldComponent stockAreasTo = (FieldComponent) state.getComponentByReference("stockAreasTo");
            FieldComponent stockAreasFrom = (FieldComponent) state.getComponentByReference("stockAreasFrom");
            FieldComponent staff = (FieldComponent) state.getComponentByReference("staff");
            type.setEnabled(false);
            date.setEnabled(false);
            stockAreasTo.setEnabled(false);
            stockAreasFrom.setEnabled(false);
            staff.setEnabled(false);
        }
    }
}
