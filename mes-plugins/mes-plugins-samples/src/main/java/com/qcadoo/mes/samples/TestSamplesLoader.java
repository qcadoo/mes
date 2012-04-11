/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
package com.qcadoo.mes.samples;

import static com.qcadoo.mes.samples.constants.SamplesConstants.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.qcadoo.mes.samples.constants.SamplesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.utils.TreeNumberingService;

@Component
public class TestSamplesLoader extends SamplesLoader {

    private static final Logger LOG = LoggerFactory.getLogger(TestSamplesLoader.class);

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    private static final long MILLIS_IN_DAY = 86400000;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TreeNumberingService treeNumberingService;

    @Autowired
    private NumberService numberService;

    @Value("${setAsDemoEnviroment}")
    private boolean setAsDemoEnviroment;

    @Override
    void loadData(final String dataset, final String locale) {

        if (setAsDemoEnviroment) {
            changeAdminPassword();
        } else {
            readDataFromXML(dataset, "users", locale);
        }

        readDataFromXML(dataset, "dictionaries", locale);
        readDataFromXML(dataset, "activeCurrency", locale);
        readDataFromXML(dataset, "company", locale);
        readDataFromXML(dataset, "workstationTypes", locale);
        readDataFromXML(dataset, BASIC_MODEL_STAFF, locale);
        readDataFromXML(dataset, PRODUCTS_PLUGIN_IDENTIFIER, locale);
        readDataFromXML(dataset, "shifts", locale);
        readDataFromXML(dataset, "divisions", locale);
        if (isEnabled(TECHNOLOGIES_PLUGIN_IDENTIFIER)) {
            readDataFromXML(dataset, "operations", locale);
            readDataFromXML(dataset, TECHNOLOGIES_PLUGIN_IDENTIFIER, locale);
        }
        if (isEnabled(ORDERS_PLUGIN_IDENTIFIER)) {
            readDataFromXML(dataset, ORDERS_PLUGIN_IDENTIFIER, locale);
            if (isEnabled(L_ORDER_GROUPS)) {
                readDataFromXML(dataset, L_ORDER_GROUPS, locale);
            }
        }

        if (isEnabled(L_COST_CALCULATION)) {
            readDataFromXML(dataset, L_COST_CALCULATION, locale);
        }
        if (isEnabled(L_MATERIAL_FLOW)) {
            readDataFromXML(dataset, L_STOCK_AREAS, locale);
            readDataFromXML(dataset, L_TRANSFORMATIONS, locale);
            readDataFromXML(dataset, L_TRANSFER, locale);
            readDataFromXML(dataset, L_STOCK_CORRECTION, locale);
        }

        if (isEnabled(L_QUALITY_CONTROLS)) {
            readDataFromXML(dataset, L_QUALITY_CONTROLS, locale);
        }

        if (isEnabled(L_MATERIAL_REQUIREMENTS)) {
            readDataFromXML(dataset, L_MATERIAL_REQUIREMENTS, locale);
        }

        if (isEnabled(L_WORK_PLANS)) {
            readDataFromXML(dataset, L_WORK_PLANS, locale);
        }

        if (isEnabled(L_PRODUCTION_COUNTING)) {

            readDataFromXML(dataset, L_PRODUCTION_RECORD, locale);
            readDataFromXML(dataset, L_PRODUCTION_COUNTING, locale);
            readDataFromXML(dataset, L_PRODUCTION_BALANCE, locale);

        }
        if (isEnabled(L_ADVANCED_GENEALOGY)) {
            readDataFromXML(dataset, L_BATCHES, locale);
            if (isEnabled(L_ADVANCED_GENEALOGY_FOR_ORDERS)) {
                readDataFromXML(dataset, L_TRACKING_RECORDS, locale);
            }
            readDataFromXML(dataset, L_GENEALOGY_TABLES, locale);
        }
        if (isEnabled("productionLines")) {
            readDataFromXML(dataset, "productionLines", locale);
            readDataFromXML(dataset, "productionLines_dict", locale);
        }
    }

    private void changeAdminPassword() {
        DataDefinition userDD = dataDefinitionService.get("qcadooSecurity", "user");
        Entity user = userDD.find().add(SearchRestrictions.eq("userName", "admin")).setMaxResults(1).uniqueResult();
        user.setField("password", "charon321Demo");
        userDD.save(user);
    }

    @Override
    void readData(final Map<String, String> values, final String type, final Element node) {
        if ("activeCurrency".equals(type)) {
            addParameters(values);
        } else if ("company".equals(type)) {
            addCompany(values);
        } else if (PRODUCTS_PLUGIN_IDENTIFIER.equals(type)) {
            addProduct(values);
        } else if (ORDERS_PLUGIN_IDENTIFIER.equals(type)) {
            prepareTechnologiesForOrder(values);
            addOrder(values);
        } else if (TECHNOLOGIES_PLUGIN_IDENTIFIER.equals(type)) {
            addTechnology(values);
        } else if ("dictionaries".equals(type)) {
            addDictionaryItems(values);
        } else if ("users".equals(type)) {
            addUser(values);
        } else if ("operations".equals(type)) {
            addOperations(values);
        } else if (BASIC_MODEL_STAFF.equals(type)) {
            addStaff(values);
        } else if ("workstationTypes".equals(type)) {
            addWorkstationType(values);
        } else if ("shifts".equals(type)) {
            addShifts(values);
        } else if (L_DIVISION.equals(type)) {
            addDivision(values);
        } else if (L_ORDER_GROUPS.equals(type)) {
            addOrderGroup(values);
        } else if (L_COST_CALCULATION.equals(type)) {
            addCostCalculation(values);
        } else if (L_STOCK_AREAS.equals(type)) {
            addStokckArea(values);
        } else if (L_TRANSFORMATIONS.equals(type)) {
            addTransformation(values);
        } else if (L_TRANSFER.equals(type)) {
            addTransfer(values);
        } else if (L_STOCK_CORRECTION.equals(type)) {
            addStockCorrection(values);
        } else if (L_BATCHES.equals(type)) {
            addBatches(values);
        } else if (L_TRACKING_RECORDS.equals(type)) {
            addTrackingRecord(values);
        } else if (L_GENEALOGY_TABLES.equals(type)) {
            addGenealogyTables(values);
        } else if (L_QUALITY_CONTROLS.equals(type)) {
            addQualityControl(values);
        } else if (L_MATERIAL_REQUIREMENTS.equals(type)) {
            addMaterialRequirements(values);
        } else if (L_WORK_PLANS.equals(type)) {
            addWorkPlan(values);
        } else if (L_PRODUCTION_RECORD.equals(type)) {
            addProductionRecord(values);
        } else if (L_PRODUCTION_COUNTING.equals(type)) {
            addProductionCounting(values);
        } else if (L_PRODUCTION_BALANCE.equals(type)) {
            addProductionBalance(values);
        } else if ("productionLines".equals(type)) {
            addProductionLines(values);
        } else if ("productionLinesDictionary".equals(type)) {
            addDictionaryItems(values);
        }
    }

    private void addWorkstationType(final Map<String, String> values) {
        Entity machine = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_WORKSTATION_TYPE).create();

        LOG.debug("id: " + values.get("id") + " name " + values.get(L_NAME) + " prod_line " + values.get("prod_line")
                + " description " + values.get(L_DESCRIPTION));
        machine.setField(L_NUMBER, values.get("id"));
        machine.setField(L_NAME, values.get(L_NAME));
        machine.setField(L_DESCRIPTION, values.get(L_DESCRIPTION));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test machine item {machine=" + machine.getField(L_NAME) + ", " + L_NUMBER + "="
                    + machine.getField(L_NUMBER) + "}");
        }

        machine = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_WORKSTATION_TYPE).save(machine);

        validateEntity(machine);
    }

    private void addStaff(final Map<String, String> values) {
        Entity staff = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_STAFF).create();

        LOG.debug("id: " + values.get("id") + " name " + values.get(L_NAME) + " " + L_SURNAME + " " + values.get(L_SURNAME)
                + " post " + values.get("post"));
        staff.setField(L_NUMBER, values.get("id"));
        staff.setField(L_NAME, values.get(L_NAME));
        staff.setField(L_SURNAME, values.get(L_SURNAME));
        staff.setField("post", values.get("post"));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test staff item {staff=" + staff.getField(L_NAME) + ", " + L_SURNAME + "= "
                    + staff.getField(L_SURNAME) + "}");
        }

        staff = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_STAFF).save(staff);
        validateEntity(staff);
    }

    private void addOperations(final Map<String, String> values) {
        Entity operation = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, TECHNOLOGY_MODEL_OPERATION).create();

        operation.setField(L_NAME, values.get(L_NAME));
        operation.setField(L_NUMBER, values.get(L_NUMBER));
        operation.setField(L_TPZ, values.get(L_TPZ));
        operation.setField("tj", values.get("tj"));
        operation.setField("productionInOneCycle", values.get("productioninonecycle"));
        operation.setField("countRealized", values.get("countRealized"));
        operation.setField("machineUtilization", values.get("machineutilization"));
        operation.setField("laborUtilization", values.get("laborutilization"));
        operation.setField("countMachineOperation", values.get("countmachine"));
        operation.setField("countRealizedOperation", "01all");
        operation.setField("timeNextOperation", values.get("timenextoperation"));
        operation.setField(BASIC_MODEL_WORKSTATION_TYPE, getMachine(values.get(L_NUMBER)));
        operation.setField(BASIC_MODEL_STAFF, getRandomStaff());

        if (isEnabled("costNormsForOperation")) {
            operation.setField("pieceworkCost", values.get("pieceworkcost"));
            operation.setField("machineHourlyCost", values.get("machinehourlycost"));
            operation.setField("laborHourlyCost", values.get("laborhourlycost"));
            operation.setField("numberOfOperations", values.get("numberofoperations"));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test operation item {name=" + operation.getField(L_NAME) + ", " + L_NUMBER + "="
                    + operation.getField(L_NUMBER) + "}");
        }

        operation = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, TECHNOLOGY_MODEL_OPERATION).save(operation);
        validateEntity(operation);
    }

    private void addProduct(final Map<String, String> values) {
        Entity product = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_PRODUCT).create();
        product.setField("category", getRandomDictionaryItem("categories"));
        if (!values.get(L_EAN).isEmpty()) {
            product.setField(L_EAN, values.get(L_EAN));
        }
        if (!values.get(L_NAME).isEmpty()) {
            product.setField(L_NAME, values.get(L_NAME));
        }
        if (!values.get(L_BATCH).isEmpty()) {
            product.setField(L_BATCH, values.get(L_BATCH));
        }
        if (!values.get(L_PRODUCT_NR).isEmpty()) {
            product.setField(L_NUMBER, values.get(L_PRODUCT_NR));
        }
        if (!values.get("typeofproduct").isEmpty()) {
            product.setField("globalTypeOfMaterial", values.get("typeofproduct"));
        }
        product.setField("unit", values.get("unit"));

        if (isEnabled("costNormsForProduct")) {
            product.setField("costForNumber", values.get("costfornumber"));
            product.setField("nominalCost", values.get("nominalcost"));
            product.setField("lastPurchaseCost", values.get("lastpurchasecost"));
            product.setField("averageCost", values.get("averagecost"));
        }

        product = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_PRODUCT).save(product);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test product {id=" + product.getId() + ", category=" + product.getField("category") + ", ean="
                    + product.getField(L_EAN) + ", name=" + product.getField(L_NAME) + ", " + L_NUMBER + "="
                    + product.getField(L_NUMBER) + ", globalTypeOfMaterial=" + product.getField("typeOfMaterial") + ", unit="
                    + product.getField("unit") + "}");
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < RANDOM.nextInt(5); i++) {
            for (int j = 0; j <= i; j++) {
                stringBuilder.append("#");
            }
            addSubstitute(values.get(L_NAME) + stringBuilder.toString(), values.get(L_PRODUCT_NR) + stringBuilder.toString(),
                    product, i + 1);
        }
    }

    private void addSubstitute(final String name, final String number, final Entity product, final int priority) {
        Entity substitute = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_SUBSTITUTE).create();
        substitute.setField(L_NAME, name);
        substitute.setField(L_NUMBER, number);
        substitute.setField("priority", priority);
        substitute.setField(BASIC_MODEL_PRODUCT, product);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test substitute {name=" + substitute.getField(L_NAME) + ", " + L_NUMBER + "="
                    + substitute.getField(L_NUMBER) + ", priority=" + substitute.getField("priority") + ", subsitute product="
                    + ((Entity) substitute.getField(BASIC_MODEL_PRODUCT)).getField(L_NUMBER) + "}");
        }

        substitute = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_SUBSTITUTE).save(substitute);
        validateEntity(substitute);

        for (int i = 0; i < 1; i++) {
            addSubstituteComponent(substitute, getRandomProduct(), 100 * RANDOM.nextDouble());
        }
    }

    private void addSubstituteComponent(final Entity substitute, final Entity product, final double quantity) {
        Entity substituteComponent = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, "substituteComponent").create();
        substituteComponent.setField(BASIC_MODEL_PRODUCT, product);
        substituteComponent.setField(L_QUANTITY, numberService.setScale(new BigDecimal(quantity)));
        substituteComponent.setField(BASIC_MODEL_SUBSTITUTE, substitute);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test substitute component {substitute="
                    + ((Entity) substituteComponent.getField(BASIC_MODEL_SUBSTITUTE)).getField(L_NUMBER) + ", subsitute product="
                    + ((Entity) substituteComponent.getField(BASIC_MODEL_PRODUCT)).getField(L_NUMBER) + ", quantity="
                    + substituteComponent.getField(L_QUANTITY) + "}");
        }

        substituteComponent = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, "substituteComponent").save(substituteComponent);
        validateEntity(substituteComponent);
    }

    private void prepareTechnologiesForOrder(final Map<String, String> values) {
        Entity technology = getTechnologyByNumber(values.get("tech_nr"));
        technology.setField(L_ORDER_STATE, "02accepted");
        technology.getDataDefinition().save(technology);
    }

    private void addOrderGroup(final Map<String, String> values) {
        Entity orderGroup = dataDefinitionService.get(L_ORDER_GROUPS, "orderGroup").create();
        orderGroup.setField(L_NUMBER, values.get(L_NUMBER));
        orderGroup.setField(L_NAME, values.get(L_NAME));

        Entity order3 = getOrderByNumber("000003");
        Entity order2 = getOrderByNumber("000002");

        orderGroup.setField(L_DATE_TO, order3.getField(L_DATE_TO));
        orderGroup.setField(L_DATE_FROM, order2.getField(L_DATE_FROM));

        orderGroup = orderGroup.getDataDefinition().save(orderGroup);
        validateEntity(orderGroup);

        order3.setField("orderGroup", orderGroup);
        order2.setField("orderGroup", orderGroup);

        validateEntity(order3.getDataDefinition().save(order3));
        validateEntity(order2.getDataDefinition().save(order2));

    }

    private void addOrder(final Map<String, String> values) {
        long startDate = System.currentTimeMillis();
        long endDate = startDate;
        long millsInHour = 3600000;
        long millsInMinute = 60000;

        if (!values.get("scheduled_start_date").isEmpty()) {
            try {
                startDate = FORMATTER.parse(values.get("scheduled_start_date")).getTime();
            } catch (ParseException e) {
                LOG.warn(e.getMessage(), e);
            }
        }

        if ("000001".equals(values.get(L_ORDER_NR))) {
            endDate = startDate + MILLIS_IN_DAY + 1 * millsInHour + 45 * millsInMinute;
        } else if ("000002".equals(values.get(L_ORDER_NR))) {
            startDate -= 2 * MILLIS_IN_DAY;
            endDate = startDate + MILLIS_IN_DAY + 3 * millsInHour + 40 * millsInMinute;
        } else if ("000003".equals(values.get(L_ORDER_NR))) {
            startDate += 2 * MILLIS_IN_DAY;
            endDate = startDate + 6 * millsInHour + 35 * millsInMinute;
        } else if ("000004".equals(values.get(L_ORDER_NR))) {
            startDate += 2 * MILLIS_IN_DAY;
            endDate = startDate + 8 * millsInHour + 55 * millsInMinute;
        } else if ("000005".equals(values.get(L_ORDER_NR))) {
            startDate += 2 * MILLIS_IN_DAY;
            endDate = startDate + 10 * millsInHour + 65 * millsInMinute;
        } else if ("000006".equals(values.get(L_ORDER_NR))) {
            startDate += 2 * MILLIS_IN_DAY;
            endDate = startDate + 10 * millsInHour + 75 * millsInMinute;
        }

        if (!values.get("scheduled_end_date").isEmpty()) {
            try {
                endDate = FORMATTER.parse(values.get("scheduled_end_date")).getTime();
            } catch (ParseException e) {
                LOG.warn(e.getMessage(), e);
            }
        }

        Entity order = dataDefinitionService.get(ORDERS_PLUGIN_IDENTIFIER, ORDERS_MODEL_ORDER).create();
        order.setField(L_DATE_FROM, new Date(startDate));
        order.setField(L_DATE_TO, new Date(endDate));
        order.setField("externalSynchronized", true);

        order.setField(TECHNOLOGY_MODEL_TECHNOLOGY, getTechnologyByNumber(values.get("tech_nr")));
        order.setField(L_NAME,
                (values.get(L_NAME).isEmpty() || values.get(L_NAME) == null) ? values.get(L_ORDER_NR) : values.get(L_NAME));
        order.setField(L_NUMBER, values.get(L_ORDER_NR));
        order.setField(L_PLANNED_QUANTITY, values.get("quantity_scheduled").isEmpty() ? new BigDecimal(
                100 * RANDOM.nextDouble() + 1) : new BigDecimal(values.get("quantity_scheduled")));

        order.setField(L_ORDER_STATE, values.get("state"));

        if (!"01pending".equals(values.get(L_ORDER_STATE))) {

            List<Entity> productionCountings = Lists.newArrayList();
            DataDefinition productionCountingDD = dataDefinitionService.get(BASICPRODUCTIONCOUNTING_PLUGIN_IDENTIFIER,
                    BASICPRODUCTIONCOUNTING_MODEL_BASICPRODUCTIONCOUNTING);
            Entity productionCounting = productionCountingDD.create();

            if ("000001".equals(values.get(L_ORDER_NR))) {

                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_13), new BigDecimal("55"),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_14), new BigDecimal("55"),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_15), new BigDecimal("55"),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_16), new BigDecimal("55"),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_11), new BigDecimal("55"),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_12),
                        new BigDecimal(L_220), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_10),
                        new BigDecimal(L_220), BigDecimal.ZERO, BigDecimal.ZERO));

            } else if ("000002".equals(values.get(L_ORDER_NR))) {

                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_28),
                        new BigDecimal(L_400), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_27),
                        new BigDecimal(L_100), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_26),
                        new BigDecimal(L_400), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_32),
                        new BigDecimal(L_100), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_31),
                        new BigDecimal(L_400), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_30),
                        new BigDecimal(L_400), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_29),
                        new BigDecimal(L_1600), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_33),
                        new BigDecimal(L_1600), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_21),
                        new BigDecimal(L_1600), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_25),
                        new BigDecimal(L_100), BigDecimal.ZERO, BigDecimal.ZERO));

            } else if ("000003".equals(values.get(L_ORDER_NR))) {

                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_20),
                        new BigDecimal(L_2400), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_19),
                        new BigDecimal(L_600), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_18),
                        new BigDecimal(L_150), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_24),
                        new BigDecimal(L_150), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_23),
                        new BigDecimal(L_600), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_22),
                        new BigDecimal(L_600), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_21),
                        new BigDecimal(L_2400), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_17),
                        new BigDecimal(L_150), BigDecimal.ZERO, BigDecimal.ZERO));

            } else if ("000004".equals(values.get(L_ORDER_NR))) {

                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_13), new BigDecimal(L_15),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_14), new BigDecimal(L_15),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_15), new BigDecimal(L_15),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_16), new BigDecimal(L_15),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_11), new BigDecimal(L_15),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_12), new BigDecimal(L_60),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_10), new BigDecimal(L_15),
                        BigDecimal.ZERO, BigDecimal.ZERO));

            } else if ("000005".equals(values.get(L_ORDER_NR))) {

                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_20),
                        new BigDecimal(L_160), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_19), new BigDecimal(L_40),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_18), new BigDecimal(L_10),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_24), new BigDecimal(L_10),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_23), new BigDecimal(L_40),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_22), new BigDecimal(L_40),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_21),
                        new BigDecimal(L_160), BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_17), new BigDecimal(L_10),
                        BigDecimal.ZERO, BigDecimal.ZERO));

            } else if ("000006".equals(values.get(L_ORDER_NR))) {

                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_28), new BigDecimal(L_20),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_27), new BigDecimal(L_5),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_26), new BigDecimal(L_20),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_32), new BigDecimal(L_5),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_31), new BigDecimal(L_20),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_30), new BigDecimal(L_20),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_29), new BigDecimal(L_80),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_33), new BigDecimal(L_80),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_21), new BigDecimal(L_80),
                        BigDecimal.ZERO, BigDecimal.ZERO));
                productionCountings.add(addBasicProductionCountingRecords(getProductByNumber(L_PROD_NR_25), new BigDecimal(L_5),
                        BigDecimal.ZERO, BigDecimal.ZERO));
            }

            validateEntity(productionCounting);
            order.setField("basicProductionCountings", productionCountings);
        }

        Entity product = getProductByNumber(values.get(L_PRODUCT_NR));

        if (isEnabled(L_PRODUCTION_COUNTING)) {
            order.setField("typeOfProductionRecording", values.get("type_of_production_recording"));
            order.setField("registerQuantityInProduct", values.get("register_quantity_in_product"));
            order.setField("registerQuantityOutProduct", values.get("register_quantity_out_product"));
            order.setField("registerProductionTime", values.get("register_production_time"));
            order.setField("registerPiecework", values.get("register_piecework"));
            order.setField("justOne", values.get("just_one"));
            order.setField("allowToClose", values.get("allow_to_close"));
            order.setField("autoCloseOrder", values.get("auto_close_order"));
        }

        if (isEnabled(L_ADVANCED_GENEALOGY_FOR_ORDERS)) {
            order.setField("trackingRecordTreatment", "01duringProduction");
            order.setField("trackingRecordForOrderTreatment", values.get("tracking_record_for_order_treatment"));
        }

        order.setField(BASIC_MODEL_PRODUCT, product);
        if (order.getField(TECHNOLOGY_MODEL_TECHNOLOGY) == null) {
            order.setField(TECHNOLOGY_MODEL_TECHNOLOGY, getDefaultTechnologyForProduct(product));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test order {id="
                    + order.getId()
                    + ", name="
                    + order.getField(L_NAME)
                    + ", "
                    + L_NUMBER
                    + "="
                    + order.getField(L_NUMBER)
                    + ", order product="
                    + (order.getField(BASIC_MODEL_PRODUCT) == null ? null : ((Entity) order.getField(BASIC_MODEL_PRODUCT))
                            .getField(L_NUMBER))
                    + ", technology="
                    + (order.getField(TECHNOLOGY_MODEL_TECHNOLOGY) == null ? null : ((Entity) order
                            .getField(TECHNOLOGY_MODEL_TECHNOLOGY)).getField(L_NUMBER)) + ", dateFrom="
                    + order.getField(L_DATE_FROM) + ", dateTo=" + order.getField(L_DATE_TO) + ", effectiveDateFrom="
                    + order.getField("effectiveDateFrom") + ", effectiveDateTo=" + order.getField("effectiveDateTo")
                    + ", doneQuantity=" + order.getField("doneQuantity") + ", plannedQuantity="
                    + order.getField(L_PLANNED_QUANTITY) + ", trackingRecordTreatment="
                    + order.getField("trackingRecordTreatment") + ", state=" + order.getField(L_ORDER_STATE) + "}");
        }

        order = dataDefinitionService.get(ORDERS_PLUGIN_IDENTIFIER, ORDERS_MODEL_ORDER).save(order);
        validateEntity(order);

    }

    private void addBatches(final Map<String, String> values) {
        Entity batch = dataDefinitionService.get(L_ADVANCED_GENEALOGY, L_BATCH).create();

        batch.setField(L_NUMBER, values.get(L_NUMBER));
        batch.setField(L_PRODUCT, getProductByNumber(values.get("product_nr")));
        batch.setField(L_SUPPLIER, getSupplierByNumber(values.get("supplier_nr")));
        batch.setField(L_STATE, "01tracked");

        batch = batch.getDataDefinition().save(batch);
        validateEntity(batch);
    }

    private void addTrackingRecord(final Map<String, String> values) {
        Entity trackingRecord = dataDefinitionService.get(L_ADVANCED_GENEALOGY, "trackingRecord").create();

        trackingRecord.setField("entityType", values.get("entity_type"));
        trackingRecord.setField(L_NUMBER, values.get(L_NUMBER));
        trackingRecord.setField("producedBatch", getBatchByNumber(values.get("produced_batch_no")));
        trackingRecord.setField(L_ORDER, getOrderByNumber(values.get("order_no")));
        trackingRecord.setField(L_STATE, "01draft");

        trackingRecord = trackingRecord.getDataDefinition().save(trackingRecord);
        validateEntity(trackingRecord);

        buildTrackingRecord(trackingRecord);
    }

    private void addGenealogyTables(final Map<String, String> values) {
        Entity genealogyTable = dataDefinitionService.get(L_ADVANCED_GENEALOGY, "genealogyReport").create();

        genealogyTable.setField(L_TYPE, values.get(L_TYPE));
        genealogyTable.setField(L_NAME, values.get(L_NAME));
        genealogyTable.setField("includeDraft", values.get("include_draft"));
        genealogyTable.setField("directRelatedOnly", values.get("direct_related_only"));
        genealogyTable.setField(L_BATCH, getBatchByNumber(values.get("batch_no")));

        genealogyTable = genealogyTable.getDataDefinition().save(genealogyTable);
        validateEntity(genealogyTable);
    }

    private void addDivision(final Map<String, String> values) {
        Entity division = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, L_DIVISION).create();

        division.setField(L_NUMBER, values.get("NUMBER"));
        division.setField(L_NAME, values.get("NAME"));
        division.setField("supervisor", values.get("SUPERVISOR"));

        division = division.getDataDefinition().save(division);
        validateEntity(division);
    }

    private void addCostCalculation(final Map<String, String> values) {
        Entity costCalculation = dataDefinitionService.get(L_COST_CALCULATION, L_COST_CALCULATION).create();

        costCalculation.setField(L_NUMBER, values.get(L_NUMBER));
        costCalculation.setField(L_DESCRIPTION, values.get(L_DESCRIPTION));
        costCalculation.setField(ORDERS_MODEL_ORDER, getOrderByNumber(values.get("orderno")));
        costCalculation.setField(TECHNOLOGY_MODEL_TECHNOLOGY, getTechnologyByNumber(values.get("techno")));
        costCalculation.setField("defaultTechnology", getTechnologyByNumber(values.get("deftechno")));
        costCalculation.setField(L_PRODUCT, getProductByNumber(values.get("prodno")));
        costCalculation.setField("quantity", values.get("quantity"));
        costCalculation.setField("includeTPZ", values.get("includetpz"));
        costCalculation.setField("sourceOfMaterialCosts", values.get("sourceofmaterialcosts"));
        costCalculation.setField("calculateMaterialCostsMode", values.get("calculatematerialcostmode"));
        costCalculation.setField("calculateOperationCostsMode", values.get("calculateoperationcostmode"));
        costCalculation.setField("additionalOverhead", values.get("additionaloverhead"));
        costCalculation.setField("productionCostMargin", values.get("productioncostmargin"));
        costCalculation.setField("materialCostMargin", values.get("materialcostmargin"));

        costCalculation = costCalculation.getDataDefinition().save(costCalculation);

        validateEntity(costCalculation);
    }

    private void addStokckArea(final Map<String, String> values) {
        Entity stockArea = dataDefinitionService.get(L_MATERIAL_FLOW, L_STOCK_AREAS).create();

        stockArea.setField(L_NUMBER, values.get(L_NUMBER));
        stockArea.setField(L_NAME, values.get(L_NAME));

        stockArea = stockArea.getDataDefinition().save(stockArea);
        validateEntity(stockArea);
    }

    private void addTransformation(final Map<String, String> values) {
        Entity transformation = dataDefinitionService.get(L_MATERIAL_FLOW, L_TRANSFORMATIONS).create();

        transformation.setField(L_NUMBER, values.get(L_NUMBER));
        transformation.setField(L_NAME, values.get(L_NAME));
        transformation.setField(L_TIME, values.get(L_TIME));
        transformation.setField("stockAreasFrom", getStockAreaByNumber(values.get("stock_areas_from")));
        transformation.setField("stockAreasTo", getStockAreaByNumber(values.get("stock_areas_to")));
        transformation.setField(L_STAFF, getStaffByNumber(values.get(L_STAFF)));

        transformation = transformation.getDataDefinition().save(transformation);
        validateEntity(transformation);
    }

    private void addStockCorrection(final Map<String, String> values) {
        Entity stockCorrection = dataDefinitionService.get(L_MATERIAL_FLOW, L_STOCK_CORRECTION).create();

        stockCorrection.setField(L_NUMBER, values.get(L_NUMBER));
        stockCorrection.setField("stockCorrectionDate", values.get("stock_correction_date"));
        stockCorrection.setField(L_STOCK_AREAS, getStockAreaByNumber(values.get("stock_areas")));
        stockCorrection.setField(L_PRODUCT, getProductByNumber(values.get(L_PRODUCT)));
        stockCorrection.setField(L_STAFF, getStaffByNumber(values.get(L_STAFF)));
        stockCorrection.setField("found", values.get("found"));

        stockCorrection = stockCorrection.getDataDefinition().save(stockCorrection);
        validateEntity(stockCorrection);
    }

    private void addTransfer(final Map<String, String> values) {
        Entity transfer = dataDefinitionService.get(L_MATERIAL_FLOW, L_TRANSFER).create();

        transfer.setField(L_NUMBER, values.get(L_NUMBER));
        transfer.setField(L_TYPE, values.get(L_TYPE));
        transfer.setField(L_PRODUCT, getProductByNumber(values.get(L_PRODUCT)));
        transfer.setField(L_QUANTITY, values.get(L_QUANTITY));
        transfer.setField(L_STAFF, getStaffByNumber(values.get(L_STAFF)));
        transfer.setField("stockAreasFrom", getStockAreaByNumber(values.get("stock_areas_from")));
        transfer.setField("stockAreasTo", getStockAreaByNumber(values.get("stock_areas_to")));
        transfer.setField(L_TIME, values.get(L_TIME));

        transfer.setField("transformationsConsumption", getTransformationByNumber(values.get("transformations_consumption")));
        transfer.setField("transformationsProduction", getTransformationByNumber(values.get("transformations_production")));

        transfer = transfer.getDataDefinition().save(transfer);
        validateEntity(transfer);
    }

    private void addTechnology(final Map<String, String> values) {
        Entity product = getProductByNumber(values.get(L_PRODUCT_NR));

        if (product != null) {
            Entity defaultTechnology = getDefaultTechnologyForProduct(product);

            Entity technology = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, TECHNOLOGY_MODEL_TECHNOLOGY).create();
            if (!values.get(L_DESCRIPTION).isEmpty()) {
                technology.setField(L_DESCRIPTION, values.get(L_DESCRIPTION));
            }
            technology.setField("master", defaultTechnology == null);
            technology.setField(L_NAME, values.get(L_NAME));
            technology.setField(L_NUMBER, values.get("bom_nr"));
            technology.setField(BASIC_MODEL_PRODUCT, product);
            technology.setField(L_ORDER_STATE, "01draft");
            technology.setField(L_DESCRIPTION, values.get("DESCRIPTION"));
            technology.setField("batchRequired", true);
            technology.setField("postFeatureRequired", false);
            technology.setField("otherFeatureRequired", false);
            technology.setField("shiftFeatureRequired", false);
            technology.setField("technologyBatchRequired", false);

            if (isEnabled(L_QUALITY_CONTROLS_FOR_OPERATION)
                    && L_QUALITY_CONTROLS_FOR_OPERATION.equals(values.get(L_QUALITYCONTROLTYPE_3))) {
                technology.setField(L_QUALITY_CONTROL_TYPE2, L_QUALITY_CONTROLS_FOR_OPERATION);
            }

            if (!(isEnabled(L_QUALITY_CONTROLS_FOR_OPERATION) && "04forOperation".equals(values.get(L_QUALITY_CONTROL_TYPE)))
                    && isEnabled(L_QUALITY_CONTROLS)
                    && ("02forUnit".equals(values.get(L_QUALITY_CONTROL_TYPE)) || "03forOrder".equals(values
                            .get(L_QUALITY_CONTROL_TYPE)))) {
                technology.setField(L_QUALITY_CONTROL_TYPE2, values.get(L_QUALITY_CONTROL_TYPE));
                if ("02forUnit".equals(values.get(L_QUALITY_CONTROL_TYPE))) {
                    technology.setField("unitSamplingNr", values.get("unit_sampling_nr"));
                }
            }

            if (!values.get("minimal").isEmpty()) {
                technology.setField("minimalQuantity", values.get("minimal"));
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Add test technology {id=" + technology.getId() + ", name=" + technology.getField(L_NAME) + ", "
                        + L_NUMBER + "=" + technology.getField(L_NUMBER) + ", technology product="
                        + ((Entity) technology.getField(BASIC_MODEL_PRODUCT)).getField(L_NUMBER) + ", description="
                        + technology.getField(L_DESCRIPTION) + ", master=" + technology.getField("master") + "}");
            }

            technology = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, TECHNOLOGY_MODEL_TECHNOLOGY).save(technology);
            validateEntity(technology);

            if (L_PROD_NR_10.equals(values.get(L_PRODUCT_NR))) {
                addTechnologyOperationComponentsForTableAdvanced(technology);
            } else if (L_PROD_NR_17.equals(values.get(L_PRODUCT_NR))) {
                addTechnologyOperationComponentsForTabouretAdvanced(technology);
            } else if (L_PROD_NR_25.equals(values.get(L_PRODUCT_NR))) {
                addTechnologyOperationComponentsForStoolAdvanced(technology);
            }

            treeNumberingService.generateNumbersAndUpdateTree(
                    dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "technologyOperationComponent"),
                    TECHNOLOGY_MODEL_TECHNOLOGY, technology.getId());
        }
    }

    private void addTechnologyOperationComponentsForTableAdvanced(final Entity technology) {
        Entity parent = addOperationComponent(technology, null, getOperationByNumber(L_5));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(L_PROD_NR_14));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(L_PROD_NR_13));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(L_PROD_NR_12));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(L_PROD_NR_11));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber(L_PROD_NR_10));
        parent = addOperationComponent(technology, parent, getOperationByNumber("1"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(L_PROD_NR_15));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber(L_PROD_NR_11));
        parent = addOperationComponent(technology, parent, getOperationByNumber("2"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(L_PROD_NR_16));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber(L_PROD_NR_15));
    }

    private void addTechnologyOperationComponentsForStoolAdvanced(final Entity technology) {
        Entity parent = addOperationComponent(technology, null, getOperationByNumber(L_5));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(L_PROD_NR_27));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(L_PROD_NR_26));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber(L_PROD_NR_25));
        parent = addOperationComponent(technology, parent, getOperationByNumber("6"));
        addProductInComponent(parent, new BigDecimal("16"), getProductByNumber(L_PROD_NR_21));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(L_PROD_NR_30));
        addProductInComponent(parent, new BigDecimal("16"), getProductByNumber(L_PROD_NR_29));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(L_PROD_NR_28));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(L_PROD_NR_26));
        Entity parent1 = addOperationComponent(technology, parent, getOperationByNumber("4"));
        addProductInComponent(parent1, BigDecimal.ONE, getProductByNumber(L_PROD_NR_33));
        addProductOutComponent(parent1, BigDecimal.ONE, getProductByNumber(L_PROD_NR_29));
        parent = addOperationComponent(technology, parent, getOperationByNumber("1"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(L_PROD_NR_31));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(L_PROD_NR_28));
        parent = addOperationComponent(technology, parent, getOperationByNumber("2"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(L_PROD_NR_32));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(L_PROD_NR_31));
    }

    private void addTechnologyOperationComponentsForTabouretAdvanced(final Entity technology) {
        Entity parent = addOperationComponent(technology, null, getOperationByNumber(L_5));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(L_PROD_NR_18));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(L_PROD_NR_19));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber(L_PROD_NR_17));
        parent = addOperationComponent(technology, parent, getOperationByNumber("6"));
        addProductInComponent(parent, new BigDecimal("16"), getProductByNumber(L_PROD_NR_21));
        addProductInComponent(parent, new BigDecimal("16"), getProductByNumber(L_PROD_NR_20));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(L_PROD_NR_22));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(L_PROD_NR_19));
        parent = addOperationComponent(technology, parent, getOperationByNumber("1"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(L_PROD_NR_23));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(L_PROD_NR_22));
        parent = addOperationComponent(technology, parent, getOperationByNumber("2"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(L_PROD_NR_24));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(L_PROD_NR_23));
    }

    private Entity addRecordOperationProductInComponent(final Entity product, final BigDecimal usedQuantity,
            final BigDecimal plannedQuantity, final BigDecimal balance) {
        DataDefinition productInComponentDD = dataDefinitionService.get(SamplesConstants.PRODUCTION_COUNTING_PLUGIN_IDENTIFIER,
                SamplesConstants.RECORDOPERATIONPRODUCTINCOMPONENT_MODEL_RECORDOPERATIONPRODUCTINCOMPONENT);
        Entity productInComponent = productInComponentDD.create();
        productInComponent.setField(L_PRODUCT, product);
        productInComponent.setField("usedQuantity", usedQuantity);
        productInComponent.setField(L_PLANNED_QUANTITY, plannedQuantity);
        productInComponent.setField("balance", balance);
        validateEntity(productInComponent);
        return productInComponentDD.save(productInComponent);
    }

    private Entity addBasicProductionCountingRecords(final Entity product, final BigDecimal plannedQuantity,
            final BigDecimal producedQuantity, final BigDecimal usedQuantity) {
        Entity productionCounting = dataDefinitionService.get(BASICPRODUCTIONCOUNTING_PLUGIN_IDENTIFIER,
                BASICPRODUCTIONCOUNTING_MODEL_BASICPRODUCTIONCOUNTING).create();
        productionCounting.setField(L_PRODUCT, product);
        productionCounting.setField(L_PLANNED_QUANTITY, plannedQuantity);
        productionCounting.setField("producedQuantity", producedQuantity);
        productionCounting.setField("usedQuantity", usedQuantity);

        return productionCounting;

    }

    private Entity addRecordOperationProductOutComponent(final Entity product, final BigDecimal usedQuantity,
            final BigDecimal plannedQuantity, final BigDecimal balance) {
        DataDefinition productOutComponentDD = dataDefinitionService.get(SamplesConstants.PRODUCTION_COUNTING_PLUGIN_IDENTIFIER,
                SamplesConstants.RECORDOPERATIONPRODUCTOUTCOMPONENT_MODEL_RECORDOPERATIONPRODUCTOUTCOMPONENT);
        Entity productOutComponent = productOutComponentDD.create();
        productOutComponent.setField(L_PRODUCT, product);
        productOutComponent.setField("usedQuantity", usedQuantity);
        productOutComponent.setField(L_PLANNED_QUANTITY, plannedQuantity);
        productOutComponent.setField("balance", balance);
        validateEntity(productOutComponent);
        return productOutComponentDD.save(productOutComponent);
    }

    private Entity addOperationComponent(final Entity technology, final Entity parent, final Entity operation) {
        Entity component = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "technologyOperationComponent").create();
        component.setField(TECHNOLOGY_MODEL_TECHNOLOGY, technology);
        component.setField("parent", parent);
        component.setField(TECHNOLOGY_MODEL_OPERATION, operation);
        component.setField("entityType", TECHNOLOGY_MODEL_OPERATION);
        component.setField(L_TPZ, operation.getField(L_TPZ));
        component.setField("tj", operation.getField("tj"));
        component.setField("machineUtilization", operation.getField("machineUtilization"));
        component.setField("laborUtilization", operation.getField("laborUtilization"));
        component.setField("productionInOneCycle", operation.getField("productionInOneCycle"));
        component.setField("countRealized", operation.getField("countRealizedOperation"));
        component.setField("countMachine", operation.getField("countMachineOperation"));
        component.setField("timeNextOperation", operation.getField("timeNextOperation"));

        component = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "technologyOperationComponent").save(component);
        validateEntity(component);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test operation component {technology="
                    + ((Entity) component.getField(TECHNOLOGY_MODEL_TECHNOLOGY)).getField(L_NUMBER) + ", parent="
                    + (parent == null ? 0 : parent.getId()) + ", operation="
                    + ((Entity) component.getField(TECHNOLOGY_MODEL_OPERATION)).getField(L_NUMBER) + "}");
        }
        return component;
    }

    private void addProductInComponent(final Entity component, final BigDecimal quantity, final Entity product) {
        Entity productComponent = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "operationProductInComponent")
                .create();
        productComponent.setField(L_OPERATION_COMPONENT, component);
        productComponent.setField(L_QUANTITY, quantity);
        productComponent.setField(BASIC_MODEL_PRODUCT, product);
        productComponent.setField("batchRequired", true);
        productComponent.setField("productBatchRequired", true);

        productComponent = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "operationProductInComponent").save(
                productComponent);
        validateEntity(productComponent);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test product component {product="
                    + ((Entity) productComponent.getField(BASIC_MODEL_PRODUCT)).getField(L_NUMBER)
                    + ", operation="
                    + ((Entity) ((Entity) productComponent.getField(SamplesConstants.L_OPERATION_COMPONENT))
                            .getField(TECHNOLOGY_MODEL_OPERATION)).getField(L_NUMBER) + ", quantity="
                    + productComponent.getField(L_QUANTITY) + "}");
        }
    }

    private void addProductOutComponent(final Entity component, final BigDecimal quantity, final Entity product) {
        Entity productComponent = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "operationProductOutComponent")
                .create();
        productComponent.setField(L_OPERATION_COMPONENT, component);
        productComponent.setField(L_QUANTITY, quantity);
        productComponent.setField(BASIC_MODEL_PRODUCT, product);

        productComponent = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "operationProductOutComponent").save(
                productComponent);
        validateEntity(productComponent);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test product component {product="
                    + ((Entity) productComponent.getField(BASIC_MODEL_PRODUCT)).getField(L_NUMBER)
                    + ", operation="
                    + ((Entity) ((Entity) productComponent.getField(L_OPERATION_COMPONENT)).getField(TECHNOLOGY_MODEL_OPERATION))
                            .getField(L_NUMBER) + ", quantity=" + productComponent.getField(L_QUANTITY) + "}");
        }
    }

    private void addMaterialRequirements(final Map<String, String> values) {
        Entity requirement = dataDefinitionService.get(SamplesConstants.MATERIALREQUIREMENTS_PLUGIN_IDENTIFIER,
                SamplesConstants.MATERIALREQUIREMENTS_MODEL_MATERIALREQUIREMENTS).create();
        requirement.setField(L_NAME, values.get(L_NAME));
        requirement.setField(L_DATE, values.get(L_DATE));
        requirement.setField(L_WORKER, values.get(L_WORKER));
        requirement.setField("onlyComponents", values.get("onlycomponents"));
        requirement.setField(L_DATE, values.get(L_DATE));
        requirement.setField(L_GENERATED, values.get(L_GENERATED));
        requirement.setField(L_FILE_NAME, values.get(L_FILE_NAME));
        requirement.setField("orders",
                Lists.newArrayList(getOrderByNumber(values.get("order1")), getOrderByNumber(values.get("order2"))));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test material requirement {name=" + requirement.getField(L_NAME) + ", date="
                    + requirement.getField(L_DATE) + ", worker=" + requirement.getField(L_WORKER) + ", onlyComponents="
                    + requirement.getField("onlyComponents") + ", generated=" + requirement.getField(L_GENERATED) + "}");
        }

        requirement = dataDefinitionService.get(SamplesConstants.MATERIALREQUIREMENTS_PLUGIN_IDENTIFIER,
                SamplesConstants.MATERIALREQUIREMENTS_MODEL_MATERIALREQUIREMENTS).save(requirement);
        validateEntity(requirement);
    }

    private void addWorkPlan(final Map<String, String> values) {

        Entity workPlan = dataDefinitionService.get(SamplesConstants.WORK_PLANS_PLUGIN_IDENTIFIER,
                SamplesConstants.WORK_PLANS_MODEL_WORK_PLAN).create();
        workPlan.setField(L_NAME, values.get(L_NAME));
        workPlan.setField(L_GENERATED, values.get(L_GENERATED));
        workPlan.setField(L_DATE, values.get(L_DATE));
        workPlan.setField(L_WORKER, values.get(L_WORKER));
        workPlan.setField(L_TYPE, values.get(L_TYPE));
        workPlan.setField(L_FILE_NAME, values.get("filename"));
        workPlan.setField("orders", Lists.newArrayList(getOrderByNumber(values.get(L_ORDER))));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test material requirement {name=" + workPlan.getField(L_NAME) + ", date=" + workPlan.getField(L_DATE)
                    + ", worker=" + workPlan.getField(L_WORKER) + ", generated=" + workPlan.getField(L_GENERATED) + "}");
        }

        workPlan = dataDefinitionService.get(SamplesConstants.WORK_PLANS_PLUGIN_IDENTIFIER,
                SamplesConstants.WORK_PLANS_MODEL_WORK_PLAN).save(workPlan);
        validateEntity(workPlan);
    }

    void addProductionRecord(final Map<String, String> values) {
        Entity productionRecord = dataDefinitionService.get(SamplesConstants.PRODUCTION_COUNTING_PLUGIN_IDENTIFIER,
                SamplesConstants.PRODUCTION_RECORD_MODEL_PRODUCTION_RECORD).create();
        productionRecord.setField(L_NUMBER, values.get("number"));
        productionRecord.setField(L_ORDER, getOrderByNumber(values.get(L_ORDER)));
        productionRecord.setField("shift", getShiftByName(values.get("shift")));
        productionRecord.setField(L_STATE, values.get(L_STATE));
        productionRecord.setField("lastRecord", values.get("lastrecord"));
        productionRecord.setField("machineTime", values.get("machinetime"));
        productionRecord.setField("machineTimeBalance", values.get("machinetimebalance"));
        productionRecord.setField("laborTime", values.get("labortime"));
        productionRecord.setField("laborTimeBalance", values.get("labortimebalance"));
        productionRecord.setField("plannedTime", values.get("plannedtime"));
        productionRecord.setField("plannedLaborTime", values.get("plannedlabortime"));
        productionRecord.setField(L_STAFF, getStaffByNumber(values.get(L_STAFF)));
        productionRecord.setField("workstationType", getWorkstationTypeByNumber(values.get("workstationtype")));
        productionRecord.setField(L_DIVISION, getDivisionByNumber(values.get(L_DIVISION)));

        // TODO ALBR add loggings for production rrcord
        // productionRecord.setField("loggings", loggings1);

        List<Entity> recOpProdInComponents = Lists.newArrayList(addRecordOperationProductInComponent(
                getProductByNumber("000018"), new BigDecimal(L_QUANTITY_150), new BigDecimal("152"), new BigDecimal("2")));
        recOpProdInComponents.add(addRecordOperationProductInComponent(getProductByNumber("000019"), new BigDecimal(
                L_QUANTITY_600), new BigDecimal(L_QUANTITY_600), BigDecimal.ZERO));
        recOpProdInComponents.add(addRecordOperationProductInComponent(getProductByNumber("000020"), new BigDecimal(
                L_QUANTITY_2400), new BigDecimal(L_QUANTITY_2400), BigDecimal.ZERO));
        recOpProdInComponents.add(addRecordOperationProductInComponent(getProductByNumber("000021"), new BigDecimal(
                L_QUANTITY_2400), new BigDecimal(L_QUANTITY_2400), BigDecimal.ZERO));
        recOpProdInComponents.add(addRecordOperationProductInComponent(getProductByNumber("000022"), new BigDecimal(
                L_QUANTITY_600), new BigDecimal(L_QUANTITY_600), BigDecimal.ZERO));
        recOpProdInComponents.add(addRecordOperationProductInComponent(getProductByNumber("000023"), new BigDecimal(
                L_QUANTITY_600), new BigDecimal(L_QUANTITY_600), BigDecimal.ZERO));
        recOpProdInComponents.add(addRecordOperationProductInComponent(getProductByNumber("000024"), new BigDecimal(
                L_QUANTITY_150), new BigDecimal("182"), new BigDecimal("32")));

        productionRecord.setField("recordOperationProductInComponents", recOpProdInComponents);

        List<Entity> recOpProdOutComponents1 = Lists.newArrayList(addRecordOperationProductOutComponent(
                getProductByNumber("000017"), new BigDecimal(L_QUANTITY_150), new BigDecimal(L_QUANTITY_150), BigDecimal.ZERO));

        recOpProdOutComponents1.add(addRecordOperationProductOutComponent(getProductByNumber("000019"), new BigDecimal(
                L_QUANTITY_600), new BigDecimal(L_QUANTITY_600), BigDecimal.ZERO));
        recOpProdOutComponents1.add(addRecordOperationProductOutComponent(getProductByNumber("000022"), new BigDecimal(
                L_QUANTITY_600), new BigDecimal(L_QUANTITY_600), BigDecimal.ZERO));
        recOpProdOutComponents1.add(addRecordOperationProductOutComponent(getProductByNumber("000023"), new BigDecimal(
                L_QUANTITY_600), new BigDecimal(L_QUANTITY_600), BigDecimal.ZERO));

        productionRecord.setField("recordOperationProductOutComponents", recOpProdOutComponents1);

        productionRecord = productionRecord.getDataDefinition().save(productionRecord);
        validateEntity(productionRecord);
    }

    void addProductionCounting(final Map<String, String> values) {
        Entity productionCounting = dataDefinitionService.get(SamplesConstants.PRODUCTION_COUNTING_PLUGIN_IDENTIFIER,
                SamplesConstants.PRODUCTION_COUNTING_MODEL_PRODUCTION_COUNTING).create();
        productionCounting.setField(L_GENERATED, values.get(L_GENERATED));
        productionCounting.setField(L_ORDER, getOrderByNumber(values.get(L_ORDER)));
        productionCounting.setField(L_PRODUCT, getProductByNumber(values.get(L_PRODUCT)));
        productionCounting.setField(L_NAME, values.get(L_NAME));
        productionCounting.setField(L_DATE, values.get(L_DATE));
        productionCounting.setField(L_WORKER, values.get(L_WORKER));
        productionCounting.setField(L_DESCRIPTION, values.get(L_DESCRIPTION));
        productionCounting.setField(L_FILE_NAME, values.get("filename"));

        productionCounting = dataDefinitionService.get(SamplesConstants.PRODUCTION_COUNTING_PLUGIN_IDENTIFIER,
                SamplesConstants.PRODUCTION_COUNTING_MODEL_PRODUCTION_COUNTING).save(productionCounting);
        validateEntity(productionCounting);
    }

    void addProductionBalance(final Map<String, String> values) {
        Entity productionbalance = dataDefinitionService.get(SamplesConstants.PRODUCTION_COUNTING_PLUGIN_IDENTIFIER,
                SamplesConstants.PRODUCTIONBALANCE_MODEL_PRODUCTIONBALANCE).create();
        productionbalance.setField(L_GENERATED, values.get(L_GENERATED));
        productionbalance.setField(L_ORDER, getOrderByNumber(values.get(L_ORDER)));
        productionbalance.setField(L_PRODUCT, getProductByNumber(values.get(L_PRODUCT)));
        productionbalance.setField(L_NAME, values.get(L_NAME));
        productionbalance.setField(L_DATE, values.get(L_DATE));
        productionbalance.setField(L_WORKER, values.get(L_WORKER));
        productionbalance.setField("recordsNumber", values.get("recordsnumber"));
        productionbalance.setField(L_DESCRIPTION, values.get(L_DESCRIPTION));
        productionbalance.setField(L_FILE_NAME, values.get("filename"));
        productionbalance.setField("calculateOperationCostsMode", values.get("calculateoperationcostsmode"));

        if (isEnabled("productionCountingWithCosts")) {
            productionbalance.setField("sourceOfMaterialCosts", values.get("sourceofmaterialcosts"));
            productionbalance.setField("calculateMaterialCostsMode", values.get("calculatematerialcostsmode"));

            productionbalance.setField("averageMachineHourlyCost", values.get("averagemachinehourlycost"));
            productionbalance.setField("averageLaborHourlyCost", values.get("averagelaborhourlycost"));
        }

        productionbalance = productionbalance.getDataDefinition().save(productionbalance);
        validateEntity(productionbalance);

    }

    void addProductionLines(final Map<String, String> values) {
        Entity productionLine = dataDefinitionService.get("productionLines", "productionLine").create();
        productionLine.setField(L_NAME, values.get(L_NAME));
        productionLine.setField(L_NUMBER, values.get(L_NUMBER));
        productionLine.setField("supportsAllTechnologies", values.get("supportsalltechnologies"));

        productionLine = productionLine.getDataDefinition().save(productionLine);
        validateEntity(productionLine);
    }

    void addQualityControl(final Map<String, String> values) {

        Entity qualitycontrol = dataDefinitionService.get(SamplesConstants.QUALITYCONTROL_PLUGIN_IDENTIFIER,
                SamplesConstants.QUALITYCONTROL_MODEL_QUALITYCONTROL).create();

        if ("qualityControlsForUnit".equals(values.get(L_QUALITYCONTROLTYPE_3))) {
            qualitycontrol.setField(L_NUMBER, values.get(L_NUMBER));
            qualitycontrol.setField(L_ORDER, getOrderByNumber(values.get(L_ORDER)));
            qualitycontrol.setField(L_COMMENT, values.get(L_COMMENT));
            qualitycontrol.setField(L_CLOSED, values.get(L_CLOSED));
            qualitycontrol.setField("controlledQuantity", values.get("controlledquantity"));
            qualitycontrol.setField("takenForControlQuantity", values.get("takenforcontrolquantity"));
            qualitycontrol.setField("rejectedQuantity", values.get("rejectedquantity"));
            qualitycontrol.setField("acceptedDefectsQuantity", values.get("accepteddefectsquantity"));
            qualitycontrol.setField(L_STAFF, values.get(L_STAFF));
            qualitycontrol.setField(L_DATE, values.get(L_DATE));
            qualitycontrol.setField("controlInstruction", values.get("controlinstruction"));
            qualitycontrol.setField(L_QUALITY_CONTROL_TYPE2, values.get(L_QUALITYCONTROLTYPE_3));

        } else if ("qualityControlsForOrder".equals(values.get(L_QUALITYCONTROLTYPE_3))) {
            qualitycontrol.setField(L_NUMBER, values.get(L_NUMBER));
            qualitycontrol.setField(L_ORDER, getOrderByNumber(values.get(L_ORDER)));
            qualitycontrol.setField("ControlResult", values.get("controlresult"));
            qualitycontrol.setField(L_COMMENT, values.get(L_COMMENT));
            qualitycontrol.setField(L_CLOSED, values.get(L_CLOSED));
            qualitycontrol.setField("controlInstruction", values.get("controlinstruction"));
            qualitycontrol.setField(L_STAFF, values.get(L_STAFF));
            qualitycontrol.setField(L_DATE, values.get(L_DATE));
            qualitycontrol.setField(L_QUALITY_CONTROL_TYPE2, values.get(L_QUALITYCONTROLTYPE_3));

        } else if (L_QUALITY_CONTROLS_FOR_OPERATION.equals(values.get(L_QUALITYCONTROLTYPE_3))) {
            qualitycontrol.setField(L_NUMBER, values.get(L_NUMBER));
            qualitycontrol.setField(L_ORDER, getOrderByNumber(values.get(L_ORDER)));
            qualitycontrol.setField(
                    L_OPERATION,
                    getTechnologyInstanceOperationComponentByNumber(values.get(L_OPERATION),
                            getOrderByNumber(values.get(L_ORDER))));
            qualitycontrol.setField("ControlResult", values.get("controlresult"));
            qualitycontrol.setField(L_COMMENT, values.get(L_COMMENT));
            qualitycontrol.setField(L_CLOSED, values.get(L_CLOSED));
            qualitycontrol.setField(L_STAFF, values.get(L_STAFF));
            qualitycontrol.setField(L_DATE, values.get(L_DATE));
            qualitycontrol.setField(L_QUALITY_CONTROL_TYPE2, values.get(L_QUALITYCONTROLTYPE_3));

        }

        qualitycontrol = qualitycontrol.getDataDefinition().save(qualitycontrol);
        validateEntity(qualitycontrol);
    }

    private Entity getRandomStaff() {
        Long total = (long) dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_STAFF).find().list()
                .getTotalNumberOfEntities();
        return dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_STAFF).find()
                .setFirstResult(RANDOM.nextInt(total.intValue())).setMaxResults(1).list().getEntities().get(0);
    }

    private Entity getMachine(final String id) {
        List<Entity> machines = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_WORKSTATION_TYPE).find()
                .add(SearchRestrictions.eq(L_NUMBER, id)).list().getEntities();
        if (machines.isEmpty()) {
            return null;
        }
        return machines.get(0);
    }

    private Entity getTechnologyByNumber(final String number) {
        return dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, TECHNOLOGY_MODEL_TECHNOLOGY).find()
                .add(SearchRestrictions.eq(L_NUMBER, number)).setMaxResults(1).uniqueResult();
    }

    private Entity getDefaultTechnologyForProduct(final Entity product) {
        if (product == null) {
            return null;
        }
        List<Entity> technologies = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, TECHNOLOGY_MODEL_TECHNOLOGY).find()
                .add(SearchRestrictions.belongsTo(BASIC_MODEL_PRODUCT, product)).add(SearchRestrictions.eq("master", true))
                .setMaxResults(1).list().getEntities();
        if (technologies.isEmpty()) {
            return null;
        } else {
            return technologies.get(0);
        }
    }

    private Entity getTechnologyInstanceOperationComponentByNumber(final String number, final Entity order) {
        Entity operation = dataDefinitionService
                .get(SamplesConstants.TECHNOLOGIES_PLUGIN_IDENTIFIER, SamplesConstants.TECHNOLOGY_MODEL_OPERATION).find()
                .add(SearchRestrictions.eq(L_NUMBER, number)).setMaxResults(1).uniqueResult();
        return dataDefinitionService
                .get(SamplesConstants.TECHNOLOGIES_PLUGIN_IDENTIFIER,
                        SamplesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT).find()
                .add(SearchRestrictions.belongsTo(L_ORDER, order)).add(SearchRestrictions.belongsTo(L_OPERATION, operation))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getShiftByName(final String name) {
        return dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, SamplesConstants.BASIC_MODEL_SHIFT).find()
                .add(SearchRestrictions.eq(L_NAME, name)).setMaxResults(1).uniqueResult();
    }

    private Entity getOrderByNumber(final String number) {
        return dataDefinitionService.get(ORDERS_PLUGIN_IDENTIFIER, ORDERS_MODEL_ORDER).find()
                .add(SearchRestrictions.eq(L_NUMBER, number)).setMaxResults(1).uniqueResult();
    }

    private Entity getProductByNumber(final String number) {
        return dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_PRODUCT).find()
                .add(SearchRestrictions.eq(L_NUMBER, number)).setMaxResults(1).uniqueResult();
    }

    private Entity getOperationByNumber(final String number) {
        return dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, TECHNOLOGY_MODEL_OPERATION).find()
                .add(SearchRestrictions.eq(L_NUMBER, number)).setMaxResults(1).uniqueResult();
    }

    private Entity getStaffByNumber(final String number) {
        return dataDefinitionService.get("basic", L_STAFF).find().add(SearchRestrictions.eq(L_NUMBER, number)).setMaxResults(1)
                .uniqueResult();
    }

    private Entity getWorkstationTypeByNumber(final String number) {
        return dataDefinitionService.get("basic", "workstationType").find().add(SearchRestrictions.eq(L_NUMBER, number))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getDivisionByNumber(final String number) {
        return dataDefinitionService.get("basic", L_DIVISION).find().add(SearchRestrictions.eq(L_NUMBER, number))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getTransformationByNumber(final String number) {
        return dataDefinitionService.get(L_MATERIAL_FLOW, L_TRANSFORMATIONS).find().add(SearchRestrictions.eq(L_NUMBER, number))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getStockAreaByNumber(final String number) {
        return dataDefinitionService.get(L_MATERIAL_FLOW, L_STOCK_AREAS).find().add(SearchRestrictions.eq(L_NUMBER, number))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getSupplierByNumber(final String number) {
        return dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, "company").find().add(SearchRestrictions.eq(L_NUMBER, number))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getBatchByNumber(final String number) {
        return dataDefinitionService.get(L_ADVANCED_GENEALOGY, L_BATCH).find().add(SearchRestrictions.eq(L_NUMBER, number))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getRandomProduct() {
        Long total = (long) dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_PRODUCT).find().list()
                .getTotalNumberOfEntities();
        return dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_PRODUCT).find()
                .setFirstResult(RANDOM.nextInt(total.intValue())).setMaxResults(1).list().getEntities().get(0);
    }

    private void buildTrackingRecord(final Entity trackingRecord) {
        Entity genProdIn = addGenealogyProductInComponent(trackingRecord, "000011", L_5);
        addUsedBatch(genProdIn, "321DEW");
        genProdIn = addGenealogyProductInComponent(trackingRecord, "000012", L_5);
        addUsedBatch(genProdIn, "706FCV");
        genProdIn = addGenealogyProductInComponent(trackingRecord, "000014", L_5);
        addUsedBatch(genProdIn, "980DEN");
        addUsedBatch(genProdIn, "767BMM");
        genProdIn = addGenealogyProductInComponent(trackingRecord, "000013", L_5);
        addUsedBatch(genProdIn, "876DEW");
        addUsedBatch(genProdIn, "444VWM");
        genProdIn = addGenealogyProductInComponent(trackingRecord, "000015", L_1);
        addUsedBatch(genProdIn, "349SWA");
        genProdIn = addGenealogyProductInComponent(trackingRecord, "000016", L_2);
        addUsedBatch(genProdIn, "150DEB");
    }

    private void addUsedBatch(final Entity genealogyProductInComponent, final String batchNumber) {
        Entity genealogyProductInBatch = dataDefinitionService.get(L_ADVANCED_GENEALOGY_FOR_ORDERS, "genealogyProductInBatch")
                .create();

        genealogyProductInBatch.setField(L_BATCH, getBatchByNumber(batchNumber));
        genealogyProductInBatch.setField("genealogyProductInComponent", genealogyProductInComponent);

        genealogyProductInBatch = genealogyProductInBatch.getDataDefinition().save(genealogyProductInBatch);
        validateEntity(genealogyProductInBatch);
    }

    private Entity addGenealogyProductInComponent(final Entity trackingRecord, final String productNumber,
            final String operationNumber) {
        Entity product = getProductByNumber(productNumber);
        Entity order = trackingRecord.getBelongsToField(ORDERS_MODEL_ORDER);
        Entity technology = order.getBelongsToField(TECHNOLOGY_MODEL_TECHNOLOGY);
        Entity operationProdInComp = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "operationProductInComponent")
                .find().add(SearchRestrictions.belongsTo(L_PRODUCT, product)).setMaxResults(1).uniqueResult();
        Entity technologyInstanceOperationComponent = dataDefinitionService
                .get(SamplesConstants.TECHNOLOGIES_PLUGIN_IDENTIFIER,
                        SamplesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)
                .find()
                .add(SearchRestrictions.and(SearchRestrictions.belongsTo(ORDERS_MODEL_ORDER, order),
                        SearchRestrictions.belongsTo(TECHNOLOGY_MODEL_TECHNOLOGY, technology),
                        SearchRestrictions.belongsTo(L_OPERATION, getOperationByNumber(operationNumber)))).setMaxResults(1)
                .uniqueResult();

        Entity genealogyProductInComponent = dataDefinitionService
                .get(L_ADVANCED_GENEALOGY_FOR_ORDERS, "genealogyProductInComponent")
                .find()
                .add(SearchRestrictions.and(SearchRestrictions.belongsTo("trackingRecord", trackingRecord), SearchRestrictions
                        .belongsTo("productInComponent", operationProdInComp), SearchRestrictions.belongsTo(
                        SamplesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT, technologyInstanceOperationComponent)))
                .setMaxResults(1).uniqueResult();
        return genealogyProductInComponent;
    }

}
