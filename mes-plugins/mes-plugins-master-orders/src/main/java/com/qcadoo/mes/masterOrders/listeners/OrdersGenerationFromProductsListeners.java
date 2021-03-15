package com.qcadoo.mes.masterOrders.listeners;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.masterOrders.GenerationOrderResult;
import com.qcadoo.mes.masterOrders.OrdersGenerationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrdersGenerationFromProductsListeners {

    private static final String DATE_FROM = "dateFrom";

    private static final String DATE_TO = "dateTo";

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private OrdersGenerationService ordersGenerationService;

    public void generateOrders(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws JSONException {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent generated = (CheckBoxComponent) view.getComponentByReference("generated");
        Entity entity = form.getPersistedEntityWithIncludedFormValues();

        entity = entity.getDataDefinition().validate(entity);
        if (!entity.isValid()) {
            form.setEntity(entity);
            return;
        }
        JSONObject context = view.getJsonContext();
        Set<Long> ids = Arrays
                .stream(context.getString("window.mainTab.form.gridLayout.selectedEntities").replaceAll("[\\[\\]]", "")
                        .split(",")).map(Long::valueOf).collect(Collectors.toSet());
        GenerationOrderResult result = new GenerationOrderResult(translationService, parameterService);
        try {
            generateOrders(result, ids, entity);
            result.showMessage(view);
        } catch (Exception exc) {
            view.addMessage("orders.ordersGenerationFromProducts.error.ordersNotGenerated", ComponentState.MessageType.FAILURE);

        }
        generated.setChecked(true);
    }

    private void generateOrders(GenerationOrderResult result, final Set<Long> ids, final Entity ordersGenerationHelper) {

        BigDecimal plannedQuantity = ordersGenerationHelper.getDecimalField("plannedQuantity");
        Date dateFrom = ordersGenerationHelper.getDateField(DATE_FROM);
        Date dateTo = ordersGenerationHelper.getDateField(DATE_TO);

        ordersGenerationService.createOrders(result, ids, plannedQuantity, dateFrom, dateTo);

    }

    public boolean validateDate(final DataDefinition dd, final Entity ordersGenerationHelper) {
        Date dateFrom = ordersGenerationHelper.getDateField(DATE_FROM);
        Date dateTo = ordersGenerationHelper.getDateField(DATE_TO);

        if (dateFrom == null || dateTo == null || dateTo.after(dateFrom)) {
            return true;
        }

        ordersGenerationHelper.addError(dd.getField(DATE_TO), "orders.validate.global.error.datesOrder");
        return false;
    }


}
