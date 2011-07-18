/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.3
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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.Module;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.security.api.SecurityRole;
import com.qcadoo.security.api.SecurityRolesService;

@Component
public class SamplesLoaderModule extends Module {

    private static final Logger LOG = LoggerFactory.getLogger(SamplesLoaderModule.class);

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static final long MILLIS_IN_DAY = 86400000;

    private static final String[] TYPE_OF_MATERIALS = new String[] { "03product", "01component", "02intermediate" };

    private static final List<String> UNITS = new ArrayList<String>();

    private static final String[] PRODUCT_ATTRIBUTES = new String[] { "ean", "name", "product_nr", "batch" };

    private static final String[] DICTIONARY_ATTRIBUTES = new String[] { "name", "item" };

    private static final String[] USER_ATTRIBUTES = new String[] { "login", "email", "firstname", "lastname", "role" };

    private static final String[] ORDER_ATTRIBUTES = new String[] { "scheduled_start_date", "scheduled_end_date",
            "quantity_completed", "started_date", "finished_date", "name", "order_nr", "quantity_scheduled", "machine_nr",
            "bom_name", "product_nr", "effective_started_date" };

    private static final String[] TECHNOLOGY_ATTRIBUTES = new String[] { "bom_id", "description", "name", "bom_nr", "product_nr",
            "algorithm", "minimal" };

    private static final String[] OPERATION_ATTRIBUTES = new String[] { "name", "number", "tpz", "tj" };

    private static final String[] MACHINE_ATTRIBUTES = new String[] { "id", "name", "prod_line", "description" };

    private static final String[] STAFF_ATTRIBUTES = new String[] { "id", "name", "surname", "post" };

    private static final String[] SHIFT_ATTRIBUTES = new String[] { "id", "name", "monday_working", "monday_hours",
            "tuesday_working", "tuesday_hours", "wensday_working", "wensday_hours", "thursday_working", "thursday_hours",
            "friday_working", "friday_hours", "saturday_working", "saturday_hours", "sunday_working", "sunday_hours" };

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    @Value("${loadTestData}")
    private boolean addTestData;

    @Autowired
    private PluginAccessor pluginAccessor;

    @Autowired
    private SecurityRolesService securityRolesService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Value("${loadTestDataLocale}")
    private String locale;

    @Value("${setAsDemoEnviroment}")
    private boolean setAsDemoEnviroment;

    @Override
    @Transactional
    public void multiTenantEnable() {
        if (databaseHasToBePrepared()) {
            LOG.info("Database has to be prepared ...");

            addParameters();

            if (addTestData) {
                if (!setAsDemoEnviroment) {
                    readDataFromXML("users", USER_ATTRIBUTES);
                } else {
                    changeAdminPassword();
                }

                readDataFromXML("dictionaries", DICTIONARY_ATTRIBUTES);
                if (isEnabled("basic")) {
                    readDataFromXML("machines", MACHINE_ATTRIBUTES);
                    readDataFromXML("staff", STAFF_ATTRIBUTES);
                    readDataFromXML("units", new String[] { "name" });
                    readDataFromXML("products", PRODUCT_ATTRIBUTES);
                }
                if (isEnabled("technologies")) {
                    readDataFromXML("operations", OPERATION_ATTRIBUTES);
                    readDataFromXML("technologies", TECHNOLOGY_ATTRIBUTES);
                }
                if (isEnabled("orders")) {
                    readDataFromXML("orders", ORDER_ATTRIBUTES);
                }
                if (isEnabled("materialRequirements")) {
                    addMaterialRequirements();
                }
                if (isEnabled("workPlans")) {
                    addWorkPlans();
                }
                if (isEnabled("productionScheduling")) {
                    readDataFromXML("shifts", SHIFT_ATTRIBUTES);
                }
            }
        } else {
            LOG.info("Database has been already prepared, skipping");
        }
    }

    private void changeAdminPassword() {
        DataDefinition userDD = dataDefinitionService.get("qcadooSecurity", "user");
        Entity user = userDD.find().add(SearchRestrictions.eq("userName", "admin")).setMaxResults(1).uniqueResult();
        user.setField("password", "charon321Demo");
        userDD.save(user);
    }

    private boolean isEnabled(final String pluginIdentifier) {
        return pluginAccessor.getPlugin(pluginIdentifier) != null;
    }

    private InputStream getXmlFile(final String type) throws IOException {
        return SamplesLoaderModule.class.getResourceAsStream("/com/qcadoo/mes/samples/" + type + "_" + locale + ".xml");
    }

    private void readDataFromXML(final String type, final String[] attributes) {
        LOG.info("Loading test data from " + type + "_" + locale + ".xml ...");

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(getXmlFile(type));
            doc.getDocumentElement().normalize();

            NodeList nodeLst = doc.getElementsByTagName("row");

            for (int s = 0; s < nodeLst.getLength(); s++) {
                readData(attributes, type, nodeLst, s);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            LOG.error(e.getMessage(), e);
        } catch (SAXException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void readData(final String[] attributes, final String type, final NodeList nodeLst, final int s) {
        Map<String, String> values = new HashMap<String, String>();
        Node fstNode = nodeLst.item(s);

        for (String attribute : attributes) {
            if (fstNode.getAttributes().getNamedItem(attribute.toUpperCase(Locale.ENGLISH)) != null) {
                String value = fstNode.getAttributes().getNamedItem(attribute.toUpperCase(Locale.ENGLISH)).getNodeValue();
                values.put(attribute, value);
            }
        }

        if ("products".equals(type)) {
            addProduct(values);
        } else if ("orders".equals(type)) {
            addOrder(values);
        } else if ("technologies".equals(type)) {
            addTechnology(values);
        } else if ("dictionaries".equals(type)) {
            addDictionary(values);
        } else if ("users".equals(type)) {
            addUser(values);
        } else if ("units".equals(type)) {
            UNITS.add(values.get("name"));
        } else if ("operations".equals(type)) {
            addOperations(values);
        } else if ("staff".equals(type)) {
            addStaff(values);
        } else if ("machines".equals(type)) {
            addMachine(values);
        } else if ("shifts".equals(type)) {
            addShift(values);
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

        if (!machine.isValid()) {
            throw new IllegalStateException("Saved entity have validation errors");
        }
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
        if (!staff.isValid()) {
            throw new IllegalStateException("Saved entity have validation errors");
        }

    }

    private void addOperations(final Map<String, String> values) {
        Entity operation = dataDefinitionService.get("technologies", "operation").create();

        operation.setField("name", values.get("name"));
        operation.setField("number", values.get("number"));
        operation.setField("tpz", values.get("tpz"));
        operation.setField("tj", values.get("tj"));
        operation.setField("countRealized", values.get("countRealized"));
        operation.setField("machine", getMachine(values.get("number")));
        operation.setField("staff", getRandomStaff());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test operation item {name=" + operation.getField("name") + ", number=" + operation.getField("number")
                    + "}");
        }

        operation = dataDefinitionService.get("technologies", "operation").save(operation);
        if (!operation.isValid()) {
            throw new IllegalStateException("Saved entity have validation errors");
        }
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
        product.setField("typeOfMaterial", getRandomTypeOfMaterial());
        product.setField("unit", getRandomUnit());
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
        if (!substitute.isValid()) {
            throw new IllegalStateException("Saved entity have validation errors");
        }

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
        if (!substituteComponent.isValid()) {
            throw new IllegalStateException("Saved entity have validation errors");
        }
    }

    private void addDictionary(final Map<String, String> values) {
        Entity dictionary = getDictionaryByName(values.get("name"));

        Entity item = dataDefinitionService.get("qcadooModel", "dictionaryItem").create();
        item.setField("dictionary", dictionary);
        item.setField("name", values.get("item"));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test dictionary item {dictionary=" + dictionary.getField("name") + ", item=" + item.getField("name")
                    + "}");
        }

        item = dataDefinitionService.get("qcadooModel", "dictionaryItem").save(item);

        if (!item.isValid()) {
            throw new IllegalStateException("Saved entity have validation errors");
        }
    }

    private void addUser(final Map<String, String> values) {
        Entity user = dataDefinitionService.get("qcadooSecurity", "user").create();
        if (!values.get("login").isEmpty()) {
            user.setField("userName", values.get("login"));
        }
        if (!values.get("email").isEmpty()) {
            user.setField("email", values.get("email"));
        }
        if (!values.get("firstname").isEmpty()) {
            user.setField("firstName", values.get("firstname"));
        }
        if (!values.get("lastname").isEmpty()) {
            user.setField("lastName", values.get("lastname"));
        }
        SecurityRole role = securityRolesService.getRoleByIdentifier(values.get("role"));
        user.setField("role", role.getName());
        user.setField("password", "123");
        user.setField("passwordConfirmation", "123");
        user.setField("enabled", true);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test user {login=" + user.getField("userName") + ", email=" + user.getField("email") + ", firstName="
                    + user.getField("firstName") + ", lastName=" + user.getField("lastName") + ", role=" + user.getField("role")
                    + "}");
        }

        user = dataDefinitionService.get("qcadooSecurity", "user").save(user);
        if (!user.isValid()) {
            throw new IllegalStateException("Saved entity have validation errors");
        }
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

        order.setField("technology", getTechnologyByName(values.get("bom_name")));
        order.setField("name",
                (values.get("name").isEmpty() || values.get("name") == null) ? values.get("order_nr") : values.get("name"));
        order.setField("number", values.get("order_nr"));
        order.setField("plannedQuantity", values.get("quantity_scheduled").isEmpty() ? new BigDecimal(100 * RANDOM.nextDouble())
                : new BigDecimal(values.get("quantity_scheduled")));
        if (Integer.parseInt(order.getField("plannedQuantity").toString()) == 0) {
            order.setField("plannedQuantity", null);
        }

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
                    + ", plannedQuantity=" + order.getField("plannedQuantity") + ", state=" + order.getField("state")
                    + ", startWorker=" + order.getField("startWorker") + ", endWorker=" + order.getField("endWorker") + "}");
        }

        order = dataDefinitionService.get("orders", "order").save(order);
        if (!order.isValid()) {
            throw new IllegalStateException("Saved entity have validation errors");
        }
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
            technology.setField("batchRequired", true);
            technology.setField("postFeatureRequired", false);
            technology.setField("otherFeatureRequired", false);
            technology.setField("shiftFeatureRequired", false);
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
            if (!technology.isValid()) {
                throw new IllegalStateException("Saved entity have validation errors");
            }

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
            } else {
                addTechnologyOperationComponents(technology, null, 3);
            }
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
        component.setField("countRealized", operation.getField("countRealized"));

        component = dataDefinitionService.get("technologies", "technologyOperationComponent").save(component);
        if (!component.isValid()) {
            throw new IllegalStateException("Saved entity have validation errors");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test operation component {technology="
                    + ((Entity) component.getField("technology")).getField("number") + ", parent="
                    + (parent != null ? parent.getId() : 0) + ", operation="
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
        if (!productComponent.isValid()) {
            throw new IllegalStateException("Saved entity have validation errors");
        }

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
        if (!productComponent.isValid()) {
            throw new IllegalStateException("Saved entity have validation errors");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test product component {product="
                    + ((Entity) productComponent.getField("product")).getField("number")
                    + ", operation="
                    + ((Entity) ((Entity) productComponent.getField("operationComponent")).getField("operation"))
                            .getField("number") + ", quantity=" + productComponent.getField("quantity") + "}");
        }
    }

    private void addTechnologyOperationComponents(final Entity technology, final Entity parent, final int depth) {
        if (depth <= 0) {
            return;
        }

        int childrenNumber = RANDOM.nextInt(4) + 1;

        if (depth == 3) {
            childrenNumber = 1;
        }

        for (int i = 0; i < childrenNumber; i++) {
            Entity component = addOperationComponent(technology, parent, getRandomOperation());

            for (int j = 0; j < RANDOM.nextInt(4) + 1; j++) {
                addProductInComponent(component, new BigDecimal(100 * RANDOM.nextDouble()).setScale(3, RoundingMode.HALF_EVEN),
                        getRandomProduct());
            }

            for (int j = 0; j < RANDOM.nextInt(4) + 1; j++) {
                addProductOutComponent(component, new BigDecimal(100 * RANDOM.nextDouble()).setScale(3, RoundingMode.HALF_EVEN),
                        getRandomProduct());
            }

            if (RANDOM.nextDouble() > 0.2) {
                addTechnologyOperationComponents(technology, component, depth - 1);
            }
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

        requirement = dataDefinitionService.get("materialRequirements", "materialRequirement").save(requirement);
        if (!requirement.isValid()) {
            throw new IllegalStateException("Saved entity have validation errors");
        }

        for (int i = 0; i < 1; i++) {
            Entity component = dataDefinitionService.get("materialRequirements", "materialRequirementComponent").create();
            component.setField("materialRequirement", requirement);
            component.setField("order", getRandomOrder());

            if (LOG.isDebugEnabled()) {
                LOG.debug("Add test material requirement component {requirement="
                        + ((Entity) component.getField("materialRequirement")).getField("name") + ", order="
                        + ((Entity) component.getField("order")).getField("number") + "}");
            }

            component = dataDefinitionService.get("materialRequirements", "materialRequirementComponent").save(component);
            if (!component.isValid()) {
                throw new IllegalStateException("Saved entity have validation errors");
            }
        }
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
        if (!workPlan.isValid()) {
            throw new IllegalStateException("Saved entity have validation errors");
        }

        for (int i = 0; i < 1; i++) {
            Entity component = dataDefinitionService.get("workPlans", "workPlanComponent").create();
            component.setField("workPlan", workPlan);
            component.setField("order", getRandomOrder());

            if (LOG.isDebugEnabled()) {
                LOG.debug("Add test work plan component {workPlan=" + ((Entity) component.getField("workPlan")).getField("name")
                        + ", order=" + ((Entity) component.getField("order")).getField("number") + "}");
            }

            component = dataDefinitionService.get("workPlans", "workPlanComponent").save(component);
            if (!component.isValid()) {
                throw new IllegalStateException("Saved entity have validation errors");
            }
        }
    }

    private void addShift(final Map<String, String> values) {
        Entity shift = dataDefinitionService.get("productionScheduling", "shift").create();
        if (values.containsKey("id")) {
            shift.setId(Long.parseLong(values.get("id")));
        }
        shift.setField("name", values.get("name"));
        for (String day : new String[] { "monday", "tuesday", "wensday", "thursday", "friday", "saturday", "sunday" }) {
            if (values.containsKey(day + "_working")) {
                shift.setField(day + "Working", Boolean.parseBoolean(values.get(day + "_working")));
            } else {
                shift.setField(day + "Working", false);
            }
            shift.setField(day + "Hours", values.get(day + "_hours"));
        }
        dataDefinitionService.get("productionScheduling", "shift").save(shift);
    }

    private Entity getRandomStaff() {
        Long total = (long) dataDefinitionService.get("basic", "staff").find().list().getTotalNumberOfEntities();
        return dataDefinitionService.get("basic", "staff").find().setFirstResult(RANDOM.nextInt(total.intValue()))
                .setMaxResults(1).list().getEntities().get(0);
    }

    private Entity getMachine(final String id) {
        List<Entity> machines = dataDefinitionService.get("basic", "machine").find().isEq("number", id).list().getEntities();
        if (machines.size() > 0) {
            return machines.get(0);
        } else {
            return null;
        }
    }

    private Entity getTechnologyByName(final String name) {
        List<Entity> technologies = dataDefinitionService.get("technologies", "technology").find().isEq("name", name)
                .setMaxResults(1).list().getEntities();
        if (technologies.size() > 0) {
            return technologies.get(0);
        } else {
            return null;
        }
    }

    private Entity getDefaultTechnologyForProduct(final Entity product) {
        if (product == null) {
            return null;
        }
        List<Entity> technologies = dataDefinitionService.get("technologies", "technology").find()
                .belongsTo("product", product.getId()).isEq("master", true).setMaxResults(1).list().getEntities();
        if (technologies.size() > 0) {
            return technologies.get(0);
        } else {
            return null;
        }
    }

    private Entity getProductByNumber(final String number) {
        return dataDefinitionService.get("basic", "product").find().isEq("number", number).setMaxResults(1).list().getEntities()
                .get(0);
    }

    private Entity getOperationByNumber(final String number) {
        return dataDefinitionService.get("technologies", "operation").find().isEq("number", number).setMaxResults(1).list()
                .getEntities().get(0);
    }

    private String getRandomDictionaryItem(final String dictionaryName) {
        Entity dictionary = getDictionaryByName(dictionaryName);
        Long total = (long) dataDefinitionService.get("qcadooModel", "dictionaryItem").find()
                .belongsTo("dictionary", dictionary.getId()).list().getTotalNumberOfEntities();
        Entity item = dataDefinitionService.get("qcadooModel", "dictionaryItem").find()
                .belongsTo("dictionary", dictionary.getId()).setFirstResult(RANDOM.nextInt(total.intValue())).setMaxResults(1)
                .list().getEntities().get(0);
        return item.getField("name").toString();
    }

    private Entity getDictionaryByName(final String name) {
        return dataDefinitionService.get("qcadooModel", "dictionary").find().isEq("name", name).setMaxResults(1).list()
                .getEntities().get(0);
    }

    private Entity getRandomProduct() {
        Long total = (long) dataDefinitionService.get("basic", "product").find().list().getTotalNumberOfEntities();
        return dataDefinitionService.get("basic", "product").find().setFirstResult(RANDOM.nextInt(total.intValue()))
                .setMaxResults(1).list().getEntities().get(0);
    }

    private Entity getRandomOperation() {
        Long total = (long) dataDefinitionService.get("technologies", "operation").find().list().getTotalNumberOfEntities();
        return dataDefinitionService.get("technologies", "operation").find().setFirstResult(RANDOM.nextInt(total.intValue()))
                .setMaxResults(1).list().getEntities().get(0);
    }

    private Entity getRandomOrder() {
        Long total = (long) dataDefinitionService.get("orders", "order").find().list().getTotalNumberOfEntities();
        return dataDefinitionService.get("orders", "order").find().setFirstResult(RANDOM.nextInt(total.intValue()))
                .setMaxResults(1).list().getEntities().get(0);
    }

    /*
     * @SuppressWarnings("unused") private Entity getRandomUser() { Long total = (long)
     * dataDefinitionService.get("qcadooSecurity", "user").find().list().getTotalNumberOfEntities(); return
     * dataDefinitionService.get("qcadooSecurity", "user").find().setFirstResult(RANDOM.nextInt(total.intValue()))
     * .setMaxResults(1).list().getEntities().get(0); }
     */

    private String getRandomTypeOfMaterial() {
        return TYPE_OF_MATERIALS[RANDOM.nextInt(TYPE_OF_MATERIALS.length)];
    }

    private String getRandomUnit() {
        return UNITS.get(RANDOM.nextInt(UNITS.size()));
    }

    private void addParameters() {
        LOG.info("Adding parameters");
        Entity parameter = dataDefinitionService.get("basic", "parameter").create();
        parameter.setField("checkDoneOrderForQuality", false);
        parameter.setField("autoGenerateQualityControl", false);
        parameter.setField("batchForDoneOrder", "01none");
        dataDefinitionService.get("basic", "parameter").save(parameter);
    }

    private boolean databaseHasToBePrepared() {
        return dataDefinitionService.get("basic", "parameter").find().list().getTotalNumberOfEntities() == 0;
    }

}
