package com.qcadoo.mes.masterOrders.listeners;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.SalesVolumeFields;
import com.qcadoo.mes.masterOrders.constants.SalesVolumeMultiFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SalesVolumeAddMultiListeners {

    private static final String L_GENERATED = "generated";

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final void createSalesVolumes(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent salesVolumeMultiForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent dailySalesVolumeField = (FieldComponent) view.getComponentByReference(SalesVolumeMultiFields.DAILY_SALES_VOLUME);
        FieldComponent optimalStockField = (FieldComponent) view.getComponentByReference(SalesVolumeMultiFields.OPTIMAL_STOCK);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference(L_GENERATED);

        Entity salesVolumeMulti = salesVolumeMultiForm.getPersistedEntityWithIncludedFormValues();
        BigDecimal dailySalesVolume = getBigDecimal(dailySalesVolumeField, true);
        BigDecimal optimalStock = getBigDecimal(optimalStockField, false);
        List<Entity> products = salesVolumeMulti.getHasManyField(SalesVolumeMultiFields.PRODUCTS);

        if (dailySalesVolumeField.isHasError() || optimalStockField.isHasError()) {
            return;
        }

        if (products.isEmpty()) {
            view.addMessage("masterOrders.salesVolumeAddMulti.products.empty", ComponentState.MessageType.INFO);

            return;
        }

        Set<String> productNumbers = getSalesVolumesForProducts(products);

        if (!productNumbers.isEmpty()) {
            view.addMessage("masterOrders.salesVolumeAddMulti.products.exists", ComponentState.MessageType.INFO, String.join(", ", productNumbers));

            return;
        }

        List<Entity> salesVolumes = createSalesVolumes(products, dailySalesVolume, optimalStock);

        boolean isValid = true;

        for (Entity salesVolume : salesVolumes) {
            for (ErrorMessage errorMessage : salesVolume.getGlobalErrors()) {
                if (!errorMessage.getMessage().equals("qcadooView.validate.global.error.custom")) {
                    view.addMessage(errorMessage.getMessage(), ComponentState.MessageType.FAILURE, errorMessage.getVars());

                    isValid = false;
                }
            }

            for (ErrorMessage error : salesVolume.getErrors().values()) {
                view.addMessage(error.getMessage(), ComponentState.MessageType.FAILURE, error.getVars());

                isValid = false;
            }
        }

        if (isValid) {
            view.addMessage("masterOrders.salesVolumeAddMulti.createSalesVolumes.success", ComponentState.MessageType.SUCCESS);

            generatedCheckBox.setChecked(true);

            salesVolumeMulti.getDataDefinition().delete(salesVolumeMulti.getId());
        } else {
            view.addMessage("masterOrders.salesVolumeAddMulti.createSalesVolumes.failure", ComponentState.MessageType.FAILURE);
        }
    }

    private BigDecimal getBigDecimal(final FieldComponent quantityField, boolean isRequired) {
        BigDecimal quantity;

        String quantityString = (String) quantityField.getFieldValue();

        Either<Exception, Optional<BigDecimal>> eitherQuantity = BigDecimalUtils.tryParseAndIgnoreSeparator(
                quantityString, LocaleContextHolder.getLocale());

        if (eitherQuantity.isRight()) {
            if (eitherQuantity.getRight().isPresent()) {
                quantity = eitherQuantity.getRight().get();

                int scale = 5;
                int valueScale = quantity.scale();

                if (valueScale > scale) {
                    quantityField.addMessage("qcadooView.validate.field.error.invalidScale.max", ComponentState.MessageType.FAILURE, String.valueOf(scale));

                    return null;
                }

                if (BigDecimal.ZERO.compareTo(quantity) >= 0) {
                    quantityField.addMessage("qcadooView.validate.field.error.outOfRange.toSmall", ComponentState.MessageType.FAILURE);

                    return null;
                }
            } else {
                if (isRequired) {
                    quantityField.addMessage("qcadooView.validate.field.error.missing", ComponentState.MessageType.FAILURE);
                }

                return null;
            }
        } else {
            quantityField.addMessage("qcadooView.validate.field.error.invalidNumericFormat", ComponentState.MessageType.FAILURE);

            return null;
        }

        return quantity;
    }

    private Set<String> getSalesVolumesForProducts(final List<Entity> products) {
        Set<Long> productIds = products.stream().map(Entity::getId).collect(Collectors.toSet());

        List<Entity> salesVolumes = getSalesVolumesForProductIds(productIds);

        return salesVolumes.stream().map(salesVolume -> salesVolume.getBelongsToField(SalesVolumeFields.PRODUCT).getStringField(ProductFields.NUMBER)).collect(Collectors.toSet());
    }

    private List<Entity> getSalesVolumesForProductIds(final Set<Long> productIds) {
        return getSalesVolumeDD().find()
                .createAlias(SalesVolumeFields.PRODUCT, SalesVolumeFields.PRODUCT, JoinType.LEFT)
                .add(SearchRestrictions.in(SalesVolumeFields.PRODUCT + L_DOT + L_ID, productIds)).list().getEntities();
    }

    private List<Entity> createSalesVolumes(final List<Entity> products, final BigDecimal dailySalesVolume, final BigDecimal optimalStock) {
        List<Entity> salesVolumes = Lists.newArrayList();

        products.forEach(product -> {
            Entity salesVolume = getSalesVolumeDD().create();

            salesVolume.setField(SalesVolumeFields.PRODUCT, product);
            salesVolume.setField(SalesVolumeFields.DAILY_SALES_VOLUME, dailySalesVolume);
            salesVolume.setField(SalesVolumeFields.OPTIMAL_STOCK, optimalStock);

            salesVolume = salesVolume.getDataDefinition().save(salesVolume);

            salesVolumes.add(salesVolume);
        });

        return salesVolumes;
    }

    public void onQuantityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent salesVolumeMultiForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent dailySalesVolumeField = (FieldComponent) view.getComponentByReference(SalesVolumeMultiFields.DAILY_SALES_VOLUME);
        FieldComponent optimalStockField = (FieldComponent) view.getComponentByReference(SalesVolumeMultiFields.OPTIMAL_STOCK);

        Object dailySalesVolume = dailySalesVolumeField.getFieldValue();
        Object optimalStock = optimalStockField.getFieldValue();

        Either<Exception, com.google.common.base.Optional<BigDecimal>> eitherDailySalesVolume = BigDecimalUtils.tryParseAndIgnoreSeparator(
                (String) dailySalesVolume, LocaleContextHolder.getLocale());
        Either<Exception, com.google.common.base.Optional<BigDecimal>> eitherOptimalStock = BigDecimalUtils.tryParseAndIgnoreSeparator(
                (String) optimalStock, LocaleContextHolder.getLocale());

        Entity salesVolumeMulti = getSalesVolumeMultiDD().get(salesVolumeMultiForm.getEntityId());

        if (eitherDailySalesVolume.isRight() && eitherDailySalesVolume.getRight().isPresent()) {
            salesVolumeMulti.setField(SalesVolumeMultiFields.DAILY_SALES_VOLUME, eitherDailySalesVolume.getRight().get());
        } else {
            salesVolumeMulti.setField(SalesVolumeMultiFields.DAILY_SALES_VOLUME, dailySalesVolume);
        }
        if (eitherOptimalStock.isRight() && eitherOptimalStock.getRight().isPresent()) {
            salesVolumeMulti.setField(SalesVolumeMultiFields.OPTIMAL_STOCK, eitherOptimalStock.getRight().get());
        } else {
            salesVolumeMulti.setField(SalesVolumeMultiFields.OPTIMAL_STOCK, optimalStock);
        }

        salesVolumeMulti = salesVolumeMulti.getDataDefinition().save(salesVolumeMulti);

        salesVolumeMultiForm.setEntity(salesVolumeMulti);
    }

    private DataDefinition getSalesVolumeDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_VOLUME);
    }

    private DataDefinition getSalesVolumeMultiDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_VOLUME_MULTI);
    }

}
