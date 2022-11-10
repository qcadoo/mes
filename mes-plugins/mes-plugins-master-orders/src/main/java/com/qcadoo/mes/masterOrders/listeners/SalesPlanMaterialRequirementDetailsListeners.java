package com.qcadoo.mes.masterOrders.listeners;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.CompanyFieldsD;
import com.qcadoo.mes.deliveries.constants.CompanyProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.constants.ParameterFieldsD;
import com.qcadoo.mes.masterOrders.constants.DeliveryFieldsMO;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.SalesPlanFields;
import com.qcadoo.mes.masterOrders.constants.SalesPlanMaterialRequirementFields;
import com.qcadoo.mes.masterOrders.constants.SalesPlanMaterialRequirementProductFields;
import com.qcadoo.mes.masterOrders.helpers.SalesPlanMaterialRequirementHelper;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class SalesPlanMaterialRequirementDetailsListeners {

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private SalesPlanMaterialRequirementHelper salesPlanMaterialRequirementHelper;

    public void generateSalesPlanMaterialRequirement(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        FormComponent salesPlanMaterialRequirementForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view
                .getComponentByReference(SalesPlanMaterialRequirementFields.GENERATED);
        LookupComponent salesPlanLookup = (LookupComponent) view
                .getComponentByReference(SalesPlanMaterialRequirementFields.SALES_PLAN);
        FieldComponent workerField = (FieldComponent) view.getComponentByReference(SalesPlanMaterialRequirementFields.WORKER);
        FieldComponent dateField = (FieldComponent) view.getComponentByReference(SalesPlanMaterialRequirementFields.DATE);

        Entity salesPlan = salesPlanLookup.getEntity();

        if (validateSalesPlanMaterialRequirement(salesPlanMaterialRequirementForm, salesPlanLookup, salesPlan)) {
            workerField.setFieldValue(securityService.getCurrentUserName());
            dateField.setFieldValue(DateUtils.toDateTimeString(new Date()));
            generatedCheckBox.setChecked(true);

            Entity salesPlanMaterialRequirement = salesPlanMaterialRequirementForm.getEntity();

            List<Entity> salesPlanMaterialRequirementProducts = salesPlanMaterialRequirementHelper
                    .generateSalesPlanMaterialRequirementProducts(salesPlanMaterialRequirement);

            salesPlanMaterialRequirement.setField(SalesPlanMaterialRequirementFields.SALES_PLAN_MATERIAL_REQUIREMENT_PRODUCTS,
                    salesPlanMaterialRequirementProducts);

            salesPlanMaterialRequirement = salesPlanMaterialRequirement.getDataDefinition().save(salesPlanMaterialRequirement);

            salesPlanMaterialRequirementForm.setEntity(salesPlanMaterialRequirement);

            view.addMessage("masterOrders.salesPlanMaterialRequirement.generate.success", ComponentState.MessageType.SUCCESS);
        } else {
            view.addMessage("masterOrders.salesPlanMaterialRequirement.generate.failure", ComponentState.MessageType.FAILURE);
        }
    }

    private boolean validateSalesPlanMaterialRequirement(final FormComponent salesPlanMaterialRequirementForm,
            final LookupComponent salesPlanLookup, final Entity salesPlan) {
        boolean isValid = true;

        if (Objects.isNull(salesPlan)) {
            salesPlanLookup.addMessage("qcadooView.validate.field.error.missing", ComponentState.MessageType.FAILURE);

            isValid = false;
        }

        return isValid;
    }

    @Transactional
    public void createDelivery(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent salesPlanMaterialRequirementForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent salesPlanMaterialRequirementProductsGrid = (GridComponent) view
                .getComponentByReference(QcadooViewConstants.L_GRID);

        Long salesPlanMaterialRequirementId = salesPlanMaterialRequirementForm.getEntityId();
        Set<Long> salesPlanMaterialRequirementProductIds = salesPlanMaterialRequirementProductsGrid.getSelectedEntitiesIds();

        if (Objects.nonNull(salesPlanMaterialRequirementId) && !salesPlanMaterialRequirementProductIds.isEmpty()) {
            Entity salesPlanMaterialRequirement = getSalesPlanMaterialRequirement(salesPlanMaterialRequirementId);
            List<Entity> salesPlanMaterialRequirementProducts = getSalesPlanMaterialRequirementProducts(
                    salesPlanMaterialRequirementProductIds);

            Entity parameter = parameterService.getParameter();
            Entity delivery = createDelivery(salesPlanMaterialRequirement, salesPlanMaterialRequirementProducts, parameter);

            if (delivery.isValid()) {
                Long deliveryId = delivery.getId();

                if (Objects.nonNull(deliveryId)) {
                    Map<String, Object> parameters = Maps.newHashMap();
                    parameters.put("form.id", deliveryId);

                    parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.deliveries");

                    String url = "../page/deliveries/deliveryDetails.html";
                    view.redirectTo(url, false, true, parameters);
                } else {
                    view.addMessage("masterOrders.salesPlanMaterialRequirement.createDelivery.info",
                            ComponentState.MessageType.INFO);
                }
            } else {
                delivery.getErrors().keySet().stream().filter(DeliveryFields.SUPPLIER::equals).findAny().ifPresent(fieldName -> {
                    if (parameter.getBooleanField(ParameterFieldsD.REQUIRE_SUPPLIER_IDENTYFICATION)) {
                        view.addMessage("deliveries.delivery.supplier.isRequired", ComponentState.MessageType.FAILURE);
                    }
                });

                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        }
    }

    private Entity createDelivery(final Entity salesPlanMaterialRequirement,
            final List<Entity> salesPlanMaterialRequirementProducts, Entity parameter) {
        Entity delivery = deliveriesService.getDeliveryDD().create();

        Entity supplier = getSupplier(salesPlanMaterialRequirementProducts).orElse(null);
        Entity salesPlan = salesPlanMaterialRequirement.getBelongsToField(SalesPlanMaterialRequirementFields.SALES_PLAN);

        List<Entity> products = salesPlanMaterialRequirementHelper
                .getSalesPlanMaterialRequirementProducts(salesPlanMaterialRequirementProducts);
        Set<Long> parentIds = salesPlanMaterialRequirementHelper.getParentIds(products);
        Set<Long> productIds = salesPlanMaterialRequirementHelper.getProductIds(products);

        List<Entity> companyProducts = getCompanyProducts(productIds, supplier);
        List<Entity> companyProductsFamilies = getCompanyProducts(parentIds, supplier);

        List<Entity> orderedProducts = createOrderedProducts(salesPlanMaterialRequirementProducts, companyProducts,
                companyProductsFamilies);

        if (!orderedProducts.isEmpty()) {
            String number = numberGeneratorService.generateNumber(DeliveriesConstants.PLUGIN_IDENTIFIER,
                    DeliveriesConstants.MODEL_DELIVERY);

            delivery.setField(DeliveryFields.NUMBER, number);
            delivery.setField(DeliveryFields.SUPPLIER, supplier);
            Entity currency = null;
            if (supplier != null) {
                currency = supplier.getBelongsToField(CompanyFieldsD.CURRENCY);
            }
            if (currency == null) {
                currency = parameter.getBelongsToField(ParameterFields.CURRENCY);
            }
            delivery.setField(DeliveryFields.CURRENCY, currency);
            delivery.setField(DeliveryFields.ORDERED_PRODUCTS, orderedProducts);
            delivery.setField(DeliveryFields.EXTERNAL_SYNCHRONIZED, true);
            delivery.setField(DeliveryFieldsMO.SALES_PLAN, salesPlan);

            delivery = delivery.getDataDefinition().save(delivery);
        }

        return delivery;
    }

    private Optional<Entity> getSupplier(final List<Entity> salesPlanMaterialRequirementProducts) {
        return salesPlanMaterialRequirementProducts.stream()
                .filter(salesPlanMaterialRequirementProduct -> Objects.nonNull(salesPlanMaterialRequirementProduct
                        .getBelongsToField(SalesPlanMaterialRequirementProductFields.SUPPLIER)))
                .map(salesPlanMaterialRequirementProduct -> salesPlanMaterialRequirementProduct
                        .getBelongsToField(SalesPlanMaterialRequirementProductFields.SUPPLIER))
                .findFirst();
    }

    private List<Entity> getCompanyProducts(final Set<Long> productIds, final Entity company) {
        List<Entity> companyProducts = Lists.newArrayList();

        if (!productIds.isEmpty() && Objects.nonNull(company)) {
            companyProducts = deliveriesService.getCompanyProductDD().find()
                    .createAlias(CompanyProductFields.PRODUCT, CompanyProductFields.PRODUCT, JoinType.LEFT)
                    .createAlias(CompanyProductFields.COMPANY, CompanyProductFields.COMPANY, JoinType.LEFT)
                    .add(SearchRestrictions.in(CompanyProductFields.PRODUCT + L_DOT + L_ID, productIds))
                    .add(SearchRestrictions.eq(CompanyProductFields.COMPANY + L_DOT + L_ID, company.getId())).list()
                    .getEntities();
        }

        return companyProducts;
    }

    private List<Entity> createOrderedProducts(final List<Entity> salesPlanMaterialRequirementProducts,
            final List<Entity> companyProducts, final List<Entity> companyProductsFamilies) {
        List<Entity> orderedProducts = Lists.newArrayList();

        salesPlanMaterialRequirementProducts.forEach(salesPlanMaterialRequirementProduct -> createOrderedProduct(orderedProducts,
                salesPlanMaterialRequirementProduct, companyProducts, companyProductsFamilies));

        return orderedProducts;
    }

    private Entity createOrderedProduct(final List<Entity> orderedProducts, final Entity salesPlanMaterialRequirementProduct,
            final List<Entity> companyProducts, final List<Entity> companyProductsFamilies) {
        Entity product = salesPlanMaterialRequirementProduct.getBelongsToField(SalesPlanMaterialRequirementProductFields.PRODUCT);
        BigDecimal quantity = BigDecimalUtils.convertNullToZero(
                salesPlanMaterialRequirementProduct.getDecimalField(SalesPlanMaterialRequirementProductFields.QUANTITY));
        BigDecimal currentStock = BigDecimalUtils.convertNullToZero(
                salesPlanMaterialRequirementProduct.getDecimalField(SalesPlanMaterialRequirementProductFields.CURRENT_STOCK));
        BigDecimal neededQuantity = BigDecimalUtils.convertNullToZero(
                salesPlanMaterialRequirementProduct.getDecimalField(SalesPlanMaterialRequirementProductFields.NEEDED_QUANTITY));
        BigDecimal minimumOrderQuantity = BigDecimalUtils
                .convertNullToZero(getMinimumOrderQuantity(product, companyProducts, companyProductsFamilies));

        BigDecimal conversion = getConversion(product);
        BigDecimal orderedQuantity = getOrderedQuantity(quantity, currentStock, neededQuantity, minimumOrderQuantity);
        BigDecimal additionalQuantity;

        Optional<Entity> mayBeOrderedProduct = orderedProducts.stream()
                .filter(orderedProduct -> filterByProduct(orderedProduct, salesPlanMaterialRequirementProduct)).findFirst();

        Entity orderedProduct;

        if (mayBeOrderedProduct.isPresent()) {
            orderedProduct = mayBeOrderedProduct.get();

            orderedQuantity = orderedQuantity.add(orderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY),
                    numberService.getMathContext());
            additionalQuantity = orderedQuantity.multiply(conversion, numberService.getMathContext());

            orderedProduct.setField(OrderedProductFields.ORDERED_QUANTITY, orderedQuantity);
            orderedProduct.setField(OrderedProductFields.ADDITIONAL_QUANTITY, additionalQuantity);
        } else {
            if (BigDecimal.ZERO.compareTo(orderedQuantity) > 0) {
                orderedQuantity = BigDecimal.ZERO;
            }

            additionalQuantity = orderedQuantity.multiply(conversion, numberService.getMathContext());

            orderedProduct = deliveriesService.getOrderedProductDD().create();

            orderedProduct.setField(OrderedProductFields.PRODUCT, product);
            orderedProduct.setField(OrderedProductFields.CONVERSION, conversion);
            orderedProduct.setField(OrderedProductFields.ORDERED_QUANTITY, orderedQuantity);
            orderedProduct.setField(OrderedProductFields.ADDITIONAL_QUANTITY, additionalQuantity);

            orderedProducts.add(orderedProduct);

            salesPlanMaterialRequirementProduct.setField(SalesPlanMaterialRequirementProductFields.IS_DELIVERY_CREATED, true);

            salesPlanMaterialRequirementProduct.getDataDefinition().save(salesPlanMaterialRequirementProduct);
        }

        return orderedProduct;
    }

    private BigDecimal getMinimumOrderQuantity(final Entity product, final List<Entity> companyProducts,
            final List<Entity> companyProductsFamilies) {
        Optional<Entity> mayBeCompanyProduct = deliveriesService.getCompanyProduct(companyProducts, product.getId());

        BigDecimal minimumOrderQuantity = null;

        if (mayBeCompanyProduct.isPresent()) {
            Entity companyProduct = mayBeCompanyProduct.get();

            minimumOrderQuantity = companyProduct.getDecimalField(CompanyProductFields.MINIMUM_ORDER_QUANTITY);
        } else {
            Entity parent = product.getBelongsToField(ProductFields.PARENT);

            if (Objects.nonNull(parent)) {
                Optional<Entity> mayBeCompanyProductsFamily = deliveriesService.getCompanyProduct(companyProductsFamilies,
                        parent.getId());

                if (mayBeCompanyProductsFamily.isPresent()) {
                    Entity companyProductsFamily = mayBeCompanyProductsFamily.get();

                    minimumOrderQuantity = companyProductsFamily
                            .getDecimalField(CompanyProductFields.MINIMUM_ORDER_QUANTITY);
                }
            }
        }

        return minimumOrderQuantity;
    }

    private BigDecimal getOrderedQuantity(final BigDecimal quantity, final BigDecimal currentStock,
            final BigDecimal neededQuantity, final BigDecimal minimumOrderQuantity) {
        BigDecimal orderedQuantity;

        if (BigDecimal.ZERO.compareTo(quantity) == 0) {
            orderedQuantity = quantity;
        } else {
            orderedQuantity = quantity.subtract(currentStock, numberService.getMathContext()).add(neededQuantity,
                    numberService.getMathContext());

            if (BigDecimal.ZERO.compareTo(orderedQuantity) < 0) {
                if (orderedQuantity.compareTo(minimumOrderQuantity) < 0) {
                    orderedQuantity = minimumOrderQuantity;
                }
            }
        }

        return orderedQuantity;
    }

    private boolean filterByProduct(final Entity orderedProduct, final Entity salesPlanMaterialRequirementProduct) {
        Entity orderedProductProduct = orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT);
        Entity salesPlanMaterialRequirementProductProduct = salesPlanMaterialRequirementProduct
                .getBelongsToField(SalesPlanMaterialRequirementProductFields.PRODUCT);

        return Objects.nonNull(orderedProductProduct) && Objects.nonNull(salesPlanMaterialRequirementProductProduct)
                && orderedProductProduct.getId().equals(salesPlanMaterialRequirementProductProduct.getId());
    }

    private BigDecimal getConversion(final Entity product) {
        String unit = product.getStringField(ProductFields.UNIT);
        String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);

        if (Objects.isNull(additionalUnit)) {
            return BigDecimal.ONE;
        }

        PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                searchCriteriaBuilder -> searchCriteriaBuilder
                        .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

        if (unitConversions.isDefinedFor(additionalUnit)) {
            return unitConversions.asUnitToConversionMap().get(additionalUnit);
        } else {
            return BigDecimal.ZERO;
        }
    }

    private Entity getSalesPlanMaterialRequirement(final Long salesPlanMaterialRequirementId) {
        return getSalesPlanMaterialRequirementDD().get(salesPlanMaterialRequirementId);
    }

    private List<Entity> getSalesPlanMaterialRequirementProducts(final Set<Long> salesPlanMaterialRequirementProductIds) {
        return getSalesPlanMaterialRequirementProductDD().find()
                .add(SearchRestrictions.in("id", salesPlanMaterialRequirementProductIds)).list().getEntities();
    }

    private DataDefinition getSalesPlanMaterialRequirementDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_SALES_PLAN_MATERIAL_REQUIREMENT);
    }

    private DataDefinition getSalesPlanMaterialRequirementProductDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_SALES_PLAN_MATERIAL_REQUIREMENT_PRODUCT);
    }

    public final void showTechnologiesWithUsingProduct(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        GridComponent salesPlanMaterialRequirementProductsGrid = (GridComponent) view
                .getComponentByReference(QcadooViewConstants.L_GRID);

        Entity product = salesPlanMaterialRequirementProductsGrid.getSelectedEntities().get(0)
                .getBelongsToField(SalesPlanMaterialRequirementProductFields.PRODUCT);

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.id", product.getId());

        String url = "../page/technologies/technologiesWithUsingProductList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showSalesPlanDeliveries(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity entity = form.getEntity();

        String salesPlanNumber = entity.getBelongsToField(SalesPlanMaterialRequirementFields.SALES_PLAN)
                .getStringField(SalesPlanFields.NUMBER);

        Map<String, String> filters = Maps.newHashMap();
        filters.put("salesPlanNumber", applyInOperator(salesPlanNumber));

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.deliveries");

        String url = "../page/deliveries/deliveriesList.html";
        view.redirectTo(url, false, true, parameters);
    }

    private String applyInOperator(final String value){
        return "[" + value + "]";
    }
}
