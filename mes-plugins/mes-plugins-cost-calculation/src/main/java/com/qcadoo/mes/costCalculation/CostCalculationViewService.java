package com.qcadoo.mes.costCalculation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_PRODUCT;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_TECHNOLOGY;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.constants.CostCalculateConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class CostCalculationViewService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CostCalculationService costCalculationService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private CurrencyService currencyService;

    private final static String EMPTY = "";

    public void showCostCalculateFromOrder(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        Long orderId = (Long) state.getFieldValue();

        if (orderId != null) {
            String url = "../page/costCalculation/costCalculationDetails.html?context={\"orderId\":\"" + orderId + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void showCostCalculateFromTechnology(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        Long technologyId = (Long) state.getFieldValue();

        if (technologyId != null) {
            String url = "../page/costCalculation/costCalculationDetails.html?context={\"technologyId\":\"" + technologyId
                    + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void copyFieldValues(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        if (args.length < 2) {
            return;
        }
        String sourceType = args[0];
        Long sourceId = Long.valueOf(args[1]);
        Boolean cameFromOrder = "order".equals(sourceType);
        Boolean cameFromTechnology = "technology".equals(sourceType);
        Entity technology;
        Entity order;

        if (!cameFromOrder && !cameFromTechnology) {
            return;
        }

        generateNumber(viewDefinitionState);

        if (cameFromOrder) {
            order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(sourceId);
            technology = order.getBelongsToField("technology");
        } else {
            order = null;
            technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY).get(sourceId);
        }

        applyValuesToFields(viewDefinitionState, technology, order);

    }

    private void applyValuesToFields(final ViewDefinitionState viewDefinitionState, final Entity technology, final Entity order) {
        Boolean cameFromOrder = false;
        Boolean cameFromTechnology = false;
        Set<String> referenceNames = new HashSet<String>(Arrays.asList("defaultTechnology", "product", "order", "quantity",
                "technology"));
        Map<String, FieldComponent> componentsMap = new HashMap<String, FieldComponent>();
        for (String referenceName : referenceNames) {
            FieldComponent fieldComponent = (FieldComponent) viewDefinitionState.getComponentByReference(referenceName);
            componentsMap.put(referenceName, fieldComponent);
        }

        if (order != null) {
            cameFromOrder = true;
        } else {
            cameFromTechnology = true;
        }

        if (cameFromOrder) {
            componentsMap.get("order").setFieldValue(order.getId());
            componentsMap.get("defaultTechnology").setEnabled(false);
            componentsMap.get("quantity").setFieldValue(order.getField("plannedQuantity"));
        } else {
            componentsMap.get("order").setFieldValue(EMPTY);
            componentsMap.get("defaultTechnology").setEnabled(false);
            componentsMap.get("quantity").setFieldValue(technology.getField("minimalQuantity"));
        }
        componentsMap.get("order").setEnabled(cameFromOrder);
        componentsMap.get("technology").setFieldValue(technology.getId());
        componentsMap.get("technology").setEnabled(cameFromTechnology);
        componentsMap.get("defaultTechnology").setFieldValue(technology.getId());

        componentsMap.get("quantity").setEnabled(!cameFromOrder);
        componentsMap.get("product").setFieldValue(technology.getBelongsToField("product").getId());
        componentsMap.get("product").setEnabled(false);
    }

    private void generateNumber(final ViewDefinitionState viewDefinitionState) {
        numberGeneratorService.generateAndInsertNumber(viewDefinitionState, CostCalculateConstants.PLUGIN_IDENTIFIER,
                CostCalculateConstants.MODEL_COST_CALCULATION, "form", "number");
    }

    public void generateDateOfCalculation(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("dateOfCalculation", new Date());
    }

    public void setCurrencyAndUnit(final ViewDefinitionState viewDefinitionState) {

        checkArgument(viewDefinitionState != null, "viewDefinitionState is null");
        String currencyAlphabeticCode = currencyService.getCurrencyAlphabeticCode();
        for (String componentReference : Arrays.asList("totalCostsPLN", "totalOverheadPLN", "additionalOverheadValuePLN",
                "materialCostMarginValuePLN", "productionCostMarginValuePLN", "totalTechnicalProductionCostsPLN",
                "totalPieceworkCostsPLN", "totalLaborHourlyCostsPLN", "totalMachineHourlyCostsPLN", "totalMaterialCostsPLN",
                "additionalOverheadPLN", "materialCostMarginPLN", "productionCostMarginPLN")) {
            FieldComponent field = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
            field.setEnabled(true);
            field.setFieldValue(currencyAlphabeticCode);
            field.setEnabled(false);
            field.requestComponentUpdateState();
        }
        FieldComponent totalCostsPerUnitUNIT = (FieldComponent) viewDefinitionState
                .getComponentByReference("totalCostsPerUnitUNIT");
        totalCostsPerUnitUNIT.setEnabled(true);
        totalCostsPerUnitUNIT.setFieldValue(currencyAlphabeticCode + " / ");
        totalCostsPerUnitUNIT.setEnabled(false);
        totalCostsPerUnitUNIT.requestComponentUpdateState();
    }

    public void fillFieldWhenTechnologyChanged(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent technologyLookup = (FieldComponent) viewDefinitionState.getComponentByReference("technology");
        if (technologyLookup.getFieldValue() == null) {
            return;
        }

        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get((Long) technologyLookup.getFieldValue());
        if (technology != null) {
            applyValuesToFields(viewDefinitionState, technology, null);
        }
    }

    public void fillFieldWhenOrderChanged(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference("order");
        if (orderLookup.getFieldValue() == null) {
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                (Long) orderLookup.getFieldValue());

        if (order == null) {
            return;
        }
        Entity technology = order.getBelongsToField("technology");
        applyValuesToFields(viewDefinitionState, technology, order);
    }

    public void setFieldEnable(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        FieldComponent technologyLookup = (FieldComponent) viewDefinitionState.getComponentByReference("technology");
        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference("order");

        FieldComponent product = (FieldComponent) viewDefinitionState.getComponentByReference("product");
        FieldComponent quantity = (FieldComponent) viewDefinitionState.getComponentByReference("quantity");

        if (technologyLookup.getFieldValue() == null) {
            product.setEnabled(true);
            quantity.setEnabled(true);
            orderLookup.setEnabled(true);
        } else {
            if (orderLookup.getFieldValue() == null) {
                technologyLookup.setEnabled(true);
                product.setEnabled(true);
                quantity.setEnabled(true);
            }
        }
    }

    /* FUNCTIONS FOR FIRE CALCULATION AND HANDLING RESULTS BELOW */

    /* Event handler, fire total calculation */
    public void calculateTotalCostView(ViewDefinitionState viewDefinitionState, ComponentState componentState, String[] args) {

        // Map<String, Object> parameters = getValueFromFields(viewDefinitionState);
        // ComponentState technologyComponent = viewDefinitionState.getComponentByReference("technology");
        // ComponentState orderComponent = viewDefinitionState.getComponentByReference("order");
        // Entity technology, source, costCalculation;

        // check that technology & operation lookup components are found
        // checkArgument(technologyComponent != null && orderComponent != null, "Incompatible view");

        // Long technologyId = (Long) technologyComponent.getFieldValue();
        // Long orderId = (Long) orderComponent.getFieldValue();

        // technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, MODEL_TECHNOLOGY)
        // .get(technologyId);
        //
        // source = technology;
        //
        // if (orderComponent.getFieldValue() != null) {
        // source = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, MODEL_ORDER).get(orderId);
        // }

        // if (technologyComponent.getFieldValue() == null) {
        // return;
        // }

        Entity costCalculation = getEntityFromForm(viewDefinitionState);
        attachBelongsToFields(costCalculation);
        // Fire cost calculation algorithm
        costCalculation = costCalculationService.calculateTotalCost(costCalculation);
        fillFields(viewDefinitionState, costCalculation);

    }

    private void attachBelongsToFields(final Entity costCalculation) {
        final Map<String, DataDefinition> belongsToFieldDDs = Maps.newHashMap();
        belongsToFieldDDs.put("order", dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, MODEL_ORDER));
        belongsToFieldDDs.put("technology", dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, MODEL_TECHNOLOGY));
        belongsToFieldDDs.put("defaultTechnology",
                dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, MODEL_TECHNOLOGY));
        belongsToFieldDDs.put("product", dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCT));

        Entity fieldEntity;
        Object fieldValue;

        for (String fieldName : belongsToFieldDDs.keySet()) {
            fieldValue = costCalculation.getField(fieldName);
            if (fieldValue == null || !(fieldValue instanceof Long)) {
                continue;
            }
            fieldEntity = belongsToFieldDDs.get(fieldName).get((Long) fieldValue);
            costCalculation.setField(fieldName, fieldEntity);
        }

        // DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, MODEL_TECHNOLOGY);
        // Entity order = costCalculation.getBelongsToField("order");
        // Entity technology = costCalculation.getBelongsToField("technology");
        // Entity defaultTechnology = costCalculation.getBelongsToField("defaultTechnology");
        // if(order != null) {
        // order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, MODEL_ORDER).get(order.getId());
        // costCalculation.setField("order", order);
        // }
        // technology = technologyDD.get(technology.getId());
        // defaultTechnology = technologyDD.get(defaultTechnology.getId());
        //
        // costCalculation.setField("technology", technology);
        // costCalculation.setField("defaultTechnology", defaultTechnology);
    }

    // get values from form fields
    private Entity getEntityFromForm(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        checkArgument(form != null, "form is null");
        checkArgument(form.isValid(), "invalid form");
        return form.getEntity();

        // Map<String, Object> resultMap = new HashMap<String, Object>();

        // Set for fields contained BigDecimal values
        // final Set<String> bigDecimalValues = new HashSet<String>();
        // bigDecimalValues.addAll(Arrays.asList("quantity", "productionCostMargin", "materialCostMargin", "additionalOverhead"));

        // Set for all input fields
        // final Set<String> referenceValues = new HashSet<String>();
        // referenceValues.addAll(bigDecimalValues);
        // referenceValues.addAll(Arrays.asList("includeTPZ", "calculateMaterialCostsMode", "calculateOperationCostsMode"));

        // for (String key : referenceValues) {
        // Object fieldValue = view.getComponentByReference(key).getFieldValue();
        // Object value = null;
        //
        // if (fieldValue != null && !fieldValue.toString().isEmpty()) {
        // if (bigDecimalValues.contains(key)) {
        // value = getBigDecimalFromField(fieldValue, view.getLocale());
        // } else {
        // value = fieldValue.toString();
        // }
        // } else if (bigDecimalValues.contains(key)) {
        // value = BigDecimal.ZERO;
        // }
        //
        // resultMap.put(key, value);
        // }

        // cast checkbox fields values to boolean
        // resultMap.put("includeTPZ", getBooleanFromField((String) resultMap.get("includeTPZ")));

        // cast mode fields to proper enum
        // ProductsCostCalculationConstants productMode = getProductMode.get("calculateMaterialCostsMode"));

        // resultMap.put("calculateMaterialCostsMode", productMode);

        // resultMap
        // .put("calculateOperationCostsMode", OperationsCostCalculationConstants.valueOf(((String) resultMap
        // .get("calculateOperationCostsMode")).toUpperCase()));

        // return resultMap;
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
    private void fillFields(final ViewDefinitionState view, final Entity costCalculation) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        form.setFieldValue(costCalculation);

        // final Set<String> outputFields = new HashSet<String>();
        // outputFields.addAll(Arrays.asList("productionCostMarginValue", "materialCostMarginValue", "totalOverhead",
        // "totalMaterialCosts", "totalMachineHourlyCosts", "totalLaborHourlyCosts", "totalPieceworkCosts",
        // "totalTechnicalProductionCosts", "totalCosts", "totalCostsPerUnit"));
        // checkArgument(resultMap.keySet().size() == outputFields.size(), "to less argument");
        //
        // for (String referenceName : outputFields) {
        // FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(referenceName);
        // fieldComponent.setFieldValue((BigDecimal) resultMap.get(referenceName).setScale(3, BigDecimal.ROUND_UP));
        // fieldComponent.requestComponentUpdateState();
        // }
    }

    public void changeOrderProduct(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent product = (FieldComponent) state;
        FieldComponent technology = (FieldComponent) viewDefinitionState.getComponentByReference("technology");
        FieldComponent defaultTechnology = (FieldComponent) viewDefinitionState.getComponentByReference("defaultTechnology");

        defaultTechnology.setFieldValue("");
        technology.setFieldValue(null);

        if (product.getFieldValue() != null) {
            Entity defaultTechnologyEntity = getDefaultTechnology((Long) product.getFieldValue());

            if (defaultTechnologyEntity != null) {
                technology.setFieldValue(defaultTechnologyEntity.getId());
                defaultTechnology.setFieldValue(defaultTechnologyEntity.getId());
            }
        }
    }

    private Entity getDefaultTechnology(final Long selectedProductId) {
        DataDefinition instructionDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);

        SearchCriteriaBuilder searchCriteria = instructionDD.find().setMaxResults(1).isEq("master", true)
                .belongsTo("product", selectedProductId);

        SearchResult searchResult = searchCriteria.list();

        if (searchResult.getTotalNumberOfEntities() == 1) {
            return searchResult.getEntities().get(0);
        } else {
            return null;
        }
    }

}
