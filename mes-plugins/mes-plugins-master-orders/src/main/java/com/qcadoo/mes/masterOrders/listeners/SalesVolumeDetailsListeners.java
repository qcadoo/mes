package com.qcadoo.mes.masterOrders.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.masterOrders.OrdersGenerationService;
import com.qcadoo.mes.masterOrders.constants.SalesVolumeFields;
import com.qcadoo.mes.masterOrders.hooks.SalesVolumeHooks;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@Service
public class SalesVolumeDetailsListeners {

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    @Autowired
    private NumberService numberService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private OrdersGenerationService ordersGenerationService;

    @Autowired
    private SalesVolumeHooks salesVolumeHooks;

    public final void createOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent salesVolumeForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity salesVolume = salesVolumeForm.getEntity();

        if (Objects.nonNull(salesVolume.getId())) {
            Entity parameter = parameterService.getParameter();

            Entity product = salesVolume.getBelongsToField(SalesVolumeFields.PRODUCT);
            BigDecimal optimalStock = BigDecimalUtils.convertNullToZero(salesVolume.getDecimalField(SalesVolumeFields.OPTIMAL_STOCK));
            BigDecimal currentStock = BigDecimalUtils.convertNullToZero(salesVolume.getDecimalField(SalesVolumeFields.CURRENT_STOCK));

            if (BigDecimal.ZERO.compareTo(optimalStock) == 0) {
                view.addMessage("masterOrders.salesVolumeDetails.createOrder.info.optimalStock", ComponentState.MessageType.INFO);
            } else {
                if (currentStock.compareTo(optimalStock) <= 0) {
                    view.addMessage("masterOrders.salesVolumeDetails.createOrder.info.currentStock", ComponentState.MessageType.INFO);
                } else {
                    Entity technology = technologyServiceO.getDefaultTechnology(product);
                    BigDecimal plannedQuantity = optimalStock.subtract(currentStock, numberService.getMathContext());

                    if (Objects.nonNull(technology)) {
                        if (PluginUtils.isEnabled("minimalAffordableQuantity")) {
                            BigDecimal minimalQuantity = technology.getDecimalField("minimalQuantity");

                            if (Objects.nonNull(minimalQuantity) && plannedQuantity.compareTo(minimalQuantity) < 0) {
                                plannedQuantity = minimalQuantity;
                            }
                        }
                    }

                    Entity order = ordersGenerationService.createOrder(parameter, technology, product, plannedQuantity, null, null, null);

                    if (order.isValid()) {
                        Map<String, Object> parameters = Maps.newHashMap();

                        parameters.put(L_WINDOW_ACTIVE_MENU, "orders.productionOrders");

                        parameters.put("form.id", order.getId());

                        String url = "../page/orders/orderDetails.html";

                        view.redirectTo(url, false, true, parameters);
                    } else {
                        order.getGlobalErrors().stream().filter(error ->
                                !error.getMessage().equals("qcadooView.validate.global.error.custom")).forEach(error ->
                                view.addMessage(error.getMessage(), ComponentState.MessageType.FAILURE, error.getVars())
                        );

                        order.getErrors().values().forEach(error ->
                                view.addMessage(error.getMessage(), ComponentState.MessageType.FAILURE, error.getVars())
                        );
                    }
                }
            }
        }
    }

    public void onQuantityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent salesVolumeForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(SalesVolumeFields.PRODUCT);

        Entity salesVolume = salesVolumeForm.getEntity();
        Entity product = productLookup.getEntity();

        salesVolumeHooks.fillStockFields(salesVolume);

        if (Objects.nonNull(product)) {
            salesVolume.setField(SalesVolumeFields.PRODUCT, product);

            salesVolumeForm.setEntity(salesVolume);
        }
    }

}
