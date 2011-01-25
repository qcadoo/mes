/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.application;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.qcadoo.mes.beans.basic.BasicMachine;
import com.qcadoo.mes.beans.basic.BasicStaff;
import com.qcadoo.mes.beans.dictionaries.DictionariesDictionary;
import com.qcadoo.mes.beans.dictionaries.DictionariesDictionaryItem;
import com.qcadoo.mes.beans.products.ProductsMaterialRequirement;
import com.qcadoo.mes.beans.products.ProductsMaterialRequirementComponent;
import com.qcadoo.mes.beans.products.ProductsOperation;
import com.qcadoo.mes.beans.products.ProductsOperationProductInComponent;
import com.qcadoo.mes.beans.products.ProductsOperationProductOutComponent;
import com.qcadoo.mes.beans.products.ProductsOrder;
import com.qcadoo.mes.beans.products.ProductsProduct;
import com.qcadoo.mes.beans.products.ProductsSubstitute;
import com.qcadoo.mes.beans.products.ProductsSubstituteComponent;
import com.qcadoo.mes.beans.products.ProductsTechnology;
import com.qcadoo.mes.beans.products.ProductsTechnologyOperationComponent;
import com.qcadoo.mes.beans.products.ProductsWorkPlan;
import com.qcadoo.mes.beans.products.ProductsWorkPlanComponent;
import com.qcadoo.mes.beans.users.UsersGroup;
import com.qcadoo.mes.beans.users.UsersUser;

@Component
public final class TestDataLoader {

    private static final Logger LOG = LoggerFactory.getLogger(TestDataLoader.class);

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static final long MILLIS_IN_DAY = 86400000;

    private static final String[] TYPE_OF_MATERIALS = new String[] { "03product", "01component", "02intermediate", "04waste" };

    private static final List<String> UNITS = new ArrayList<String>();

    private static final String[] PRODUCT_ATTRIBUTES = new String[] { "product_id", "ean", "name", "product_nr", "batch" };

    private static final String[] DICTIONARY_ATTRIBUTES = new String[] { "name", "item" };

    private static final String[] USER_ATTRIBUTES = new String[] { "login", "email", "firstname", "lastname", "role" };

    private static final String[] ORDER_ATTRIBUTES = new String[] { "order_id", "scheduled_start_date", "scheduled_end_date",
            "quantity_completed", "started_date", "finished_date", "name", "order_nr", "quantity_scheduled", "machine_nr",
            "bom_name", "product_nr" };

    private static final String[] TECHNOLOGY_ATTRIBUTES = new String[] { "bom_id", "description", "name", "bom_nr", "product_nr" };

    private static final String[] OPERATION_ATTRIBUTES = new String[] { "name", "number" };

    private static final String[] MACHINE_ATTRIBUTES = new String[] { "id", "name", "prod_line", "description" };

    private static final String[] STAFF_ATTRIBUTES = new String[] { "id", "name", "surname", "post" };

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${loadTestDataLocale}")
    private String locale;

    @Value("${addOtherUsers}")
    private boolean addOtherUsers;

    public void loadTestData() {
        readDataFromXML("units", new String[] { "name" });

        if (addOtherUsers) {
            readDataFromXML("users", USER_ATTRIBUTES);
        }
        readDataFromXML("dictionaries", DICTIONARY_ATTRIBUTES);
        readDataFromXML("products", PRODUCT_ATTRIBUTES);
        readDataFromXML("machines", MACHINE_ATTRIBUTES);
        readDataFromXML("staff", STAFF_ATTRIBUTES);
        readDataFromXML("operations", OPERATION_ATTRIBUTES);
        readDataFromXML("technologies", TECHNOLOGY_ATTRIBUTES);
        readDataFromXML("orders", ORDER_ATTRIBUTES);
        addMaterialRequirements();
        addWorkPlans();
    }

    private File getXmlFile(final String type) throws IOException {
        return applicationContext.getResource("classpath:/com/qcadoo/mes/testdata/" + type + "_" + locale + ".xml").getFile();
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
            String value = fstNode.getAttributes().getNamedItem(attribute.toUpperCase(Locale.ENGLISH)).getNodeValue();
            values.put(attribute, value);
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
        }
    }

    private void addMachine(final Map<String, String> values) {
        BasicMachine machine = new BasicMachine();

        LOG.debug("id: " + values.get("id") + " name " + values.get("name") + " prod_line " + values.get("prod_line")
                + " description " + values.get("description"));
        machine.setNumber(values.get("id"));
        machine.setName(values.get("name"));
        machine.setProductionLine(Boolean.getBoolean(values.get("prod_line")));
        machine.setDescription(values.get("description"));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test machine item {machine=" + machine.getName() + ", number=" + machine.getNumber() + "}");
        }

        sessionFactory.getCurrentSession().save(machine);
    }

    private void addStaff(final Map<String, String> values) {
        BasicStaff staff = new BasicStaff();

        LOG.debug("id: " + values.get("id") + " name " + values.get("name") + " surname " + values.get("surname") + " post "
                + values.get("post"));
        staff.setNumber(values.get("id"));
        staff.setName(values.get("name"));
        staff.setSurname(values.get("surname"));
        staff.setPost(values.get("post"));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test staff item {staff=" + staff.getName() + ", surName=" + staff.getSurname() + "}");
        }

        sessionFactory.getCurrentSession().save(staff);

    }

    private void addOperations(final Map<String, String> values) {
        ProductsOperation operation = new ProductsOperation();

        operation.setName(values.get("name"));
        operation.setNumber(values.get("number"));
        operation.setMachine(getMachine(values.get("number")));
        operation.setStaff(getRandomStaff());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test operation item {name=" + operation.getName() + ", number=" + operation.getNumber() + "}");
        }

        sessionFactory.getCurrentSession().save(operation);

    }

    private void addProduct(final Map<String, String> values) {
        ProductsProduct product = new ProductsProduct();
        product.setId(Long.valueOf(values.get("product_id")));
        product.setCategory(getRandomDictionaryItem("categories"));
        if (!values.get("ean").isEmpty()) {
            product.setEan(values.get("ean"));
        }
        if (!values.get("name").isEmpty()) {
            product.setName(values.get("name"));
        }
        if (!values.get("batch").isEmpty()) {
            product.setBatch(values.get("batch"));
        }
        if (!values.get("product_nr").isEmpty()) {
            product.setNumber(values.get("product_nr"));
        }
        product.setTypeOfMaterial(getRandomTypeOfMaterial());
        product.setUnit(getRandomUnit());
        sessionFactory.getCurrentSession().save(product);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test product {id=" + product.getId() + ", category=" + product.getCategory() + ", ean="
                    + product.getEan() + ", name=" + product.getName() + ", number=" + product.getName() + ", typeOfMaterial="
                    + product.getTypeOfMaterial() + ", unit=" + product.getUnit() + "}");
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < RANDOM.nextInt(5); i++) {
            for (int j = 0; j <= i; j++) {
                stringBuilder.append("*");
            }
            addSubstitute(values.get("name") + stringBuilder.toString(), values.get("product_nr") + stringBuilder.toString(),
                    product, i + 1);
        }
    }

    private void addDictionary(final Map<String, String> values) {
        DictionariesDictionary dictionary = getOrAddDictionary(values.get("name"));

        DictionariesDictionaryItem item = new DictionariesDictionaryItem();
        item.setDictionary(dictionary);
        item.setName(values.get("item"));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test dictionary item {dictionary=" + dictionary.getName() + ", name=" + item.getName() + "}");
        }

        sessionFactory.getCurrentSession().save(item);
    }

    private void addUser(final Map<String, String> values) {
        UsersUser user = new UsersUser();
        if (!values.get("login").isEmpty()) {
            user.setUserName(values.get("login"));
        }
        if (!values.get("email").isEmpty()) {
            user.setEmail(values.get("email"));
        }
        if (!values.get("firstname").isEmpty()) {
            user.setFirstName(values.get("firstname"));
        }
        if (!values.get("lastname").isEmpty()) {
            user.setLastName(values.get("lastname"));
        }
        user.setUserGroup(getGroupByRole(values.get("role")));
        user.setPassword("a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test user {login=" + user.getUserName() + ", email=" + user.getEmail() + ", firstName="
                    + user.getFirstName() + ", lastName=" + user.getLastName() + ", role=" + user.getUserGroup().getName() + "}");
        }

        sessionFactory.getCurrentSession().save(user);
    }

    private DictionariesDictionary getOrAddDictionary(final String name) {
        DictionariesDictionary dictionary = getDictionaryByName(name);

        if (dictionary == null) {
            dictionary = new DictionariesDictionary();
            dictionary.setName(name);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Add test dictionary {name=" + dictionary.getName() + "}");
            }

            sessionFactory.getCurrentSession().save(dictionary);
        }

        return dictionary;
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

        long effectiveStartDate = startDate + MILLIS_IN_DAY * (RANDOM.nextInt(10) - 5);

        if (!values.get("started_date").isEmpty()) {
            try {
                effectiveStartDate = FORMATTER.parse(values.get("started_date")).getTime();
            } catch (ParseException e) {
                LOG.warn(e.getMessage(), e);
            }
        }

        long effectiveEndDate = effectiveStartDate + (MILLIS_IN_DAY * RANDOM.nextInt(50));

        if (!values.get("finished_date").isEmpty()) {
            try {
                effectiveEndDate = FORMATTER.parse(values.get("finished_date")).getTime();
            } catch (ParseException e) {
                LOG.warn(e.getMessage(), e);
            }
        }

        ProductsOrder order = new ProductsOrder();
        order.setId(Long.valueOf(values.get("order_id")));
        order.setDateFrom(new Date(startDate));
        order.setDateTo(new Date(endDate));
        order.setDoneQuantity(values.get("quantity_completed").isEmpty() ? new BigDecimal(100 * RANDOM.nextDouble())
                : new BigDecimal(values.get("quantity_completed")));
        if (order.getDoneQuantity().intValue() == 0) {
            order.setDoneQuantity(null);
        }
        order.setEffectiveDateFrom(new Date(effectiveStartDate));
        order.setEffectiveDateTo(new Date(effectiveEndDate));
        order.setEndWorker(getRandomUser().getUserName());
        order.setTechnology(getTechnologyByName(values.get("bom_name")));
        order.setName((values.get("name").isEmpty() || values.get("name") == null) ? values.get("order_nr") : values.get("name"));
        order.setNumber(values.get("order_nr"));
        order.setPlannedQuantity(values.get("quantity_scheduled").isEmpty() ? new BigDecimal(100 * RANDOM.nextDouble())
                : new BigDecimal(values.get("quantity_scheduled")));
        if (order.getPlannedQuantity().intValue() == 0) {
            order.setPlannedQuantity(null);
        }
        order.setStartWorker(getRandomUser().getUserName());

        String state = (RANDOM.nextDouble() > 0.4) ? "03done" : "01pending";

        order.setState(state);

        if ("01pending".equals(state)) {
            order.setEndWorker(null);
            order.setEffectiveDateTo(null);
        }

        ProductsProduct product = getProductByNumber(values.get("product_nr"));

        order.setProduct(product);

        order.setTechnology(getDefaultTechnologyForProduct(product));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test order {id=" + order.getId() + ", name=" + order.getName() + ", number=" + order.getNumber()
                    + ", product=" + (order.getProduct() != null ? order.getProduct().getNumber() : null) + ", technology="
                    + (order.getTechnology() != null ? order.getTechnology().getNumber() : null) + ", dateFrom="
                    + order.getDateFrom() + ", dateTo=" + order.getDateTo() + ", effectiveDateFrom="
                    + order.getEffectiveDateFrom() + ", effectiveDateTo=" + order.getEffectiveDateTo() + ", doneQuantity="
                    + order.getDoneQuantity() + ", plannedQuantity=" + order.getPlannedQuantity() + ", state=" + order.getState()
                    + ", startWorker=" + order.getStartWorker() + ", endWorker=" + order.getEndWorker() + "}");
        }

        sessionFactory.getCurrentSession().save(order);
    }

    private ProductsSubstitute addSubstitute(final String name, final String number, final ProductsProduct product,
            final int priority) {
        ProductsSubstitute substitute = new ProductsSubstitute();
        substitute.setName(name);
        substitute.setNumber(number);
        substitute.setPriority(priority);
        substitute.setProduct(product);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test substitute {name=" + substitute.getName() + ", number=" + substitute.getNumber() + ", priority="
                    + substitute.getPriority() + ", product=" + substitute.getProduct().getNumber() + "}");
        }

        sessionFactory.getCurrentSession().save(substitute);

        for (int i = 0; i < RANDOM.nextInt(4) + 1; i++) {
            addSubstituteComponent(substitute, getRandomProduct(), 100 * RANDOM.nextDouble());
        }

        return substitute;
    }

    private ProductsSubstituteComponent addSubstituteComponent(final ProductsSubstitute substitute,
            final ProductsProduct product, final double quantity) {
        ProductsSubstituteComponent substituteComponent = new ProductsSubstituteComponent();
        substituteComponent.setProduct(product);
        substituteComponent.setQuantity(new BigDecimal(quantity));
        substituteComponent.setSubstitute(substitute);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test substitute component {substitute=" + substituteComponent.getSubstitute().getNumber()
                    + ", product=" + substituteComponent.getProduct().getNumber() + ", quantity="
                    + substituteComponent.getQuantity() + "}");
        }

        sessionFactory.getCurrentSession().save(substituteComponent);
        return substituteComponent;
    }

    private void addTechnology(final Map<String, String> values) {
        ProductsProduct product = getProductByNumber(values.get("product_nr"));

        if (product != null) {
            ProductsTechnology defaultTechnology = getDefaultTechnologyForProduct(product);

            ProductsTechnology technology = new ProductsTechnology();
            technology.setId(Long.valueOf(values.get("bom_id")));
            if (!values.get("description").isEmpty()) {
                technology.setDescription(values.get("description"));
            }
            technology.setMaster(defaultTechnology == null);
            technology.setName(values.get("name"));
            technology.setNumber(values.get("bom_nr"));
            technology.setProduct(product);
            technology.setBatchRequired(true);
            technology.setPostFeatureRequired(false);
            technology.setOtherFeatureRequired(false);
            technology.setShiftFeatureRequired(false);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Add test technology {id=" + technology.getId() + ", name=" + technology.getName() + ", number="
                        + technology.getNumber() + ", product=" + technology.getProduct().getNumber() + ", description="
                        + technology.getDescription() + ", master=" + technology.getMaster() + "}");
            }

            sessionFactory.getCurrentSession().save(technology);

            addTechnologyOperationComponents(technology, null, 3);
        }
    }

    private void addTechnologyOperationComponents(final ProductsTechnology technology,
            final ProductsTechnologyOperationComponent parent, final int depth) {
        if (depth <= 0) {
            return;
        }

        for (int i = 0; i < RANDOM.nextInt(4) + 1; i++) {
            ProductsTechnologyOperationComponent component = new ProductsTechnologyOperationComponent();
            component.setTechnology(technology);
            component.setParent(parent);
            component.setOperation(getRandomOperation());

            sessionFactory.getCurrentSession().save(component);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Add test operation component {technology=" + component.getTechnology().getNumber() + ", parent="
                        + (parent != null ? parent.getId() : 0) + ", operation=" + component.getOperation().getNumber() + "}");
            }

            for (int j = 0; j < RANDOM.nextInt(4) + 1; j++) {
                ProductsOperationProductInComponent productComponent = new ProductsOperationProductInComponent();
                productComponent.setOperationComponent(component);
                productComponent.setQuantity(new BigDecimal(100 * RANDOM.nextDouble()));
                productComponent.setProduct(getRandomProduct());
                productComponent.setBatchRequired(true);

                sessionFactory.getCurrentSession().save(productComponent);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Add test product component {product=" + productComponent.getProduct().getNumber() + ", operation="
                            + productComponent.getOperationComponent().getOperation().getNumber() + ", quantity="
                            + productComponent.getQuantity() + "}");
                }
            }

            for (int j = 0; j < RANDOM.nextInt(4) + 1; j++) {
                ProductsOperationProductOutComponent productComponent = new ProductsOperationProductOutComponent();
                productComponent.setOperationComponent(component);
                productComponent.setQuantity(new BigDecimal(100 * RANDOM.nextDouble()));
                productComponent.setProduct(getRandomProduct());

                sessionFactory.getCurrentSession().save(productComponent);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Add test product component {product=" + productComponent.getProduct().getNumber() + ", operation="
                            + productComponent.getOperationComponent().getOperation().getNumber() + ", quantity="
                            + productComponent.getQuantity() + "}");
                }
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
        ProductsMaterialRequirement requirement = new ProductsMaterialRequirement();
        requirement.setName(getRandomProduct().getName());
        requirement.setGenerated(false);
        requirement.setDate(null);
        requirement.setOnlyComponents(RANDOM.nextBoolean());
        requirement.setWorker(null);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test material requirement {name=" + requirement.getName() + ", date=" + requirement.getDate()
                    + ", worker=" + requirement.getWorker() + ", onlyComponents=" + requirement.isOnlyComponents()
                    + ", generated=" + requirement.isGenerated() + "}");
        }

        sessionFactory.getCurrentSession().save(requirement);

        for (int i = 0; i < RANDOM.nextInt(8) + 2; i++) {
            ProductsMaterialRequirementComponent component = new ProductsMaterialRequirementComponent();
            component.setMaterialRequirement(requirement);
            component.setOrder(getRandomOrder());

            if (LOG.isDebugEnabled()) {
                LOG.debug("Add test material requirement component {requirement=" + component.getMaterialRequirement().getName()
                        + ", order=" + component.getOrder().getNumber() + "}");
            }

            sessionFactory.getCurrentSession().save(component);
        }
    }

    private void addWorkPlans() {
        for (int i = 0; i < 50; i++) {
            addWorkPlan();
        }
    }

    private void addWorkPlan() {
        ProductsWorkPlan workPlan = new ProductsWorkPlan();
        workPlan.setName(getRandomProduct().getName());
        workPlan.setGenerated(false);
        workPlan.setDate(null);
        workPlan.setWorker(null);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test material requirement {name=" + workPlan.getName() + ", date=" + workPlan.getDate() + ", worker="
                    + workPlan.getWorker() + ", generated=" + workPlan.isGenerated() + "}");
        }

        sessionFactory.getCurrentSession().save(workPlan);

        for (int i = 0; i < RANDOM.nextInt(8) + 2; i++) {
            ProductsWorkPlanComponent component = new ProductsWorkPlanComponent();
            component.setWorkPlan(workPlan);
            component.setOrder(getRandomOrder());

            if (LOG.isDebugEnabled()) {
                LOG.debug("Add test material requirement component {requirement=" + component.getWorkPlan().getName()
                        + ", order=" + component.getOrder().getNumber() + "}");
            }

            sessionFactory.getCurrentSession().save(component);
        }
    }

    private BasicStaff getRandomStaff() {
        Long total = (Long) sessionFactory.getCurrentSession().createCriteria(BasicStaff.class)
                .setProjection(Projections.rowCount()).uniqueResult();
        return (BasicStaff) sessionFactory.getCurrentSession().createCriteria(BasicStaff.class)
                .setFirstResult(RANDOM.nextInt(total.intValue())).setMaxResults(1).uniqueResult();
    }

    private BasicMachine getMachine(final String id) {
        // Long total = (Long) sessionFactory.getCurrentSession().createCriteria(BasicMachine.class)
        // .setProjection(Projections.rowCount()).uniqueResult();
        // return (BasicMachine) sessionFactory.getCurrentSession().createCriteria(BasicMachine.class)
        // .setFirstResult(Integer.parseInt(id)).setMaxResults(1).uniqueResult();

        LOG.debug(">>>>>>>>>>>>>>>>>>>>>>>> ID: " + id);

        LOG.debug(">>>>>>>>>>>>>>>>>>>>>>>> Machine: "
                + sessionFactory.getCurrentSession().createCriteria(BasicMachine.class)
                        .add(Restrictions.idEq(Long.parseLong(id))).uniqueResult());

        return (BasicMachine) sessionFactory.getCurrentSession().createCriteria(BasicMachine.class)
                .add(Restrictions.eq("number", id)).uniqueResult();
    }

    private ProductsTechnology getTechnologyByName(final String name) {
        return (ProductsTechnology) sessionFactory.getCurrentSession().createCriteria(ProductsTechnology.class)
                .add(Restrictions.eq("name", name)).setMaxResults(1).uniqueResult();
    }

    private ProductsTechnology getDefaultTechnologyForProduct(final ProductsProduct product) {
        if (product == null) {
            return null;
        }
        return (ProductsTechnology) sessionFactory.getCurrentSession().createCriteria(ProductsTechnology.class)
                .add(Restrictions.eq("product", product)).add(Restrictions.eq("master", true)).setMaxResults(1).uniqueResult();
    }

    private ProductsProduct getProductByNumber(final String number) {
        return (ProductsProduct) sessionFactory.getCurrentSession().createCriteria(ProductsProduct.class)
                .add(Restrictions.eq("number", number)).setMaxResults(1).uniqueResult();
    }

    private UsersGroup getGroupByRole(final String role) {
        return (UsersGroup) sessionFactory.getCurrentSession().createCriteria(UsersGroup.class)
                .add(Restrictions.eq("role", role)).setMaxResults(1).uniqueResult();
    }

    private String getRandomDictionaryItem(final String dictionaryName) {
        DictionariesDictionary dictionary = getDictionaryByName(dictionaryName);
        Long total = (Long) sessionFactory.getCurrentSession().createCriteria(DictionariesDictionaryItem.class)
                .add(Restrictions.eq("dictionary", dictionary)).setProjection(Projections.rowCount()).uniqueResult();
        DictionariesDictionaryItem item = (DictionariesDictionaryItem) sessionFactory.getCurrentSession()
                .createCriteria(DictionariesDictionaryItem.class).add(Restrictions.eq("dictionary", dictionary))
                .setFirstResult(RANDOM.nextInt(total.intValue())).setMaxResults(1).uniqueResult();
        return item.getName();
    }

    private ProductsProduct getRandomProduct() {
        Long total = (Long) sessionFactory.getCurrentSession().createCriteria(ProductsProduct.class)
                .setProjection(Projections.rowCount()).uniqueResult();

        return (ProductsProduct) sessionFactory.getCurrentSession().createCriteria(ProductsProduct.class)
                .setFirstResult(RANDOM.nextInt(total.intValue())).setMaxResults(1).uniqueResult();
    }

    private ProductsOperation getRandomOperation() {
        Long total = (Long) sessionFactory.getCurrentSession().createCriteria(ProductsOperation.class)
                .setProjection(Projections.rowCount()).uniqueResult();
        return (ProductsOperation) sessionFactory.getCurrentSession().createCriteria(ProductsOperation.class)
                .setFirstResult(RANDOM.nextInt(total.intValue())).setMaxResults(1).uniqueResult();
    }

    private ProductsOrder getRandomOrder() {
        Long total = (Long) sessionFactory.getCurrentSession().createCriteria(ProductsOrder.class)
                .setProjection(Projections.rowCount()).uniqueResult();
        return (ProductsOrder) sessionFactory.getCurrentSession().createCriteria(ProductsOrder.class)
                .setFirstResult(RANDOM.nextInt(total.intValue())).setMaxResults(1).uniqueResult();
    }

    private UsersUser getRandomUser() {
        Long total = (Long) sessionFactory.getCurrentSession().createCriteria(UsersUser.class)
                .setProjection(Projections.rowCount()).uniqueResult();
        return (UsersUser) sessionFactory.getCurrentSession().createCriteria(UsersUser.class)
                .setFirstResult(RANDOM.nextInt(total.intValue())).setMaxResults(1).uniqueResult();
    }

    private DictionariesDictionary getDictionaryByName(final String name) {
        return (DictionariesDictionary) sessionFactory.getCurrentSession().createCriteria(DictionariesDictionary.class)
                .add(Restrictions.eq("name", name)).setMaxResults(1).uniqueResult();
    }

    private String getRandomTypeOfMaterial() {
        return TYPE_OF_MATERIALS[RANDOM.nextInt(TYPE_OF_MATERIALS.length)];
    }

    private String getRandomUnit() {
        return UNITS.get(RANDOM.nextInt(UNITS.size()));
    }

}
