/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.1
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
import static com.qcadoo.mes.samples.constants.SamplesConstants.BOM_ID;
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
import static com.qcadoo.mes.samples.constants.SamplesConstants.WORK_PLANS_MODEL_WORK_PLAN;
import static com.qcadoo.mes.samples.constants.SamplesConstants.WORK_PLANS_PLUGIN_IDENTIFIER;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        if (isEnabled(TECHNOLOGIES_PLUGIN_IDENTIFIER)) {
            readDataFromXML(dataset, "operations", locale);
            readDataFromXML(dataset, TECHNOLOGIES_PLUGIN_IDENTIFIER, locale);
        }
        if (isEnabled(ORDERS_PLUGIN_IDENTIFIER)) {
            readDataFromXML(dataset, ORDERS_PLUGIN_IDENTIFIER, locale);
        }
        if (isEnabled("materialRequirements")) {
            addMaterialRequirements();
        }
        if (isEnabled(WORK_PLANS_PLUGIN_IDENTIFIER)) {
            addWorkPlans();
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
        }
    }

    private void addWorkstationType(final Map<String, String> values) {
        Entity machine = dataDefinitionService.get(SamplesConstants.BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_WORKSTATION_TYPE)
                .create();

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
            product.setField("typeOfMaterial", values.get("typeofproduct"));
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
                    + product.getField(FIELD_NUMBER) + ", typeOfMaterial=" + product.getField("typeOfMaterial") + ", unit="
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
        substituteComponent.setField(FIELD_QUANTITY, new BigDecimal(quantity).setScale(3, RoundingMode.HALF_EVEN));
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

    private void addOrder(final Map<String, String> values) {
        long startDate = System.currentTimeMillis() + MILLIS_IN_DAY * (RANDOM.nextInt(50) - 25);

        if (!values.get("scheduled_start_date").isEmpty()) {
            try {
                startDate = FORMATTER.parse(values.get("scheduled_start_date")).getTime();
            } catch (ParseException e) {
                LOG.warn(e.getMessage(), e);
            }
        }

        long endDate = startDate + (MILLIS_IN_DAY * RANDOM.nextInt(50));

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
        order.setField(ORDER_STATE, "01pending");

        Entity product = getProductByNumber(values.get(PRODUCT_NUMBER));

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
            technology.setField("batchRequired", true);
            technology.setField("postFeatureRequired", false);
            technology.setField("otherFeatureRequired", false);
            technology.setField("shiftFeatureRequired", false);
            technology.setField("technologyBatchRequired", false);

            if (!values.get("minimal").isEmpty()) {
                technology.setField("minimalQuantity", values.get("minimal"));
            }
            if (!values.get("algorithm").isEmpty()) {
                technology.setField("componentQuantityAlgorithm", values.get("algorithm"));
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
                if ("9".equals(values.get(BOM_ID))) {
                    addTechnologyOperationComponentsForTable(technology);
                } else if ("10".equals(values.get(BOM_ID))) {
                    addTechnologyOperationComponentsForTableAdvanced(technology);
                }
            } else if (PROD_NR_17.equals(values.get(PRODUCT_NUMBER))) {
                if ("11".equals(values.get(BOM_ID))) {
                    addTechnologyOperationComponentsForTabouret(technology);
                } else if ("12".equals(values.get(BOM_ID))) {
                    addTechnologyOperationComponentsForTabouretAdvanced(technology);
                }
            } else if (PROD_NR_25.equals(values.get(PRODUCT_NUMBER))) {
                if ("13".equals(values.get(BOM_ID))) {
                    addTechnologyOperationComponentsForStool(technology);
                } else if ("14".equals(values.get(BOM_ID))) {
                    addTechnologyOperationComponentsForStoolAdvanced(technology);
                }
            }

            treeNumberingService.generateNumbersAndUpdateTree(
                    dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "technologyOperationComponent"),
                    TECHNOLOGY_MODEL_TECHNOLOGY, technology.getId());
        }
    }

    private void addTechnologyOperationComponentsForTable(final Entity technology) {
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
        addProductInComponent(parent, new BigDecimal("0.25"), getProductByNumber(PROD_NR_16));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_15));
    }

    private void addTechnologyOperationComponentsForTableAdvanced(final Entity technology) {
        Entity parent = addOperationComponent(technology, null, getOperationByNumber("5"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_14));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_13));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_12));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_11));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_10));
        parent = addOperationComponent(technology, parent, getOperationByNumber("1"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_11));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_15));
        parent = addOperationComponent(technology, parent, getOperationByNumber("2"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_16));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_15));
    }

    private void addTechnologyOperationComponentsForStool(final Entity technology) {
        Entity parent = addOperationComponent(technology, null, getOperationByNumber("5"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_27));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_26));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_25));
        parent = addOperationComponent(technology, parent, getOperationByNumber("6"));
        addProductInComponent(parent, new BigDecimal("8"), getProductByNumber(PROD_NR_21));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_30));
        addProductInComponent(parent, new BigDecimal("16"), getProductByNumber(PROD_NR_29));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_28));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_26));
        Entity parent1 = addOperationComponent(technology, parent, getOperationByNumber("4"));
        addProductInComponent(parent1, new BigDecimal("16"), getProductByNumber(PROD_NR_33));
        addProductOutComponent(parent1, new BigDecimal("16"), getProductByNumber(PROD_NR_29));
        parent = addOperationComponent(technology, parent, getOperationByNumber("1"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_31));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_28));
        parent = addOperationComponent(technology, parent, getOperationByNumber("2"));
        addProductInComponent(parent, new BigDecimal("0.125"), getProductByNumber(PROD_NR_32));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_31));
    }

    private void addTechnologyOperationComponentsForStoolAdvanced(final Entity technology) {
        Entity parent = addOperationComponent(technology, null, getOperationByNumber("5"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_27));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_26));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_25));
        parent = addOperationComponent(technology, parent, getOperationByNumber("6"));
        addProductInComponent(parent, new BigDecimal("8"), getProductByNumber(PROD_NR_21));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_30));
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
        addProductOutComponent(parent, new BigDecimal("32"), getProductByNumber(PROD_NR_31));
    }

    private void addTechnologyOperationComponentsForTabouret(final Entity technology) {
        Entity parent = addOperationComponent(technology, null, getOperationByNumber("5"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_18));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_19));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_17));
        parent = addOperationComponent(technology, parent, getOperationByNumber("6"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_21));
        addProductInComponent(parent, new BigDecimal("16"), getProductByNumber(PROD_NR_20));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_22));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_19));
        parent = addOperationComponent(technology, parent, getOperationByNumber("1"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_23));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_22));
        parent = addOperationComponent(technology, parent, getOperationByNumber("2"));
        addProductInComponent(parent, new BigDecimal("0.125"), getProductByNumber(PROD_NR_24));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_23));
    }

    private void addTechnologyOperationComponentsForTabouretAdvanced(final Entity technology) {
        Entity parent = addOperationComponent(technology, null, getOperationByNumber("5"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_18));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_19));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_17));
        parent = addOperationComponent(technology, parent, getOperationByNumber("6"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_21));
        addProductInComponent(parent, new BigDecimal("16"), getProductByNumber(PROD_NR_20));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_22));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber(PROD_NR_19));
        parent = addOperationComponent(technology, parent, getOperationByNumber("1"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_23));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_22));
        parent = addOperationComponent(technology, parent, getOperationByNumber("2"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber(PROD_NR_24));
        addProductOutComponent(parent, new BigDecimal("32"), getProductByNumber(PROD_NR_23));
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

    private void addMaterialRequirements() {
        for (int i = 0; i < 50; i++) {
            addMaterialRequirement();
        }
    }

    private void addMaterialRequirement() {
        Entity requirement = dataDefinitionService.get("materialRequirements", "materialRequirement").create();
        requirement.setField(FIELD_NAME, getRandomProduct().getField(FIELD_NAME));
        requirement.setField(FIELD_GENERATED, false);
        requirement.setField(FIELD_DATE, null);
        requirement.setField("onlyComponents", RANDOM.nextBoolean());
        requirement.setField(FIELD_WORKER, null);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test material requirement {name=" + requirement.getField(FIELD_NAME) + ", date="
                    + requirement.getField(FIELD_DATE) + ", worker=" + requirement.getField(FIELD_WORKER) + ", onlyComponents="
                    + requirement.getField("onlyComponents") + ", generated=" + requirement.getField(FIELD_GENERATED) + "}");
        }

        requirement.setField(ORDERS_PLUGIN_IDENTIFIER, Lists.newArrayList(getRandomOrder(), getRandomOrder(), getRandomOrder()));

        requirement = dataDefinitionService.get("materialRequirements", "materialRequirement").save(requirement);
        validateEntity(requirement);
    }

    private void addWorkPlans() {
        for (int i = 0; i < 50; i++) {
            addWorkPlan();
        }
    }

    private void addWorkPlan() {
        Entity workPlan = dataDefinitionService.get(WORK_PLANS_PLUGIN_IDENTIFIER, WORK_PLANS_MODEL_WORK_PLAN).create();
        workPlan.setField(FIELD_NAME, getRandomProduct().getField(FIELD_NAME));
        workPlan.setField(FIELD_GENERATED, false);
        workPlan.setField(FIELD_DATE, null);
        workPlan.setField(FIELD_WORKER, null);
        workPlan.setField("type", "01noDistinction");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test material requirement {name=" + workPlan.getField(FIELD_NAME) + ", date="
                    + workPlan.getField(FIELD_DATE) + ", worker=" + workPlan.getField(FIELD_WORKER) + ", generated="
                    + workPlan.getField(FIELD_GENERATED) + "}");
        }

        workPlan.setField("orders", Lists.newArrayList(getRandomOrder()));

        workPlan = workPlan.getDataDefinition().save(workPlan);
        validateEntity(workPlan);
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

    private Entity getProductByNumber(final String number) {
        return dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_PRODUCT).find()
                .add(SearchRestrictions.eq(FIELD_NUMBER, number)).setMaxResults(1).list().getEntities().get(0);
    }

    private Entity getOperationByNumber(final String number) {
        return dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, TECHNOLOGY_MODEL_OPERATION).find()
                .add(SearchRestrictions.eq(FIELD_NUMBER, number)).setMaxResults(1).list().getEntities().get(0);
    }

    private Entity getRandomProduct() {
        Long total = (long) dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_PRODUCT).find().list()
                .getTotalNumberOfEntities();
        return dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_PRODUCT).find()
                .setFirstResult(RANDOM.nextInt(total.intValue())).setMaxResults(1).list().getEntities().get(0);
    }

    private Entity getRandomOrder() {
        Long total = (long) dataDefinitionService.get(ORDERS_PLUGIN_IDENTIFIER, ORDERS_MODEL_ORDER).find().list()
                .getTotalNumberOfEntities();
        return dataDefinitionService.get(ORDERS_PLUGIN_IDENTIFIER, ORDERS_MODEL_ORDER).find()
                .setFirstResult(RANDOM.nextInt(total.intValue())).setMaxResults(1).list().getEntities().get(0);
    }

}
