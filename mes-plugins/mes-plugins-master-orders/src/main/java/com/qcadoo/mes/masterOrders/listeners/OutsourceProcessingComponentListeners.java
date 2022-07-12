package com.qcadoo.mes.masterOrders.listeners;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.masterOrders.OrdersGenerationService;
import com.qcadoo.mes.masterOrders.constants.OutsourceProcessingComponentHelperFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Service
public class OutsourceProcessingComponentListeners {

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private OrdersGenerationService ordersGenerationService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    public void generateOrder(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws JSONException {
        FormComponent outsourceProcessingComponentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference("generated");

        Entity outsourceProcessingComponent = outsourceProcessingComponentForm.getPersistedEntityWithIncludedFormValues();

        outsourceProcessingComponent = outsourceProcessingComponent.getDataDefinition().validate(outsourceProcessingComponent);

        if (!outsourceProcessingComponent.isValid()) {
            outsourceProcessingComponentForm.setEntity(outsourceProcessingComponent);

            return;
        }

        Entity order = generateOrder(outsourceProcessingComponent);

        if (order.isValid()) {
            view.addMessage("masterOrders.outsourceProcessingComponent.info.generatedOrder", ComponentState.MessageType.SUCCESS, order.getStringField(OrderFields.NUMBER));
        } else {
            view.addMessage("masterOrders.outsourceProcessingComponent.info.notGeneratedOrder", ComponentState.MessageType.FAILURE);
        }

        generatedCheckBox.setChecked(true);
    }

    private Entity generateOrder(final Entity outsourceProcessingComponent) {
        Entity product = outsourceProcessingComponent.getBelongsToField(OutsourceProcessingComponentHelperFields.PRODUCT);
        BigDecimal quantity = outsourceProcessingComponent.getDecimalField(OutsourceProcessingComponentHelperFields.QUANTITY);
        Entity technology = outsourceProcessingComponent.getBelongsToField(OutsourceProcessingComponentHelperFields.TECHNOLOGY);
        Date dateFrom = outsourceProcessingComponent.getDateField(OutsourceProcessingComponentHelperFields.DATE_FROM);
        Date dateTo = outsourceProcessingComponent.getDateField(OutsourceProcessingComponentHelperFields.DATE_TO);

        Entity parameter = parameterService.getParameter();

        BigDecimal plannedQuantity = calculatePlannedQuantity(technology, product, quantity);

        return ordersGenerationService.createOrder(parameter, technology, technology.getBelongsToField(TechnologyFields.PRODUCT), plannedQuantity, null, dateFrom, dateTo);
    }

    private BigDecimal calculatePlannedQuantity(final Entity technology, final Entity product, final BigDecimal quantity) {
        BigDecimal plannedQuantity = BigDecimal.ONE;

        Map<OperationProductComponentHolder, BigDecimal> neededQuantities = productQuantitiesService
                .getNeededProductQuantities(technology, technology.getBelongsToField(TechnologyFields.PRODUCT), BigDecimal.ONE);

        for (Map.Entry<OperationProductComponentHolder, BigDecimal> neededProductQuantity : neededQuantities.entrySet()) {
            Long productId = neededProductQuantity.getKey().getProductId();

            if (productId.equals(product.getId())) {
                BigDecimal neededQuantity = neededProductQuantity.getValue();

                plannedQuantity = quantity.divide(neededQuantity, numberService.getMathContext());
            }
        }

        return numberService.setScaleWithDefaultMathContext(plannedQuantity);
    }

}
