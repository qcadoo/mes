package com.qcadoo.mes.materialFlowResources.listeners;

import com.qcadoo.mes.basic.CalculationQuantityService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.mes.materialFlowResources.print.StocktakingReportService;
import com.qcadoo.mes.materialFlowResources.states.StocktakingServiceMarker;
import com.qcadoo.mes.materialFlowResources.states.constants.StocktakingStateStringValues;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
}
