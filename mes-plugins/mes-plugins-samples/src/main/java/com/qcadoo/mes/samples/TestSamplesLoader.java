/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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

import static com.qcadoo.mes.samples.constants.SamplesConstants.ADVANCED_GENEALOGY_FOR_ORDERS_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.ADVANCED_GENEALOGY_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.BASIC_MODEL_PRODUCT;
import static com.qcadoo.mes.samples.constants.SamplesConstants.BASIC_MODEL_STAFF;
import static com.qcadoo.mes.samples.constants.SamplesConstants.BASIC_MODEL_SUBSTITUTE;
import static com.qcadoo.mes.samples.constants.SamplesConstants.BASIC_MODEL_WORKSTATION_TYPE;
import static com.qcadoo.mes.samples.constants.SamplesConstants.BASIC_PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.BATCHES_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.BATCH_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.CLOSED_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.COMMENT_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.COST_CALCULATION_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.DATE_FROM_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.DATE_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.DATE_TO_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.DESCRIPTION_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.DIVISION_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_DATE;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_DESCRIPTION;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_EAN;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_GENERATED;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_NAME;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_OPERATION_COMPONENT;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_QUANTITY;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_SURNAME;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_TPZ;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_WORKER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FILE_NAME_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.GENEALOGY_TABLES_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.GENERATED_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.MATERIAL_FLOW_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.MATERIAL_REQUIREMENTS_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.NAME_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.NUMBER_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.OPERATION_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.ORDERS_MODEL_ORDER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.ORDERS_PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.ORDER_GROUPS_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.ORDER_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.ORDER_NR_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.ORDER_STATE;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PLANNED_QUANTITY_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PRODUCTION_BALANCE_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PRODUCTION_COUNTING_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PRODUCTION_RECORD_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PRODUCTS_PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PRODUCT_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PRODUCT_NUMBER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_10;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_11;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_12;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_13;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_14;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_15;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_16;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_17;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_18;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_19;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_20;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_21;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_22;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_23;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_24;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_25;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_26;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_27;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_28;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_29;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_30;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_31;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_32;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PROD_NR_33;
import static com.qcadoo.mes.samples.constants.SamplesConstants.QUALITYCONTROLTYPE_3;
import static com.qcadoo.mes.samples.constants.SamplesConstants.QUALITY_CONTROLS_FOR_OPERATION_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.QUALITY_CONTROLS_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.QUALITY_CONTROL_TYPE2_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.QUALITY_CONTROL_TYPE_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.QUANTITY_150_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.QUANTITY_2400_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.QUANTITY_600_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.STAFF_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.STATE_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.STOCK_AREAS_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.STOCK_CORRECTION_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.TECHNOLOGIES_PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.TECHNOLOGY_MODEL_OPERATION;
import static com.qcadoo.mes.samples.constants.SamplesConstants.TECHNOLOGY_MODEL_TECHNOLOGY;
import static com.qcadoo.mes.samples.constants.SamplesConstants.TIME_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.TRACKING_RECORDS_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.TRANSFER_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.TRANSFORMATIONS_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.TYPE_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.WORKER_L;
import static com.qcadoo.mes.samples.constants.SamplesConstants.WORK_PLANS_L;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");

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
            if (isEnabled(ORDER_GROUPS_L)) {
                readDataFromXML(dataset, ORDER_GROUPS_L, locale);
            }
        }

        if (isEnabled(COST_CALCULATION_L)) {
            readDataFromXML(dataset, COST_CALCULATION_L, locale);
        }
        if (isEnabled(MATERIAL_FLOW_L)) {
            readDataFromXML(dataset, STOCK_AREAS_L, locale);
            readDataFromXML(dataset, TRANSFORMATIONS_L, locale);
            readDataFromXML(dataset, TRANSFER_L, locale);
            readDataFromXML(dataset, STOCK_CORRECTION_L, locale);
        }

        if (isEnabled(QUALITY_CONTROLS_L)) {
            readDataFromXML(dataset, QUALITY_CONTROLS_L, locale);
        }

        if (isEnabled(MATERIAL_REQUIREMENTS_L)) {
            readDataFromXML(dataset, MATERIAL_REQUIREMENTS_L, locale);
        }

        if (isEnabled(WORK_PLANS_L)) {
            readDataFromXML(dataset, WORK_PLANS_L, locale);
        }

        if (isEnabled(PRODUCTION_COUNTING_L)) {

            readDataFromXML(dataset, PRODUCTION_RECORD_L, locale);
            readDataFromXML(dataset, PRODUCTION_COUNTING_L, locale);
            readDataFromXML(dataset, PRODUCTION_BALANCE_L, locale);

        }
        if (isEnabled(ADVANCED_GENEALOGY_L)) {
            readDataFromXML(dataset, BATCHES_L, locale);
            if (isEnabled(ADVANCED_GENEALOGY_FOR_ORDERS_L)) {
                readDataFromXML(dataset, TRACKING_RECORDS_L, locale);
            }
            readDataFromXML(dataset, GENEALOGY_TABLES_L, locale);
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
        } else if (DIVISION_L.equals(type)) {
            addDivision(values);
        } else if (ORDER_GROUPS_L.equals(type)) {
            addOrderGroup(values);
        } else if (COST_CALCULATION_L.equals(type)) {
            addCostCalculation(values);
        } else if (STOCK_AREAS_L.equals(type)) {
            addStokckArea(values);
        } else if (TRANSFORMATIONS_L.equals(type)) {
            addTransformation(values);
        } else if (TRANSFER_L.equals(type)) {
            addTransfer(values);
        } else if (STOCK_CORRECTION_L.equals(type)) {
            addStockCorrection(values);
        } else if (BATCHES_L.equals(type)) {
            addBatches(values);
        } else if (TRACKING_RECORDS_L.equals(type)) {
            addTrackingRecord(values);
        } else if (GENEALOGY_TABLES_L.equals(type)) {
            addGenealogyTables(values);
        } else if (QUALITY_CONTROLS_L.equals(type)) {
            addQualityControl(values);
        } else if (MATERIAL_REQUIREMENTS_L.equals(type)) {
            addMaterialRequirements(values);
        } else if (WORK_PLANS_L.equals(type)) {
            addWorkPlan(values);
        } else if (PRODUCTION_RECORD_L.equals(type)) {
            addProductionRecord(values);
        } else if (PRODUCTION_COUNTING_L.equals(type)) {
            addProductionCounting(values);
        } else if (PRODUCTION_BALANCE_L.equals(type)) {
            addProductionBalance(values);
        }
    }

    private void addWorkstationType(final Map<String, String> values) {
        Entity machine = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_WORKSTATION_TYPE).create();

        LOG.debug("id: " + values.get("id") + " name " + values.get(FIELD_NAME) + " prod_line " + values.get("prod_line")
                + " description " + values.get(FIELD_DESCRIPTION));
        machine.setField(NUMBER_L, values.get("id"));
        machine.setField(FIELD_NAME, values.get(FIELD_NAME));
        machine.setField(FIELD_DESCRIPTION, values.get(FIELD_DESCRIPTION));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test machine item {machine=" + machine.getField(FIELD_NAME) + ", " + NUMBER_L + "="
                    + machine.getField(NUMBER_L) + "}");
        }

        machine = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_WORKSTATION_TYPE).save(machine);

        validateEntity(machine);
    }

    private void addStaff(final Map<String, String> values) {
        Entity staff = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_STAFF).create();

        LOG.debug("id: " + values.get("id") + " name " + values.get(FIELD_NAME) + " " + FIELD_SURNAME + " "
                + values.get(FIELD_SURNAME) + " post " + values.get("post"));
        staff.setField(NUMBER_L, values.get("id"));
        staff.setField(FIELD_NAME, values.get(FIELD_NAME));
        staff.setField(FIELD_SURNAME, values.get(FIELD_SURNAME));
        staff.setField("post", values.get("post"));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test staff item {staff=" + staff.getField(FIELD_NAME) + ", " + FIELD_SURNAME + "= "
                    + staff.getField(FIELD_SURNAME) + "}");
        }

        staff = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_STAFF).save(staff);
        validateEntity(staff);
    }

    private void addOperations(final Map<String, String> values) {
        Entity operation = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, TECHNOLOGY_MODEL_OPERATION).create();

        operation.setField(FIELD_NAME, values.get(FIELD_NAME));
        operation.setField(NUMBER_L, values.get(NUMBER_L));
        operation.setField(FIELD_TPZ, values.get(FIELD_TPZ));
        operation.setField("tj", values.get("tj"));
        operation.setField("productionInOneCycle", values.get("productioninonecycle"));
        operation.setField("countRealized", values.get("countRealized"));
        operation.setField("machineUtilization", values.get("machineutilization"));
        operation.setField("laborUtilization", values.get("laborutilization"));
        operation.setField("countMachineOperation", values.get("countmachine"));
        operation.setField("countRealizedOperation", "01all");
        operation.setField("timeNextOperation", values.get("timenextoperation"));
        operation.setField(BASIC_MODEL_WORKSTATION_TYPE, getMachine(values.get(NUMBER_L)));
        operation.setField(BASIC_MODEL_STAFF, getRandomStaff());

        if (isEnabled("costNormsForOperation")) {
            operation.setField("pieceworkCost", values.get("pieceworkcost"));
            operation.setField("machineHourlyCost", values.get("machinehourlycost"));
            operation.setField("laborHourlyCost", values.get("laborhourlycost"));
            operation.setField("numberOfOperations", values.get("numberofoperations"));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test operation item {name=" + operation.getField(FIELD_NAME) + ", " + NUMBER_L + "="
                    + operation.getField(NUMBER_L) + "}");
        }

        operation = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, TECHNOLOGY_MODEL_OPERATION).save(operation);
        validateEntity(operation);
    }

    private void addProduct(final Map<String, String> values) {
        Entity product = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_PRODUCT).create();
        product.setField("category", getRandomDictionaryItem("categories"));
        if (!values.get(FIELD_EAN).isEmpty()) {
            product.setField(FIELD_EAN, values.get(FIELD_EAN));
        }
        if (!values.get(FIELD_NAME).isEmpty()) {
            product.setField(FIELD_NAME, values.get(FIELD_NAME));
        }
        if (!values.get(BATCH_L).isEmpty()) {
            product.setField(BATCH_L, values.get(BATCH_L));
        }
        if (!values.get(PRODUCT_NUMBER).isEmpty()) {
            product.setField(NUMBER_L, values.get(PRODUCT_NUMBER));
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
                    + product.getField(FIELD_EAN) + ", name=" + product.getField(FIELD_NAME) + ", " + NUMBER_L + "="
                    + product.getField(NUMBER_L) + ", globalTypeOfMaterial=" + product.getField("typeOfMaterial") + ", unit="
                    + product.getField("unit") + "}");
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < RANDOM.nextInt(5); i++) {
            for (int j = 0; j <= i; j++) {
                stringBuilder.append("#");
            }
            addSubstitute(values.get(FIELD_NAME) + stringBuilder.toString(),
                    values.get(PRODUCT_NUMBER) + stringBuilder.toString(), product, i + 1);
        }
    }

    private void addSubstitute(final String name, final String number, final Entity product, final int priority) {
        Entity substitute = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_SUBSTITUTE).create();
        substitute.setField(FIELD_NAME, name);
        substitute.setField(NUMBER_L, number);
        substitute.setField("priority", priority);
        substitute.setField(BASIC_MODEL_PRODUCT, product);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test substitute {name=" + substitute.getField(FIELD_NAME) + ", " + NUMBER_L + "="
                    + substitute.getField(NUMBER_L) + ", priority=" + substitute.getField("priority") + ", product="
                    + ((Entity) substitute.getField(BASIC_MODEL_PRODUCT)).getField(NUMBER_L) + "}");
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
        substituteComponent.setField(FIELD_QUANTITY, numberService.setScale(new BigDecimal(quantity)));
        substituteComponent.setField(BASIC_MODEL_SUBSTITUTE, substitute);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test substitute component {substitute="
                    + ((Entity) substituteComponent.getField(BASIC_MODEL_SUBSTITUTE)).getField(NUMBER_L) + ", product="
                    + ((Entity) substituteComponent.getField(BASIC_MODEL_PRODUCT)).getField(NUMBER_L) + ", quantity="
                    + substituteComponent.getField(FIELD_QUANTITY) + "}");
        }

        substituteComponent = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, "substituteComponent").save(substituteComponent);
        validateEntity(substituteComponent);
    }

    private void prepareTechnologiesForOrder(final Map<String, String> values) {
        Entity technology = getTechnologyByNumber(values.get("tech_nr"));
        technology.setField(ORDER_STATE, "02accepted");
        technology.getDataDefinition().save(technology);
    }

    private void addOrderGroup(final Map<String, String> values) {
        Entity orderGroup = dataDefinitionService.get(ORDER_GROUPS_L, "orderGroup").create();
        orderGroup.setField(NUMBER_L, values.get(NUMBER_L));
        orderGroup.setField(FIELD_NAME, values.get(FIELD_NAME));

        Entity order3 = getOrderByNumber("000003");
        Entity order2 = getOrderByNumber("000002");

        orderGroup.setField(DATE_TO_L, order3.getField(DATE_TO_L));
        orderGroup.setField(DATE_FROM_L, order2.getField(DATE_FROM_L));

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

        if ("000001".equals(values.get(ORDER_NR_L))) {
            endDate = startDate + MILLIS_IN_DAY + 1 * millsInHour + 45 * millsInMinute;
        } else if ("000002".equals(values.get(ORDER_NR_L))) {
            startDate -= 2 * MILLIS_IN_DAY;
            endDate = startDate + MILLIS_IN_DAY + 3 * millsInHour + 40 * millsInMinute;
        } else if ("000003".equals(values.get(ORDER_NR_L))) {
            startDate += 2 * MILLIS_IN_DAY;
            endDate = startDate + 6 * millsInHour + 50 * millsInMinute;
        }

        if (!values.get("scheduled_end_date").isEmpty()) {
            try {
                endDate = FORMATTER.parse(values.get("scheduled_end_date")).getTime();
            } catch (ParseException e) {
                LOG.warn(e.getMessage(), e);
            }
        }

        Entity order = dataDefinitionService.get(ORDERS_PLUGIN_IDENTIFIER, ORDERS_MODEL_ORDER).create();
        order.setField(DATE_FROM_L, new Date(startDate));
        order.setField(DATE_TO_L, new Date(endDate));
        order.setField("externalSynchronized", true);

        order.setField(TECHNOLOGY_MODEL_TECHNOLOGY, getTechnologyByNumber(values.get("tech_nr")));
        order.setField(FIELD_NAME, (values.get(FIELD_NAME).isEmpty() || values.get(FIELD_NAME) == null) ? values.get(ORDER_NR_L)
                : values.get(FIELD_NAME));
        order.setField(NUMBER_L, values.get(ORDER_NR_L));
        order.setField(PLANNED_QUANTITY_L, values.get("quantity_scheduled").isEmpty() ? new BigDecimal(
                100 * RANDOM.nextDouble() + 1) : new BigDecimal(values.get("quantity_scheduled")));

        order.setField("trackingRecordTreatment", "01duringProduction");
        order.setField(ORDER_STATE, values.get("status"));

        Entity product = getProductByNumber(values.get(PRODUCT_NUMBER));

        if (isEnabled(PRODUCTION_COUNTING_L)) {
            order.setField("typeOfProductionRecording", values.get("type_of_production_recording"));
            order.setField("registerQuantityInProduct", values.get("register_quantity_in_product"));
            order.setField("registerQuantityOutProduct", values.get("register_quantity_out_product"));
            order.setField("registerProductionTime", values.get("register_production_time"));
            order.setField("justOne", values.get("just_one"));
            order.setField("allowToClose", values.get("allow_to_close"));
            order.setField("autoCloseOrder", values.get("auto_close_order"));
        }

        if (isEnabled(ADVANCED_GENEALOGY_FOR_ORDERS_L)) {
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
                    + order.getField(FIELD_NAME)
                    + ", "
                    + NUMBER_L
                    + "="
                    + order.getField(NUMBER_L)
                    + ", product="
                    + (order.getField(BASIC_MODEL_PRODUCT) == null ? null : ((Entity) order.getField(BASIC_MODEL_PRODUCT))
                            .getField(NUMBER_L))
                    + ", technology="
                    + (order.getField(TECHNOLOGY_MODEL_TECHNOLOGY) == null ? null : ((Entity) order
                            .getField(TECHNOLOGY_MODEL_TECHNOLOGY)).getField(NUMBER_L)) + ", dateFrom="
                    + order.getField(DATE_FROM_L) + ", dateTo=" + order.getField(DATE_TO_L) + ", effectiveDateFrom="
                    + order.getField("effectiveDateFrom") + ", effectiveDateTo=" + order.getField("effectiveDateTo")
                    + ", doneQuantity=" + order.getField("doneQuantity") + ", plannedQuantity="
                    + order.getField(PLANNED_QUANTITY_L) + ", trackingRecordTreatment="
                    + order.getField("trackingRecordTreatment") + ", state=" + order.getField(ORDER_STATE) + "}");
        }

        order = dataDefinitionService.get(ORDERS_PLUGIN_IDENTIFIER, ORDERS_MODEL_ORDER).save(order);
        validateEntity(order);

    }

    private void addBatches(final Map<String, String> values) {
        Entity batch = dataDefinitionService.get(ADVANCED_GENEALOGY_L, BATCH_L).create();

        batch.setField(NUMBER_L, values.get(NUMBER_L));
        batch.setField(PRODUCT_L, getProductByNumber(values.get("product_nr")));
        batch.setField("manufacturer", getManufacturerByNumber(values.get("manufacturer_nr")));
        batch.setField(STATE_L, "01tracked");

        batch = batch.getDataDefinition().save(batch);
        validateEntity(batch);
    }

    private void addTrackingRecord(final Map<String, String> values) {
        Entity trackingRecord = dataDefinitionService.get(ADVANCED_GENEALOGY_L, "trackingRecord").create();

        trackingRecord.setField("entityType", values.get("entity_type"));
        trackingRecord.setField(NUMBER_L, values.get(NUMBER_L));
        trackingRecord.setField("producedBatch", getBatchByNumber(values.get("produced_batch_no")));
        trackingRecord.setField(ORDER_L, getOrderByNumber(values.get("order_no")));
        trackingRecord.setField(STATE_L, "01draft");

        trackingRecord = trackingRecord.getDataDefinition().save(trackingRecord);
        validateEntity(trackingRecord);

        buildTrackingRecord(trackingRecord);
    }

    private void addGenealogyTables(final Map<String, String> values) {
        Entity genealogyTable = dataDefinitionService.get(ADVANCED_GENEALOGY_L, "genealogyReport").create();

        genealogyTable.setField(TYPE_L, values.get(TYPE_L));
        genealogyTable.setField(FIELD_NAME, values.get(FIELD_NAME));
        genealogyTable.setField("includeDraft", values.get("include_draft"));
        genealogyTable.setField("directRelatedOnly", values.get("direct_related_only"));
        genealogyTable.setField(BATCH_L, getBatchByNumber(values.get("batch_no")));

        genealogyTable = genealogyTable.getDataDefinition().save(genealogyTable);
        validateEntity(genealogyTable);
    }

    private void addDivision(final Map<String, String> values) {
        Entity division = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, DIVISION_L).create();

        division.setField(NUMBER_L, values.get("NUMBER"));
        division.setField(NAME_L, values.get("NAME"));
        division.setField("supervisor", values.get("SUPERVISOR"));

        division = division.getDataDefinition().save(division);
        validateEntity(division);
    }

    private void addCostCalculation(final Map<String, String> values) {
        Entity costCalculation = dataDefinitionService.get(COST_CALCULATION_L, COST_CALCULATION_L).create();

        costCalculation.setField(NUMBER_L, values.get(NUMBER_L));
        costCalculation.setField(FIELD_DESCRIPTION, values.get(FIELD_DESCRIPTION));
        costCalculation.setField(ORDERS_MODEL_ORDER, getOrderByNumber(values.get("order_no")));
        costCalculation.setField(TECHNOLOGY_MODEL_TECHNOLOGY, getTechnologyByNumber(values.get("tech_no")));
        costCalculation.setField("defaultTechnology", getTechnologyByNumber(values.get("def_tech_no")));
        costCalculation.setField(PRODUCT_L, getProductByNumber(values.get("prod_no")));
        costCalculation.setField("quantity", values.get("quantity"));
        costCalculation.setField("includeTPZ", values.get("include_tpz"));
        costCalculation.setField("calculateMaterialCostsMode", values.get("calculate_material_cost_mode"));
        costCalculation.setField("calculateOperationCostsMode", values.get("calculate_operation_cost_mode"));
        costCalculation.setField("additionalOverhead", values.get("additional_overhead"));
        costCalculation.setField("productionCostMargin", values.get("production_cost_margin"));
        costCalculation.setField("materialCostMargin", values.get("material_cost_margin"));

        costCalculation = costCalculation.getDataDefinition().save(costCalculation);

        validateEntity(costCalculation);
    }

    private void addStokckArea(Map<String, String> values) {
        Entity stockArea = dataDefinitionService.get(MATERIAL_FLOW_L, STOCK_AREAS_L).create();

        stockArea.setField(NUMBER_L, values.get(NUMBER_L));
        stockArea.setField(FIELD_NAME, values.get(FIELD_NAME));

        stockArea = stockArea.getDataDefinition().save(stockArea);
        validateEntity(stockArea);
    }

    private void addTransformation(Map<String, String> values) {
        Entity transformation = dataDefinitionService.get(MATERIAL_FLOW_L, TRANSFORMATIONS_L).create();

        transformation.setField(NUMBER_L, values.get(NUMBER_L));
        transformation.setField(FIELD_NAME, values.get(FIELD_NAME));
        transformation.setField(TIME_L, values.get(TIME_L));
        transformation.setField("stockAreasFrom", getStockAreaByNumber(values.get("stock_areas_from")));
        transformation.setField("stockAreasTo", getStockAreaByNumber(values.get("stock_areas_to")));
        transformation.setField(STAFF_L, getStaffByNumber(values.get(STAFF_L)));

        transformation = transformation.getDataDefinition().save(transformation);
        validateEntity(transformation);
    }

    private void addStockCorrection(Map<String, String> values) {
        Entity stockCorrection = dataDefinitionService.get(MATERIAL_FLOW_L, STOCK_CORRECTION_L).create();

        stockCorrection.setField(NUMBER_L, values.get(NUMBER_L));
        stockCorrection.setField("stockCorrectionDate", values.get("stock_correction_date"));
        stockCorrection.setField(STOCK_AREAS_L, getStockAreaByNumber(values.get("stock_areas")));
        stockCorrection.setField(PRODUCT_L, getProductByNumber(values.get(PRODUCT_L)));
        stockCorrection.setField(STAFF_L, getStaffByNumber(values.get(STAFF_L)));
        stockCorrection.setField("found", values.get("found"));

        stockCorrection = stockCorrection.getDataDefinition().save(stockCorrection);
        validateEntity(stockCorrection);
    }

    private void addTransfer(Map<String, String> values) {
        Entity transfer = dataDefinitionService.get(MATERIAL_FLOW_L, TRANSFER_L).create();

        transfer.setField(NUMBER_L, values.get(NUMBER_L));
        transfer.setField(TYPE_L, values.get(TYPE_L));
        transfer.setField(PRODUCT_L, getProductByNumber(values.get(PRODUCT_L)));
        transfer.setField(FIELD_QUANTITY, values.get(FIELD_QUANTITY));
        transfer.setField(STAFF_L, getStaffByNumber(values.get(STAFF_L)));
        transfer.setField("stockAreasFrom", getStockAreaByNumber(values.get("stock_areas_from")));
        transfer.setField("stockAreasTo", getStockAreaByNumber(values.get("stock_areas_to")));
        transfer.setField(TIME_L, values.get(TIME_L));

        transfer.setField("transformationsConsumption", getTransformationByNumber(values.get("transformations_consumption")));
        transfer.setField("transformationsProduction", getTransformationByNumber(values.get("transformations_production")));

        transfer = transfer.getDataDefinition().save(transfer);
        validateEntity(transfer);
    }

    private void addTechnology(final Map<String, String> values) {
        Entity product = getProductByNumber(values.get(PRODUCT_NUMBER));

        if (product != null) {
            Entity defaultTechnology = getDefaultTechnologyForProduct(product);

            Entity technology = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, TECHNOLOGY_MODEL_TECHNOLOGY).create();
            if (!values.get(FIELD_DESCRIPTION).isEmpty()) {
                technology.setField(FIELD_DESCRIPTION, values.get(FIELD_DESCRIPTION));
            }
            technology.setField("master", defaultTechnology == null);
            technology.setField(FIELD_NAME, values.get(FIELD_NAME));
            technology.setField(NUMBER_L, values.get("bom_nr"));
            technology.setField(BASIC_MODEL_PRODUCT, product);
            technology.setField(ORDER_STATE, "01draft");
            technology.setField(DESCRIPTION_L, values.get("DESCRIPTION"));
            technology.setField("batchRequired", true);
            technology.setField("postFeatureRequired", false);
            technology.setField("otherFeatureRequired", false);
            technology.setField("shiftFeatureRequired", false);
            technology.setField("technologyBatchRequired", false);

            if (isEnabled(QUALITY_CONTROLS_FOR_OPERATION_L)
                    && QUALITY_CONTROLS_FOR_OPERATION_L.equals(values.get(QUALITYCONTROLTYPE_3))) {
                technology.setField(QUALITY_CONTROL_TYPE2_L, QUALITY_CONTROLS_FOR_OPERATION_L);
            }

            if (!(isEnabled(QUALITY_CONTROLS_FOR_OPERATION_L) && "04forOperation".equals(values.get(QUALITY_CONTROL_TYPE_L)))
                    && isEnabled(QUALITY_CONTROLS_L)
                    && ("02forUnit".equals(values.get(QUALITY_CONTROL_TYPE_L)) || "03forOrder".equals(values
                            .get(QUALITY_CONTROL_TYPE_L)))) {
                technology.setField(QUALITY_CONTROL_TYPE2_L, values.get(QUALITY_CONTROL_TYPE_L));
                if ("02forUnit".equals(values.get(QUALITY_CONTROL_TYPE_L))) {
                    technology.setField("unitSamplingNr", values.get("unit_sampling_nr"));
                }
            }

            if (!values.get("minimal").isEmpty()) {
                technology.setField("minimalQuantity", values.get("minimal"));
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Add test technology {id=" + technology.getId() + ", name=" + technology.getField(FIELD_NAME) + ", "
                        + NUMBER_L + "=" + technology.getField(NUMBER_L) + ", product="
                        + ((Entity) technology.getField(BASIC_MODEL_PRODUCT)).getField(NUMBER_L) + ", description="
                        + technology.getField(FIELD_DESCRIPTION) + ", master=" + technology.getField("master") + "}");
            }

            technology = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, TECHNOLOGY_MODEL_TECHNOLOGY).save(technology);
            validateEntity(technology);

            if (PROD_NR_10.equals(values.get(PRODUCT_NUMBER))) {
                addTechnologyOperationComponentsForTableAdvanced(technology);
            } else if (PROD_NR_17.equals(values.get(PRODUCT_NUMBER))) {
                addTechnologyOperationComponentsForTabouretAdvanced(technology);
            } else if (PROD_NR_25.equals(values.get(PRODUCT_NUMBER))) {
                addTechnologyOperationComponentsForStoolAdvanced(technology);
            }

            treeNumberingService.generateNumbersAndUpdateTree(
                    dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "technologyOperationComponent"),
                    TECHNOLOGY_MODEL_TECHNOLOGY, technology.getId());
        }
    }

    private void addTechnologyOperationComponentsForTableAdvanced(final Entity technology) {
        Entity parent = addOperationComponent(technology, null, getOperationByNumber("5"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_14));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_13));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_12));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_11));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_10));
        parent = addOperationComponent(technology, parent, getOperationByNumber("1"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_15));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_11));
        parent = addOperationComponent(technology, parent, getOperationByNumber("2"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_16));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_15));
    }

    private void addTechnologyOperationComponentsForStoolAdvanced(final Entity technology) {
        Entity parent = addOperationComponent(technology, null, getOperationByNumber("5"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_27));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_26));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_25));
        parent = addOperationComponent(technology, parent, getOperationByNumber("6"));
        addProductInComponent(parent, new BigDecimal("16"), getProductByNumber(PROD_NR_21));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_30));
        addProductInComponent(parent, new BigDecimal("16"), getProductByNumber(PROD_NR_29));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_28));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_26));
        Entity parent1 = addOperationComponent(technology, parent, getOperationByNumber("4"));
        addProductInComponent(parent1, BigDecimal.ONE, getProductByNumber(PROD_NR_33));
        addProductOutComponent(parent1, BigDecimal.ONE, getProductByNumber(PROD_NR_29));
        parent = addOperationComponent(technology, parent, getOperationByNumber("1"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_31));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_28));
        parent = addOperationComponent(technology, parent, getOperationByNumber("2"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_32));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_31));
    }

    private void addTechnologyOperationComponentsForTabouretAdvanced(final Entity technology) {
        Entity parent = addOperationComponent(technology, null, getOperationByNumber("5"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_18));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_19));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_17));
        parent = addOperationComponent(technology, parent, getOperationByNumber("6"));
        addProductInComponent(parent, new BigDecimal("16"), getProductByNumber(PROD_NR_21));
        addProductInComponent(parent, new BigDecimal("16"), getProductByNumber(PROD_NR_20));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_22));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_19));
        parent = addOperationComponent(technology, parent, getOperationByNumber("1"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_23));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_22));
        parent = addOperationComponent(technology, parent, getOperationByNumber("2"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_24));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_23));
    }

    private Entity addRecordOperationProductInComponent(Entity product, BigDecimal usedQuantity, BigDecimal plannedQuantity,
            BigDecimal balance) {
        Entity productInComponent = dataDefinitionService.get(SamplesConstants.PRODUCTION_COUNTING_PLUGIN_IDENTIFIER,
                SamplesConstants.RECORDOPERATIONPRODUCTINCOMPONENT_MODEL_RECORDOPERATIONPRODUCTINCOMPONENT).create();
        productInComponent.setField(PRODUCT_L, product);
        productInComponent.setField("usedQuantity", usedQuantity);
        productInComponent.setField(PLANNED_QUANTITY_L, plannedQuantity);
        productInComponent.setField("balance", balance);

        return productInComponent;
    }

    private Entity addRecordOperationProductOutComponent(Entity product, BigDecimal usedQuantity, BigDecimal plannedQuantity,
            BigDecimal balance) {
        Entity productOutComponent = dataDefinitionService.get(SamplesConstants.PRODUCTION_COUNTING_PLUGIN_IDENTIFIER,
                SamplesConstants.RECORDOPERATIONPRODUCTOUTCOMPONENT_MODEL_RECORDOPERATIONPRODUCTOUTCOMPONENT).create();
        productOutComponent.setField(PRODUCT_L, product);
        productOutComponent.setField("usedQuantity", usedQuantity);
        productOutComponent.setField(PLANNED_QUANTITY_L, plannedQuantity);
        productOutComponent.setField("balance", balance);

        return productOutComponent;
    }

    private Entity addOperationComponent(final Entity technology, final Entity parent, final Entity operation) {
        Entity component = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "technologyOperationComponent").create();
        component.setField(TECHNOLOGY_MODEL_TECHNOLOGY, technology);
        component.setField("parent", parent);
        component.setField(TECHNOLOGY_MODEL_OPERATION, operation);
        component.setField("entityType", TECHNOLOGY_MODEL_OPERATION);
        component.setField(FIELD_TPZ, operation.getField(FIELD_TPZ));
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
                    + ((Entity) component.getField(TECHNOLOGY_MODEL_TECHNOLOGY)).getField(NUMBER_L) + ", parent="
                    + (parent == null ? 0 : parent.getId()) + ", operation="
                    + ((Entity) component.getField(TECHNOLOGY_MODEL_OPERATION)).getField(NUMBER_L) + "}");
        }
        return component;
    }

    private void addProductInComponent(final Entity component, final BigDecimal quantity, final Entity product) {
        Entity productComponent = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "operationProductInComponent")
                .create();
        productComponent.setField(FIELD_OPERATION_COMPONENT, component);
        productComponent.setField(FIELD_QUANTITY, quantity);
        productComponent.setField(BASIC_MODEL_PRODUCT, product);
        productComponent.setField("batchRequired", true);
        productComponent.setField("productBatchRequired", true);

        productComponent = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "operationProductInComponent").save(
                productComponent);
        validateEntity(productComponent);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test product component {product="
                    + ((Entity) productComponent.getField(BASIC_MODEL_PRODUCT)).getField(NUMBER_L)
                    + ", operation="
                    + ((Entity) ((Entity) productComponent.getField(SamplesConstants.FIELD_OPERATION_COMPONENT))
                            .getField(TECHNOLOGY_MODEL_OPERATION)).getField(NUMBER_L) + ", quantity="
                    + productComponent.getField(FIELD_QUANTITY) + "}");
        }
    }

    private void addProductOutComponent(final Entity component, final BigDecimal quantity, final Entity product) {
        Entity productComponent = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "operationProductOutComponent")
                .create();
        productComponent.setField(FIELD_OPERATION_COMPONENT, component);
        productComponent.setField(FIELD_QUANTITY, quantity);
        productComponent.setField(BASIC_MODEL_PRODUCT, product);

        productComponent = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "operationProductOutComponent").save(
                productComponent);
        validateEntity(productComponent);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test product component {product="
                    + ((Entity) productComponent.getField(BASIC_MODEL_PRODUCT)).getField(NUMBER_L)
                    + ", operation="
                    + ((Entity) ((Entity) productComponent.getField(FIELD_OPERATION_COMPONENT))
                            .getField(TECHNOLOGY_MODEL_OPERATION)).getField(NUMBER_L) + ", quantity="
                    + productComponent.getField(FIELD_QUANTITY) + "}");
        }
    }

    private void addMaterialRequirements(final Map<String, String> values) {
        Entity requirement = dataDefinitionService.get(SamplesConstants.MATERIALREQUIREMENTS_PLUGIN_IDENTIFIER,
                SamplesConstants.MATERIALREQUIREMENTS_MODEL_MATERIALREQUIREMENTS).create();
        requirement.setField(NAME_L, values.get(NAME_L));
        requirement.setField(DATE_L, values.get(DATE_L));
        requirement.setField(WORKER_L, values.get(WORKER_L));
        requirement.setField("onlyComponents", values.get("onlycomponents"));
        requirement.setField(DATE_L, values.get(DATE_L));
        requirement.setField(GENERATED_L, values.get(GENERATED_L));
        requirement.setField(FILE_NAME_L, values.get("filename"));
        requirement.setField("orders",
                Lists.newArrayList(getOrderByNumber(values.get("order1")), getOrderByNumber(values.get("order2"))));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test material requirement {name=" + requirement.getField(FIELD_NAME) + ", date="
                    + requirement.getField(FIELD_DATE) + ", worker=" + requirement.getField(FIELD_WORKER) + ", onlyComponents="
                    + requirement.getField("onlyComponents") + ", generated=" + requirement.getField(FIELD_GENERATED) + "}");
        }

        requirement = dataDefinitionService.get(SamplesConstants.MATERIALREQUIREMENTS_PLUGIN_IDENTIFIER,
                SamplesConstants.MATERIALREQUIREMENTS_MODEL_MATERIALREQUIREMENTS).save(requirement);
        validateEntity(requirement);
    }

    private void addWorkPlan(final Map<String, String> values) {

        Entity workPlan = dataDefinitionService.get(SamplesConstants.WORK_PLANS_PLUGIN_IDENTIFIER,
                SamplesConstants.WORK_PLANS_MODEL_WORK_PLAN).create();
        workPlan.setField(FIELD_NAME, values.get(NAME_L));
        workPlan.setField(FIELD_GENERATED, values.get(GENERATED_L));
        workPlan.setField(FIELD_DATE, values.get(DATE_L));
        workPlan.setField(FIELD_WORKER, values.get(WORKER_L));
        workPlan.setField(TYPE_L, values.get(TYPE_L));
        workPlan.setField(FILE_NAME_L, values.get("filename"));
        workPlan.setField("orders", Lists.newArrayList(getOrderByNumber(values.get(ORDER_L))));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test material requirement {name=" + workPlan.getField(FIELD_NAME) + ", date="
                    + workPlan.getField(FIELD_DATE) + ", worker=" + workPlan.getField(FIELD_WORKER) + ", generated="
                    + workPlan.getField(FIELD_GENERATED) + "}");
        }

        workPlan = dataDefinitionService.get(SamplesConstants.WORK_PLANS_PLUGIN_IDENTIFIER,
                SamplesConstants.WORK_PLANS_MODEL_WORK_PLAN).save(workPlan);
        validateEntity(workPlan);
    }

    void addProductionRecord(final Map<String, String> values) {
        Entity productionRecord = dataDefinitionService.get(SamplesConstants.PRODUCTION_COUNTING_PLUGIN_IDENTIFIER,
                SamplesConstants.PRODUCTION_RECORD_MODEL_PRODUCTION_RECORD).create();
        productionRecord.setField(NUMBER_L, getProductionRecordByNumber(NUMBER_L));
        productionRecord.setField(NAME_L, values.get(NAME_L));
        productionRecord.setField(ORDER_L, getOrderByNumber(values.get(ORDER_L)));
        productionRecord.setField("orderOperationComponent",
                getOrderOperationComponentByNumber(values.get("orderoperationcomponent"), getOrderByNumber(values.get(ORDER_L))));
        productionRecord.setField("shift", getShiftByName(values.get("shift")));
        productionRecord.setField(STATE_L, values.get(STATE_L));
        productionRecord.setField("lastRecord", values.get("lastrecord"));
        productionRecord.setField("machineTime", values.get("machinetime"));
        productionRecord.setField("machineTimeBalance", values.get("machinetimebalance"));
        productionRecord.setField("laborTime", values.get("labortime"));
        productionRecord.setField("laborTimeBalance", values.get("labortimebalance"));
        productionRecord.setField("plannedTime", values.get("plannedtime"));
        productionRecord.setField("plannedLaborTime", values.get("plannedlabortime"));
        productionRecord.setField(STAFF_L, getStaffByNumber(values.get(STAFF_L)));
        productionRecord.setField("workstationType", getWorkstationTypeByNumber(values.get("workstationtype")));
        productionRecord.setField(DIVISION_L, getDivisionByNumber(values.get(DIVISION_L)));

        String idString3 = values.get("loggings");
        Long id3 = Long.valueOf(idString3);
        Entity loggings = GetLoggingsByNumber(id3);
        List<Entity> loggings1 = Lists.newArrayList(loggings);
        productionRecord.setField("loggings", loggings1);

        List<Entity> recOpProdInComponents = Lists.newArrayList(addRecordOperationProductInComponent(
                getProductByNumber("000018"), new BigDecimal(QUANTITY_150_L), new BigDecimal("152"), new BigDecimal("2")));
        recOpProdInComponents.add(addRecordOperationProductInComponent(getProductByNumber("000019"), new BigDecimal(
                QUANTITY_600_L), new BigDecimal(QUANTITY_600_L), BigDecimal.ZERO));
        recOpProdInComponents.add(addRecordOperationProductInComponent(getProductByNumber("000020"), new BigDecimal(
                QUANTITY_2400_L), new BigDecimal(QUANTITY_2400_L), BigDecimal.ZERO));
        recOpProdInComponents.add(addRecordOperationProductInComponent(getProductByNumber("000021"), new BigDecimal(
                QUANTITY_2400_L), new BigDecimal(QUANTITY_2400_L), BigDecimal.ZERO));
        recOpProdInComponents.add(addRecordOperationProductInComponent(getProductByNumber("000022"), new BigDecimal(
                QUANTITY_600_L), new BigDecimal(QUANTITY_600_L), BigDecimal.ZERO));
        recOpProdInComponents.add(addRecordOperationProductInComponent(getProductByNumber("000023"), new BigDecimal(
                QUANTITY_600_L), new BigDecimal(QUANTITY_600_L), BigDecimal.ZERO));
        recOpProdInComponents.add(addRecordOperationProductInComponent(getProductByNumber("000024"), new BigDecimal(
                QUANTITY_150_L), new BigDecimal("182"), new BigDecimal("32")));

        productionRecord.setField("recordOperationProductInComponents", recOpProdInComponents);

        List<Entity> recOpProdOutComponents1 = Lists.newArrayList(addRecordOperationProductOutComponent(
                getProductByNumber("000017"), new BigDecimal(QUANTITY_150_L), new BigDecimal(QUANTITY_150_L), BigDecimal.ZERO));

        recOpProdOutComponents1.add(addRecordOperationProductOutComponent(getProductByNumber("000019"), new BigDecimal(
                QUANTITY_600_L), new BigDecimal(QUANTITY_600_L), BigDecimal.ZERO));
        recOpProdOutComponents1.add(addRecordOperationProductOutComponent(getProductByNumber("000022"), new BigDecimal(
                QUANTITY_600_L), new BigDecimal(QUANTITY_600_L), BigDecimal.ZERO));
        recOpProdOutComponents1.add(addRecordOperationProductOutComponent(getProductByNumber("000023"), new BigDecimal(
                QUANTITY_600_L), new BigDecimal(QUANTITY_600_L), BigDecimal.ZERO));

        productionRecord.setField("recordOperationProductOutComponents", recOpProdOutComponents1);

        productionRecord = productionRecord.getDataDefinition().save(productionRecord);
        validateEntity(productionRecord);
    }

    void addProductionCounting(final Map<String, String> values) {
        Entity productionCounting = dataDefinitionService.get(SamplesConstants.PRODUCTION_COUNTING_PLUGIN_IDENTIFIER,
                SamplesConstants.PRODUCTION_COUNTING_MODEL_PRODUCTION_COUNTING).create();
        productionCounting.setField(FIELD_GENERATED, values.get(GENERATED_L));
        productionCounting.setField(ORDER_L, getOrderByNumber(values.get(ORDER_L)));
        productionCounting.setField(PRODUCT_L, getProductByNumber(values.get(PRODUCT_L)));
        productionCounting.setField(FIELD_NAME, values.get(NAME_L));
        productionCounting.setField(FIELD_DATE, values.get(DATE_L));
        productionCounting.setField(FIELD_WORKER, values.get(WORKER_L));
        productionCounting.setField(DESCRIPTION_L, values.get(DESCRIPTION_L));
        productionCounting.setField(FILE_NAME_L, values.get("filename"));

        productionCounting = dataDefinitionService.get(SamplesConstants.PRODUCTION_COUNTING_PLUGIN_IDENTIFIER,
                SamplesConstants.PRODUCTION_COUNTING_MODEL_PRODUCTION_COUNTING).save(productionCounting);
        validateEntity(productionCounting);
    }

    void addProductionBalance(final Map<String, String> values) {
        Entity productionbalance = dataDefinitionService.get(SamplesConstants.PRODUCTION_COUNTING_PLUGIN_IDENTIFIER,
                SamplesConstants.PRODUCTIONBALANCE_MODEL_PRODUCTIONBALANCE).create();
        productionbalance.setField(GENERATED_L, values.get(GENERATED_L));
        productionbalance.setField(ORDER_L, getOrderByNumber(values.get(ORDER_L)));
        productionbalance.setField(PRODUCT_L, getProductByNumber(values.get(PRODUCT_L)));
        productionbalance.setField(NAME_L, values.get(NAME_L));
        productionbalance.setField(DATE_L, values.get(DATE_L));
        productionbalance.setField(WORKER_L, values.get(WORKER_L));
        productionbalance.setField("recordsNumber", values.get("recordsnumber"));
        productionbalance.setField(DESCRIPTION_L, values.get(DESCRIPTION_L));
        productionbalance.setField(FILE_NAME_L, values.get("filename"));

        productionbalance = productionbalance.getDataDefinition().save(productionbalance);
        validateEntity(productionbalance);

    }

    void addQualityControl(final Map<String, String> values) {

        Entity qualitycontrol = dataDefinitionService.get(SamplesConstants.QUALITYCONTROL_PLUGIN_IDENTIFIER,
                SamplesConstants.QUALITYCONTROL_MODEL_QUALITYCONTROL).create();

        if ("qualityControlsForUnit".equals(values.get(QUALITYCONTROLTYPE_3))) {
            qualitycontrol.setField(NUMBER_L, values.get(NUMBER_L));
            qualitycontrol.setField(ORDER_L, getOrderByNumber(values.get(ORDER_L)));
            qualitycontrol.setField(COMMENT_L, values.get(COMMENT_L));
            qualitycontrol.setField(CLOSED_L, values.get(CLOSED_L));
            qualitycontrol.setField("controlledQuantity", values.get("controlledquantity"));
            qualitycontrol.setField("takenForControlQuantity", values.get("takenforcontrolquantity"));
            qualitycontrol.setField("rejectedQuantity", values.get("rejectedquantity"));
            qualitycontrol.setField("acceptedDefectsQuantity", values.get("accepteddefectsquantity"));
            qualitycontrol.setField(STAFF_L, values.get(STAFF_L));
            qualitycontrol.setField(DATE_L, values.get(DATE_L));
            qualitycontrol.setField("controlInstruction", values.get("controlinstruction"));
            qualitycontrol.setField(QUALITY_CONTROL_TYPE2_L, values.get(QUALITYCONTROLTYPE_3));

        } else if ("qualityControlsForOrder".equals(values.get(QUALITYCONTROLTYPE_3))) {
            qualitycontrol.setField(NUMBER_L, values.get(NUMBER_L));
            qualitycontrol.setField(ORDER_L, getOrderByNumber(values.get(ORDER_L)));
            qualitycontrol.setField("ControlResult", values.get("controlresult"));
            qualitycontrol.setField(COMMENT_L, values.get(COMMENT_L));
            qualitycontrol.setField(CLOSED_L, values.get(CLOSED_L));
            qualitycontrol.setField("controlInstruction", values.get("controlinstruction"));
            qualitycontrol.setField(STAFF_L, values.get(STAFF_L));
            qualitycontrol.setField(DATE_L, values.get(DATE_L));
            qualitycontrol.setField(QUALITY_CONTROL_TYPE2_L, values.get(QUALITYCONTROLTYPE_3));

        } else if (QUALITY_CONTROLS_FOR_OPERATION_L.equals(values.get(QUALITYCONTROLTYPE_3))) {
            qualitycontrol.setField(NUMBER_L, values.get(NUMBER_L));
            qualitycontrol.setField(ORDER_L, getOrderByNumber(values.get(ORDER_L)));
            qualitycontrol.setField(NAME_L, values.get(NAME_L));
            qualitycontrol.setField(OPERATION_L, getOperationByNumber(values.get(OPERATION_L)));
            qualitycontrol.setField("ControlResult", values.get("controlresult"));
            qualitycontrol.setField(COMMENT_L, values.get(COMMENT_L));
            qualitycontrol.setField(CLOSED_L, values.get(CLOSED_L));
            qualitycontrol.setField(STAFF_L, values.get(STAFF_L));
            qualitycontrol.setField(DATE_L, values.get(DATE_L));
            qualitycontrol.setField(QUALITY_CONTROL_TYPE2_L, values.get(QUALITYCONTROLTYPE_3));

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
                .add(SearchRestrictions.eq(NUMBER_L, id)).list().getEntities();
        if (machines.isEmpty()) {
            return null;
        }
        return machines.get(0);
    }

    private Entity getTechnologyByNumber(final String number) {
        return dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, TECHNOLOGY_MODEL_TECHNOLOGY).find()
                .add(SearchRestrictions.eq(NUMBER_L, number)).setMaxResults(1).uniqueResult();
    }

    private Entity getDefaultTechnologyForProduct(final Entity product) {
        if (product == null) {
            return null;
        }
        List<Entity> technologies = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, TECHNOLOGY_MODEL_TECHNOLOGY).find()
                .add(SearchRestrictions.belongsTo(BASIC_MODEL_PRODUCT, product)).add(SearchRestrictions.eq("master", true))
                .setMaxResults(1).list().getEntities();
        if (technologies.size() > 0) {
            return technologies.get(0);
        } else {
            return null;
        }
    }

    private Entity getOrderOperationComponentByNumber(final String number, final Entity order) {
        Entity operation = dataDefinitionService
                .get(SamplesConstants.TECHNOLOGIES_PLUGIN_IDENTIFIER, SamplesConstants.TECHNOLOGY_MODEL_OPERATION).find()
                .add(SearchRestrictions.eq(NUMBER_L, number)).setMaxResults(1).uniqueResult();
        return dataDefinitionService
                .get(SamplesConstants.PRODUCTION_SCHEDULING_PLUGIN_IDENTIFIER,
                        SamplesConstants.PRODUCTION_SCHEDULING_MODEL_PRODUCTION_SCHEDULING).find()
                .add(SearchRestrictions.belongsTo(ORDER_L, order)).add(SearchRestrictions.belongsTo(OPERATION_L, operation))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getShiftByName(final String name) {
        return dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, SamplesConstants.BASIC_MODEL_SHIFT).find()
                .add(SearchRestrictions.eq(NAME_L, name)).setMaxResults(1).uniqueResult();
    }

    private Entity getOrderByNumber(String number) {
        return dataDefinitionService.get(ORDERS_PLUGIN_IDENTIFIER, ORDERS_MODEL_ORDER).find()
                .add(SearchRestrictions.eq(NUMBER_L, number)).setMaxResults(1).uniqueResult();
    }

    private Entity getProductByNumber(final String number) {
        return dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_PRODUCT).find()
                .add(SearchRestrictions.eq(NUMBER_L, number)).setMaxResults(1).uniqueResult();
    }

    private Entity getProductionRecordByNumber(final String number) {
        return dataDefinitionService
                .get(SamplesConstants.PRODUCTION_COUNTING_PLUGIN_IDENTIFIER,
                        SamplesConstants.PRODUCTION_RECORD_MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq(NUMBER_L, number)).setMaxResults(1).uniqueResult();
    }

    private Entity getOperationByNumber(final String number) {
        return dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, TECHNOLOGY_MODEL_OPERATION).find()
                .add(SearchRestrictions.eq(NUMBER_L, number)).setMaxResults(1).uniqueResult();
    }

    private Entity getStaffByNumber(String number) {
        return dataDefinitionService.get("basic", STAFF_L).find().add(SearchRestrictions.eq(NUMBER_L, number)).setMaxResults(1)
                .uniqueResult();
    }

    private Entity getWorkstationTypeByNumber(String number) {
        return dataDefinitionService.get("basic", "workstationType").find().add(SearchRestrictions.eq(NUMBER_L, number))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getDivisionByNumber(String number) {
        return dataDefinitionService.get("basic", DIVISION_L).find().add(SearchRestrictions.eq(NUMBER_L, number))
                .setMaxResults(1).uniqueResult();
    }

    private Entity GetLoggingsByNumber(Long id3) {
        return dataDefinitionService.get(PRODUCTION_COUNTING_L, "productionRecordLogging").get(id3);
    }

    private Entity getTransformationByNumber(String number) {
        return dataDefinitionService.get(MATERIAL_FLOW_L, TRANSFORMATIONS_L).find().add(SearchRestrictions.eq(NUMBER_L, number))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getStockAreaByNumber(String number) {
        return dataDefinitionService.get(MATERIAL_FLOW_L, STOCK_AREAS_L).find().add(SearchRestrictions.eq(NUMBER_L, number))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getManufacturerByNumber(String number) {
        return dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, "company").find().add(SearchRestrictions.eq(NUMBER_L, number))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getBatchByNumber(String number) {
        return dataDefinitionService.get(ADVANCED_GENEALOGY_L, BATCH_L).find().add(SearchRestrictions.eq(NUMBER_L, number))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getRandomProduct() {
        Long total = (long) dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_PRODUCT).find().list()
                .getTotalNumberOfEntities();
        return dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_PRODUCT).find()
                .setFirstResult(RANDOM.nextInt(total.intValue())).setMaxResults(1).list().getEntities().get(0);
    }

    private void buildTrackingRecord(final Entity trackingRecord) {
        Entity genProdIn = addGenealogyProductInComponent(trackingRecord, "000011", "5");
        addUsedBatch(genProdIn, "321DEW");
        genProdIn = addGenealogyProductInComponent(trackingRecord, "000012", "5");
        addUsedBatch(genProdIn, "706FCV");
        genProdIn = addGenealogyProductInComponent(trackingRecord, "000014", "5");
        addUsedBatch(genProdIn, "980DEN");
        addUsedBatch(genProdIn, "767BMM");
        genProdIn = addGenealogyProductInComponent(trackingRecord, "000013", "5");
        addUsedBatch(genProdIn, "876DEW");
        addUsedBatch(genProdIn, "444VWM");
        genProdIn = addGenealogyProductInComponent(trackingRecord, "000015", "1");
        addUsedBatch(genProdIn, "349SWA");
        genProdIn = addGenealogyProductInComponent(trackingRecord, "000016", "2");
        addUsedBatch(genProdIn, "150DEB");
    }

    private void addUsedBatch(final Entity genealogyProductInComponent, final String batchNumber) {
        Entity genealogyProductInBatch = dataDefinitionService.get(ADVANCED_GENEALOGY_FOR_ORDERS_L, "genealogyProductInBatch")
                .create();

        genealogyProductInBatch.setField(BATCH_L, getBatchByNumber(batchNumber));
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
                .find().add(SearchRestrictions.belongsTo(PRODUCT_L, product)).setMaxResults(1).uniqueResult();
        Entity orderOperationComponent = dataDefinitionService
                .get("productionScheduling", "orderOperationComponent")
                .find()
                .add(SearchRestrictions.and(SearchRestrictions.belongsTo(ORDERS_MODEL_ORDER, order),
                        SearchRestrictions.belongsTo(TECHNOLOGY_MODEL_TECHNOLOGY, technology),
                        SearchRestrictions.belongsTo(OPERATION_L, getOperationByNumber(operationNumber)))).setMaxResults(1)
                .uniqueResult();

        Entity genealogyProductInComponent = dataDefinitionService
                .get(ADVANCED_GENEALOGY_FOR_ORDERS_L, "genealogyProductInComponent")
                .find()
                .add(SearchRestrictions.and(SearchRestrictions.belongsTo("trackingRecord", trackingRecord),
                        SearchRestrictions.belongsTo("productInComponent", operationProdInComp),
                        SearchRestrictions.belongsTo("orderOperationComponent", orderOperationComponent))).setMaxResults(1)
                .uniqueResult();
        return genealogyProductInComponent;
    }

}
