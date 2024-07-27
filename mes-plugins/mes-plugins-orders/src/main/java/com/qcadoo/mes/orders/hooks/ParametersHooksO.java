/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.orders.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.GridComponent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.qcadoo.mes.orders.constants.ParameterFieldsO.*;

@Service
public class ParametersHooksO {

    private static final String L_REALIZATION_FROM_STOCK = "realizationFromStock";

    private static final String L_ALWAYS_ORDER_ITEMS_WITH_PERSONALIZATION = "alwaysOrderItemsWithPersonalization";

    private static final String L_REALIZATION_LOCATIONS = "realizationLocations";

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING = "qcadooView.validate.field.error.missing";

    private static final String L_AUTOMATICALLY_GENERATE_TASKS_FOR_ORDER = "automaticallyGenerateTasksForOrder";

    private static final String L_COMPLETE_STATION_AND_EMPLOYEE_IN_GENERATED_TASKS = "completeStationAndEmployeeInGeneratedTasks";

    public static final String L_CONSIDER_MINIMUM_STOCK_LEVEL_WHEN_CREATING_PRODUCTION_ORDERS = "considerMinimumStockLevelWhenCreatingProductionOrders";

    @Autowired
    private OrderService orderService;

    @Autowired
    private TranslationService translationService;

    public void onSave(final DataDefinition parameterDD, final Entity parameter) {
        if (!parameter.getBooleanField(ParameterFieldsO.REALIZATION_FROM_STOCK)) {
            parameter.setField(ParameterFieldsO.REALIZATION_LOCATIONS, Lists.newArrayList());
            parameter.setField(L_CONSIDER_MINIMUM_STOCK_LEVEL_WHEN_CREATING_PRODUCTION_ORDERS, false);
        }
    }

    public boolean validatesWith(final DataDefinition parameterDD, final Entity parameter) {
        boolean isValid = true;

        if (parameter.getBooleanField(ParameterFieldsO.REALIZATION_FROM_STOCK)
                && parameter.getHasManyField(ParameterFieldsO.REALIZATION_LOCATIONS).isEmpty()) {
            parameter.addGlobalError("orders.ordersParameters.window.mainTab.ordersParameters.realizationLocations.error.empty",
                    Boolean.FALSE);

            isValid = false;
        }
        if (parameter.getBooleanField(ParameterFieldsO.ADVISE_START_DATE_OF_THE_ORDER)
                && StringUtils.isEmpty(parameter.getStringField(ParameterFieldsO.ORDER_START_DATE_BASED_ON))) {
            parameter.addError(parameterDD.getField(ParameterFieldsO.ORDER_START_DATE_BASED_ON),
                    L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);

            isValid = false;
        }

        boolean generatePacksForOrders = parameter.getBooleanField(GENERATE_PACKS_FOR_ORDERS);
        if (generatePacksForOrders && parameter.getDecimalField(OPTIMAL_PACK_SIZE) == null) {
            parameter.addError(parameterDD.getField(OPTIMAL_PACK_SIZE), L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
            isValid = false;
        }
        if (generatePacksForOrders && parameter.getDecimalField(REST_FEEDING_LAST_PACK) == null) {
            parameter.addError(parameterDD.getField(REST_FEEDING_LAST_PACK), L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
            isValid = false;
        }

        if (parameter.getDecimalField(OPTIMAL_PACK_SIZE) != null && parameter.getDecimalField(REST_FEEDING_LAST_PACK) != null
                && parameter.getDecimalField(REST_FEEDING_LAST_PACK)
                        .compareTo(parameter.getDecimalField(OPTIMAL_PACK_SIZE)) >= 0) {
            parameter.addError(parameterDD.getField(REST_FEEDING_LAST_PACK),
                    "basic.parameter.restFeedingLastPackBiggerThanOptimalPackSize.error");
            isValid = false;
        }

        boolean dimensionControlOfProducts = parameter.getBooleanField(ParameterFieldsO.ORDER_DIMENSION_CONTROL_OF_PRODUCTS);
        List<Entity> dimensionControlAttributes = parameter.getHasManyField(ParameterFieldsO.ORDER_DIMENSION_CONTROL_ATTRIBUTES);

        if (dimensionControlOfProducts && dimensionControlAttributes.isEmpty()) {
            parameter.addGlobalError("basic.parameter.dimensionControlOfProducts.error.areEmpty");

            isValid = false;
        }

        return isValid;
    }

    public void onOrdersParameterBeforeRender(final ViewDefinitionState view) {
        CheckBoxComponent realizationFromStockComponent = (CheckBoxComponent) view
                .getComponentByReference(L_REALIZATION_FROM_STOCK);
        CheckBoxComponent alwaysOrderItemsWithPersonalizationComponent = (CheckBoxComponent) view
                .getComponentByReference(L_ALWAYS_ORDER_ITEMS_WITH_PERSONALIZATION);
        GridComponent realizationLocationsGrid = (GridComponent) view.getComponentByReference(L_REALIZATION_LOCATIONS);

        if (realizationFromStockComponent.isChecked()) {
            alwaysOrderItemsWithPersonalizationComponent.setEnabled(true);
            realizationLocationsGrid.setEditable(true);
        } else {
            alwaysOrderItemsWithPersonalizationComponent.setEnabled(false);
            realizationLocationsGrid.setEditable(false);
        }

        alwaysOrderItemsWithPersonalizationComponent.requestComponentUpdateState();
    }

    public void onPlanningParametersBeforeRender(final ViewDefinitionState view) {
        CheckBoxComponent automaticallyGenerateTasksForOrder = (CheckBoxComponent) view
                .getComponentByReference(L_AUTOMATICALLY_GENERATE_TASKS_FOR_ORDER);
        CheckBoxComponent completeStationAndEmployeeInGeneratedTasks = (CheckBoxComponent) view
                .getComponentByReference(L_COMPLETE_STATION_AND_EMPLOYEE_IN_GENERATED_TASKS);
        if (automaticallyGenerateTasksForOrder.isChecked()) {
            completeStationAndEmployeeInGeneratedTasks.setEnabled(true);
        } else {
            completeStationAndEmployeeInGeneratedTasks.setEnabled(false);
        }
    }

    public void onBeforeRender(final ViewDefinitionState view) {
        showTimeFields(view);

        CheckBoxComponent realizationFromStockComponent = (CheckBoxComponent) view
                .getComponentByReference(L_REALIZATION_FROM_STOCK);
        CheckBoxComponent alwaysOrderItemsWithPersonalizationComponent = (CheckBoxComponent) view
                .getComponentByReference(L_ALWAYS_ORDER_ITEMS_WITH_PERSONALIZATION);
        GridComponent realizationLocationsGrid = (GridComponent) view.getComponentByReference(L_REALIZATION_LOCATIONS);

        if (realizationFromStockComponent.isChecked()) {
            alwaysOrderItemsWithPersonalizationComponent.setEnabled(true);
            realizationLocationsGrid.setEditable(true);
        } else {
            alwaysOrderItemsWithPersonalizationComponent.setEnabled(false);
            realizationLocationsGrid.setEditable(false);
        }

        alwaysOrderItemsWithPersonalizationComponent.requestComponentUpdateState();

        CheckBoxComponent adviseStartDateOfTheOrder = (CheckBoxComponent) view
                .getComponentByReference(ParameterFieldsO.ADVISE_START_DATE_OF_THE_ORDER);
        FieldComponent orderStartDateBasedOn = (FieldComponent) view
                .getComponentByReference(ParameterFieldsO.ORDER_START_DATE_BASED_ON);

        if (adviseStartDateOfTheOrder.isChecked()) {
            orderStartDateBasedOn.setEnabled(true);
        } else {
            orderStartDateBasedOn.setEnabled(false);
            orderStartDateBasedOn.setFieldValue(null);
        }

        CheckBoxComponent generatePacksForOrders = (CheckBoxComponent) view.getComponentByReference(GENERATE_PACKS_FOR_ORDERS);
        FieldComponent optimalPackSize = (FieldComponent) view.getComponentByReference(OPTIMAL_PACK_SIZE);
        FieldComponent restFeedingLastPack = (FieldComponent) view.getComponentByReference(REST_FEEDING_LAST_PACK);

        if (generatePacksForOrders.isChecked()) {
            optimalPackSize.setEnabled(true);
            optimalPackSize.setRequired(true);
            restFeedingLastPack.setEnabled(true);
            restFeedingLastPack.setRequired(true);
        } else {
            optimalPackSize.setEnabled(false);
            optimalPackSize.setFieldValue(null);
            optimalPackSize.setRequired(false);
            restFeedingLastPack.setEnabled(false);
            restFeedingLastPack.setFieldValue(null);
            restFeedingLastPack.setRequired(false);
        }
        optimalPackSize.requestComponentUpdateState();
        restFeedingLastPack.requestComponentUpdateState();

        CheckBoxComponent deadlineForOrderBasedOnDeliveryDate = (CheckBoxComponent) view.getComponentByReference(DEADLINE_FOR_ORDER_BASED_ON_DELIVERY_DATE);
        FieldComponent deadlineForOrderEarlierThanDeliveryDate = (FieldComponent) view.getComponentByReference(DEADLINE_FOR_ORDER_EARLIER_THAN_DELIVERY_DATE);
        if (deadlineForOrderBasedOnDeliveryDate.isChecked()) {
            deadlineForOrderEarlierThanDeliveryDate.setEnabled(true);
        } else {
            deadlineForOrderEarlierThanDeliveryDate.setEnabled(false);
            deadlineForOrderEarlierThanDeliveryDate.setFieldValue(0);
        }

        FieldComponent deadlineForOrderEarlierThanDeliveryDateUnit = (FieldComponent) view.getComponentByReference("deadlineForOrderEarlierThanDeliveryDateUnit");
        deadlineForOrderEarlierThanDeliveryDateUnit.setFieldValue(translationService.translate("orders.ordersParameters.window.ordersFromMasterOrdersTab.deadlineForOrderEarlierThanDeliveryDateUnit", view.getLocale()));
        deadlineForOrderEarlierThanDeliveryDateUnit.requestComponentUpdateState();

        setDimensionControlAttributesEnabled(view);
    }

    private void setDimensionControlAttributesEnabled(final ViewDefinitionState view) {
        CheckBoxComponent dimensionControlOfProductsCheckBox = (CheckBoxComponent) view.getComponentByReference(ParameterFieldsO.ORDER_DIMENSION_CONTROL_OF_PRODUCTS);
        AwesomeDynamicListComponent dimensionControlAttributesADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(ParameterFieldsO.ORDER_DIMENSION_CONTROL_ATTRIBUTES);

        if (dimensionControlOfProductsCheckBox.isChecked()) {
            dimensionControlAttributesADL.setEnabled(true);
            dimensionControlAttributesADL.getFormComponents().forEach(formComponent -> formComponent.setFormEnabled(true));
        } else {
            dimensionControlAttributesADL.setEnabled(false);
            dimensionControlAttributesADL.setFieldValue(null);
        }

        dimensionControlAttributesADL.requestComponentUpdateState();
    }

    public void showTimeFields(final ViewDefinitionState view) {
        orderService.changeFieldState(view, REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM, DELAYED_EFFECTIVE_DATE_FROM_TIME);
        orderService.changeFieldState(view, REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM, EARLIER_EFFECTIVE_DATE_FROM_TIME);
        orderService.changeFieldState(view, REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO, DELAYED_EFFECTIVE_DATE_TO_TIME);
        orderService.changeFieldState(view, REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO, EARLIER_EFFECTIVE_DATE_TO_TIME);
    }

}
