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
package com.qcadoo.mes.materialFlow;

import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_PRODUCT;
import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static com.qcadoo.mes.materialFlow.constants.LocationFields.EXTERNAL_NUMBER;
import static com.qcadoo.mes.materialFlow.constants.MaterialsInLocationFields.MATERIALS_IN_LOCATION_COMPONENTS;
import static com.qcadoo.mes.materialFlow.constants.MaterialsInLocationFields.MATERIAL_FLOW_FOR_DATE;
import static com.qcadoo.mes.materialFlow.constants.StockCorrectionFields.FOUND;
import static com.qcadoo.mes.materialFlow.constants.StockCorrectionFields.LOCATION;
import static com.qcadoo.mes.materialFlow.constants.StockCorrectionFields.PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.StockCorrectionFields.SHOULD_BE;
import static com.qcadoo.mes.materialFlow.constants.StockCorrectionFields.STOCK_CORRECTION_DATE;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.NUMBER;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.QUANTITY;
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
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class MaterialFlowService {

    private static final String L_FORM = "form";

    private static final String L_ID = "id";

    private static final String L_TRANS = "trans";

    private static final String L_TRANS_PRODUCT = "trans.product";

    private static final String L_TRANS_PRODUCT_ID = "trans.product.id";

    private static final String L_TRANS_LOCATION_TO_ID = "trans.locationTo.id";

    private static final String L_LOC = "loc";

    private static final String L_LOC_PRODUCT = "loc.product";

    private static final String L_LOC_PRODUCT_ID = "loc.product.id";

    private static final String L_LOC_LOCATION_ID = "loc.location.id";

    private static final String L_LOCATION_ID = "location.id";

    private static final String L_LOCATION_FROM_ID = "locationFrom.id";

    private static final String L_LOCATION_TO_ID = "locationTo.id";

    private static final String L_PRODUCT_ID = "product.id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private NumberService numberService;

    public BigDecimal calculateShouldBeInLocation(final Long locationId, final Long productId, final Date forDate) {
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

        Entity resultDataCorrection = transferDataCorrection.find().add(SearchRestrictions.eq(L_LOCATION_ID, locationId))
                .add(SearchRestrictions.eq(L_PRODUCT_ID, productId)).addOrder(SearchOrders.desc(STOCK_CORRECTION_DATE))
                .setMaxResults(1).uniqueResult();

        if (resultDataCorrection != null) {
            lastCorrectionDate = (Date) resultDataCorrection.getField(STOCK_CORRECTION_DATE);
            countProduct = (BigDecimal) resultDataCorrection.getField(FOUND);
        }

        SearchResult resultTo = null;
        SearchResult resultFrom = null;

        if (lastCorrectionDate == null) {
            resultTo = transferTo.find().add(SearchRestrictions.eq(L_LOCATION_TO_ID, locationId))
                    .add(SearchRestrictions.eq(L_PRODUCT_ID, productId)).add(SearchRestrictions.le(TIME, forDate)).list();

            resultFrom = transferFrom.find().add(SearchRestrictions.eq(L_LOCATION_FROM_ID, locationId))
                    .add(SearchRestrictions.eq(L_PRODUCT_ID, productId)).add(SearchRestrictions.le(TIME, forDate)).list();

        } else {
            resultTo = transferTo.find().add(SearchRestrictions.eq(L_LOCATION_TO_ID, locationId))
                    .add(SearchRestrictions.eq(L_PRODUCT_ID, productId)).add(SearchRestrictions.le(TIME, forDate))
                    .add(SearchRestrictions.gt(TIME, lastCorrectionDate)).list();

            resultFrom = transferFrom.find().add(SearchRestrictions.eq(L_LOCATION_FROM_ID, locationId))
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

    public void refreshShouldBeInStockCorrectionDetails(final ViewDefinitionState state, final ComponentState componentState,
            final String[] args) {
        refreshShouldBeInStockCorrectionDetails(state);
    }

    public void refreshShouldBeInStockCorrectionDetails(final ViewDefinitionState state) {
        FieldComponent location = (FieldComponent) state.getComponentByReference(LOCATION);
        FieldComponent product = (FieldComponent) state.getComponentByReference(PRODUCT);
        FieldComponent date = (FieldComponent) state.getComponentByReference(STOCK_CORRECTION_DATE);
        FieldComponent should = (FieldComponent) state.getComponentByReference(SHOULD_BE);

        if ((location != null) && (product != null) && (date != null) && (location.getFieldValue() != null)
                && (product.getFieldValue() != null) && !date.getFieldValue().toString().equals("")) {
            Long locationNumber = (Long) location.getFieldValue();
            Long productNumber = (Long) product.getFieldValue();

            Date forDate = DateUtils.parseDate(date.getFieldValue());

            BigDecimal shouldBe = calculateShouldBeInLocation(locationNumber, productNumber, forDate);

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
            final String model) {
        if (!(componentState instanceof FieldComponent)) {
            throw new IllegalStateException("component is not FieldComponentState");
        }
        FieldComponent number = (FieldComponent) state.getComponentByReference(NUMBER);
        FieldComponent productState = (FieldComponent) componentState;

        if (!numberGeneratorService.checkIfShouldInsertNumber(state, L_FORM, NUMBER)) {
            return;
        }
        if (productState.getFieldValue() != null) {
            Entity product = getProductById((Long) productState.getFieldValue());
            number.setFieldValue(generateNumberFromProduct(product, model));
        }
        number.requestComponentUpdateState();
    }

    public String generateNumberFromProduct(final Entity product, final String model) {
        String number = "";

        if (product != null) {
            String generatedNumber = numberGeneratorService.generateNumber(MaterialFlowConstants.PLUGIN_IDENTIFIER, model, 3);

            String prefix = product.getStringField(NUMBER);

            number = prefix + "-" + generatedNumber;

            Long parsedNumber = Long.parseLong(generatedNumber);

            while (numberAlreadyExist(model, number)) {
                parsedNumber++;

                number = prefix + "-" + String.format("%03d", parsedNumber);
            }
        }

        return number;
    }

    public void fillUnitFieldValues(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        fillUnitFieldValues(view);
    }

    public void fillUnitFieldValues(final ViewDefinitionState view) {
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

    public void fillCurrencyFieldValues(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        fillCurrencyFieldValues(view);
    }

    public void fillCurrencyFieldValues(final ViewDefinitionState view) {
        FieldComponent currencyField = null;
        String currency = currencyService.getCurrencyAlphabeticCode();

        for (String referenceName : Sets.newHashSet("priceCurrency")) {
            currencyField = (FieldComponent) view.getComponentByReference(referenceName);
            if (currencyField == null) {
                continue;
            }
            currencyField.setFieldValue(currency);
            currencyField.requestComponentUpdateState();
        }
    }

    public Map<Entity, BigDecimal> calculateMaterialQuantitiesInLocation(final Entity materialsInLocation) {
        List<Entity> materialsInLocationComponents = new ArrayList<Entity>(
                materialsInLocation.getHasManyField(MATERIALS_IN_LOCATION_COMPONENTS));
        Map<Entity, BigDecimal> reportData = new HashMap<Entity, BigDecimal>();

        for (Entity materialsInLocationComponent : materialsInLocationComponents) {
            Entity location = materialsInLocationComponent.getBelongsToField(LOCATION);

            List<Entity> products = getProductsSeenInLocation(location.getStringField(NUMBER));

            Date forDate = ((Date) materialsInLocation.getField(MATERIAL_FLOW_FOR_DATE));

            for (Entity product : products) {
                BigDecimal quantity = calculateShouldBeInLocation(location.getId(), product.getId(), forDate);

                if (reportData.containsKey(product)) {
                    reportData.put(product, reportData.get(product).add(quantity, numberService.getMathContext()));
                } else {
                    reportData.put(product, quantity);
                }
            }
        }
        return reportData;
    }

    public List<Entity> getProductsSeenInLocation(final String locationNumber) {
        DataDefinition dataDefLocation = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_LOCATION);

        Long id = dataDefLocation.find().add(SearchRestrictions.eq(NUMBER, locationNumber)).uniqueResult().getId();

        DataDefinition dataDefProduct = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);

        List<Entity> productsFromTransfers = dataDefProduct.find().createAlias(MaterialFlowConstants.MODEL_TRANSFER, L_TRANS)
                .addOrder(SearchOrders.asc(L_TRANS_PRODUCT_ID))
                .setProjection(SearchProjections.distinct(SearchProjections.field(L_TRANS_PRODUCT)))
                .add(SearchRestrictions.eqField(L_TRANS_PRODUCT_ID, L_ID)).add(SearchRestrictions.eq(L_TRANS_LOCATION_TO_ID, id))
                .list().getEntities();

        List<Entity> productsFromStockCorrections = dataDefProduct.find()
                .createAlias(MaterialFlowConstants.MODEL_STOCK_CORRECTION, L_LOC).addOrder(SearchOrders.asc(L_LOC_PRODUCT_ID))
                .setProjection(SearchProjections.distinct(SearchProjections.field(L_LOC_PRODUCT)))
                .add(SearchRestrictions.eqField(L_LOC_PRODUCT_ID, L_ID)).add(SearchRestrictions.eq(L_LOC_LOCATION_ID, id)).list()
                .getEntities();

        for (Entity product : productsFromStockCorrections) {
            if (!productsFromTransfers.contains(product)) {
                productsFromTransfers.add(product);
            }
        }

        return productsFromTransfers;
    }

    public void disableLocationFieldForParticularTransferType(final ViewDefinitionState view,
            final ComponentState componentState, final String[] args) {
        disableLocationFieldForParticularTransferType(view);
    }

    public void disableLocationFieldForParticularTransferType(final ViewDefinitionState view) {
        if (view.getComponentByReference(TYPE).getFieldValue() == null) {
            return;
        }

        String type = view.getComponentByReference(TYPE).getFieldValue().toString();
        FieldComponent locationTo = (FieldComponent) view.getComponentByReference(LOCATION_TO);
        FieldComponent locationFrom = (FieldComponent) view.getComponentByReference(LOCATION_FROM);

        if (CONSUMPTION.getStringValue().equals(type)) {
            locationFrom.setEnabled(true);
            locationFrom.setRequired(true);
            locationTo.setRequired(false);
            locationTo.setEnabled(false);
            locationTo.setFieldValue("");
        } else if (PRODUCTION.getStringValue().equals(type)) {
            locationTo.setEnabled(true);
            locationTo.setRequired(true);
            locationFrom.setRequired(false);
            locationFrom.setEnabled(false);
            locationFrom.setFieldValue("");
        } else {
            locationTo.setEnabled(true);
            locationTo.setRequired(true);
            locationFrom.setRequired(true);
            locationFrom.setEnabled(true);
        }

        locationTo.requestComponentUpdateState();
        locationFrom.requestComponentUpdateState();
    }

    public void fillDefaultLocationToFieldInTransformations(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FieldComponent locationTo = (FieldComponent) view.getComponentByReference(LOCATION_TO);

        if (locationTo.getFieldValue() == null) {
            FieldComponent locationFrom = (FieldComponent) view.getComponentByReference(LOCATION_FROM);
            locationTo.setFieldValue(locationFrom.getFieldValue());
        }
        locationTo.requestComponentUpdateState();
    }

    public boolean numberAlreadyExist(final String model, final String number) {
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, model).find()
                .add(SearchRestrictions.eq(NUMBER, number)).setMaxResults(1).uniqueResult() != null;
    }

    public List<Entity> getLocationsFromDB() {
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION).find()
                .list().getEntities();
    }

    private Entity getProductById(final Long productId) {
        if (productId == null) {
            return null;
        }

        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(productId);
    }

    public Entity getLocationById(final Long locationId) {
        if (locationId == null) {
            return null;
        }

        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION).get(
                locationId);
    }

    public Entity getLocationByName(final String name) {
        if (name == null) {
            return null;
        }
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION).find()
                .add(SearchRestrictions.eq(LocationFields.NAME, name)).setMaxResults(1).uniqueResult();
    }

    public Entity getStaffById(final Long staffId) {
        if (staffId == null) {
            return null;
        }

        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_STAFF).get(staffId);
    }

    public boolean checkIfLocationHasExternalNumber(final Entity location) {
        if ((location == null) || (location.getStringField(EXTERNAL_NUMBER) == null)) {
            return false;
        }

        return true;
    }

    public void checkIfLocationHasExternalNumber(final ViewDefinitionState view, final String lookupName) {
        LookupComponent locationLookup = (LookupComponent) view.getComponentByReference(lookupName);
        Entity location = locationLookup.getEntity();

        if (checkIfLocationHasExternalNumber(location)) {
            locationLookup.addMessage("materialFlow.validate.global.error.locationHasExternalNumber",
                    ComponentState.MessageType.FAILURE);
        }
    }

}
