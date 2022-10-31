/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.supplyNegotiations;

import static com.qcadoo.mes.basic.constants.ProductFields.PARENT;
import static com.qcadoo.mes.deliveries.constants.CompanyFieldsD.BUFFER;
import static com.qcadoo.mes.deliveries.constants.CompanyProductFields.COMPANY;
import static com.qcadoo.mes.deliveries.constants.ProductFieldsD.PRODUCT_COMPANIES;
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationFields.INCLUDED_COMPANIES;
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationFields.NEGOTIATION_PRODUCTS;
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationProductFields.DUE_DATE;
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationProductFields.LEFT_QUANTITY;
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationProductFields.PRODUCT;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.DESIRED_DATE;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.NEGOTIATION;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.NUMBER;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.REQUEST_FOR_QUOTATION_PRODUCTS;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.SUPPLIER;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationProductFields.ORDERED_QUANTITY;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.mes.supplyNegotiations.constants.SupplyNegotiationsConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class NegotiationServiceImpl implements NegotiationService {

    @Autowired
    private SupplyNegotiationsService supplyNegotiationsService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private NumberService numberService;

    @Override
    public void generateRequestForQuotations(final StateChangeContext stateChangeContext) {
        Entity negotiation = stateChangeContext.getOwner();

        List<Entity> negotiationProducts = negotiation.getHasManyField(NEGOTIATION_PRODUCTS);

        List<Entity> includedCompanies = Lists.newArrayList();
        Map<Long, Entity> companyAndRequestForQuotations = Maps.newHashMap();

        boolean companiesEmpty = false;

        for (Entity negotiationProduct : negotiationProducts) {
            Date dueDate = (Date) negotiationProduct.getField(DUE_DATE);

            List<Entity> companies = getCompaniesForNegotiationProduct(negotiationProduct);

            if (companies.isEmpty()) {
                companiesEmpty = true;
            } else {
                for (Entity company : companies) {
                    includedCompanies.add(company);

                    Entity requestForQuotationProduct = createRequestForQuotationProduct(negotiationProduct);

                    fillCompanyAndRequestForQuotation(companyAndRequestForQuotations, company, dueDate,
                            requestForQuotationProduct);
                }
            }
        }

        if (companiesEmpty) {
            stateChangeContext.addMessage("supplyNegotiations.negotiation.error.requestForQuotationsNotGenerated",
                    StateMessageType.INFO);
        }

        saveRequestForQuotationsAndFillNumbers(companyAndRequestForQuotations, negotiation);

        negotiation.setField(INCLUDED_COMPANIES, includedCompanies);

        negotiation.getDataDefinition().save(negotiation);
    }

    private List<Entity> getCompaniesForNegotiationProduct(final Entity negotiationProduct) {
        List<Entity> companies = Lists.newArrayList();

        Entity product = negotiationProduct.getBelongsToField(PRODUCT);

        addCompaniesWhichSellsProduct(companies, product);

        Entity parent = product.getBelongsToField(PARENT);

        if (parent != null) {
            addCompaniesWhichSellsProduct(companies, parent);
        }

        return companies;
    }

    private void addCompaniesWhichSellsProduct(final List<Entity> companies, final Entity product) {
        List<Entity> productCompanies = product.getHasManyField(PRODUCT_COMPANIES);

        for (Entity productCompany : productCompanies) {
            Entity company = productCompany.getBelongsToField(COMPANY);

            if (!companies.contains(company)) {
                companies.add(company);
            }
        }
    }

    private Entity createRequestForQuotationProduct(final Entity negotiationProduct) {
        Entity requestForQuotationProduct = supplyNegotiationsService.getRequestForQuotationProductDD().create();

        Entity product = negotiationProduct.getBelongsToField(PRODUCT);
        BigDecimal leftQuantity = negotiationProduct.getDecimalField(LEFT_QUANTITY);

        requestForQuotationProduct.setField(PRODUCT, product);
        requestForQuotationProduct.setField(ORDERED_QUANTITY, numberService.setScaleWithDefaultMathContext(leftQuantity));

        return requestForQuotationProduct;
    }

    private void fillCompanyAndRequestForQuotation(final Map<Long, Entity> companyAndRequestForQuotations, final Entity company,
                                                   final Date dueDate, final Entity requestForQuotationProduct) {
        if (requestForQuotationProduct != null) {
            if (companyAndRequestForQuotations.containsKey(company.getId())) {
                updateCompanyAndRequestForQuotation(companyAndRequestForQuotations, company, dueDate, requestForQuotationProduct);
            } else {
                addCompanyAndRequestForQuotation(companyAndRequestForQuotations, company, dueDate, requestForQuotationProduct);
            }
        }
    }

    private void addCompanyAndRequestForQuotation(final Map<Long, Entity> companyAndRequestForQuotations, final Entity company,
                                                  final Date dueDate, final Entity requestForQuotationProduct) {
        Entity requestForQuotation = supplyNegotiationsService.getRequestForQuotationDD().create();

        requestForQuotation.setField(SUPPLIER, company);
        requestForQuotation.setField(DESIRED_DATE, getDueDateMinusCompanyBuffer(dueDate, company));
        requestForQuotation.setField(REQUEST_FOR_QUOTATION_PRODUCTS, Lists.newArrayList(requestForQuotationProduct));

        companyAndRequestForQuotations.put(company.getId(), requestForQuotation);
    }

    private void updateCompanyAndRequestForQuotation(final Map<Long, Entity> companyAndRequestForQuotations,
                                                     final Entity company, final Date dueDate, final Entity requestForQuotationProduct) {
        Entity addedRequestForQuotation = companyAndRequestForQuotations.get(company.getId());

        Date desiredDate = (Date) addedRequestForQuotation.getField(DESIRED_DATE);

        List<Entity> requestForQuotationProducts = Lists.newArrayList(addedRequestForQuotation
                .getHasManyField(REQUEST_FOR_QUOTATION_PRODUCTS));
        requestForQuotationProducts.add(requestForQuotationProduct);

        addedRequestForQuotation.setField(DESIRED_DATE,
                updateDesiredDate(desiredDate, getDueDateMinusCompanyBuffer(dueDate, company)));
        addedRequestForQuotation.setField(REQUEST_FOR_QUOTATION_PRODUCTS, requestForQuotationProducts);

        companyAndRequestForQuotations.put(company.getId(), addedRequestForQuotation);
    }

    private Date getDueDateMinusCompanyBuffer(final Date dueDate, final Entity company) {
        Integer buffer = company.getIntegerField(BUFFER);

        if (buffer == null) {
            return dueDate;
        } else {
            return new DateTime(dueDate).minusDays(buffer).toDate();
        }
    }

    private Date updateDesiredDate(final Date desiredDate, final Date newDesiredDate) {
        if (desiredDate.before(newDesiredDate)) {
            return desiredDate;
        } else {
            return newDesiredDate;
        }
    }

    private void saveRequestForQuotationsAndFillNumbers(final Map<Long, Entity> companyAndRequestForQuotations,
                                                        final Entity negotiation) {
        for (Entity requestForQuotation : companyAndRequestForQuotations.values()) {
            requestForQuotation.setField(NUMBER, numberGeneratorService.generateNumber(
                    SupplyNegotiationsConstants.PLUGIN_IDENTIFIER, SupplyNegotiationsConstants.MODEL_REQUEST_FOR_QUOTATION));

            requestForQuotation.setField(NEGOTIATION, negotiation);

            requestForQuotation.getDataDefinition().save(requestForQuotation);
        }
    }

}
