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

import static com.qcadoo.mes.samples.constants.SamplesConstants.BASIC_MODEL_PRODUCT;
import static com.qcadoo.mes.samples.constants.SamplesConstants.BASIC_MODEL_STAFF;
import static com.qcadoo.mes.samples.constants.SamplesConstants.BASIC_MODEL_SUBSTITUTE;
import static com.qcadoo.mes.samples.constants.SamplesConstants.BASIC_MODEL_WORKSTATION_TYPE;
import static com.qcadoo.mes.samples.constants.SamplesConstants.BASIC_PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_DATE;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_DESCRIPTION;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_EAN;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_GENERATED;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_NAME;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_NUMBER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_OPERATION_COMPONENT;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_QUANTITY;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_SURNAME;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_TPZ;
import static com.qcadoo.mes.samples.constants.SamplesConstants.FIELD_WORKER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.ORDERS_MODEL_ORDER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.ORDERS_PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.ORDER_STATE;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PRODUCTS_PLUGIN_IDENTIFIER;
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
import static com.qcadoo.mes.samples.constants.SamplesConstants.TECHNOLOGIES_PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.TECHNOLOGY_MODEL_OPERATION;
import static com.qcadoo.mes.samples.constants.SamplesConstants.TECHNOLOGY_MODEL_TECHNOLOGY;

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
            if (isEnabled("orderGroups")) {
                readDataFromXML(dataset, "orderGroups", locale);
            }
        }

        if (isEnabled("costCalculation")) {
            readDataFromXML(dataset, "costCalculation", locale);
        }
        if (isEnabled("materialFlow")) {
            readDataFromXML(dataset, "stockAreas", locale);
            readDataFromXML(dataset, "transformations", locale);
            readDataFromXML(dataset, "transfer", locale);
            readDataFromXML(dataset, "stockCorrection", locale);
        }

        if (isEnabled("qualityControls")) {
            readDataFromXML(dataset, "qualityControls", locale);
        }

        if (isEnabled("materialRequirements")) {
            readDataFromXML(dataset, "materialRequirements", locale);
        }

        if (isEnabled("workPlans")) {
            readDataFromXML(dataset, "workPlans", locale);
        }

        if (isEnabled("productionCounting")) {

            readDataFromXML(dataset, "productionRecord", locale);
            readDataFromXML(dataset, "productionCounting", locale);
            readDataFromXML(dataset, "productionBalance", locale);

        }
        if (isEnabled("advancedGenealogy")) {
            readDataFromXML(dataset, "batches", locale);
            if (isEnabled("advancedGenealogyForOrders")) {
                readDataFromXML(dataset, "trackingRecords", locale);
            }
            readDataFromXML(dataset, "genealogyTables", locale);
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
        } else if ("division".equals(type)) {
            addDivision(values);
        } else if ("orderGroups".equals(type)) {
            addOrderGroup(values);
        } else if ("costCalculation".equals(type)) {
            addCostCalculation(values);
        } else if ("stockAreas".equals(type)) {
            addStokckArea(values);
        } else if ("transformations".equals(type)) {
            addTransformation(values);
        } else if ("transfer".equals(type)) {
            addTransfer(values);
        } else if ("stockCorrection".equals(type)) {
            addStockCorrection(values);
        } else if ("batches".equals(type)) {
            addBatches(values);
        } else if ("trackingRecords".equals(type)) {
            addTrackingRecord(values);
        } else if ("genealogyTables".equals(type)) {
            addGenealogyTables(values);
        } else if ("qualityControls".equals(type)) {
            addQualityControl(values);
        } else if ("materialRequirements".equals(type)) {
            addMaterialRequirements(values);
        } else if ("workPlans".equals(type)) {
            addWorkPlan(values);
        } else if ("productionRecord".equals(type)) {
            addProductionRecord(values);
        } else if ("productionCounting".equals(type)) {
            addProductionCounting(values);
        } else if ("productionBalance".equals(type)) {
            addProductionBalance(values);
        }
    }

    private void addWorkstationType(final Map<String, String> values) {
        Entity machine = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_WORKSTATION_TYPE).create();

        LOG.debug("id: " + values.get("id") + " name " + values.get(FIELD_NAME) + " prod_line " + values.get("prod_line")
                + " description " + values.get(FIELD_DESCRIPTION));
        machine.setField(FIELD_NUMBER, values.get("id"));
        machine.setField(FIELD_NAME, values.get(FIELD_NAME));
        machine.setField(FIELD_DESCRIPTION, values.get(FIELD_DESCRIPTION));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test machine item {machine=" + machine.getField(FIELD_NAME) + ", " + FIELD_NUMBER + "="
                    + machine.getField(FIELD_NUMBER) + "}");
        }

        machine = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_WORKSTATION_TYPE).save(machine);

        validateEntity(machine);
    }

    private void addStaff(final Map<String, String> values) {
        Entity staff = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_STAFF).create();

        LOG.debug("id: " + values.get("id") + " name " + values.get(FIELD_NAME) + " " + FIELD_SURNAME + " "
                + values.get(FIELD_SURNAME) + " post " + values.get("post"));
        staff.setField(FIELD_NUMBER, values.get("id"));
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
        operation.setField(FIELD_NUMBER, values.get(FIELD_NUMBER));
        operation.setField(FIELD_TPZ, values.get(FIELD_TPZ));
        operation.setField("tj", values.get("tj"));
        operation.setField("productionInOneCycle", values.get("productioninonecycle"));
        operation.setField("countRealized", values.get("countRealized"));
        operation.setField("machineUtilization", values.get("machineutilization"));
        operation.setField("laborUtilization", values.get("laborutilization"));
        operation.setField("countMachineOperation", values.get("countmachine"));
        operation.setField("countRealizedOperation", "01all");
        operation.setField("timeNextOperation", values.get("timenextoperation"));
        operation.setField(BASIC_MODEL_WORKSTATION_TYPE, getMachine(values.get(FIELD_NUMBER)));
        operation.setField(BASIC_MODEL_STAFF, getRandomStaff());

        if (isEnabled("costNormsForOperation")) {
            operation.setField("pieceworkCost", values.get("pieceworkcost"));
            operation.setField("machineHourlyCost", values.get("machinehourlycost"));
            operation.setField("laborHourlyCost", values.get("laborhourlycost"));
            operation.setField("numberOfOperations", values.get("numberofoperations"));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test operation item {name=" + operation.getField(FIELD_NAME) + ", " + FIELD_NUMBER + "="
                    + operation.getField(FIELD_NUMBER) + "}");
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
        if (!values.get("batch").isEmpty()) {
            product.setField("batch", values.get("batch"));
        }
        if (!values.get(PRODUCT_NUMBER).isEmpty()) {
            product.setField(FIELD_NUMBER, values.get(PRODUCT_NUMBER));
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
                    + product.getField(FIELD_EAN) + ", name=" + product.getField(FIELD_NAME) + ", " + FIELD_NUMBER + "="
                    + product.getField(FIELD_NUMBER) + ", globalTypeOfMaterial=" + product.getField("typeOfMaterial") + ", unit="
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
        substitute.setField(FIELD_NUMBER, number);
        substitute.setField("priority", priority);
        substitute.setField(BASIC_MODEL_PRODUCT, product);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test substitute {name=" + substitute.getField(FIELD_NAME) + ", " + FIELD_NUMBER + "="
                    + substitute.getField(FIELD_NUMBER) + ", priority=" + substitute.getField("priority") + ", product="
                    + ((Entity) substitute.getField(BASIC_MODEL_PRODUCT)).getField(FIELD_NUMBER) + "}");
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
                    + ((Entity) substituteComponent.getField(BASIC_MODEL_SUBSTITUTE)).getField(FIELD_NUMBER) + ", product="
                    + ((Entity) substituteComponent.getField(BASIC_MODEL_PRODUCT)).getField(FIELD_NUMBER) + ", quantity="
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
        Entity orderGroup = dataDefinitionService.get("orderGroups", "orderGroup").create();
        orderGroup.setField(FIELD_NUMBER, values.get(FIELD_NUMBER));
        orderGroup.setField(FIELD_NAME, values.get(FIELD_NAME));

        Entity order3 = getOrderByNumber("000003");
        Entity order2 = getOrderByNumber("000002");

        orderGroup.setField("dateTo", order3.getField("dateTo"));
        orderGroup.setField("dateFrom", order2.getField("dateFrom"));

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

        if ("000001".equals(values.get("order_nr"))) {
            endDate = startDate + MILLIS_IN_DAY + 1 * millsInHour + 45 * millsInMinute;
        } else if ("000002".equals(values.get("order_nr"))) {
            startDate -= 2 * MILLIS_IN_DAY;
            endDate = startDate + MILLIS_IN_DAY + 3 * millsInHour + 40 * millsInMinute;
        } else if ("000003".equals(values.get("order_nr"))) {
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
        order.setField("dateFrom", new Date(startDate));
        order.setField("dateTo", new Date(endDate));
        order.setField("externalSynchronized", true);

        order.setField(TECHNOLOGY_MODEL_TECHNOLOGY, getTechnologyByNumber(values.get("tech_nr")));
        order.setField(FIELD_NAME, (values.get(FIELD_NAME).isEmpty() || values.get(FIELD_NAME) == null) ? values.get("order_nr")
                : values.get(FIELD_NAME));
        order.setField(FIELD_NUMBER, values.get("order_nr"));
        order.setField("plannedQuantity", values.get("quantity_scheduled").isEmpty() ? new BigDecimal(
                100 * RANDOM.nextDouble() + 1) : new BigDecimal(values.get("quantity_scheduled")));

        order.setField("trackingRecordTreatment", "01duringProduction");
        order.setField(ORDER_STATE, values.get("status"));

        Entity product = getProductByNumber(values.get(PRODUCT_NUMBER));

        if (isEnabled("productionCounting")) {
            order.setField("typeOfProductionRecording", values.get("type_of_production_recording"));
            order.setField("registerQuantityInProduct", values.get("register_quantity_in_product"));
            order.setField("registerQuantityOutProduct", values.get("register_quantity_out_product"));
            order.setField("registerProductionTime", values.get("register_production_time"));
            order.setField("justOne", values.get("just_one"));
            order.setField("allowToClose", values.get("allow_to_close"));
            order.setField("autoCloseOrder", values.get("auto_close_order"));
        }

        if (isEnabled("advancedGenealogyForOrders")) {
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
                    + FIELD_NUMBER
                    + "="
                    + order.getField(FIELD_NUMBER)
                    + ", product="
                    + (order.getField(BASIC_MODEL_PRODUCT) == null ? null : ((Entity) order.getField(BASIC_MODEL_PRODUCT))
                            .getField(FIELD_NUMBER))
                    + ", technology="
                    + (order.getField(TECHNOLOGY_MODEL_TECHNOLOGY) == null ? null : ((Entity) order
                            .getField(TECHNOLOGY_MODEL_TECHNOLOGY)).getField(FIELD_NUMBER)) + ", dateFrom="
                    + order.getField("dateFrom") + ", dateTo=" + order.getField("dateTo") + ", effectiveDateFrom="
                    + order.getField("effectiveDateFrom") + ", effectiveDateTo=" + order.getField("effectiveDateTo")
                    + ", doneQuantity=" + order.getField("doneQuantity") + ", plannedQuantity="
                    + order.getField("plannedQuantity") + ", trackingRecordTreatment="
                    + order.getField("trackingRecordTreatment") + ", state=" + order.getField(ORDER_STATE) + "}");
        }

        order = dataDefinitionService.get(ORDERS_PLUGIN_IDENTIFIER, ORDERS_MODEL_ORDER).save(order);
        validateEntity(order);

    }

    private void addBatches(final Map<String, String> values) {
        Entity batch = dataDefinitionService.get("advancedGenealogy", "batch").create();

        batch.setField(FIELD_NUMBER, values.get(FIELD_NUMBER));
        batch.setField("product", getProductByNumber(values.get("product_nr")));
        batch.setField("manufacturer", getManufacturerByNumber(values.get("manufacturer_nr")));
        batch.setField("state", "01tracked");

        batch = batch.getDataDefinition().save(batch);
        validateEntity(batch);
    }

    private void addTrackingRecord(final Map<String, String> values) {
        Entity trackingRecord = dataDefinitionService.get("advancedGenealogy", "trackingRecord").create();

        trackingRecord.setField("entityType", values.get("entity_type"));
        trackingRecord.setField("number", values.get("number"));
        trackingRecord.setField("producedBatch", getBatchByNumber(values.get("produced_batch_no")));
        trackingRecord.setField("order", getOrderByNumber(values.get("order_no")));
        trackingRecord.setField("state", "01draft");

        trackingRecord = trackingRecord.getDataDefinition().save(trackingRecord);
        validateEntity(trackingRecord);

        buildTrackingRecord(trackingRecord);
    }

    private void addGenealogyTables(final Map<String, String> values) {
        Entity genealogyTable = dataDefinitionService.get("advancedGenealogy", "genealogyReport").create();

        genealogyTable.setField("type", values.get("type"));
        genealogyTable.setField(FIELD_NAME, values.get(FIELD_NAME));
        genealogyTable.setField("includeDraft", values.get("include_draft"));
        genealogyTable.setField("directRelatedOnly", values.get("direct_related_only"));
        genealogyTable.setField("batch", getBatchByNumber(values.get("batch_no")));

        genealogyTable = genealogyTable.getDataDefinition().save(genealogyTable);
        validateEntity(genealogyTable);
    }

    private void addDivision(final Map<String, String> values) {
        Entity division = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, "division").create();

        division.setField("number", values.get("NUMBER"));
        division.setField("name", values.get("NAME"));
        division.setField("supervisor", values.get("SUPERVISOR"));

        division = division.getDataDefinition().save(division);
        validateEntity(division);
    }

    private void addCostCalculation(final Map<String, String> values) {
        Entity costCalculation = dataDefinitionService.get("costCalculation", "costCalculation").create();

        costCalculation.setField(FIELD_NUMBER, values.get(FIELD_NUMBER));
        costCalculation.setField(FIELD_DESCRIPTION, values.get(FIELD_DESCRIPTION));
        costCalculation.setField(ORDERS_MODEL_ORDER, getOrderByNumber(values.get("order_no")));
        costCalculation.setField(TECHNOLOGY_MODEL_TECHNOLOGY, getTechnologyByNumber(values.get("tech_no")));
        costCalculation.setField("defaultTechnology", getTechnologyByNumber(values.get("def_tech_no")));
        costCalculation.setField("product", getProductByNumber(values.get("prod_no")));
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
        Entity stockArea = dataDefinitionService.get("materialFlow", "stockAreas").create();

        stockArea.setField(FIELD_NUMBER, values.get(FIELD_NUMBER));
        stockArea.setField(FIELD_NAME, values.get(FIELD_NAME));

        stockArea = stockArea.getDataDefinition().save(stockArea);
        validateEntity(stockArea);
    }

    private void addTransformation(Map<String, String> values) {
        Entity transformation = dataDefinitionService.get("materialFlow", "transformations").create();

        transformation.setField(FIELD_NUMBER, values.get(FIELD_NUMBER));
        transformation.setField(FIELD_NAME, values.get(FIELD_NAME));
        transformation.setField("time", values.get("time"));
        transformation.setField("stockAreasFrom", getStockAreaByNumber(values.get("stock_areas_from")));
        transformation.setField("stockAreasTo", getStockAreaByNumber(values.get("stock_areas_to")));
        transformation.setField("staff", getStaffByNumber(values.get("staff")));

        transformation = transformation.getDataDefinition().save(transformation);
        validateEntity(transformation);
    }

    private void addStockCorrection(Map<String, String> values) {
        Entity stockCorrection = dataDefinitionService.get("materialFlow", "stockCorrection").create();

        stockCorrection.setField(FIELD_NUMBER, values.get(FIELD_NUMBER));
        stockCorrection.setField("stockCorrectionDate", values.get("stock_correction_date"));
        stockCorrection.setField("stockAreas", getStockAreaByNumber(values.get("stock_areas")));
        stockCorrection.setField("product", getProductByNumber(values.get("product")));
        stockCorrection.setField("staff", getStaffByNumber(values.get("staff")));
        stockCorrection.setField("found", values.get("found"));

        stockCorrection = stockCorrection.getDataDefinition().save(stockCorrection);
        validateEntity(stockCorrection);
    }

    private void addTransfer(Map<String, String> values) {
        Entity transfer = dataDefinitionService.get("materialFlow", "transfer").create();

        transfer.setField(FIELD_NUMBER, values.get(FIELD_NUMBER));
        transfer.setField("type", values.get("type"));
        transfer.setField("product", getProductByNumber(values.get("product")));
        transfer.setField(FIELD_QUANTITY, values.get(FIELD_QUANTITY));
        transfer.setField("staff", getStaffByNumber(values.get("staff")));
        transfer.setField("stockAreasFrom", getStockAreaByNumber(values.get("stock_areas_from")));
        transfer.setField("stockAreasTo", getStockAreaByNumber(values.get("stock_areas_to")));
        transfer.setField("time", values.get("time"));

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
            technology.setField(FIELD_NUMBER, values.get("bom_nr"));
            technology.setField(BASIC_MODEL_PRODUCT, product);
            technology.setField(ORDER_STATE, "01draft");
            technology.setField("description", values.get("DESCRIPTION"));
            technology.setField("batchRequired", true);
            technology.setField("postFeatureRequired", false);
            technology.setField("otherFeatureRequired", false);
            technology.setField("shiftFeatureRequired", false);
            technology.setField("technologyBatchRequired", false);

            if (isEnabled("qualityControlsForOperation")
                    && "qualityControlsForOperation".equals(values.get("qualitycontroltype"))) {
                technology.setField("qualityControlType", "qualityControlsForOperation");
            }

            if (!(isEnabled("qualityControlsForOperation") && "04forOperation".equals(values.get("quality_control_type")))
                    && isEnabled("qualityControls")
                    && ("02forUnit".equals(values.get("quality_control_type")) || "03forOrder".equals(values
                            .get("quality_control_type")))) {
                technology.setField("qualityControlType", values.get("quality_control_type"));
                if ("02forUnit".equals(values.get("quality_control_type"))) {
                    technology.setField("unitSamplingNr", values.get("unit_sampling_nr"));
                }
            }

            if (!values.get("minimal").isEmpty()) {
                technology.setField("minimalQuantity", values.get("minimal"));
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Add test technology {id=" + technology.getId() + ", name=" + technology.getField(FIELD_NAME) + ", "
                        + FIELD_NUMBER + "=" + technology.getField(FIELD_NUMBER) + ", product="
                        + ((Entity) technology.getField(BASIC_MODEL_PRODUCT)).getField(FIELD_NUMBER) + ", description="
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
        productInComponent.setField("product", product);
        productInComponent.setField("usedQuantity", usedQuantity);
        productInComponent.setField("plannedQuantity", plannedQuantity);
        productInComponent.setField("balance", balance);

        return productInComponent;
    }

    private Entity addRecordOperationProductOutComponent(Entity product, BigDecimal usedQuantity, BigDecimal plannedQuantity,
            BigDecimal balance) {
        Entity productOutComponent = dataDefinitionService.get(SamplesConstants.PRODUCTION_COUNTING_PLUGIN_IDENTIFIER,
                SamplesConstants.RECORDOPERATIONPRODUCTOUTCOMPONENT_MODEL_RECORDOPERATIONPRODUCTOUTCOMPONENT).create();
        productOutComponent.setField("product", product);
        productOutComponent.setField("usedQuantity", usedQuantity);
        productOutComponent.setField("plannedQuantity", plannedQuantity);
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
                    + ((Entity) component.getField(TECHNOLOGY_MODEL_TECHNOLOGY)).getField(FIELD_NUMBER) + ", parent="
                    + (parent == null ? 0 : parent.getId()) + ", operation="
                    + ((Entity) component.getField(TECHNOLOGY_MODEL_OPERATION)).getField(FIELD_NUMBER) + "}");
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
                    + ((Entity) productComponent.getField(BASIC_MODEL_PRODUCT)).getField(FIELD_NUMBER)
                    + ", operation="
                    + ((Entity) ((Entity) productComponent.getField(SamplesConstants.FIELD_OPERATION_COMPONENT))
                            .getField(TECHNOLOGY_MODEL_OPERATION)).getField(FIELD_NUMBER) + ", quantity="
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
                    + ((Entity) productComponent.getField(BASIC_MODEL_PRODUCT)).getField(FIELD_NUMBER)
                    + ", operation="
                    + ((Entity) ((Entity) productComponent.getField(FIELD_OPERATION_COMPONENT))
                            .getField(TECHNOLOGY_MODEL_OPERATION)).getField(FIELD_NUMBER) + ", quantity="
                    + productComponent.getField(FIELD_QUANTITY) + "}");
        }
    }

    private void addMaterialRequirements(final Map<String, String> values) {
        Entity requirement = dataDefinitionService.get(SamplesConstants.MATERIALREQUIREMENTS_PLUGIN_IDENTIFIER,
                SamplesConstants.MATERIALREQUIREMENTS_MODEL_MATERIALREQUIREMENTS).create();
        requirement.setField("name", values.get("name"));
        requirement.setField("date", values.get("date"));
        requirement.setField("worker", values.get("worker"));
        requirement.setField("onlyComponents", values.get("onlycomponents"));
        requirement.setField("date", values.get("date"));
        requirement.setField("generated", values.get("generated"));
        requirement.setField("fileName", values.get("filename"));
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
        workPlan.setField(FIELD_NAME, values.get("name"));
        workPlan.setField(FIELD_GENERATED, values.get("generated"));
        workPlan.setField(FIELD_DATE, values.get("date"));
        workPlan.setField(FIELD_WORKER, values.get("worker"));
        workPlan.setField("type", values.get("type"));
        workPlan.setField("fileName", values.get("filename"));
        workPlan.setField("orders", Lists.newArrayList(getOrderByNumber(values.get("order"))));

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
        productionRecord.setField("number", getProductionRecordByNumber("number"));
        productionRecord.setField("name", values.get("name"));
        productionRecord.setField("order", getOrderByNumber(values.get("order")));
        productionRecord.setField("orderOperationComponent",
                getOrderOperationComponentByNumber(values.get("orderoperationcomponent"), getOrderByNumber(values.get("order"))));
        productionRecord.setField("shift", getShiftByName(values.get("shift")));
        productionRecord.setField("state", values.get("state"));
        productionRecord.setField("lastRecord", values.get("lastrecord"));
        productionRecord.setField("machineTime", values.get("machinetime"));
        productionRecord.setField("machineTimeBalance", values.get("machinetimebalance"));
        productionRecord.setField("laborTime", values.get("labortime"));
        productionRecord.setField("laborTimeBalance", values.get("labortimebalance"));
        productionRecord.setField("plannedTime", values.get("plannedtime"));
        productionRecord.setField("plannedLaborTime", values.get("plannedlabortime"));
        productionRecord.setField("staff", getStaffByNumber(values.get("staff")));
        productionRecord.setField("workstationType", getWorkstationTypeByNumber(values.get("workstationtype")));
        productionRecord.setField("division", getDivisionByNumber(values.get("division")));

        String idString3 = values.get("loggings");
        Long id3 = Long.valueOf(idString3);
        Entity loggings = GetLoggingsByNumber(id3);
        List<Entity> loggings1 = Lists.newArrayList(loggings);
        productionRecord.setField("loggings", loggings1);

        List<Entity> recOpProdInComponents = Lists.newArrayList(addRecordOperationProductInComponent(
                getProductByNumber("000018"), new BigDecimal("150"), new BigDecimal("152"), new BigDecimal("2")));
        recOpProdInComponents.add(addRecordOperationProductInComponent(getProductByNumber("000019"), new BigDecimal("600"),
                new BigDecimal("600"), BigDecimal.ZERO));
        recOpProdInComponents.add(addRecordOperationProductInComponent(getProductByNumber("000020"), new BigDecimal("2400"),
                new BigDecimal("2400"), BigDecimal.ZERO));
        recOpProdInComponents.add(addRecordOperationProductInComponent(getProductByNumber("000021"), new BigDecimal("2400"),
                new BigDecimal("2400"), BigDecimal.ZERO));
        recOpProdInComponents.add(addRecordOperationProductInComponent(getProductByNumber("000022"), new BigDecimal("600"),
                new BigDecimal("600"), BigDecimal.ZERO));
        recOpProdInComponents.add(addRecordOperationProductInComponent(getProductByNumber("000023"), new BigDecimal("600"),
                new BigDecimal("600"), BigDecimal.ZERO));
        recOpProdInComponents.add(addRecordOperationProductInComponent(getProductByNumber("000024"), new BigDecimal("150"),
                new BigDecimal("182"), new BigDecimal("32")));

        productionRecord.setField("recordOperationProductInComponents", recOpProdInComponents);

        List<Entity> recOpProdOutComponents1 = Lists.newArrayList(addRecordOperationProductOutComponent(
                getProductByNumber("000017"), new BigDecimal("150"), new BigDecimal("150"), BigDecimal.ZERO));

        recOpProdOutComponents1.add(addRecordOperationProductOutComponent(getProductByNumber("000019"), new BigDecimal("600"),
                new BigDecimal("600"), BigDecimal.ZERO));
        recOpProdOutComponents1.add(addRecordOperationProductOutComponent(getProductByNumber("000022"), new BigDecimal("600"),
                new BigDecimal("600"), BigDecimal.ZERO));
        recOpProdOutComponents1.add(addRecordOperationProductOutComponent(getProductByNumber("000023"), new BigDecimal("600"),
                new BigDecimal("600"), BigDecimal.ZERO));

        productionRecord.setField("recordOperationProductOutComponents", recOpProdOutComponents1);

        productionRecord = productionRecord.getDataDefinition().save(productionRecord);
        validateEntity(productionRecord);
    }

    void addProductionCounting(final Map<String, String> values) {
        Entity productionCounting = dataDefinitionService.get(SamplesConstants.PRODUCTION_COUNTING_PLUGIN_IDENTIFIER,
                SamplesConstants.PRODUCTION_COUNTING_MODEL_PRODUCTION_COUNTING).create();
        productionCounting.setField(FIELD_GENERATED, values.get("generated"));
        productionCounting.setField("order", getOrderByNumber(values.get("order")));
        productionCounting.setField("product", getProductByNumber(values.get("product")));
        productionCounting.setField(FIELD_NAME, values.get("name"));
        productionCounting.setField(FIELD_DATE, values.get("date"));
        productionCounting.setField(FIELD_WORKER, values.get("worker"));
        productionCounting.setField("description", values.get("description"));
        productionCounting.setField("fileName", values.get("filename"));

        productionCounting = dataDefinitionService.get(SamplesConstants.PRODUCTION_COUNTING_PLUGIN_IDENTIFIER,
                SamplesConstants.PRODUCTION_COUNTING_MODEL_PRODUCTION_COUNTING).save(productionCounting);
        validateEntity(productionCounting);
    }

    void addProductionBalance(final Map<String, String> values) {
        Entity productionbalance = dataDefinitionService.get(SamplesConstants.PRODUCTION_COUNTING_PLUGIN_IDENTIFIER,
                SamplesConstants.PRODUCTIONBALANCE_MODEL_PRODUCTIONBALANCE).create();
        productionbalance.setField("generated", values.get("generated"));
        productionbalance.setField("order", getOrderByNumber(values.get("order")));
        productionbalance.setField("product", getProductByNumber(values.get("product")));
        productionbalance.setField("name", values.get("name"));
        productionbalance.setField("date", values.get("date"));
        productionbalance.setField("worker", values.get("worker"));
        productionbalance.setField("recordsNumber", values.get("recordsnumber"));
        productionbalance.setField("description", values.get("description"));
        productionbalance.setField("fileName", values.get("filename"));

        productionbalance = productionbalance.getDataDefinition().save(productionbalance);
        validateEntity(productionbalance);

    }

    void addQualityControl(final Map<String, String> values) {

        Entity qualitycontrol = dataDefinitionService.get(SamplesConstants.QUALITYCONTROL_PLUGIN_IDENTIFIER,
                SamplesConstants.QUALITYCONTROL_MODEL_QUALITYCONTROL).create();

        if ("qualityControlsForUnit".equals(values.get("qualitycontroltype"))) {
            qualitycontrol.setField("number", values.get("number"));
            qualitycontrol.setField("order", getOrderByNumber(values.get("order")));
            qualitycontrol.setField("comment", values.get("comment"));
            qualitycontrol.setField("closed", values.get("closed"));
            qualitycontrol.setField("controlledQuantity", values.get("controlledquantity"));
            qualitycontrol.setField("takenForControlQuantity", values.get("takenforcontrolquantity"));
            qualitycontrol.setField("rejectedQuantity", values.get("rejectedquantity"));
            qualitycontrol.setField("acceptedDefectsQuantity", values.get("accepteddefectsquantity"));
            qualitycontrol.setField("staff", values.get("staff"));
            qualitycontrol.setField("date", values.get("date"));
            qualitycontrol.setField("controlInstruction", values.get("controlinstruction"));
            qualitycontrol.setField("qualityControlType", values.get("qualitycontroltype"));

        } else if ("qualityControlsForOrder".equals(values.get("qualitycontroltype"))) {
            qualitycontrol.setField("number", values.get("number"));
            qualitycontrol.setField("order", getOrderByNumber(values.get("order")));
            qualitycontrol.setField("ControlResult", values.get("controlresult"));
            qualitycontrol.setField("comment", values.get("comment"));
            qualitycontrol.setField("closed", values.get("closed"));
            qualitycontrol.setField("controlInstruction", values.get("controlinstruction"));
            qualitycontrol.setField("staff", values.get("staff"));
            qualitycontrol.setField("date", values.get("date"));
            qualitycontrol.setField("qualityControlType", values.get("qualitycontroltype"));

        } else if ("qualityControlsForOperation".equals(values.get("qualitycontroltype"))) {
            qualitycontrol.setField("number", values.get("number"));
            qualitycontrol.setField("order", getOrderByNumber(values.get("order")));
            qualitycontrol.setField("name", values.get("name"));
            qualitycontrol.setField("operation", getOperationByNumber(values.get("operation")));
            qualitycontrol.setField("ControlResult", values.get("controlresult"));
            qualitycontrol.setField("comment", values.get("comment"));
            qualitycontrol.setField("closed", values.get("closed"));
            qualitycontrol.setField("staff", values.get("staff"));
            qualitycontrol.setField("date", values.get("date"));
            qualitycontrol.setField("qualityControlType", values.get("qualitycontroltype"));

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
                .add(SearchRestrictions.eq(FIELD_NUMBER, id)).list().getEntities();
        if (machines.isEmpty()) {
            return null;
        }
        return machines.get(0);
    }

    private Entity getTechnologyByNumber(final String number) {
        return dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, TECHNOLOGY_MODEL_TECHNOLOGY).find()
                .add(SearchRestrictions.eq(FIELD_NUMBER, number)).setMaxResults(1).uniqueResult();
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
                .add(SearchRestrictions.eq("number", number)).setMaxResults(1).uniqueResult();
        return dataDefinitionService
                .get(SamplesConstants.PRODUCTION_SCHEDULING_PLUGIN_IDENTIFIER,
                        SamplesConstants.PRODUCTION_SCHEDULING_MODEL_PRODUCTION_SCHEDULING).find()
                .add(SearchRestrictions.belongsTo("order", order)).add(SearchRestrictions.belongsTo("operation", operation))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getShiftByName(final String name) {
        return dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, SamplesConstants.BASIC_MODEL_SHIFT).find()
                .add(SearchRestrictions.eq("name", name)).setMaxResults(1).uniqueResult();
    }

    private Entity getOrderByNumber(String number) {
        return dataDefinitionService.get(ORDERS_PLUGIN_IDENTIFIER, ORDERS_MODEL_ORDER).find()
                .add(SearchRestrictions.eq("number", number)).setMaxResults(1).uniqueResult();
    }

    private Entity getProductByNumber(final String number) {
        return dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_PRODUCT).find()
                .add(SearchRestrictions.eq(FIELD_NUMBER, number)).setMaxResults(1).uniqueResult();
    }

    private Entity getProductionRecordByNumber(final String number) {
        return dataDefinitionService
                .get(SamplesConstants.PRODUCTION_COUNTING_PLUGIN_IDENTIFIER,
                        SamplesConstants.PRODUCTION_RECORD_MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq(FIELD_NUMBER, number)).setMaxResults(1).uniqueResult();
    }

    private Entity getOperationByNumber(final String number) {
        return dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, TECHNOLOGY_MODEL_OPERATION).find()
                .add(SearchRestrictions.eq(FIELD_NUMBER, number)).setMaxResults(1).uniqueResult();
    }

    private Entity getStaffByNumber(String number) {
        return dataDefinitionService.get("basic", "staff").find().add(SearchRestrictions.eq(FIELD_NUMBER, number))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getWorkstationTypeByNumber(String number) {
        return dataDefinitionService.get("basic", "workstationType").find().add(SearchRestrictions.eq(FIELD_NUMBER, number))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getDivisionByNumber(String number) {
        return dataDefinitionService.get("basic", "division").find().add(SearchRestrictions.eq(FIELD_NUMBER, number))
                .setMaxResults(1).uniqueResult();
    }

    private Entity GetLoggingsByNumber(Long id3) {
        return dataDefinitionService.get("productionCounting", "productionRecordLogging").get(id3);
    }

    private Entity getTransformationByNumber(String number) {
        return dataDefinitionService.get("materialFlow", "transformations").find()
                .add(SearchRestrictions.eq(FIELD_NUMBER, number)).setMaxResults(1).uniqueResult();
    }

    private Entity getStockAreaByNumber(String number) {
        return dataDefinitionService.get("materialFlow", "stockAreas").find().add(SearchRestrictions.eq(FIELD_NUMBER, number))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getManufacturerByNumber(String number) {
        return dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, "company").find()
                .add(SearchRestrictions.eq(FIELD_NUMBER, number)).setMaxResults(1).uniqueResult();
    }

    private Entity getBatchByNumber(String number) {
        return dataDefinitionService.get("advancedGenealogy", "batch").find().add(SearchRestrictions.eq(FIELD_NUMBER, number))
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
        Entity genealogyProductInBatch = dataDefinitionService.get("advancedGenealogyForOrders", "genealogyProductInBatch")
                .create();

        genealogyProductInBatch.setField("batch", getBatchByNumber(batchNumber));
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
                .find().add(SearchRestrictions.belongsTo("product", product)).setMaxResults(1).uniqueResult();
        Entity orderOperationComponent = dataDefinitionService
                .get("productionScheduling", "orderOperationComponent")
                .find()
                .add(SearchRestrictions.and(SearchRestrictions.belongsTo(ORDERS_MODEL_ORDER, order),
                        SearchRestrictions.belongsTo(TECHNOLOGY_MODEL_TECHNOLOGY, technology),
                        SearchRestrictions.belongsTo("operation", getOperationByNumber(operationNumber)))).setMaxResults(1)
                .uniqueResult();

        Entity genealogyProductInComponent = dataDefinitionService
                .get("advancedGenealogyForOrders", "genealogyProductInComponent")
                .find()
                .add(SearchRestrictions.and(SearchRestrictions.belongsTo("trackingRecord", trackingRecord),
                        SearchRestrictions.belongsTo("productInComponent", operationProdInComp),
                        SearchRestrictions.belongsTo("orderOperationComponent", orderOperationComponent))).setMaxResults(1)
                .uniqueResult();
        return genealogyProductInComponent;
    }

}
