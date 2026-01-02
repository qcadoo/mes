package com.qcadoo.mes.materialFlowResources.listeners;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.CalculationQuantityService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.CurrencyFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.mes.materialFlowResources.print.StocktakingReportService;
import com.qcadoo.mes.materialFlowResources.states.StocktakingServiceMarker;
import com.qcadoo.mes.materialFlowResources.states.constants.StocktakingStateStringValues;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StocktakingDetailsListeners {

    @Autowired
    private StocktakingReportService reportService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private StateExecutorService stateExecutorService;

    @Autowired
    private CalculationQuantityService calculationQuantityService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private NumberService numberService;

    public void changeState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        stateExecutorService.changeState(StocktakingServiceMarker.class, view, args);
    }

    public void copyFromStock(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity entity = form.getPersistedEntityWithIncludedFormValues();
        for (Entity position : entity.getHasManyField(StocktakingFields.POSITIONS)) {
            BigDecimal stock = position.getDecimalField(StocktakingPositionFields.STOCK);
            position.setField(StocktakingPositionFields.QUANTITY, stock);
            Entity product = position.getBelongsToField(StocktakingPositionFields.PRODUCT);
            BigDecimal positionConversion = position.getDecimalField(StocktakingPositionFields.CONVERSION);
            String unit = product.getStringField(ProductFields.UNIT);
            String additionalUnit = Optional.ofNullable(product.getStringField(ProductFields.ADDITIONAL_UNIT)).orElse(
                    unit);
            if (!unit.equals(additionalUnit)) {
                BigDecimal conversion = materialFlowResourcesService.getConversion(product, unit, additionalUnit, positionConversion);
                BigDecimal stockInAdditionalQuantity = calculationQuantityService.calculateAdditionalQuantity(stock,
                        conversion, additionalUnit);
                position.setField(StocktakingPositionFields.QUANTITY_IN_ADDITIONAL_UNIT, stockInAdditionalQuantity);
            } else {
                position.setField(StocktakingPositionFields.QUANTITY_IN_ADDITIONAL_UNIT, stock);
            }
            position.getDataDefinition().fastSave(position);
        }
    }

    public void settle(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity entity = form.getPersistedEntityWithIncludedFormValues();
        List<Entity> differences = new ArrayList<>();
        DataDefinition differenceDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_STOCKTAKING_DIFFERENCE);
        DataDefinition positionDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_STOCKTAKING_POSITION);
        for (Entity position : entity.getHasManyField(StocktakingFields.POSITIONS)) {
            BigDecimal positionQuantity = position.getDecimalField(StocktakingPositionFields.QUANTITY);
            if (positionQuantity == null) {
                positionQuantity = BigDecimal.ZERO;
                position.setField(StocktakingPositionFields.QUANTITY, positionQuantity);
                position.setField(StocktakingPositionFields.QUANTITY_IN_ADDITIONAL_UNIT, positionQuantity);
            }
            position = positionDD.save(position);
            if (!position.isValid()) {
                position.getGlobalErrors().forEach(view::addMessage);
                return;
            }
            BigDecimal positionStock = position.getDecimalField(StocktakingPositionFields.STOCK);
            if (positionStock.compareTo(positionQuantity) != 0) {
                Entity product = position.getBelongsToField(StocktakingPositionFields.PRODUCT);
                BigDecimal positionConversion = position.getDecimalField(StocktakingPositionFields.CONVERSION);
                Entity differenceEntity = differenceDD.create();
                differenceEntity.setField(StocktakingDifferenceFields.STORAGE_LOCATION, position.getBelongsToField(StocktakingPositionFields.STORAGE_LOCATION));
                differenceEntity.setField(StocktakingDifferenceFields.PALLET_NUMBER, position.getBelongsToField(StocktakingPositionFields.PALLET_NUMBER));
                differenceEntity.setField(StocktakingDifferenceFields.TYPE_OF_LOAD_UNIT, position.getBelongsToField(StocktakingPositionFields.TYPE_OF_LOAD_UNIT));
                differenceEntity.setField(StocktakingDifferenceFields.PRODUCT, product);
                differenceEntity.setField(StocktakingDifferenceFields.BATCH, position.getBelongsToField(StocktakingPositionFields.BATCH));
                differenceEntity.setField(StocktakingDifferenceFields.EXPIRATION_DATE, position.getDateField(StocktakingPositionFields.EXPIRATION_DATE));
                differenceEntity.setField(StocktakingDifferenceFields.CONVERSION, positionConversion);
                BigDecimal difference = positionQuantity.subtract(positionStock);
                differenceEntity.setField(StocktakingDifferenceFields.QUANTITY, difference);
                String unit = product.getStringField(ProductFields.UNIT);
                String additionalUnit = Optional.ofNullable(product.getStringField(ProductFields.ADDITIONAL_UNIT)).orElse(
                        unit);
                if (!unit.equals(additionalUnit)) {
                    BigDecimal conversion = materialFlowResourcesService.getConversion(product, unit, additionalUnit, positionConversion);
                    BigDecimal differenceInAdditionalQuantity = calculationQuantityService.calculateAdditionalQuantity(difference,
                            conversion, additionalUnit);
                    differenceEntity.setField(StocktakingDifferenceFields.QUANTITY_IN_ADDITIONAL_UNIT, differenceInAdditionalQuantity);
                } else {
                    differenceEntity.setField(StocktakingDifferenceFields.QUANTITY_IN_ADDITIONAL_UNIT, difference);
                }
                if (difference.compareTo(BigDecimal.ZERO) > 0) {
                    differenceEntity.setField(StocktakingDifferenceFields.TYPE, StocktakingDifferenceType.SURPLUS.getStringValue());
                } else {
                    differenceEntity.setField(StocktakingDifferenceFields.TYPE, StocktakingDifferenceType.SHORTAGE.getStringValue());
                }
                differences.add(differenceEntity);
            }
        }
        entity.setField(StocktakingFields.DIFFERENCES, differences);
        entity.getDataDefinition().save(entity);
        if (entity.getStringField(StocktakingFields.STATE).equals(StocktakingStateStringValues.IN_PROGRESS)) {
            stateExecutorService.changeState(StocktakingServiceMarker.class, view, args);
        }
    }

    public void print(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        reportService.printReport(view, state);
    }

    public void fillPrices(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity documentPositionParameters = parameterService.getParameter()
                .getBelongsToField(ParameterFieldsMFR.DOCUMENT_POSITION_PARAMETERS);

        String stocktakingMaterialCostsUsed = documentPositionParameters.getStringField(ParameterFieldsMFR.STOCKTAKING_MATERIAL_COSTS_USED);
        boolean stocktakingUseNominalCostPriceNotSpecified = documentPositionParameters.getBooleanField(ParameterFieldsMFR.STOCKTAKING_USE_NOMINAL_COST_PRICE_NOT_SPECIFIED);

        GridComponent grid = (GridComponent) view.getComponentByReference(StocktakingFields.DIFFERENCES);
        DataDefinition differenceDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_STOCKTAKING_DIFFERENCE);
        List<Entity> differences = getSelectedStocktakingDifferences(grid, differenceDD);
        List<Entity> productsToMessage = Lists.newArrayList();
        differences.forEach(difference -> {
            Entity product = difference.getBelongsToField(StocktakingDifferenceFields.PRODUCT);
            BigDecimal pricePerUnit = calculateProductCostPerUnit(product, stocktakingMaterialCostsUsed, stocktakingUseNominalCostPriceNotSpecified, productsToMessage);
            difference.setField(StocktakingDifferenceFields.PRICE, pricePerUnit);

            differenceDD.save(difference);
        });

        if (!productsToMessage.isEmpty()) {
            view.addMessage("materialFlowResources.stocktakingDifferences.differentCurrencies", ComponentState.MessageType.INFO, false, productsToMessage.stream()
                    .map(productToMessage -> productToMessage.getStringField(ProductFields.NUMBER)).collect(Collectors.joining(", ")));
        }
        grid.reloadEntities();
    }

    private List<Entity> getSelectedStocktakingDifferences(final GridComponent stocktakingDifferencesGrid, DataDefinition differenceDD) {
        List<Entity> result = Lists.newArrayList();

        Set<Long> ids = stocktakingDifferencesGrid.getSelectedEntitiesIds();

        if (Objects.nonNull(ids) && !ids.isEmpty()) {

            final SearchCriteriaBuilder searchCriteria = differenceDD.find();

            searchCriteria.add(SearchRestrictions.in("id", ids));

            result = searchCriteria.list().getEntities();
        }

        return result;
    }

    private BigDecimal calculateProductCostPerUnit(final Entity product, final String materialCostsUsed,
                                                   final boolean useNominalCostPriceNotSpecified, List<Entity> productsToMessage) {
        Entity materialCurrency = null;
        BigDecimal cost = BigDecimalUtils
                .convertNullToZero(product.getField(ProductsCostFields.forMode(materialCostsUsed).getStrValue()));
        if (useNominalCostPriceNotSpecified && BigDecimalUtils.valueEquals(cost, BigDecimal.ZERO)) {
            cost = BigDecimalUtils.convertNullToZero(product.getField(ProductsCostFields.NOMINAL.getStrValue()));
            materialCurrency = product.getBelongsToField(ProductFieldsCNFP.NOMINAL_COST_CURRENCY);
        } else if (ProductsCostFields.NOMINAL.getMode().equals(materialCostsUsed)) {
            materialCurrency = product.getBelongsToField(ProductFieldsCNFP.NOMINAL_COST_CURRENCY);
        } else if (ProductsCostFields.LAST_PURCHASE.getMode().equals(materialCostsUsed)) {
            materialCurrency = product.getBelongsToField(ProductFieldsCNFP.LAST_PURCHASE_COST_CURRENCY);
        }
        if (materialCurrency == null) {
            materialCurrency = currencyService.getCurrentCurrency();
        }

        String currency = currencyService.getCurrencyAlphabeticCode();
        if (!currency.isEmpty() && materialCurrency != null && !currency.equals(materialCurrency.getStringField(CurrencyFields.ALPHABETIC_CODE))) {
            if (CurrencyService.PLN.equals(currency)) {
                cost = currencyService.getConvertedValue(cost, materialCurrency);
            } else if (CurrencyService.PLN.equals(materialCurrency.getStringField(CurrencyFields.ALPHABETIC_CODE))) {
                cost = currencyService.getRevertedValue(cost, currencyService.getCurrentCurrency());
            } else {
                productsToMessage.add(product);
                cost = BigDecimal.ZERO;
            }
        }

        BigDecimal costForNumber = BigDecimalUtils.convertNullToOne(product.getDecimalField("costForNumber"));
        if (BigDecimalUtils.valueEquals(costForNumber, BigDecimal.ZERO)) {
            costForNumber = BigDecimal.ONE;
        }

        return cost.divide(costForNumber, numberService.getMathContext());
    }
}
