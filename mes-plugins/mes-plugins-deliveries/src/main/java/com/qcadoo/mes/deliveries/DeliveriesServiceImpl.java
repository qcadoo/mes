/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
import static com.qcadoo.mes.deliveries.constants.ParameterFieldsD.DEFAULT_ADDRESS;
import static com.qcadoo.mes.deliveries.constants.ParameterFieldsD.DEFAULT_DESCRIPTION;
import static com.qcadoo.mes.deliveries.constants.ParameterFieldsD.OTHER_ADDRESS;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields;
import com.qcadoo.mes.deliveries.constants.ColumnForOrdersFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
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
    public List<Entity> getColumnsForDeliveries() {
        return getColumnForDeliveriesDD().find().addOrder(SearchOrders.asc(ColumnForDeliveriesFields.SUCCESSION)).list()
                .getEntities();
    }

    @Override
    public List<Entity> getColumnsForOrders() {
        return getColumnForOrdersDD().find().addOrder(SearchOrders.asc(ColumnForOrdersFields.SUCCESSION)).list().getEntities();
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
    public DataDefinition getColumnForDeliveriesDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COLUMN_FOR_DELIVERIES);
    }

    @Override
    public DataDefinition getColumnForOrdersDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COLUMN_FOR_ORDERS);
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
}
