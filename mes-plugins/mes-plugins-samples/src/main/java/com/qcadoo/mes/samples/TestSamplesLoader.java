/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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

        if (!setAsDemoEnviroment) {
            readDataFromXML(dataset, "users", locale);
        } else {
            changeAdminPassword();
        }

        readDataFromXML(dataset, "dictionaries", locale);
        readDataFromXML(dataset, "activeCurrency", locale);
        readDataFromXML(dataset, "company", locale);
        readDataFromXML(dataset, "machines", locale);
        readDataFromXML(dataset, "staff", locale);
        readDataFromXML(dataset, "products", locale);
        readDataFromXML(dataset, "shifts", locale);
        if (isEnabled("technologies")) {
            readDataFromXML(dataset, "operations", locale);
            readDataFromXML(dataset, "technologies", locale);
        }
        if (isEnabled("orders")) {
            readDataFromXML(dataset, "orders", locale);
        }
        if (isEnabled("materialRequirements")) {
            addMaterialRequirements();
        }
        if (isEnabled("workPlans")) {
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
        } else if ("products".equals(type)) {
            addProduct(values);
        } else if ("orders".equals(type)) {
            prepareTechnologiesForOrder(values);
            addOrder(values);
        } else if ("technologies".equals(type)) {
            addTechnology(values);
        } else if ("dictionaries".equals(type)) {
            addDictionaryItems(values);
        } else if ("users".equals(type)) {
            addUser(values);
        } else if ("operations".equals(type)) {
            addOperations(values);
        } else if ("staff".equals(type)) {
            addStaff(values);
        } else if ("machines".equals(type)) {
            addMachine(values);
        } else if ("shifts".equals(type)) {
            addShifts(values);
        }
    }

    private void addMachine(final Map<String, String> values) {
        Entity machine = dataDefinitionService.get("basic", "machine").create();

        LOG.debug("id: " + values.get("id") + " name " + values.get("name") + " prod_line " + values.get("prod_line")
                + " description " + values.get("description"));
        machine.setField("number", values.get("id"));
        machine.setField("name", values.get("name"));
        machine.setField("description", values.get("description"));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test machine item {machine=" + machine.getField("name") + ", number=" + machine.getField("number")
                    + "}");
        }

        machine = dataDefinitionService.get("basic", "machine").save(machine);

        validateEntity(machine);
    }

    private void addStaff(final Map<String, String> values) {
        Entity staff = dataDefinitionService.get("basic", "staff").create();

        LOG.debug("id: " + values.get("id") + " name " + values.get("name") + " surname " + values.get("surname") + " post "
                + values.get("post"));
        staff.setField("number", values.get("id"));
        staff.setField("name", values.get("name"));
        staff.setField("surname", values.get("surname"));
        staff.setField("post", values.get("post"));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test staff item {staff=" + staff.getField("name") + ", surName=" + staff.getField("surname") + "}");
        }

        staff = dataDefinitionService.get("basic", "staff").save(staff);
        validateEntity(staff);
    }

    private void addOperations(final Map<String, String> values) {
        Entity operation = dataDefinitionService.get("technologies", "operation").create();

        operation.setField("name", values.get("name"));
        operation.setField("number", values.get("number"));
        operation.setField("tpz", values.get("tpz"));
        operation.setField("tj", values.get("tj"));
        operation.setField("productionInOneCycle", values.get("productioninonecycle"));
        operation.setField("countRealized", values.get("countRealized"));
        operation.setField("machineUtilization", values.get("machineutilization"));
        operation.setField("laborUtilization", values.get("laborutilization"));
        operation.setField("countMachineOperation", values.get("countmachine"));
        operation.setField("countRealizedOperation", "01all");
        operation.setField("timeNextOperation", values.get("timenextoperation"));
        operation.setField("machine", getMachine(values.get("number")));
        operation.setField("staff", getRandomStaff());

        if (isEnabled("costNormsForOperation")) {
            operation.setField("pieceworkCost", values.get("pieceworkcost"));
            operation.setField("machineHourlyCost", values.get("machinehourlycost"));
            operation.setField("laborHourlyCost", values.get("laborhourlycost"));
            operation.setField("numberOfOperations", values.get("numberofoperations"));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test operation item {name=" + operation.getField("name") + ", number=" + operation.getField("number")
                    + "}");
        }

        operation = dataDefinitionService.get("technologies", "operation").save(operation);
        validateEntity(operation);
    }

    private void addProduct(final Map<String, String> values) {
        Entity product = dataDefinitionService.get("basic", "product").create();
        product.setField("category", getRandomDictionaryItem("categories"));
        if (!values.get("ean").isEmpty()) {
            product.setField("ean", values.get("ean"));
        }
        if (!values.get("name").isEmpty()) {
            product.setField("name", values.get("name"));
        }
        if (!values.get("batch").isEmpty()) {
            product.setField("batch", values.get("batch"));
        }
        if (!values.get("product_nr").isEmpty()) {
            product.setField("number", values.get("product_nr"));
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

        product = dataDefinitionService.get("basic", "product").save(product);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test product {id=" + product.getId() + ", category=" + product.getField("category") + ", ean="
                    + product.getField("ean") + ", name=" + product.getField("name") + ", number=" + product.getField("number")
                    + ", typeOfMaterial=" + product.getField("typeOfMaterial") + ", unit=" + product.getField("unit") + "}");
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < RANDOM.nextInt(5); i++) {
            for (int j = 0; j <= i; j++) {
                stringBuilder.append("#");
            }
            addSubstitute(values.get("name") + stringBuilder.toString(), values.get("product_nr") + stringBuilder.toString(),
                    product, i + 1);
        }
    }

    private void addSubstitute(final String name, final String number, final Entity product, final int priority) {
        Entity substitute = dataDefinitionService.get("basic", "substitute").create();
        substitute.setField("name", name);
        substitute.setField("number", number);
        substitute.setField("priority", priority);
        substitute.setField("product", product);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test substitute {name=" + substitute.getField("name") + ", number=" + substitute.getField("number")
                    + ", priority=" + substitute.getField("priority") + ", product="
                    + ((Entity) substitute.getField("product")).getField("number") + "}");
        }

        substitute = dataDefinitionService.get("basic", "substitute").save(substitute);
        validateEntity(substitute);

        for (int i = 0; i < 1; i++) {
            addSubstituteComponent(substitute, getRandomProduct(), 100 * RANDOM.nextDouble());
        }
    }

    private void addSubstituteComponent(final Entity substitute, final Entity product, final double quantity) {
        Entity substituteComponent = dataDefinitionService.get("basic", "substituteComponent").create();
        substituteComponent.setField("product", product);
        substituteComponent.setField("quantity", new BigDecimal(quantity).setScale(3, RoundingMode.HALF_EVEN));
        substituteComponent.setField("substitute", substitute);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test substitute component {substitute="
                    + ((Entity) substituteComponent.getField("substitute")).getField("number") + ", product="
                    + ((Entity) substituteComponent.getField("product")).getField("number") + ", quantity="
                    + substituteComponent.getField("quantity") + "}");
        }

        substituteComponent = dataDefinitionService.get("basic", "substituteComponent").save(substituteComponent);
        validateEntity(substituteComponent);
    }

    private void prepareTechnologiesForOrder(final Map<String, String> values) {
        Entity technology = getTechnologyByNumber(values.get("tech_nr"));
        technology.setField("state", "02accepted");
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

        Entity order = dataDefinitionService.get("orders", "order").create();
        order.setField("dateFrom", new Date(startDate));
        order.setField("dateTo", new Date(endDate));
        order.setField("externalSynchronized", true);

        order.setField("technology", getTechnologyByNumber(values.get("tech_nr")));
        order.setField("name",
                (values.get("name").isEmpty() || values.get("name") == null) ? values.get("order_nr") : values.get("name"));
        order.setField("number", values.get("order_nr"));
        order.setField("plannedQuantity", values.get("quantity_scheduled").isEmpty() ? new BigDecimal(
                100 * RANDOM.nextDouble() + 1) : new BigDecimal(values.get("quantity_scheduled")));

        order.setField("state", "01pending");

        Entity product = getProductByNumber(values.get("product_nr"));

        order.setField("product", product);
        if (order.getField("technology") == null) {
            order.setField("technology", getDefaultTechnologyForProduct(product));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test order {id=" + order.getId() + ", name=" + order.getField("name") + ", number="
                    + order.getField("number") + ", product="
                    + (order.getField("product") != null ? ((Entity) order.getField("product")).getField("number") : null)
                    + ", technology="
                    + (order.getField("technology") != null ? ((Entity) order.getField("technology")).getField("number") : null)
                    + ", dateFrom=" + order.getField("dateFrom") + ", dateTo=" + order.getField("dateTo")
                    + ", effectiveDateFrom=" + order.getField("effectiveDateFrom") + ", effectiveDateTo="
                    + order.getField("effectiveDateTo") + ", doneQuantity=" + order.getField("doneQuantity")
                    + ", plannedQuantity=" + order.getField("plannedQuantity") + ", state=" + order.getField("state") + "}");
        }

        order = dataDefinitionService.get("orders", "order").save(order);
        validateEntity(order);
    }

    private void addTechnology(final Map<String, String> values) {
        Entity product = getProductByNumber(values.get("product_nr"));

        if (product != null) {
            Entity defaultTechnology = getDefaultTechnologyForProduct(product);

            Entity technology = dataDefinitionService.get("technologies", "technology").create();
            if (!values.get("description").isEmpty()) {
                technology.setField("description", values.get("description"));
            }
            technology.setField("master", defaultTechnology == null);
            technology.setField("name", values.get("name"));
            technology.setField("number", values.get("bom_nr"));
            technology.setField("product", product);
            technology.setField("state", "01draft");
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
                LOG.debug("Add test technology {id=" + technology.getId() + ", name=" + technology.getField("name") + ", number="
                        + technology.getField("number") + ", product="
                        + ((Entity) technology.getField("product")).getField("number") + ", description="
                        + technology.getField("description") + ", master=" + technology.getField("master") + "}");
            }

            technology = dataDefinitionService.get("technologies", "technology").save(technology);
            validateEntity(technology);

            if ("000010".equals(values.get("product_nr"))) {
                if ("9".equals(values.get("bom_id"))) {
                    addTechnologyOperationComponentsForTable(technology);
                } else if ("10".equals(values.get("bom_id"))) {
                    addTechnologyOperationComponentsForTableAdvanced(technology);
                }
            } else if ("000017".equals(values.get("product_nr"))) {
                if ("11".equals(values.get("bom_id"))) {
                    addTechnologyOperationComponentsForTabouret(technology);
                } else if ("12".equals(values.get("bom_id"))) {
                    addTechnologyOperationComponentsForTabouretAdvanced(technology);
                }
            } else if ("000025".equals(values.get("product_nr"))) {
                if ("13".equals(values.get("bom_id"))) {
                    addTechnologyOperationComponentsForStool(technology);
                } else if ("14".equals(values.get("bom_id"))) {
                    addTechnologyOperationComponentsForStoolAdvanced(technology);
                }
            }

            treeNumberingService.generateNumbersAndUpdateTree(
                    dataDefinitionService.get("technologies", "technologyOperationComponent"), "technology", technology.getId());
        }
    }

    private void addTechnologyOperationComponentsForTable(final Entity technology) {
        Entity parent = addOperationComponent(technology, null, getOperationByNumber("5"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber("000014"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber("000013"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber("000012"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber("000011"));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber("000010"));
        parent = addOperationComponent(technology, parent, getOperationByNumber("1"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber("000011"));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber("000015"));
        parent = addOperationComponent(technology, parent, getOperationByNumber("2"));
        addProductInComponent(parent, new BigDecimal("0.25"), getProductByNumber("000016"));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber("000015"));
    }

    private void addTechnologyOperationComponentsForTableAdvanced(final Entity technology) {
        Entity parent = addOperationComponent(technology, null, getOperationByNumber("5"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber("000014"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber("000013"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber("000012"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber("000011"));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber("000010"));
        parent = addOperationComponent(technology, parent, getOperationByNumber("1"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber("000011"));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber("000015"));
        parent = addOperationComponent(technology, parent, getOperationByNumber("2"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber("000016"));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber("000015"));
    }

    private void addTechnologyOperationComponentsForStool(final Entity technology) {
        Entity parent = addOperationComponent(technology, null, getOperationByNumber("5"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber("000027"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber("000026"));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber("000025"));
        parent = addOperationComponent(technology, parent, getOperationByNumber("6"));
        addProductInComponent(parent, new BigDecimal("8"), getProductByNumber("000021"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber("000030"));
        addProductInComponent(parent, new BigDecimal("16"), getProductByNumber("000029"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber("000028"));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber("000026"));
        Entity parent1 = addOperationComponent(technology, parent, getOperationByNumber("4"));
        addProductInComponent(parent1, new BigDecimal("16"), getProductByNumber("000033"));
        addProductOutComponent(parent1, new BigDecimal("16"), getProductByNumber("000029"));
        parent = addOperationComponent(technology, parent, getOperationByNumber("1"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber("000031"));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber("000028"));
        parent = addOperationComponent(technology, parent, getOperationByNumber("2"));
        addProductInComponent(parent, new BigDecimal("0.125"), getProductByNumber("000032"));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber("000031"));
    }

    private void addTechnologyOperationComponentsForStoolAdvanced(final Entity technology) {
        Entity parent = addOperationComponent(technology, null, getOperationByNumber("5"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber("000027"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber("000026"));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber("000025"));
        parent = addOperationComponent(technology, parent, getOperationByNumber("6"));
        addProductInComponent(parent, new BigDecimal("8"), getProductByNumber("000021"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber("000030"));
        addProductInComponent(parent, new BigDecimal("16"), getProductByNumber("000029"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber("000028"));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber("000026"));
        Entity parent1 = addOperationComponent(technology, parent, getOperationByNumber("4"));
        addProductInComponent(parent1, BigDecimal.ONE, getProductByNumber("000033"));
        addProductOutComponent(parent1, BigDecimal.ONE, getProductByNumber("000029"));
        parent = addOperationComponent(technology, parent, getOperationByNumber("1"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber("000031"));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber("000028"));
        parent = addOperationComponent(technology, parent, getOperationByNumber("2"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber("000032"));
        addProductOutComponent(parent, new BigDecimal("32"), getProductByNumber("000031"));
    }

    private void addTechnologyOperationComponentsForTabouret(final Entity technology) {
        Entity parent = addOperationComponent(technology, null, getOperationByNumber("5"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber("000018"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber("000019"));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber("000017"));
        parent = addOperationComponent(technology, parent, getOperationByNumber("6"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber("000021"));
        addProductInComponent(parent, new BigDecimal("16"), getProductByNumber("000020"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber("000022"));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber("000019"));
        parent = addOperationComponent(technology, parent, getOperationByNumber("1"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber("000023"));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber("000022"));
        parent = addOperationComponent(technology, parent, getOperationByNumber("2"));
        addProductInComponent(parent, new BigDecimal("0.125"), getProductByNumber("000024"));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber("000023"));
    }

    private void addTechnologyOperationComponentsForTabouretAdvanced(final Entity technology) {
        Entity parent = addOperationComponent(technology, null, getOperationByNumber("5"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber("000018"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber("000019"));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber("000017"));
        parent = addOperationComponent(technology, parent, getOperationByNumber("6"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber("000021"));
        addProductInComponent(parent, new BigDecimal("16"), getProductByNumber("000020"));
        addProductInComponent(parent, new BigDecimal("4"), getProductByNumber("000022"));
        addProductOutComponent(parent, new BigDecimal("4"), getProductByNumber("000019"));
        parent = addOperationComponent(technology, parent, getOperationByNumber("1"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber("000023"));
        addProductOutComponent(parent, BigDecimal.ONE, getProductByNumber("000022"));
        parent = addOperationComponent(technology, parent, getOperationByNumber("2"));
        addProductInComponent(parent, BigDecimal.ONE, getProductByNumber("000024"));
        addProductOutComponent(parent, new BigDecimal("32"), getProductByNumber("000023"));
    }

    private Entity addOperationComponent(final Entity technology, final Entity parent, final Entity operation) {
        Entity component = dataDefinitionService.get("technologies", "technologyOperationComponent").create();
        component.setField("technology", technology);
        component.setField("parent", parent);
        component.setField("operation", operation);
        component.setField("entityType", "operation");
        component.setField("tpz", operation.getField("tpz"));
        component.setField("tj", operation.getField("tj"));
        component.setField("machineUtilization", operation.getField("machineUtilization"));
        component.setField("laborUtilization", operation.getField("laborUtilization"));
        component.setField("productionInOneCycle", operation.getField("productionInOneCycle"));
        component.setField("countRealized", operation.getField("countRealizedOperation"));
        component.setField("countMachine", operation.getField("countMachineOperation"));
        component.setField("timeNextOperation", operation.getField("timeNextOperation"));

        component = dataDefinitionService.get("technologies", "technologyOperationComponent").save(component);
        validateEntity(component);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test operation component {technology="
                    + ((Entity) component.getField("technology")).getField("number") + ", parent="
                    + (parent == null ? 0 : parent.getId()) + ", operation="
                    + ((Entity) component.getField("operation")).getField("number") + "}");
        }
        return component;
    }

    private void addProductInComponent(final Entity component, final BigDecimal quantity, final Entity product) {
        Entity productComponent = dataDefinitionService.get("technologies", "operationProductInComponent").create();
        productComponent.setField("operationComponent", component);
        productComponent.setField("quantity", quantity);
        productComponent.setField("product", product);
        productComponent.setField("batchRequired", true);

        productComponent = dataDefinitionService.get("technologies", "operationProductInComponent").save(productComponent);
        validateEntity(productComponent);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test product component {product="
                    + ((Entity) productComponent.getField("product")).getField("number")
                    + ", operation="
                    + ((Entity) ((Entity) productComponent.getField("operationComponent")).getField("operation"))
                            .getField("number") + ", quantity=" + productComponent.getField("quantity") + "}");
        }
    }

    private void addProductOutComponent(final Entity component, final BigDecimal quantity, final Entity product) {
        Entity productComponent = dataDefinitionService.get("technologies", "operationProductOutComponent").create();
        productComponent.setField("operationComponent", component);
        productComponent.setField("quantity", quantity);
        productComponent.setField("product", product);

        productComponent = dataDefinitionService.get("technologies", "operationProductOutComponent").save(productComponent);
        validateEntity(productComponent);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test product component {product="
                    + ((Entity) productComponent.getField("product")).getField("number")
                    + ", operation="
                    + ((Entity) ((Entity) productComponent.getField("operationComponent")).getField("operation"))
                            .getField("number") + ", quantity=" + productComponent.getField("quantity") + "}");
        }
    }

    private void addMaterialRequirements() {
        for (int i = 0; i < 50; i++) {
            addMaterialRequirement();
        }
    }

    private void addMaterialRequirement() {
        Entity requirement = dataDefinitionService.get("materialRequirements", "materialRequirement").create();
        requirement.setField("name", getRandomProduct().getField("name"));
        requirement.setField("generated", false);
        requirement.setField("date", null);
        requirement.setField("onlyComponents", RANDOM.nextBoolean());
        requirement.setField("worker", null);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test material requirement {name=" + requirement.getField("name") + ", date="
                    + requirement.getField("date") + ", worker=" + requirement.getField("worker") + ", onlyComponents="
                    + requirement.getField("onlyComponents") + ", generated=" + requirement.getField("generated") + "}");
        }

        requirement.setField("orders", Lists.newArrayList(getRandomOrder(), getRandomOrder(), getRandomOrder()));

        requirement = dataDefinitionService.get("materialRequirements", "materialRequirement").save(requirement);
        validateEntity(requirement);
    }

    private void addWorkPlans() {
        for (int i = 0; i < 50; i++) {
            addWorkPlan();
        }
    }

    private void addWorkPlan() {
        Entity workPlan = dataDefinitionService.get("workPlans", "workPlan").create();
        workPlan.setField("name", getRandomProduct().getField("name"));
        workPlan.setField("generated", false);
        workPlan.setField("date", null);
        workPlan.setField("worker", null);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test material requirement {name=" + workPlan.getField("name") + ", date=" + workPlan.getField("date")
                    + ", worker=" + workPlan.getField("worker") + ", generated=" + workPlan.getField("generated") + "}");
        }

        workPlan = dataDefinitionService.get("workPlans", "workPlan").save(workPlan);
        validateEntity(workPlan);

        for (int i = 0; i < 1; i++) {
            Entity component = dataDefinitionService.get("workPlans", "workPlanComponent").create();
            component.setField("workPlan", workPlan);
            component.setField("order", getRandomOrder());

            if (LOG.isDebugEnabled()) {
                LOG.debug("Add test work plan component {workPlan=" + ((Entity) component.getField("workPlan")).getField("name")
                        + ", order=" + ((Entity) component.getField("order")).getField("number") + "}");
            }

            component = dataDefinitionService.get("workPlans", "workPlanComponent").save(component);
            validateEntity(component);
        }
    }

    private Entity getRandomStaff() {
        Long total = (long) dataDefinitionService.get("basic", "staff").find().list().getTotalNumberOfEntities();
        return dataDefinitionService.get("basic", "staff").find().setFirstResult(RANDOM.nextInt(total.intValue()))
                .setMaxResults(1).list().getEntities().get(0);
    }

    private Entity getMachine(final String id) {
        List<Entity> machines = dataDefinitionService.get("basic", "machine").find().add(SearchRestrictions.eq("number", id))
                .list().getEntities();
        if (machines.isEmpty()) {
            return null;
        }
        return machines.get(0);
    }

    private Entity getTechnologyByNumber(final String number) {
        return dataDefinitionService.get("technologies", "technology").find().add(SearchRestrictions.eq("number", number))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getDefaultTechnologyForProduct(final Entity product) {
        if (product == null) {
            return null;
        }
        List<Entity> technologies = dataDefinitionService.get("technologies", "technology").find()
                .add(SearchRestrictions.belongsTo("product", product)).add(SearchRestrictions.eq("master", true))
                .setMaxResults(1).list().getEntities();
        if (technologies.size() > 0) {
            return technologies.get(0);
        } else {
            return null;
        }
    }

    private Entity getProductByNumber(final String number) {
        return dataDefinitionService.get("basic", "product").find().add(SearchRestrictions.eq("number", number)).setMaxResults(1)
                .list().getEntities().get(0);
    }

    private Entity getOperationByNumber(final String number) {
        return dataDefinitionService.get("technologies", "operation").find().add(SearchRestrictions.eq("number", number))
                .setMaxResults(1).list().getEntities().get(0);
    }

    private Entity getRandomProduct() {
        Long total = (long) dataDefinitionService.get("basic", "product").find().list().getTotalNumberOfEntities();
        return dataDefinitionService.get("basic", "product").find().setFirstResult(RANDOM.nextInt(total.intValue()))
                .setMaxResults(1).list().getEntities().get(0);
    }

    private Entity getRandomOrder() {
        Long total = (long) dataDefinitionService.get("orders", "order").find().list().getTotalNumberOfEntities();
        return dataDefinitionService.get("orders", "order").find().setFirstResult(RANDOM.nextInt(total.intValue()))
                .setMaxResults(1).list().getEntities().get(0);
    }

}
