package com.qcadoo.mes.masterOrders.listeners;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.masterOrders.GenerationOrderResult;
import com.qcadoo.mes.masterOrders.OrdersGenerationService;
import com.qcadoo.mes.masterOrders.constants.OrdersGenerationHelperFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrdersGenerationFromProductsListeners {

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private OrdersGenerationService ordersGenerationService;

    public void generateOrders(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws JSONException {
        FormComponent ordersGenerationHelperForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference("generated");

        Entity ordersGenerationHelper = ordersGenerationHelperForm.getPersistedEntityWithIncludedFormValues();

        ordersGenerationHelper = ordersGenerationHelper.getDataDefinition().validate(ordersGenerationHelper);

        if (!ordersGenerationHelper.isValid()) {
            ordersGenerationHelperForm.setEntity(ordersGenerationHelper);

            return;
        }

        JSONObject context = view.getJsonContext();

        Set<Long> ids = Arrays
                .stream(context.getString("window.mainTab.form.gridLayout.selectedEntities").replaceAll("[\\[\\]]", "")
                        .split(",")).map(Long::valueOf).collect(Collectors.toSet());

        GenerationOrderResult result = new GenerationOrderResult(translationService, parameterService);

        try {
            generateOrders(result, ids, ordersGenerationHelper);

            result.showMessage(view);
        } catch (Exception exc) {
            view.addMessage("orders.ordersGenerationFromProducts.error.ordersNotGenerated", ComponentState.MessageType.FAILURE);
        }

        generatedCheckBox.setChecked(true);
    }

    private void generateOrders(final GenerationOrderResult result, final Set<Long> ids, final Entity ordersGenerationHelper) {
        BigDecimal plannedQuantity = ordersGenerationHelper.getDecimalField(OrdersGenerationHelperFields.PLANNED_QUANTITY);
        Date dateFrom = ordersGenerationHelper.getDateField(OrdersGenerationHelperFields.DATE_FROM);
        Date dateTo = ordersGenerationHelper.getDateField(OrdersGenerationHelperFields.DATE_TO);

        ordersGenerationService.createOrders(result, ids, plannedQuantity, dateFrom, dateTo);
    }

}
