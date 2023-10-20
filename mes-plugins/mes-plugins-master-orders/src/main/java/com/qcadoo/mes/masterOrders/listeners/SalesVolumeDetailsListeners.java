package com.qcadoo.mes.masterOrders.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.masterOrders.OrdersGenerationService;
import com.qcadoo.mes.masterOrders.constants.SalesVolumeFields;
import com.qcadoo.mes.masterOrders.hooks.SalesVolumeHooks;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
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

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public final void createOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent salesVolumeForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        salesVolumeForm.performEvent(view, "save");

        if (salesVolumeForm.isValid()) {
            Entity salesVolume = salesVolumeForm.getEntity();

            if (Objects.nonNull(salesVolume.getId())) {
                Entity parameter = parameterService.getParameter();

                Entity product = salesVolume.getBelongsToField(SalesVolumeFields.PRODUCT);
                BigDecimal optimalStock = BigDecimalUtils.convertNullToZero(salesVolume.getDecimalField(SalesVolumeFields.OPTIMAL_STOCK));
                BigDecimal currentStock = BigDecimalUtils.convertNullToZero(salesVolume.getDecimalField(SalesVolumeFields.CURRENT_STOCK));

                if (BigDecimal.ZERO.compareTo(optimalStock) == 0) {
                    view.addMessage("masterOrders.salesVolumeDetails.createOrder.info.optimalStock", ComponentState.MessageType.INFO);
                } else {
                    if (currentStock.compareTo(optimalStock) >= 0) {
                        view.addMessage("masterOrders.salesVolumeDetails.createOrder.info.currentStock", ComponentState.MessageType.INFO);
                    } else {
                        Entity technology = technologyServiceO.getDefaultTechnology(product);
                        BigDecimal plannedQuantity = optimalStock.subtract(currentStock, numberService.getMathContext());

                        if (Objects.nonNull(technology)) {
                            BigDecimal minimalQuantity = technology.getDecimalField(TechnologyFields.MINIMAL_QUANTITY);

                            if (Objects.nonNull(minimalQuantity) && plannedQuantity.compareTo(minimalQuantity) < 0) {
                                plannedQuantity = minimalQuantity;
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
    }

    public void onQuantityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent salesVolumeForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent currentStockField = (FieldComponent) view.getComponentByReference(SalesVolumeFields.CURRENT_STOCK);
        FieldComponent stockForDaysField = (FieldComponent) view.getComponentByReference(SalesVolumeFields.STOCK_FOR_DAYS);

        Entity salesVolume = salesVolumeForm.getEntity();

        Entity product = salesVolume.getBelongsToField(SalesVolumeFields.PRODUCT);

        if (Objects.nonNull(product)) {
            BigDecimal dailySalesVolume;

            try {
                dailySalesVolume = salesVolume.getDecimalField(SalesVolumeFields.DAILY_SALES_VOLUME);

                if (Objects.nonNull(dailySalesVolume) && BigDecimal.ZERO.compareTo(dailySalesVolume) < 0) {
                    BigDecimal currentStock = getCurrentStock(product);
                    Integer stockForDays = currentStock.divide(dailySalesVolume, 0, RoundingMode.FLOOR).intValue();

                    currentStockField.setFieldValue(numberService.formatWithMinimumFractionDigits(currentStock, 0));
                    stockForDaysField.setFieldValue(stockForDays);
                }
            } catch (IllegalArgumentException ex) {
            }
        }

        currentStockField.requestComponentUpdateState();
        stockForDaysField.requestComponentUpdateState();
    }

    private BigDecimal getCurrentStock(final Entity product) {
        BigDecimal currentStock = BigDecimal.ZERO;

        List<Entity> locations = materialFlowResourcesService.getWarehouseLocationsFromDB();

        Map<Long, Map<Long, BigDecimal>> resourceStocks = materialFlowResourcesService.getQuantitiesForProductsAndLocations(com.google.common.collect.Lists.newArrayList(product), locations);

        for (Map.Entry<Long, Map<Long, BigDecimal>> resourceStock : resourceStocks.entrySet()) {
            currentStock = currentStock.add(BigDecimalUtils.convertNullToZero(resourceStock.getValue().get(product.getId())),
                    numberService.getMathContext());
        }

        return currentStock;
    }

}
