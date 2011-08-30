package com.qcadoo.mes.costCalculation;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.costCalculation.constants.CostCalculateConstants;
import com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants;
import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class CostCalculationViewService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CostCalculationService costCalculationService;

    private final static String EMPTY = "";

    public void showCostCalculateFromOrder(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        Long orderId = (Long) state.getFieldValue();

        if (orderId != null) {
            String url = "../page/costCalculation/costCalculationDetails.html?context={\"orderId.id\":\"" + orderId + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void showCostCalculateFromTechnology(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        Long technologyId = (Long) state.getFieldValue();

        if (technologyId != null) {
            String url = "../page/costCalculation/costCalculationDetails.html?context={\"technologyId.id\":\"" + technologyId
                    + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void fillField(final ViewDefinitionState state) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FormComponent formOrderId = (FormComponent) state.getComponentByReference("orderId");
        FormComponent formTechnologyId = (FormComponent) state.getComponentByReference("technologyId");

        if (formOrderId != null && formOrderId.getFieldValue() != null) {
            fillFieldFromOrder(state);
        } else if (formTechnologyId != null && formTechnologyId.getFieldValue() != null) {
            fillFieldFromTechnology(state);
        }

    }

    public void fillFieldFromOrder(final ViewDefinitionState state) {
        FormComponent formOrderId = (FormComponent) state.getComponentByReference("orderId");
        FormComponent form = (FormComponent) state.getComponentByReference("form");
        FieldComponent order = (FieldComponent) state.getComponentByReference("order");
        FieldComponent number = (FieldComponent) state.getComponentByReference("number");
        Long orderId = (Long) formOrderId.getFieldValue();
        Entity orderEntity = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                orderId);

        if (orderEntity != null) {
            formOrderId.getEntity().setId(null);
            form.getEntity().setId(null);
            order.setFieldValue(orderId);
            number.setFieldValue(generateNumber(6));
            Entity technologyEntity = orderEntity.getBelongsToField("technology");
            if (technologyEntity != null) {
                setValueToField(state, orderEntity, technologyEntity);
            }
        }
    }

    public void fillFieldFromTechnology(final ViewDefinitionState state) {

        FormComponent formTechnologyId = (FormComponent) state.getComponentByReference("technologyId");
        FormComponent form = (FormComponent) state.getComponentByReference("form");
        FieldComponent number = (FieldComponent) state.getComponentByReference("number");

        Long technologyId = (Long) formTechnologyId.getFieldValue();
        Entity technologyEntity = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);

        if (technologyEntity != null) {
            form.getEntity().setId(null);
            setValueToField(state, null, technologyEntity);
            number.setFieldValue(generateNumber(6));
        }
    }

    private String generateNumber(final int digitsNumber) {
        List<Entity> costs = dataDefinitionService
                .get(CostCalculateConstants.PLUGIN_IDENTIFIER, CostCalculateConstants.MODEL_COST_CALCULATION).find().list()
                .getEntities();

        long longValue = 0;
        if (costs.size() == 0) {
            longValue++;
        } else {
            longValue = costs.size() + 1;
        }
        return String.format("%0" + digitsNumber + "d", longValue);
    }

    public void generateDateOfCalculation(final DataDefinition dataDefinition, final Entity entity) {

        entity.setField("dateOfCalculation", new Date());

    }

    public void fillFieldWhenTechnologyChanged(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent technologyLookup = (FieldComponent) viewDefinitionState.getComponentByReference("technology");
        if (technologyLookup.getFieldValue() != null) {
            Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY).get((Long) technologyLookup.getFieldValue());
            if (technology != null) {
                setValueToField(viewDefinitionState, null, technology);
            }
        }
    }

    public void fillFieldWhenOrderChanged(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference("order");
        if (orderLookup.getFieldValue() != null) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                    (Long) orderLookup.getFieldValue());

            if (order != null) {
                Entity technologyEntity = order.getBelongsToField("technology");
                if (technologyEntity != null) {
                    setValueToField(viewDefinitionState, order, technologyEntity);
                }
            }
        }
    }

    private void setValueToField(final ViewDefinitionState viewDefinitionState, Entity orderEntity, Entity technologyEntity) {

        FieldComponent defaultTechnology = (FieldComponent) viewDefinitionState.getComponentByReference("defaultTechnology");
        FieldComponent product = (FieldComponent) viewDefinitionState.getComponentByReference("product");
        FieldComponent order = (FieldComponent) viewDefinitionState.getComponentByReference("order");
        FieldComponent quantity = (FieldComponent) viewDefinitionState.getComponentByReference("quantity");
        FieldComponent technology = (FieldComponent) viewDefinitionState.getComponentByReference("technology");
        Entity productEntity = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                technologyEntity.getBelongsToField("product").getId());

        technology.setFieldValue(technologyEntity.getId());
        defaultTechnology.setFieldValue(technologyEntity.getId());
        if (productEntity != null) {
            product.setFieldValue(productEntity.getId());
            product.setEnabled(false);
        }
        if (orderEntity != null) {
            quantity.setFieldValue(orderEntity.getField("plannedQuantity"));
            quantity.setEnabled(false);
            technology.setEnabled(false);
        } else {
            order.setFieldValue(EMPTY);
            order.setEnabled(false);
            quantity.setFieldValue(technologyEntity.getField("minimalQuantity"));
            quantity.setEnabled(true);
        }
        defaultTechnology.requestComponentUpdateState();
        product.requestComponentUpdateState();
        order.requestComponentUpdateState();
        technology.requestComponentUpdateState();
        quantity.requestComponentUpdateState();

    }

    public void setFieldEnable(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        FieldComponent technologyLookup = (FieldComponent) viewDefinitionState.getComponentByReference("technology");
        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference("order");

        FieldComponent product = (FieldComponent) viewDefinitionState.getComponentByReference("product");
        FieldComponent quantity = (FieldComponent) viewDefinitionState.getComponentByReference("quantity");

        if (technologyLookup.getFieldValue() == null) {
            product.setEnabled(true);
            quantity.setEnabled(true);
        }

        if (orderLookup.getFieldValue() == null) {
            technologyLookup.setEnabled(true);
            product.setEnabled(true);
            quantity.setEnabled(true);
        }
    }

    public void clearFieldWhenTechnologyFieldIsEmpty(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent defaultTechnology = (FieldComponent) viewDefinitionState.getComponentByReference("defaultTechnology");
        FieldComponent technologyLookup = (FieldComponent) viewDefinitionState.getComponentByReference("technology");
        FieldComponent product = (FieldComponent) viewDefinitionState.getComponentByReference("product");
        FieldComponent order = (FieldComponent) viewDefinitionState.getComponentByReference("order");
        if (EMPTY.equals(technologyLookup.getFieldValue())) {
            defaultTechnology.setFieldValue(EMPTY);
            defaultTechnology.requestComponentUpdateState();
            product.setFieldValue(EMPTY);
            product.requestComponentUpdateState();
            order.setEnabled(true);
            order.requestComponentUpdateState();
        }

    }

    /* FUNCTIONS FOR FIRE CALCULATION AND HANDLING RESULTS BELOW */

    /* Event handler, fire total calculation */
    public void calculateTotalCostView(ViewDefinitionState viewDefinitionState, ComponentState componentState, String[] args) {
        Map<String, Object> parameters = getValueFromFields(viewDefinitionState);
        Map<String, BigDecimal> results;
        ComponentState technologyComponent = viewDefinitionState.getComponentByReference("technology");
        ComponentState orderComponent = viewDefinitionState.getComponentByReference("order");
        Entity order;
        Entity technology;
        Entity source;

        // check that technology & operation lookup components are found
        checkArgument(technologyComponent != null && orderComponent != null, "Incompatible view");

        technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY)
                .get((Long) technologyComponent.getFieldValue());

        source = technology;

        if (orderComponent.getFieldValue() != null) {
            order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                    (Long) orderComponent.getFieldValue());
            source = order;
        }

        if (technologyComponent.getFieldValue() == null) {
            return;
        }

        // Fire cost calculation algorithm
        results = costCalculationService.calculateTotalCost(source, parameters);
        fillFields(viewDefinitionState, results);
    }

    // get values from form fields
    private Map<String, Object> getValueFromFields(final ViewDefinitionState view) {
        // Set for fields contained BigDecimal values
        final Set<String> bigDecimalValues = new HashSet<String>();
        bigDecimalValues.addAll(Arrays.asList("quantity", "productionCostMargin", "materialCostMargin", "additionalOverhead"));

        // Set for all input fields
        final Set<String> referenceValues = new HashSet<String>();
        referenceValues.addAll(bigDecimalValues);
        referenceValues.addAll(Arrays.asList("includeTPZ", "calculateMaterialCostsMode", "calculateOperationCostsMode"));

        Map<String, Object> resultMap = new HashMap<String, Object>();
        for (String key : referenceValues) {
            Object fieldValue = view.getComponentByReference(key).getFieldValue();
            Object value = null;

            if (fieldValue != null && !fieldValue.toString().isEmpty()) {
                if (bigDecimalValues.contains(key)) {
                    value = getBigDecimalFromField(fieldValue, view.getLocale());
                } else {
                    value = fieldValue.toString();
                }
            } else if (bigDecimalValues.contains(key)) {
                value = BigDecimal.ZERO;
            }

            resultMap.put(key, value);
        }

        // cast cost input fields values to BigDeciaml
        // for (String key : bigDecimalValues) {
        //
        // resultMap.put(key, new BigDecimal((String) resultMap.get(key)));
        // }

        // cast checkbox fields values to boolean
        resultMap.put("includeTPZ", Boolean.valueOf((String) resultMap.get("includeTPZ")));

        // cast mode fields to proper enum
        resultMap.put("calculateMaterialCostsMode",
                ProductsCostCalculationConstants.valueOf(((String) resultMap.get("calculateMaterialCostsMode")).toUpperCase()));
        resultMap
                .put("calculateOperationCostsMode", OperationsCostCalculationConstants.valueOf(((String) resultMap
                        .get("calculateOperationCostsMode")).toUpperCase()));

        return resultMap;
    }

    private BigDecimal getBigDecimalFromField(final Object value, final Locale locale) {
        try {
            DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(locale);
            format.setParseBigDecimal(true);
            return new BigDecimal(format.parse(value.toString()).doubleValue());
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    // put result values into proper form fields
    private void fillFields(final ViewDefinitionState view, final Map<String, BigDecimal> resultMap) {
        final Set<String> outputFields = new HashSet<String>();
        outputFields.addAll(Arrays.asList("productionCostMarginValue", "materialCostMarginValue", "totalOverhead",
                "totalMaterialCosts", "totalMachineHourlyCosts", "totalLaborHourlyCosts", "totalPieceworkCosts",
                "totalTechnicalProductionCosts", "totalCosts", "totalCostsPerUnit"));
        checkArgument(resultMap.keySet().size() == outputFields.size(), "to less argument");

        for (String referenceName : outputFields) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(referenceName);
            fieldComponent.setFieldValue(resultMap.get(referenceName));
            fieldComponent.requestComponentUpdateState();
        }

    }

    // public void setPdfButtonEnabled(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[]
    // args) {
    // ComponentState pdfRaport = (ComponentState) viewDefinitionState.getComponentByReference("pdf");
    // if (viewDefinitionState.getComponentByReference("totalCosts").getFieldValue() != null) {
    // pdfRaport.setEnabled(true);
    // }
    // }

}
