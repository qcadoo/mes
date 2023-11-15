package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.hooks;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.CalculationQuantityService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.ResourceStockDtoFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.WarehouseIssueParameterService;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.IssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.ProductsToIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.model.api.*;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class ProductsToIssueDetailsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private WarehouseIssueParameterService warehouseIssueParameterService;

    @Autowired
    private CalculationQuantityService calculationQuantityService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent productToIssueForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent locationFromLabel = (FieldComponent) view.getComponentByReference("locationFromLabel");
        FieldComponent helperField = (FieldComponent) view.getComponentByReference("generated");

        Entity helper = productToIssueForm.getEntity();
        Entity locationFrom = helper.getBelongsToField("locationFrom");

        if (Objects.nonNull(locationFrom)) {
            locationFromLabel.setFieldValue(translationService.translate(
                    "productFlowThruDivision.productsToIssueHelperDetails.window.mainTab.form.locationFromLabel.label",
                    LocaleContextHolder.getLocale(), locationFrom.getStringField(LocationFields.NUMBER)));
            locationFromLabel.setRequired(true);
        }

        if (view.isViewAfterRedirect() && helper.getHasManyField("issues").isEmpty()) {
            String idsStr = helper.getStringField("productsToIssueIds");
            String[] split = idsStr.split(",");

            List<Long> ids = Lists.newArrayList(split).stream().map(Long::valueOf).collect(Collectors.toList());

            Multimap<Entity, Entity> locationFromProductMultimap = HashMultimap.create();
            Multimap<Entity, Entity> locationToProductMultimap = HashMultimap.create();

            List<Entity> createdIssues = Lists.newArrayList();

            for (Long id : ids) {
                Entity productToIssue = getProductToIssueDD().get(id);
                Entity product = productToIssue.getBelongsToField(ProductsToIssueFields.PRODUCT);

                locationFromProductMultimap.put(productToIssue.getBelongsToField(ProductsToIssueFields.WAREHOUSE_ISSUE)
                        .getBelongsToField(WarehouseIssueFields.PLACE_OF_ISSUE), product);
                locationToProductMultimap.put(productToIssue.getBelongsToField(ProductsToIssueFields.LOCATION), product);

                createdIssues.add(createIssue(getIssueDD(), productToIssue));
            }

            Map<Long, Map<Long, BigDecimal>> stockForWarehousesFrom = getStockForWarehouses(locationFromProductMultimap);
            Map<Long, Map<Long, BigDecimal>> stockForWarehousesTo = getStockForWarehouses(locationToProductMultimap);

            fillLocationQuantity(createdIssues, stockForWarehousesFrom, stockForWarehousesTo);

            helper.setField("issues", sortIssuesBasedOnFilter(view, createdIssues));

            productToIssueForm.setEntity(helper);
        } else {
            List<Entity> issues = helper.getHasManyField("issues");

            if (!issues.isEmpty() && issues.stream().allMatch(issue -> Objects.nonNull(issue.getId()))) {
                WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
                Ribbon ribbon = window.getRibbon();
                RibbonGroup group = ribbon.getGroupByName("actions");
                RibbonActionItem createDocumentsItem = group.getItemByName("createDocuments");
                RibbonActionItem createDocumentsAndGoBackItem = group.getItemByName("createDocumentsAndGoBack");

                createDocumentsItem.setEnabled(!Boolean.valueOf((String) helperField.getFieldValue()));
                createDocumentsItem.requestUpdate(true);
                createDocumentsAndGoBackItem.setEnabled(!Boolean.valueOf((String) helperField.getFieldValue()));
                createDocumentsAndGoBackItem.requestUpdate(true);
            }

            fillQuantitiesInAdditionalUnit(view);
        }

        fillUnits(view);
    }

    private List<Entity> sortIssuesBasedOnFilter(final ViewDefinitionState view, final List<Entity> createdIssues) {
        try {
            sortEntries:
            {
                String jsonKeyName = "window.mainTab.form.gridProductNumberFilter";

                final String gridProductNumberFilter;

                JSONObject jsonContext = view.getJsonContext();

                if (jsonContext.has(jsonKeyName) && isNotBlank(gridProductNumberFilter = jsonContext.getString(jsonKeyName))) {
                    String[] productsNumbers = gridProductNumberFilter.substring(1, gridProductNumberFilter.length() - 1)
                            .split(",");

                    List<String> productNumbersList = new ArrayList<>(
                            Arrays.stream(productsNumbers).map(String::trim).filter(s -> !s.isEmpty()).map(String::toUpperCase)
                                    .collect(Collectors.toCollection(LinkedHashSet::new)));

                    if (productNumbersList.isEmpty()) {
                        break sortEntries;
                    }

                    Comparator<Entity> comparator = Comparator
                            .comparing(e -> productNumbersList.indexOf(e.getBelongsToField(ProductsToIssueFields.PRODUCT)
                                    .getStringField(ProductFields.NUMBER).toUpperCase()));

                    createdIssues.sort(comparator);
                }
            }

            return createdIssues;
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    private void fillLocationQuantity(final List<Entity> createdIssues,
                                      final Map<Long, Map<Long, BigDecimal>> stockForWarehousesFrom,
                                      final Map<Long, Map<Long, BigDecimal>> stockForWarehousesTo) {
        for (Entity issue : createdIssues) {
            BigDecimal quantityFrom = stockForWarehousesFrom
                    .get(issue.getBelongsToField(IssueFields.WAREHOUSE_ISSUE)
                            .getBelongsToField(WarehouseIssueFields.PLACE_OF_ISSUE).getId())
                    .get(issue.getBelongsToField(IssueFields.PRODUCT).getId());
            BigDecimal quantityTo = stockForWarehousesTo.get(issue.getBelongsToField(IssueFields.LOCATION).getId())
                    .get(issue.getBelongsToField(IssueFields.PRODUCT).getId());

            issue.setField(IssueFields.LOCATIONS_QUANTITY, quantityFrom);
            issue.setField(IssueFields.LOCATION_TO_QUANTITY, quantityTo);
        }
    }

    private Map<Long, Map<Long, BigDecimal>> getStockForWarehouses(final Multimap<Entity, Entity> locationProductMultimap) {
        Map<Long, Map<Long, BigDecimal>> stockForWarehouses = Maps.newHashMap();

        for (Entity warehouse : locationProductMultimap.keySet()) {
            List<Entity> products = Lists.newArrayList(locationProductMultimap.get(warehouse));

            Map<Long, BigDecimal> stockMap = materialFlowResourcesService.getQuantitiesForProductsAndLocation(products, warehouse,
                    false, ResourceStockDtoFields.QUANTITY);

            stockForWarehouses.put(warehouse.getId(), stockMap);
        }

        return stockForWarehouses;
    }

    public void fillQuantitiesInAdditionalUnit(final ViewDefinitionState view) {
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference("issues");

        for (FormComponent form : adl.getFormComponents()) {
            Entity issue = form.getPersistedEntityWithIncludedFormValues();

            FieldComponent quantityField = form.findFieldComponentByName("issueQuantity");

            Either<Exception, Optional<BigDecimal>> maybeQuantity = BigDecimalUtils
                    .tryParse(quantityField.getFieldValue().toString(), LocaleContextHolder.getLocale());

            BigDecimal conversion = issue.getDecimalField(IssueFields.CONVERSION);

            if (Objects.nonNull(conversion) && maybeQuantity.isRight() && maybeQuantity.getRight().isPresent()) {
                Entity product = issue.getBelongsToField(IssueFields.PRODUCT);
                String unit = product.getStringField(ProductFields.UNIT);
                String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
                BigDecimal issueQuantity = maybeQuantity.getRight().get();

                BigDecimal issuedQuantityAdditionalUnit = calculationQuantityService.calculateAdditionalQuantity(issueQuantity,
                        conversion, Optional.fromNullable(additionalUnit).or(unit));

                issue.setField(IssueFields.ISSUE_QUANTITY_ADDITIONAL_UNIT, issuedQuantityAdditionalUnit);

                form.setEntity(issue);
            }
        }
    }

    public void fillUnits(final ViewDefinitionState view) {
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference("issues");

        for (FormComponent form : adl.getFormComponents()) {
            Entity issue = form.getPersistedEntityWithIncludedFormValues();

            Entity product = issue.getBelongsToField(IssueFields.PRODUCT);

            if (Objects.nonNull(product)) {
                FieldComponent unitField = form.findFieldComponentByName(ProductFields.UNIT);
                FieldComponent additionalUnitField = form.findFieldComponentByName(ProductFields.ADDITIONAL_UNIT);

                unitField.setFieldValue(product.getStringField(ProductFields.UNIT));
                String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);

                if (StringUtils.isEmpty(additionalUnit)) {
                    additionalUnitField.setFieldValue(product.getStringField(ProductFields.UNIT));
                } else {
                    additionalUnitField.setFieldValue(additionalUnit);
                }
            }
        }
    }

    private Entity createIssue(final DataDefinition issueDD, final Entity productToIssue) {
        Entity warehouseIssue = productToIssue.getBelongsToField(ProductsToIssueFields.WAREHOUSE_ISSUE);
        BigDecimal demandQuantity = productToIssue.getDecimalField(ProductsToIssueFields.DEMAND_QUANTITY);
        BigDecimal issuedQuantity = productToIssue.getDecimalField(ProductsToIssueFields.ISSUE_QUANTITY);
        BigDecimal quantityPerUnit = null;
        BigDecimal issueQuantity = BigDecimal.ZERO;
        BigDecimal issuedQuantityAdditionalUnit = BigDecimal.ZERO;
        BigDecimal conversion = productToIssue.getDecimalField(ProductsToIssueFields.CONVERSION);
        BigDecimal correction = java.util.Optional.ofNullable(productToIssue.getDecimalField(ProductsToIssueFields.CORRECTION))
                .orElse(BigDecimal.ZERO);

        if (warehouseIssueParameterService.issueForOrder()) {
            Entity order = getOrderDD().get(warehouseIssue.getBelongsToField(WarehouseIssueFields.ORDER).getId());
            BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);

            if (Objects.nonNull(plannedQuantity) && Objects.nonNull(demandQuantity)) {
                quantityPerUnit = numberService
                        .setScaleWithDefaultMathContext(demandQuantity.divide(plannedQuantity, numberService.getMathContext()));
            }
        }

        Entity product = productToIssue.getBelongsToField(ProductsToIssueFields.PRODUCT);
        String additionalOrPrimaryUnit = Optional.fromNullable(product.getStringField(ProductFields.ADDITIONAL_UNIT))
                .or(product.getStringField(ProductFields.UNIT));

        if (Objects.nonNull(demandQuantity) && Objects.nonNull(issuedQuantity)) {
            issueQuantity = demandQuantity.subtract(issuedQuantity).subtract(correction);

            if (Objects.nonNull(conversion)) {
                issuedQuantityAdditionalUnit = calculationQuantityService.calculateAdditionalQuantity(issueQuantity, conversion,
                        additionalOrPrimaryUnit);
            }
        }

        if (issueQuantity.compareTo(BigDecimal.ZERO) < 0) {
            issueQuantity = BigDecimal.ZERO;
            issuedQuantityAdditionalUnit = BigDecimal.ZERO;
        }

        Entity locationTo = productToIssue.getBelongsToField(ProductsToIssueFields.LOCATION);

        Entity issue = issueDD.create();
        issue.setField(IssueFields.PRODUCT, product);

        if (Objects.nonNull(conversion) && Objects.nonNull(demandQuantity)) {
            issue.setField(IssueFields.CONVERSION, conversion);

            BigDecimal newAdditionalQuantity = calculationQuantityService.calculateAdditionalQuantity(demandQuantity, conversion,
                    additionalOrPrimaryUnit);

            issue.setField(IssueFields.ADDITIONAL_DEMAND_QUANTITY, newAdditionalQuantity);
        }

        issue.setField(IssueFields.LOCATION, locationTo);
        issue.setField(IssueFields.WAREHOUSE_ISSUE, warehouseIssue);
        issue.setField(IssueFields.DEMAND_QUANTITY, demandQuantity);
        issue.setField(IssueFields.QUANTITY_PER_UNIT, quantityPerUnit);
        issue.setField(IssueFields.ISSUE_QUANTITY, issueQuantity);
        issue.setField(IssueFields.ISSUED, false);
        issue.setField(IssueFields.ISSUE_QUANTITY_ADDITIONAL_UNIT, issuedQuantityAdditionalUnit);
        issue.setField(IssueFields.PRODUCTS_TO_ISSUE_ID, productToIssue.getId());

        return issue;
    }

    private DataDefinition getOrderDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }

    private DataDefinition getProductToIssueDD() {
        return dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_PRODUCTS_TO_ISSUE);
    }

    private DataDefinition getIssueDD() {
        return dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_ISSUE);
    }

}
