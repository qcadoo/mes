/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.deliveries;

import static com.qcadoo.mes.basic.constants.CompanyFields.CITY;
import static com.qcadoo.mes.basic.constants.CompanyFields.FLAT;
import static com.qcadoo.mes.basic.constants.CompanyFields.HOUSE;
import static com.qcadoo.mes.basic.constants.CompanyFields.STREET;
import static com.qcadoo.mes.basic.constants.CompanyFields.ZIP_CODE;
import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static com.qcadoo.mes.deliveries.constants.DefaultAddressType.OTHER;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.PRODUCT;
import static com.qcadoo.mes.deliveries.constants.ParameterFieldsD.DEFAULT_ADDRESS;
import static com.qcadoo.mes.deliveries.constants.ParameterFieldsD.DEFAULT_DESCRIPTION;
import static com.qcadoo.mes.deliveries.constants.ParameterFieldsD.OTHER_ADDRESS;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields;
import com.qcadoo.mes.deliveries.constants.ColumnForOrdersFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.print.DeliveryProduct;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class DeliveriesServiceImpl implements DeliveriesService {

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private CurrencyService currencyService;

    @Override
    public Entity getDelivery(final Long deliveryId) {
        return getDeliveryDD().get(deliveryId);
    }

    @Override
    public Entity getOrderedProduct(final Long deliveredProductId) {
        return getOrderedProductDD().get(deliveredProductId);
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
    public Entity getCompanyProductsFamily(final Long companyProductsFamilyId) {
        return getCompanyProductsFamilyDD().get(companyProductsFamilyId);
    }

    @Override
    public List<Entity> getColumnsForDeliveries() {
        return getColumnForDeliveriesDD().find().addOrder(SearchOrders.asc(ColumnForDeliveriesFields.SUCCESSION)).list()
                .getEntities();
    }

    @Override
    public List<Entity> getColumnsForOrders() {

        List<Entity> columns = new LinkedList<Entity>();
        List<Entity> columnComponents = getColumnForOrdersDD().find()
                .addOrder(SearchOrders.asc(ColumnForOrdersFields.SUCCESSION)).list().getEntities();

        for (Entity columnComponent : columnComponents) {
            Entity columnDefinition = columnComponent.getBelongsToField("columnForOrders");

            columns.add(columnDefinition);
        }

        return columns;
    }

    @Override
    public DataDefinition getDeliveryDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY);
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
    public DataDefinition getCompanyProductsFamilyDD() {
        return dataDefinitionService
                .get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COMPANY_PRODUCTS_FAMILY);
    }

    @Override
    public DataDefinition getColumnForDeliveriesDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COLUMN_FOR_DELIVERIES);
    }

    @Override
    public DataDefinition getColumnForOrdersDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_PARAMETER_DELIVERY_ORDER_COLUMN);
    }

    @Override
    public void fillUnitFields(final ViewDefinitionState view, final String productName, final List<String> referenceNames) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(productName);
        Entity product = productLookup.getEntity();

        String unit = "";

        if (product != null) {
            unit = product.getStringField(UNIT);
        }

        for (String referenceName : referenceNames) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(referenceName);
            field.setFieldValue(unit);
            field.requestComponentUpdateState();
        }
    }

    @Override
    public String getDeliveryAddressDefaultValue() {
        Entity parameter = parameterService.getParameter();

        if (OTHER.getStringValue().equals(parameter.getStringField(DEFAULT_ADDRESS))) {
            return parameter.getStringField(OTHER_ADDRESS);
        } else {
            return generateAddressFromCompany();
        }
    }

    @Override
    public String getDescriptionDefaultValue() {
        Entity parameter = parameterService.getParameter();

        return parameter.getStringField(DEFAULT_DESCRIPTION);
    }

    private String generateAddressFromCompany() {
        Entity company = companyService.getCompany();

        StringBuffer address = new StringBuffer();

        String street = company.getStringField(STREET);
        String house = company.getStringField(HOUSE);
        String flat = company.getStringField(FLAT);
        String zipCode = company.getStringField(ZIP_CODE);
        String city = company.getStringField(CITY);

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

        return address.toString();
    }

    @Override
    public Entity getProduct(final DeliveryProduct deliveryProduct) {
        if (deliveryProduct.getOrderedProductId() == null) {
            return getDeliveredProduct(deliveryProduct.getDeliveredProductId()).getBelongsToField(PRODUCT);
        } else {
            return getOrderedProduct(deliveryProduct.getOrderedProductId()).getBelongsToField(PRODUCT);
        }
    }

    @Override
    public void calculatePricePerUnit(final Entity entity, final String quantityFieldName) {
        BigDecimal totalPrice = entity.getDecimalField(OrderedProductFields.TOTAL_PRICE);
        BigDecimal quantity = entity.getDecimalField(quantityFieldName);

        if ((quantity != null) && (totalPrice != null) && (BigDecimal.ZERO.compareTo(quantity) != 0)) {
            BigDecimal pricePerUnit = totalPrice.divide(quantity, numberService.getMathContext());

            entity.setField("totalPrice", numberService.setScale(pricePerUnit));
            entity.setField("pricePerUnit", numberService.setScale(pricePerUnit));
        } else {
            entity.setField("totalPrice", null);
            entity.setField("pricePerUnit", null);
        }
    }

    @Override
    public void fillCurrencyFields(final ViewDefinitionState view, final List<String> referenceNames) {
        String currency = currencyService.getCurrencyAlphabeticCode();

        if (currency == null) {
            return;
        }

        for (String reference : referenceNames) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setFieldValue(currency);
            field.requestComponentUpdateState();
        }
    }

    @Override
    public List<Entity> getColumnsWithFilteredCurrencies(final List<Entity> columns) {
        List<Entity> filteredCurrencyColumn = Lists.newArrayList();

        if (checkIfContainsPriceColumns(columns)) {
            filteredCurrencyColumn.addAll(columns);
        } else {
            for (Entity column : columns) {
                String identifier = column.getStringField(ColumnForOrdersFields.IDENTIFIER);

                if (!"currency".equals(identifier)) {
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

            if ("pricePerUnit".equals(identifier) || "totalPrice".equals(identifier)) {
                contains = true;
            }
        }

        return contains;
    }

}
