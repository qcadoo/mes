/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.deliveries;

import com.google.common.collect.Lists;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.*;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.deliveries.constants.*;
import com.qcadoo.mes.deliveries.print.DeliveryProduct;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.*;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeliveriesServiceImpl implements DeliveriesService {

    private static final String L_PRODUCT = "product";

    private static final String L_SHOW_PRODUCT = "showProduct";

    private static final String L_PRICE_PER_UNIT = "pricePerUnit";

    private static final String L_TOTAL_PRICE = "totalPrice";

    private static final String L_CURRENCY = "currency";

    private static final String L_OPERATION = "operation";

    private static final String L_OFFER = "offer";

    private static final String L_EXPIRATION_DATE = "expirationDate";

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Override
    public Entity getDelivery(final Long deliveryId) {
        return getDeliveryDD().get(deliveryId);
    }

    @Override
    public Entity getOrderedProduct(final Long orderedProductId) {
        return getOrderedProductDD().get(orderedProductId);
    }

    @Override
    public Entity getDeliveredProduct(final Long deliveredProductId) {
        return getDeliveredProductDD().get(deliveredProductId);
    }

    @Override
    public Entity getCompanyProduct(final Long companyProductId) {
        return getCompanyProductDD().get(companyProductId);
    }

    @Override
    public List<Entity> getColumnsForDeliveries() {
        boolean hasCurrentUserRole = securityService.hasCurrentUserRole("ROLE_DELIVERIES_PRICE");
        List<Entity> columnsForDeliveries = Lists.newArrayList();

        List<Entity> sortedColumnsForDeliveries = getColumnForDeliveriesDD().find()
                .addOrder(SearchOrders.asc(ColumnForDeliveriesFields.SUCCESSION)).list().getEntities();

        Entity successionColumn = getColumnForDeliveriesDD().find()
                .add(SearchRestrictions.eq(ColumnForDeliveriesFields.IDENTIFIER, ColumnForDeliveriesFields.SUCCESSION))
                .setMaxResults(1).uniqueResult();

        columnsForDeliveries.add(successionColumn);

        for (Entity columnForDeliveries : sortedColumnsForDeliveries) {
            if (!columnForDeliveries.getStringField(ColumnForDeliveriesFields.IDENTIFIER).equals(DeliveredProductFields.PRICE_PER_UNIT)
                    && !columnForDeliveries.getStringField(ColumnForDeliveriesFields.IDENTIFIER).equals(DeliveredProductFields.TOTAL_PRICE) &&
                    !columnForDeliveries.getStringField(ColumnForDeliveriesFields.IDENTIFIER)
                            .equals(successionColumn.getStringField(ColumnForDeliveriesFields.IDENTIFIER)) ||
                    (columnForDeliveries.getStringField(ColumnForDeliveriesFields.IDENTIFIER).equals(DeliveredProductFields.PRICE_PER_UNIT)
                            || columnForDeliveries.getStringField(ColumnForDeliveriesFields.IDENTIFIER).equals(DeliveredProductFields.TOTAL_PRICE))
                            && hasCurrentUserRole) {
                columnsForDeliveries.add(columnForDeliveries);
            }
        }

        return columnsForDeliveries;
    }

    @Override
    public List<Entity> getColumnsForOrders() {
        List<Entity> columnsForOrders = Lists.newArrayList();

        List<Entity> sortedParameterDeliveryOrderColumns = getParameterDeliveryOrderColumnDD().find()
                .addOrder(SearchOrders.asc(ColumnForOrdersFields.SUCCESSION)).list().getEntities();

        for (Entity parameterDeliveryOrderColumn : sortedParameterDeliveryOrderColumns) {
            Entity columnDefinition = parameterDeliveryOrderColumn
                    .getBelongsToField(ParameterDeliveryOrderColumnFields.COLUMN_FOR_ORDERS);

            columnsForOrders.add(columnDefinition);
        }

        return columnsForOrders;
    }

    @Override
    public DataDefinition getDeliveryDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY);
    }

    public DataDefinition getProductDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }

    @Override
    public DataDefinition getOrderedProductDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_ORDERED_PRODUCT);
    }

    @Override
    public DataDefinition getDeliveredProductDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERED_PRODUCT);
    }

    @Override
    public DataDefinition getCompanyProductDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COMPANY_PRODUCT);
    }

    @Override
    public DataDefinition getColumnForDeliveriesDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COLUMN_FOR_DELIVERIES);
    }

    @Override
    public DataDefinition getParameterDeliveryOrderColumnDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_PARAMETER_DELIVERY_ORDER_COLUMN);
    }

    @Override
    public Entity getProduct(final DeliveryProduct deliveryProduct) {
        if (Objects.isNull(deliveryProduct.getOrderedProductId())) {
            return getDeliveredProduct(deliveryProduct.getDeliveredProductId()).getBelongsToField(DeliveredProductFields.PRODUCT);
        } else {
            return getOrderedProduct(deliveryProduct.getOrderedProductId()).getBelongsToField(OrderedProductFields.PRODUCT);
        }
    }

    @Override
    public String getDescriptionDefaultValue() {
        Entity parameter = parameterService.getParameter();

        return parameter.getStringField(ParameterFieldsD.DEFAULT_DESCRIPTION);
    }

    @Override
    public String getDeliveryAddressDefaultValue() {
        Entity parameter = parameterService.getParameter();

        if (DefaultAddressType.OTHER.getStringValue().equals(parameter.getStringField(ParameterFieldsD.DEFAULT_ADDRESS))) {
            return parameter.getStringField(ParameterFieldsD.OTHER_ADDRESS);
        } else {
            return generateAddressFromCompany(companyService.getCompany());
        }
    }

    @Override
    public String generateAddressFromCompany(final Entity company) {
        StringBuilder address = new StringBuilder();

        if (Objects.nonNull(company)) {
            String street = company.getStringField(CompanyFields.STREET);
            String house = company.getStringField(CompanyFields.HOUSE);
            String flat = company.getStringField(CompanyFields.FLAT);
            String zipCode = company.getStringField(CompanyFields.ZIP_CODE);
            String city = company.getStringField(CompanyFields.CITY);

            if (StringUtils.isNotEmpty(street)) {
                address.append(street);
                if (StringUtils.isNotEmpty(house)) {
                    address.append(" ");
                    address.append(house);

                    if (StringUtils.isNotEmpty(flat)) {
                        address.append("/");
                        address.append(flat);
                    }
                }
                if (StringUtils.isNotEmpty(city)) {
                    address.append(", ");
                }
            }
            if (StringUtils.isNotEmpty(city)) {
                if (StringUtils.isNotEmpty(zipCode)) {
                    address.append(zipCode);
                    address.append(" ");
                }
                address.append(city);
            }
        }

        return address.toString();
    }

    @Override
    public void fillUnitFields(final ViewDefinitionState view, final String productName,
                               final List<String> referenceNames) {
        Entity product = getProductEntityByComponentName(view, productName);

        fillUnitFields(view, product, referenceNames);
    }

    public void fillUnitFields(final ViewDefinitionState view, final Entity product,
                               final List<String> referenceNames) {
        String unit = "";

        if (Objects.nonNull(product)) {
            unit = product.getStringField(ProductFields.UNIT);
        }

        setFieldValues(view, referenceNames, unit);
    }

    @Override
    public void fillUnitFields(final ViewDefinitionState view, final Entity product, final List<String> referenceNames,
                               final List<String> additionalUnitNames) {
        fillUnitFields(view, product, referenceNames);
        String additionalUnit = "";

        if (Objects.nonNull(product)) {
            additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);

            if (Objects.isNull(additionalUnit)) {
                additionalUnit = product.getStringField(ProductFields.UNIT);
            }
        }

        setFieldValues(view, additionalUnitNames, additionalUnit);
    }

    @Override
    public void fillUnitFields(final ViewDefinitionState view, final String productName,
                               final List<String> referenceNames,
                               final List<String> additionalUnitNames) {
        Entity product = getProductEntityByComponentName(view, productName);

        fillUnitFields(view, product, referenceNames, additionalUnitNames);
    }

    @Override
    public void fillCurrencyFields(final ViewDefinitionState view, final List<String> referenceNames) {
        String currency = currencyService.getCurrencyAlphabeticCode();

        if (StringUtils.isEmpty(currency)) {
            return;
        }

        setFieldValues(view, referenceNames, currency);
    }

    @Override
    public void fillCurrencyFieldsForDelivery(final ViewDefinitionState view, final List<String> referenceNames,
                                              final Entity delivery) {
        String currency = getCurrency(delivery);

        if (Objects.isNull(currency)) {
            return;
        }

        setFieldValues(view, referenceNames, currency);
    }

    private void setFieldValues(final ViewDefinitionState view, final List<String> referenceNames, final String value) {
        for (String reference : referenceNames) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(reference);

            fieldComponent.setFieldValue(value);
            fieldComponent.requestComponentUpdateState();
        }
    }

    @Override
    public String getCurrency(final Entity delivery) {
        if (Objects.isNull(delivery)) {
            return "";
        }

        Entity currency = delivery.getBelongsToField(DeliveryFields.CURRENCY);

        if (Objects.isNull(currency)) {
            return currencyService.getCurrencyAlphabeticCode();
        } else {
            return currency.getDataDefinition().get(currency.getId()).getStringField(CurrencyFields.ALPHABETIC_CODE);
        }
    }

    @Override
    public void recalculatePriceFromTotalPrice(final ViewDefinitionState view, final String quantityFieldReference) {
        if (!isValidDecimalField(view, Arrays.asList(L_PRICE_PER_UNIT, L_TOTAL_PRICE, quantityFieldReference))) {
            return;
        }

        FieldComponent quantityField = (FieldComponent) view.getComponentByReference(quantityFieldReference);
        FieldComponent totalPriceField = (FieldComponent) view.getComponentByReference(L_TOTAL_PRICE);

        if (StringUtils.isNotEmpty((String) quantityField.getFieldValue())
                && StringUtils.isNotEmpty((String) totalPriceField.getFieldValue())) {
            calculatePriceUsingTotalCost(view, quantityField, totalPriceField);
        }
    }

    private void calculatePriceUsingTotalCost(final ViewDefinitionState view, final FieldComponent quantityField,
                                              final FieldComponent totalPriceField) {
        FieldComponent pricePerUnitField = (FieldComponent) view.getComponentByReference(L_PRICE_PER_UNIT);

        Locale locale = view.getLocale();

        BigDecimal quantity = getBigDecimalFromField(quantityField, locale);
        BigDecimal totalPrice = getBigDecimalFromField(totalPriceField, locale);
        BigDecimal pricePerUnit = numberService
                .setScaleWithDefaultMathContext(totalPrice.divide(quantity, numberService.getMathContext()));

        pricePerUnitField.setFieldValue(numberService.format(pricePerUnit));
        pricePerUnitField.requestComponentUpdateState();
    }

    @Override
    public void recalculatePriceFromPricePerUnit(final ViewDefinitionState view, final String quantityFieldReference) {
        if (!isValidDecimalField(view, Arrays.asList(L_PRICE_PER_UNIT, L_TOTAL_PRICE, quantityFieldReference))) {
            return;
        }

        FieldComponent quantityField = (FieldComponent) view.getComponentByReference(quantityFieldReference);
        FieldComponent pricePerUnitField = (FieldComponent) view.getComponentByReference(L_PRICE_PER_UNIT);

        if (StringUtils.isNotEmpty((String) quantityField.getFieldValue())
                && StringUtils.isNotEmpty((String) pricePerUnitField.getFieldValue())) {
            calculatePriceUsingPricePerUnit(view, quantityField, pricePerUnitField);
        }
    }

    private void calculatePriceUsingPricePerUnit(final ViewDefinitionState view, final FieldComponent quantityField,
                                                 final FieldComponent pricePerUnitField) {
        FieldComponent totalPriceField = (FieldComponent) view.getComponentByReference(L_TOTAL_PRICE);

        Locale locale = view.getLocale();

        BigDecimal pricePerUnit = getBigDecimalFromField(pricePerUnitField, locale);
        BigDecimal quantity = getBigDecimalFromField(quantityField, locale);
        BigDecimal totalPrice = numberService
                .setScaleWithDefaultMathContext(pricePerUnit.multiply(quantity, numberService.getMathContext()));

        totalPriceField.setFieldValue(numberService.format(totalPrice));
        totalPriceField.requestComponentUpdateState();
    }

    @Override
    public void recalculatePrice(final ViewDefinitionState view, final String quantityFieldReference) {
        if (!isValidDecimalField(view, Arrays.asList(L_PRICE_PER_UNIT, L_TOTAL_PRICE, quantityFieldReference))) {
            return;
        }

        FieldComponent quantityField = (FieldComponent) view.getComponentByReference(quantityFieldReference);
        FieldComponent pricePerUnitField = (FieldComponent) view.getComponentByReference(L_PRICE_PER_UNIT);
        FieldComponent totalPriceField = (FieldComponent) view.getComponentByReference(L_TOTAL_PRICE);

        if (StringUtils.isNotEmpty((String) quantityField.getFieldValue())
                && StringUtils.isNotEmpty((String) pricePerUnitField.getFieldValue())) {
            calculatePriceUsingPricePerUnit(view, quantityField, pricePerUnitField);
        } else if (StringUtils.isNotEmpty((String) quantityField.getFieldValue())
                && StringUtils.isNotEmpty((String) totalPriceField.getFieldValue())) {
            calculatePriceUsingTotalCost(view, quantityField, totalPriceField);
        }
    }

    @Override
    public BigDecimal getBigDecimalFromField(final FieldComponent fieldComponent, final Locale locale) {
        Object value = fieldComponent.getFieldValue();

        try {
            DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(locale);
            format.setParseBigDecimal(true);

            return BigDecimal.valueOf(format.parse(value.toString()).doubleValue());
        } catch (ParseException e) {
            return null;
        }
    }

    private boolean isValidDecimalField(final ViewDefinitionState view, final List<String> fieldNames) {
        boolean isValid = true;

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity entity = form.getEntity();

        for (String fieldName : fieldNames) {
            try {
                entity.getDecimalField(fieldName);
            } catch (IllegalArgumentException e) {
                form.findFieldComponentByName(fieldName).addMessage("qcadooView.validate.field.error.invalidNumericFormat",
                        MessageType.FAILURE);

                isValid = false;
            }
        }

        return isValid;
    }

    @Override
    public void calculatePricePerUnit(final Entity entity, final String quantityFieldName) {
        BigDecimal totalPrice = entity.getDecimalField(OrderedProductFields.TOTAL_PRICE);
        BigDecimal pricePerUnit = entity.getDecimalField(OrderedProductFields.PRICE_PER_UNIT);
        BigDecimal quantity = entity.getDecimalField(quantityFieldName);

        boolean save = true;

        if ((Objects.nonNull(pricePerUnit) && changedFieldValue(entity, pricePerUnit, OrderedProductFields.PRICE_PER_UNIT))
                || (Objects.nonNull(pricePerUnit) && Objects.isNull(totalPrice))) {
            totalPrice = numberService.setScaleWithDefaultMathContext(calculateTotalPrice(quantity, pricePerUnit));
        } else if ((Objects.nonNull(totalPrice) && changedFieldValue(entity, totalPrice, OrderedProductFields.TOTAL_PRICE))
                || (Objects.nonNull(totalPrice) && Objects.isNull(pricePerUnit))) {
            pricePerUnit = numberService.setScaleWithDefaultMathContext(calculatePricePerUnit(quantity, totalPrice));
        } else {
            save = false;
        }

        if (save) {
            entity.setField(L_PRICE_PER_UNIT, pricePerUnit);
            entity.setField(L_TOTAL_PRICE, totalPrice);
        }
    }

    private boolean changedFieldValue(final Entity entity, final BigDecimal fieldValue, final String reference) {
        if (Objects.isNull(entity.getId())) {
            return true;
        }

        Entity entityFromDB = entity.getDataDefinition().get(entity.getId());

        return Objects.isNull(entityFromDB.getDecimalField(reference))
                || (fieldValue.compareTo(entityFromDB.getDecimalField(reference)) != 0);
    }

    private BigDecimal calculatePricePerUnit(final BigDecimal quantity, final BigDecimal totalPrice) {
        BigDecimal pricePerUnit;

        if (Objects.isNull(quantity) || (BigDecimal.ZERO.compareTo(quantity) == 0)) {
            pricePerUnit = null;
        } else {
            pricePerUnit = totalPrice.divide(quantity, numberService.getMathContext());
        }

        return pricePerUnit;
    }

    public BigDecimal calculateTotalPrice(final BigDecimal quantity, final BigDecimal pricePerUnit) {
        BigDecimal totalPrice;

        if (Objects.isNull(quantity) || (BigDecimal.ZERO.compareTo(quantity) == 0)) {
            totalPrice = BigDecimal.ZERO;
        } else {
            totalPrice = pricePerUnit.multiply(quantity, numberService.getMathContext());
        }

        return totalPrice;
    }

    @Override
    public List<Entity> getColumnsWithFilteredCurrencies(final List<Entity> columns) {
        List<Entity> filteredCurrencyColumn = Lists.newArrayList();

        if (checkIfContainsPriceColumns(columns)) {
            filteredCurrencyColumn.addAll(columns);
        } else {
            for (Entity column : columns) {
                String identifier = column.getStringField(ColumnForOrdersFields.IDENTIFIER);

                if (!L_CURRENCY.equals(identifier)) {
                    filteredCurrencyColumn.add(column);
                }
            }
        }

        return filteredCurrencyColumn;
    }

    private boolean checkIfContainsPriceColumns(final List<Entity> columns) {
        boolean contains = false;

        for (Entity column : columns) {
            String identifier = column.getStringField(ColumnForOrdersFields.IDENTIFIER);

            if (L_PRICE_PER_UNIT.equals(identifier) || L_TOTAL_PRICE.equals(identifier)) {
                contains = true;
            }
        }

        return contains;
    }

    @Override
    public void disableShowProductButton(final ViewDefinitionState view) {
        GridComponent orderedProductGrid = (GridComponent) view.getComponentByReference(DeliveryFields.ORDERED_PRODUCTS);
        GridComponent deliveredProductsGrid = (GridComponent) view.getComponentByReference(DeliveryFields.DELIVERED_PRODUCTS);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup product = window.getRibbon().getGroupByName(L_PRODUCT);
        RibbonActionItem showProduct = product.getItemByName(L_SHOW_PRODUCT);

        int sizeOfSelectedEntitiesOrderedGrid = orderedProductGrid.getSelectedEntities().size();
        int sizeOfSelectedEntitiesDeliveredGrid = deliveredProductsGrid.getSelectedEntities().size();

        boolean isEnabled = ((sizeOfSelectedEntitiesOrderedGrid == 1) && (sizeOfSelectedEntitiesDeliveredGrid == 0))
                || ((sizeOfSelectedEntitiesOrderedGrid == 0) && (sizeOfSelectedEntitiesDeliveredGrid == 1));

        showProduct.setEnabled(isEnabled);
        showProduct.requestUpdate(true);
        window.requestRibbonRender();
    }

    private Entity getProductEntityByComponentName(final ViewDefinitionState view, final String productName) {
        ComponentState productComponentState = view.getComponentByReference(productName);

        Entity product = null;

        if (productComponentState instanceof LookupComponent) {
            product = ((LookupComponent) productComponentState).getEntity();
        }

        return product;
    }

    public Optional<Entity> getDefaultSupplier(final Long productId) {
        Entity product = getProductDD().get(productId);

        if (Objects.nonNull(product) && ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue()
                .equals(product.getStringField(ProductFields.ENTITY_TYPE))) {

            Entity defaultSupplier = getDefaultSupplierForParticularProduct(productId);

            if (Objects.nonNull(defaultSupplier)) {
                return Optional.of(defaultSupplier);
            } else {
                return Optional.ofNullable(getDefaultSupplierForProductsFamily(productId));
            }
        }

        return Optional.empty();
    }

    public Optional<Entity> getDefaultSupplierWithIntegration(final Long productId) {
        Entity product = getProductDD().get(productId);

        if (Objects.nonNull(product) && ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue()
                .equals(product.getStringField(ProductFields.ENTITY_TYPE))) {

            Entity defaultSupplier = getDefaultSupplierForParticularProduct(productId);

            if (Objects.nonNull(defaultSupplier)) {
                return Optional.of(defaultSupplier.getBelongsToField(CompanyProductFields.COMPANY));
            } else {
                defaultSupplier = getDefaultSupplierForProductsFamily(productId);

                if (Objects.nonNull(defaultSupplier)) {
                    return Optional.of(defaultSupplier.getBelongsToField(CompanyProductFields.COMPANY));
                }
            }
        }

        return getIntegrationDefaultSupplier();
    }

    public List<Entity> getSuppliersWithIntegration(final Long productId) {
        List<Entity> suppliers = getSuppliersForProductsFamily(productId);

        suppliers.addAll(getSuppliersForParticularProduct(productId));

        getIntegrationDefaultSupplier().ifPresent(suppliers::add);

        return suppliers;
    }

    private Entity getDefaultSupplierForProductsFamily(final Long productId) {
        Entity product = getProductDD().get(productId);

        Entity productFamily = product.getBelongsToField(ProductFields.PARENT);

        if (Objects.nonNull(productFamily)) {
            return getDefaultSupplierForParticularProduct(productFamily.getId());
        } else {
            return null;
        }
    }

    private Entity getDefaultSupplierForParticularProduct(final Long productId) {
        String query = "select company from #deliveries_companyProduct company where company.product.id = :id"
                + " and company.isDefault = true";

        return getCompanyProductDD().find(query).setParameter("id", productId).setMaxResults(1).uniqueResult();
    }

    private Optional<Entity> getIntegrationDefaultSupplier() {
        return Optional.ofNullable(parameterService.getParameter().getBelongsToField("companyName"));
    }

    private List<Entity> getSuppliersForProductsFamily(final Long productId) {
        Entity product = getProductDD().get(productId);

        Entity productFamily = product.getBelongsToField(ProductFields.PARENT);

        if (Objects.nonNull(productFamily)) {
            return getSuppliersForParticularProduct(productFamily.getId());
        } else {
            return new ArrayList<>();
        }
    }

    private List<Entity> getSuppliersForParticularProduct(final Long productId) {
        String query = "select company.company from #deliveries_companyProduct company where company.product.id = :id";

        return getCompanyProductDD().find(query).setParameter("id", productId).list().getEntities();
    }

    public List<Entity> getCompanyProducts(final Set<Long> productIds) {
        List<Entity> companyProducts = Lists.newArrayList();

        if (!productIds.isEmpty()) {
            companyProducts = getCompanyProductDD().find()
                    .createAlias(CompanyProductFields.PRODUCT, CompanyProductFields.PRODUCT, JoinType.LEFT)
                    .add(SearchRestrictions.in(CompanyProductFields.PRODUCT + L_DOT + L_ID, productIds))
                    .add(SearchRestrictions.eq(CompanyProductFields.IS_DEFAULT, true)).list().getEntities();
        }

        return companyProducts;
    }

    public Optional<Entity> getCompanyProduct(final List<Entity> companyProducts, final Long productId) {
        return companyProducts.stream().filter(
                        companyProduct -> companyProduct.getBelongsToField(CompanyProductFields.PRODUCT).getId().equals(productId))
                .findAny();
    }

    public List<Entity> getSelectedOrderedProducts(final GridComponent orderedProductGrid) {
        List<Entity> result = Lists.newArrayList();

        Set<Long> ids = orderedProductGrid.getSelectedEntitiesIds();

        if (Objects.nonNull(ids) && !ids.isEmpty()) {
            final SearchCriteriaBuilder searchCriteria = getOrderedProductDD().find();

            searchCriteria.add(SearchRestrictions.in("id", ids));

            result = searchCriteria.list().getEntities();
        }

        return result;
    }

    public Optional<Entity> getOrderedProductForDeliveredProduct(final Entity deliveredProduct) {
        return getOrderedProductForDeliveredProduct(deliveredProduct, null, null, null);
    }

    public Optional<Entity> getOrderedProductForDeliveredProduct(final Entity deliveredProduct,
                                                                 final SearchCriterion batchCustomSearchCriterion,
                                                                 final SearchCriterion offerCustomSearchCriterion,
                                                                 final SearchCriterion operationCustomSearchCriterion) {
        SearchCriteriaBuilder searchCriteriaBuilder = getSearchCriteriaBuilderForOrderedProduct(getOrderedProductDD().find(),
                deliveredProduct, batchCustomSearchCriterion, offerCustomSearchCriterion, operationCustomSearchCriterion);

        Entity orderedProduct = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

        return Optional.ofNullable(orderedProduct);
    }

    public Optional<Entity> getSuitableOrderedProductForDeliveredProduct(final Entity deliveredProduct) {
        Entity batch = deliveredProduct.getBelongsToField(DeliveredProductFields.BATCH);
        Entity offer = deliveredProduct.getBelongsToField(L_OFFER);
        Entity operation = deliveredProduct.getBelongsToField(L_OPERATION);

        if (Objects.nonNull(batch) && Objects.nonNull(offer) && Objects.nonNull(operation)) {
            return gerOrderedProductForDeliveredProductWhenAllNotNull(deliveredProduct);
        } else if (Objects.nonNull(batch) && Objects.nonNull(offer)) {
            return getOrderedProductForDeliveredProductWhenOperationIsNull(deliveredProduct);
        } else if (Objects.nonNull(batch) && Objects.nonNull(operation)) {
            return getOrderedProductForDeliveredProductWhenOfferIsNull(deliveredProduct);
        } else if (Objects.nonNull(offer) && Objects.nonNull(operation)) {
            return getOrderedProductForDeliveredProductWhenBatchIsNull(deliveredProduct);
        }

        return getOrderedProductForDeliveredProductWithAllNull(deliveredProduct);
    }

    private Optional<Entity> gerOrderedProductForDeliveredProductWhenAllNotNull(final Entity deliveredProduct) {
        Optional<Entity> mayBeOrderedProduct;

        mayBeOrderedProduct = getOrderedProductForDeliveredProductWithBatchAndOffer(deliveredProduct);

        if (mayBeOrderedProduct.isPresent()) {
            return mayBeOrderedProduct;
        }

        mayBeOrderedProduct = getOrderedProductForDeliveredProductWithBatchAndOperation(deliveredProduct);

        if (mayBeOrderedProduct.isPresent()) {
            return mayBeOrderedProduct;
        }

        mayBeOrderedProduct = getOrderedProductForDeliveredProductWithOfferAndOperation(deliveredProduct);

        if (mayBeOrderedProduct.isPresent()) {
            return mayBeOrderedProduct;
        }

        mayBeOrderedProduct = getOrderedProductForDeliveredProductWithBatch(deliveredProduct);

        if (mayBeOrderedProduct.isPresent()) {
            return mayBeOrderedProduct;
        }

        return getOrderedProductForDeliveredProductWhenBatchIsNull(deliveredProduct);
    }

    private Optional<Entity> getOrderedProductForDeliveredProductWhenBatchIsNull(final Entity deliveredProduct) {
        Optional<Entity> mayBeOrderedProduct;

        mayBeOrderedProduct = getOrderedProductForDeliveredProductWithOffer(deliveredProduct);

        if (mayBeOrderedProduct.isPresent()) {
            return mayBeOrderedProduct;
        }

        mayBeOrderedProduct = getOrderedProductForDeliveredProductWithOperation(deliveredProduct);

        if (mayBeOrderedProduct.isPresent()) {
            return mayBeOrderedProduct;
        }

        return getOrderedProductForDeliveredProductWithAllNull(deliveredProduct);
    }

    private Optional<Entity> getOrderedProductForDeliveredProductWhenOfferIsNull(final Entity deliveredProduct) {
        Optional<Entity> mayBeOrderedProduct;

        mayBeOrderedProduct = getOrderedProductForDeliveredProductWithBatch(deliveredProduct);

        if (mayBeOrderedProduct.isPresent()) {
            return mayBeOrderedProduct;
        }

        mayBeOrderedProduct = getOrderedProductForDeliveredProductWithOperation(deliveredProduct);

        if (mayBeOrderedProduct.isPresent()) {
            return mayBeOrderedProduct;
        }

        return getOrderedProductForDeliveredProductWithAllNull(deliveredProduct);
    }

    private Optional<Entity> getOrderedProductForDeliveredProductWhenOperationIsNull(final Entity deliveredProduct) {
        Optional<Entity> mayBeOrderedProduct;

        mayBeOrderedProduct = getOrderedProductForDeliveredProductWithBatch(deliveredProduct);

        if (mayBeOrderedProduct.isPresent()) {
            return mayBeOrderedProduct;
        }

        mayBeOrderedProduct = getOrderedProductForDeliveredProductWithOffer(deliveredProduct);

        if (mayBeOrderedProduct.isPresent()) {
            return mayBeOrderedProduct;
        }

        return getOrderedProductForDeliveredProductWithAllNull(deliveredProduct);
    }

    private Optional<Entity> getOrderedProductForDeliveredProductWithBatchAndOffer(final Entity deliveredProduct) {
        return getOrderedProductForDeliveredProduct(deliveredProduct, null, null, SearchRestrictions.isNull(L_OPERATION));
    }

    private Optional<Entity> getOrderedProductForDeliveredProductWithBatchAndOperation(final Entity deliveredProduct) {
        return getOrderedProductForDeliveredProduct(deliveredProduct, null, SearchRestrictions.isNull(L_OFFER), null);
    }

    private Optional<Entity> getOrderedProductForDeliveredProductWithOfferAndOperation(final Entity deliveredProduct) {
        return getOrderedProductForDeliveredProduct(deliveredProduct, SearchRestrictions.isNull(OrderedProductFields.BATCH), null,
                null);
    }

    private Optional<Entity> getOrderedProductForDeliveredProductWithBatch(final Entity deliveredProduct) {
        return getOrderedProductForDeliveredProduct(deliveredProduct, null, SearchRestrictions.isNull(L_OFFER),
                SearchRestrictions.isNull(L_OPERATION));
    }

    private Optional<Entity> getOrderedProductForDeliveredProductWithOffer(final Entity deliveredProduct) {
        return getOrderedProductForDeliveredProduct(deliveredProduct, SearchRestrictions.isNull(OrderedProductFields.BATCH), null,
                SearchRestrictions.isNull(L_OPERATION));
    }

    private Optional<Entity> getOrderedProductForDeliveredProductWithOperation(final Entity deliveredProduct) {
        return getOrderedProductForDeliveredProduct(deliveredProduct, SearchRestrictions.isNull(OrderedProductFields.BATCH),
                SearchRestrictions.isNull(L_OFFER), null);
    }

    private Optional<Entity> getOrderedProductForDeliveredProductWithAllNull(final Entity deliveredProduct) {

        return getOrderedProductForDeliveredProduct(deliveredProduct,
                SearchRestrictions.isNull(OrderedProductFields.BATCH), SearchRestrictions.isNull(L_OFFER),
                SearchRestrictions.isNull(L_OPERATION));

    }

    public SearchCriteriaBuilder getSearchCriteriaBuilderForOrderedProduct(
            final SearchCriteriaBuilder searchCriteriaBuilder,
            final Entity deliveredProduct) {
        return getSearchCriteriaBuilderForOrderedProduct(searchCriteriaBuilder, deliveredProduct, null, null, null);
    }

    public SearchCriteriaBuilder getSearchCriteriaBuilderForOrderedProduct(
            final SearchCriteriaBuilder searchCriteriaBuilder,
            final Entity deliveredProduct, final SearchCriterion batchCustomSearchCriterion,
            final SearchCriterion offerCustomSearchCriterion, final SearchCriterion operationCustomSearchCriterion) {
        Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);
        Entity supplier = delivery.getBelongsToField(DeliveryFields.SUPPLIER);
        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);
        String batchNumber = deliveredProduct.getStringField(OrderedProductFields.BATCH_NUMBER);
        Entity batch = deliveredProduct.getBelongsToField(DeliveredProductFields.BATCH);
        Entity offer = deliveredProduct.getBelongsToField(L_OFFER);
        Entity operation = deliveredProduct.getBelongsToField(L_OPERATION);

        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(OrderedProductFields.DELIVERY, delivery))
                .add(SearchRestrictions.belongsTo(OrderedProductFields.PRODUCT, product));

        if (Objects.nonNull(batchCustomSearchCriterion)) {
            searchCriteriaBuilder.createAlias(DeliveredProductFields.BATCH, OrderedProductFields.BATCH, JoinType.LEFT)
                    .add(batchCustomSearchCriterion);
        } else {
            if (StringUtils.isNoneEmpty(batchNumber)) {
                searchCriteriaBuilder.createAlias(OrderedProductFields.BATCH, OrderedProductFields.BATCH, JoinType.LEFT)
                        .add(SearchRestrictions.eq(OrderedProductFields.BATCH + "." + BatchFields.NUMBER, batchNumber))
                        .add(SearchRestrictions.belongsTo(OrderedProductFields.BATCH + "." + BatchFields.PRODUCT, product));

                if (Objects.nonNull(supplier)) {
                    searchCriteriaBuilder
                            .add(SearchRestrictions.belongsTo(OrderedProductFields.BATCH + "." + BatchFields.SUPPLIER, supplier));
                }
            } else {
                if (Objects.nonNull(batch)) {
                    searchCriteriaBuilder.add(SearchRestrictions.belongsTo(OrderedProductFields.BATCH, batch));
                } else {
                    searchCriteriaBuilder.add(SearchRestrictions.isNull(OrderedProductFields.BATCH));
                }
            }
        }

        if (PluginUtils.isEnabled("supplyNegotiations")) {
            if (Objects.nonNull(offerCustomSearchCriterion)) {
                searchCriteriaBuilder.createAlias(L_OFFER, L_OFFER, JoinType.LEFT).add(offerCustomSearchCriterion);
            } else {
                if (Objects.nonNull(offer)) {
                    searchCriteriaBuilder.add(SearchRestrictions.belongsTo(L_OFFER, offer));
                } else {
                    searchCriteriaBuilder.add(SearchRestrictions.isNull(L_OFFER));
                }
            }
        }

        if (PluginUtils.isEnabled("techSubcontrForDeliveries")) {
            if (Objects.nonNull(operationCustomSearchCriterion)) {
                searchCriteriaBuilder.createAlias(L_OPERATION, L_OPERATION, JoinType.LEFT).add(operationCustomSearchCriterion);
            } else {
                if (Objects.nonNull(operation)) {
                    searchCriteriaBuilder.add(SearchRestrictions.belongsTo(L_OPERATION, operation));
                } else {
                    searchCriteriaBuilder.add(SearchRestrictions.isNull(L_OPERATION));
                }
            }
        }

        return searchCriteriaBuilder;
    }

    public SearchCriteriaBuilder getSearchCriteriaBuilderForDeliveredProduct(
            final SearchCriteriaBuilder searchCriteriaBuilder,
            final Entity deliveredProduct) {
        return getSearchCriteriaBuilderForDeliveredProduct(searchCriteriaBuilder, deliveredProduct, true, null, null, null);
    }

    public SearchCriteriaBuilder getSearchCriteriaBuilderForDeliveredProduct(
            final SearchCriteriaBuilder searchCriteriaBuilder,
            final Entity deliveredProduct, final boolean checkOther, final SearchCriterion batchCustomSearchCriterion,
            final SearchCriterion offerCustomSearchCriterion, final SearchCriterion operationCustomSearchCriterion) {
        Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);
        Entity supplier = delivery.getBelongsToField(DeliveryFields.SUPPLIER);
        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);
        String batchNumber = deliveredProduct.getStringField(DeliveredProductFields.BATCH_NUMBER);
        Entity batch = deliveredProduct.getBelongsToField(DeliveredProductFields.BATCH);
        Entity offer = deliveredProduct.getBelongsToField(L_OFFER);
        Entity operation = deliveredProduct.getBelongsToField(L_OPERATION);

        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(DeliveredProductFields.DELIVERY, delivery))
                .add(SearchRestrictions.belongsTo(DeliveredProductFields.PRODUCT, product));

        if (Objects.nonNull(batchCustomSearchCriterion)) {
            searchCriteriaBuilder.createAlias(DeliveredProductFields.BATCH, DeliveredProductFields.BATCH, JoinType.LEFT)
                    .add(batchCustomSearchCriterion);
        } else {
            if (StringUtils.isNoneEmpty(batchNumber)) {
                searchCriteriaBuilder.createAlias(DeliveredProductFields.BATCH, DeliveredProductFields.BATCH, JoinType.LEFT)
                        .add(SearchRestrictions.eq(DeliveredProductFields.BATCH + "." + BatchFields.NUMBER, batchNumber))
                        .add(SearchRestrictions.belongsTo(DeliveredProductFields.BATCH + "." + BatchFields.PRODUCT, product));

                if (Objects.nonNull(supplier)) {
                    searchCriteriaBuilder.add(
                            SearchRestrictions.belongsTo(DeliveredProductFields.BATCH + "." + BatchFields.SUPPLIER, supplier));
                }
            } else {
                if (Objects.nonNull(batch)) {
                    searchCriteriaBuilder.add(SearchRestrictions.belongsTo(DeliveredProductFields.BATCH, batch));
                } else {
                    searchCriteriaBuilder.add(SearchRestrictions.isNull(DeliveredProductFields.BATCH));
                }
            }
        }

        if (PluginUtils.isEnabled("supplyNegotiations")) {
            if (Objects.nonNull(offerCustomSearchCriterion)) {
                searchCriteriaBuilder.createAlias(L_OFFER, L_OFFER, JoinType.LEFT).add(offerCustomSearchCriterion);
            } else {
                if (Objects.nonNull(offer)) {
                    searchCriteriaBuilder.add(SearchRestrictions.belongsTo(L_OFFER, offer));
                } else {
                    searchCriteriaBuilder.add(SearchRestrictions.isNull(L_OFFER));
                }
            }
        }

        if (PluginUtils.isEnabled("techSubcontrForDeliveries")) {
            if (Objects.nonNull(operationCustomSearchCriterion)) {
                searchCriteriaBuilder.createAlias(L_OPERATION, L_OPERATION, JoinType.LEFT).add(operationCustomSearchCriterion);
            } else {
                if (Objects.nonNull(operation)) {
                    searchCriteriaBuilder.add(SearchRestrictions.belongsTo(L_OPERATION, operation));
                } else {
                    searchCriteriaBuilder.add(SearchRestrictions.isNull(L_OPERATION));
                }
            }
        }

        if (Objects.nonNull(deliveredProduct.getField(DeliveredProductFields.PRICE_PER_UNIT))) {
            searchCriteriaBuilder
                    .add(SearchRestrictions.eq(DeliveredProductFields.PRICE_PER_UNIT, deliveredProduct.getField(DeliveredProductFields.PRICE_PER_UNIT)));
        } else {
            searchCriteriaBuilder.add(SearchRestrictions.isNull(DeliveredProductFields.PRICE_PER_UNIT));
        }

        if (checkOther) {
            if (PluginUtils.isEnabled("deliveriesToMaterialFlow")) {
                Entity palletNumber = deliveredProduct.getBelongsToField(DeliveredProductFields.PALLET_NUMBER);

                searchCriteriaBuilder.add(SearchRestrictions.belongsTo(DeliveredProductFields.PALLET_NUMBER, palletNumber));

                if (Objects.nonNull(deliveredProduct.getField(L_EXPIRATION_DATE))) {
                    searchCriteriaBuilder
                            .add(SearchRestrictions.eq(L_EXPIRATION_DATE, deliveredProduct.getField(L_EXPIRATION_DATE)));
                } else {
                    searchCriteriaBuilder.add(SearchRestrictions.isNull(L_EXPIRATION_DATE));
                }
            }
        }

        return searchCriteriaBuilder;
    }

    public SearchCriterion getBatchCustomSearchCriterion(final Entity deliveredProduct) {
        List<Entity> orderedProducts = getSearchCriteriaBuilderForOrderedProduct(getOrderedProductDD().find(), deliveredProduct,
                SearchRestrictions.isNotNull(OrderedProductFields.BATCH), null, null).list().getEntities();

        if (orderedProducts.isEmpty()) {
            return SearchRestrictions.isNull(DeliveredProductFields.BATCH);
        } else {
            Set<Long> batchIds = orderedProducts.stream()
                    .map(orderedProduct -> orderedProduct.getBelongsToField(OrderedProductFields.BATCH)).map(Entity::getId)
                    .collect(Collectors.toSet());

            return SearchRestrictions.or(SearchRestrictions.isNull(DeliveredProductFields.BATCH),
                    SearchRestrictions.not(SearchRestrictions.in(DeliveredProductFields.BATCH + "." + L_ID, batchIds)));
        }
    }

    public SearchCriterion getOfferCustomSearchCriterion(final Entity deliveredProduct) {
        List<Entity> orderedProducts = getSearchCriteriaBuilderForOrderedProduct(getOrderedProductDD().find(), deliveredProduct,
                null, SearchRestrictions.isNotNull(L_OFFER), null).list().getEntities();

        if (orderedProducts.isEmpty()) {
            return SearchRestrictions.isNull(L_OFFER);
        } else {
            Set<Long> offerIds = orderedProducts.stream().map(orderedProduct -> orderedProduct.getBelongsToField(L_OFFER))
                    .map(Entity::getId).collect(Collectors.toSet());

            return SearchRestrictions.or(SearchRestrictions.isNull(L_OFFER),
                    SearchRestrictions.not(SearchRestrictions.in(L_OFFER + "." + L_ID, offerIds)));
        }
    }

    public SearchCriterion getOperationCustomSearchCriterion(final Entity deliveredProduct) {
        List<Entity> orderedProducts = getSearchCriteriaBuilderForOrderedProduct(getOrderedProductDD().find(), deliveredProduct,
                null, null, SearchRestrictions.isNotNull(L_OPERATION)).list().getEntities();

        if (orderedProducts.isEmpty()) {
            return SearchRestrictions.isNull(L_OPERATION);
        } else {
            Set<Long> operationIds = orderedProducts.stream().map(orderedProduct -> orderedProduct.getBelongsToField(L_OPERATION))
                    .map(Entity::getId).collect(Collectors.toSet());

            return SearchRestrictions.or(SearchRestrictions.isNull(L_OPERATION),
                    SearchRestrictions.not(SearchRestrictions.in(L_OPERATION + "." + L_ID, operationIds)));
        }
    }

    public BigDecimal getConversion(final Entity product, String unit, String additionalUnit, BigDecimal dbConversion) {
        BigDecimal conversion = BigDecimal.ONE;
        if (!unit.equals(additionalUnit)) {
            PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                    searchCriteriaBuilder -> searchCriteriaBuilder
                            .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

            if (unitConversions.isDefinedFor(additionalUnit)) {
                conversion = unitConversions.asUnitToConversionMap().get(additionalUnit);
            } else {
                conversion = BigDecimal.ZERO;
            }
            if (Objects.nonNull(dbConversion) && dbConversion.compareTo(numberService.setScaleWithDefaultMathContext(conversion)) != 0) {
                conversion = dbConversion;
            }
        }
        return conversion;
    }

}
