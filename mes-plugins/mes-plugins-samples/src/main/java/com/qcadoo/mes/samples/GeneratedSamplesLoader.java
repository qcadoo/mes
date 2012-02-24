/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
import static com.qcadoo.mes.samples.constants.SamplesConstants.BASIC_MODEL_WORKSTATION_TYPE;
import static com.qcadoo.mes.samples.constants.SamplesConstants.BASIC_PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.L_NAME;
import static com.qcadoo.mes.samples.constants.SamplesConstants.L_NUMBER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.ORDERS_MODEL_ORDER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.ORDERS_PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.TECHNOLOGIES_PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.TECHNOLOGY_MODEL_OPERATION;
import static com.qcadoo.mes.samples.constants.SamplesConstants.TECHNOLOGY_MODEL_TECHNOLOGY;
import static java.util.Collections.singletonMap;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.utils.TreeNumberingService;
import com.qcadoo.security.api.SecurityRole;
import com.qcadoo.security.api.SecurityRolesService;

@Component
public class GeneratedSamplesLoader extends SamplesLoader {

    private static final String STATE_L = "state";

    private static final String TECHNOLOGIES_PLUGIN = "technologies";

    private static final String ORDER_GROUP_LITERAL = "orderGroup";

    private static final String CHARS_ONLY = "QWERTYUIOPLKJHGFDSAZXCVBNMmnbvcxzasdfghjklpoiuytrewq";

    private static final String DIGITS_ONLY = "0123456789";

    private static final String CHARS_AND_DIGITS = CHARS_ONLY + DIGITS_ONLY;

    private static final String[] ACCEPTABLE_PRODUCT_TYPE = { "01component", "02intermediate", "03finalProduct", "04waste" };

    private static final String[] WORK_SHIFT = { "mondayWorking", "tuesdayWorking", "wensdayWorking", "thursdayWorking",
            "fridayWorking", "saturdayWorking", "sundayWorking" };

    private static final String[] SHIFT_HOURS = { "mondayHours", "tuesdayHours", "wensdayHours", "thursdayHours", "fridayHours",
            "saturdayHours", "sundayHours", };

    private static final String[] ACCEPTABLE_DICTIONARIES = { "categories", "posts", "units" };

    private static final String TECHNOLOGY_PLUGIN_NAME = TECHNOLOGIES_PLUGIN_IDENTIFIER;

    private static final String ORDER_GROUPS_PLUGIN_NAME = "orderGroups";

    private static final String ORDER_GROUPS_MODEL_ORDER_GROUP = ORDER_GROUP_LITERAL;

    @Autowired
    private SecurityRolesService securityRolesService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TreeNumberingService treeNumberingService;

    @Autowired
    private NumberService numberService;

    @Value("${generatorIterations}")
    private int iterations;

    @Override
    void loadData(final String dataset, final String locale) {
        generateAndAddUser();
        generateAndAddDictionary();
        addParameters(singletonMap("code", "PLN"));
        generateAndAddCompany();
        for (int i = 0; i < iterations; i++) {
            generateAndAddProduct();
            generateAndAddWorkstationType();
            generateAndAddStaff();
        }
        for (int i = 0; i < 10; i++) {
            generateAndAddShift();
        }
        if (isEnabled(TECHNOLOGY_PLUGIN_NAME)) {
            for (int i = 0; i < iterations; i++) {
                generateAndAddOperation();
            }
            generateAndAddTechnologies();
        }
        if (isEnabled(ORDERS_PLUGIN_IDENTIFIER)) {
            for (int i = 0; i < iterations; i++) {
                generateAndAddOrder();
            }
            if (isEnabled(ORDER_GROUPS_PLUGIN_NAME)) {
                for (int i = 0; i < 10; i++) {
                    generateAndAddOrderGroup();
                }
            }
        }
        if (isEnabled("workPlans")) {
            for (int i = 0; i < (iterations / 40); i++) {
                generateAndAddWorkPlan();
            }
        }

    }

    private void generateAndAddWorkPlan() {
        Entity workPlan = dataDefinitionService.get("workPlans", "workPlan").create();

        workPlan.setField(L_NAME,
                getNameFromNumberAndPrefix("WorkPlan-", 5 + generateString(CHARS_AND_DIGITS, RANDOM.nextInt(45))));
        workPlan.setField("date", new Date(generateRandomDate()));
        workPlan.setField("worker",
                getNameFromNumberAndPrefix("Worker-", 5 + generateString(CHARS_AND_DIGITS, RANDOM.nextInt(45))));
        workPlan.setField("generated", false);
        workPlan.setField("type", "01noDistinction");

        workPlan = workPlan.getDataDefinition().save(workPlan);

        List<Entity> allOrders = dataDefinitionService.get("orders", ORDERS_MODEL_ORDER).find().list().getEntities();

        int iters = RANDOM.nextInt(allOrders.size() / 30 + 1);
        for (int i = 0; i < iters; i++) {
            addWorkPlanComponent(workPlan, allOrders);
        }

        validateEntity(workPlan);
    }

    private void addWorkPlanComponent(final Entity workPlan, final List<Entity> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        List<Entity> existingOrders = workPlan.getManyToManyField("orders");
        existingOrders.addAll(orders);
        workPlan.setField("orders", existingOrders);
    }

    private void generateAndAddOrderGroup() {
        Entity orderGroup = dataDefinitionService.get(ORDER_GROUPS_PLUGIN_NAME, ORDER_GROUPS_MODEL_ORDER_GROUP).create();

        final String number = generateString(CHARS_AND_DIGITS, RANDOM.nextInt(34) + 5);

        orderGroup.setField(L_NUMBER, number);
        orderGroup.setField(L_NAME, getNameFromNumberAndPrefix("OrderGroup-", number));

        orderGroup = orderGroup.getDataDefinition().save(orderGroup);

        addOrdersToOrderGroup(orderGroup);

        validateEntity(orderGroup);
    }

    private void addOrdersToOrderGroup(final Entity orderGroup) {
        List<Entity> orders;
        SearchCriteriaBuilder searchBuilder = dataDefinitionService.get(ORDERS_PLUGIN_IDENTIFIER, ORDERS_MODEL_ORDER).find();
        int ordersLeft = searchBuilder.add(SearchRestrictions.isNull(ORDER_GROUP_LITERAL)).list().getTotalNumberOfEntities();
        if (ordersLeft >= 0) {
            orders = searchBuilder.add(SearchRestrictions.isNull(ORDER_GROUP_LITERAL)).setMaxResults(10).list().getEntities();
            for (Entity order : orders) {
                order.setField(ORDER_GROUP_LITERAL, orderGroup);
                order.setField("doneQuantity", RANDOM.nextInt(10) + 1);
                order.getDataDefinition().save(order);
                validateEntity(order);
            }
        }
    }

    private void generateAndAddTechnologies() {
        List<Entity> products = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_PRODUCT).find().list()
                .getEntities();
        for (Entity product : products) {
            generateAndAddTechnology(product);
        }
    }

    private void generateAndAddOperationProductOutComponent(final Entity operationComponent, final BigDecimal quantity,
            final Entity product) {

        Preconditions.checkArgument(operationComponent != null, "operation component is null");

        Entity productComponent = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "operationProductOutComponent")
                .create();

        productComponent.setField("operationComponent", operationComponent);
        productComponent.setField(BASIC_MODEL_PRODUCT, product);
        productComponent.setField("quantity", quantity);

        productComponent = productComponent.getDataDefinition().save(productComponent);

        operationComponent.setField("operationProductOutComponents", productComponent);

        validateEntity(operationComponent);
        validateEntity(productComponent);
    }

    private void generateAndAddOperation() {
        Entity operation = dataDefinitionService.get(TECHNOLOGIES_PLUGIN, TECHNOLOGY_MODEL_OPERATION).create();

        String number = generateString(CHARS_ONLY, RANDOM.nextInt(40) + 5);

        operation.setField("number", number);
        operation.setField("name", getNameFromNumberAndPrefix("Operation-", number));
        operation.setField(BASIC_MODEL_STAFF, getRandomStaff());
        operation.setField(BASIC_MODEL_WORKSTATION_TYPE, getRandomMachine());

        operation.setField("tpz", RANDOM.nextInt(1000));
        operation.setField("tj", RANDOM.nextInt(1000));
        operation.setField("productionInOneCycle", RANDOM.nextInt(20));
        operation.setField("countRealized", RANDOM.nextInt(10));
        operation.setField("machineUtilization", numberService.setScale(new BigDecimal(RANDOM.nextDouble()).abs()));
        operation.setField("laborUtilization", numberService.setScale(new BigDecimal(RANDOM.nextDouble()).abs()));
        operation.setField("countMachineOperation", RANDOM.nextInt(15));
        operation.setField("countRealizedOperation", "01all");
        operation.setField("timeNextOperation", RANDOM.nextInt(30));
        operation.setField("countMachine", "0");

        if (isEnabled("costNormsForOperation")) {
            operation.setField("pieceworkCost", RANDOM.nextInt(100));
            operation.setField("machineHourlyCost", RANDOM.nextInt(100));
            operation.setField("laborHourlyCost", RANDOM.nextInt(100));
            operation.setField("numberOfOperations", RANDOM.nextInt(10) + 1);
        }
        operation = dataDefinitionService.get(TECHNOLOGIES_PLUGIN, TECHNOLOGY_MODEL_OPERATION).save(operation);

        validateEntity(operation);

    }

    private Entity getRandomMachine() {
        return getRandomEntity("basic", BASIC_MODEL_WORKSTATION_TYPE);
    }

    private Object getRandomStaff() {
        return getRandomEntity("basic", BASIC_MODEL_STAFF);
    }

    private void generateAndAddTechnology(final Entity product) {
        Entity technology = dataDefinitionService.get(TECHNOLOGIES_PLUGIN, TECHNOLOGY_MODEL_TECHNOLOGY).create();

        Entity defaultTechnology = getDefaultTechnologyForProduct(product);

        String number = generateString(DIGITS_ONLY, RANDOM.nextInt(40) + 5);

        technology.setField("master", defaultTechnology == null);
        technology.setField(L_NAME, getNameFromNumberAndPrefix("Technology-", number));
        technology.setField(L_NUMBER, number);
        technology.setField(BASIC_MODEL_PRODUCT, product);
        technology.setField(STATE_L, "01draft");
        technology.setField("batchRequired", true);
        technology.setField("postFeatureRequired", false);
        technology.setField("otherFeatureRequired", false);
        technology.setField("shiftFeatureRequired", false);
        technology.setField("minimalQuantity", RANDOM.nextInt(40) + 10);
        technology.setField("technologyBatchRequired", false);

        technology.setField("qualityControlType", "02forUnit");
        technology.setField("unitSamplingNr", "123");
        technology.setField("qualityControlInstruction", "asd23");

        technology = dataDefinitionService.get(TECHNOLOGIES_PLUGIN, TECHNOLOGY_MODEL_TECHNOLOGY).save(technology);
        validateEntity(technology);

        generateAndAddTechnologyOperationComponent(technology);

        treeNumberingService.generateNumbersAndUpdateTree(
                dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "technologyOperationComponent"),
                TECHNOLOGY_MODEL_TECHNOLOGY, technology.getId());

        technology.setField(STATE_L, "02accepted");
        technology = dataDefinitionService.get(TECHNOLOGIES_PLUGIN, TECHNOLOGY_MODEL_TECHNOLOGY).save(technology);
        validateEntity(technology);
    }

    private Entity addOperationComponent(final Entity technology, final Entity parent, final Entity operation,
            final int productsComponentsQuantity) {
        Preconditions.checkNotNull(technology, "Technology entity is null");
        Entity operationComponent = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "technologyOperationComponent")
                .create();

        int productInComponentQuantity = RANDOM.nextInt(productsComponentsQuantity);
        int productOutComponentQuantity = productsComponentsQuantity - productInComponentQuantity;

        operationComponent.setField("name", "operationComponent" + generateString(CHARS_AND_DIGITS, 15));
        operationComponent.setField("number", generateString(CHARS_AND_DIGITS, 20));
        operationComponent.setField(TECHNOLOGY_MODEL_TECHNOLOGY, technology);
        operationComponent.setField("parent", parent);
        operationComponent.setField(TECHNOLOGY_MODEL_OPERATION, operation);
        operationComponent.setField("entityType", TECHNOLOGY_MODEL_OPERATION);
        operationComponent.setField("tpz", operation.getField("tpz"));
        operationComponent.setField("tj", operation.getField("tj"));
        operationComponent.setField("machineUtilization", operation.getField("machineUtilization"));
        operationComponent.setField("laborUtilization", operation.getField("laborUtilization"));
        operationComponent.setField("productionInOneCycle", operation.getField("productionInOneCycle"));
        operationComponent.setField("countRealized", operation.getField("countRealized"));
        operationComponent.setField("countMachine", "0");
        operationComponent.setField("timeNextOperation", operation.getField("timeNextOperation"));

        operationComponent = operationComponent.getDataDefinition().save(operationComponent);
        validateEntity(operationComponent);

        for (int i = 0; i < productOutComponentQuantity; i++) {
            generateAndAddOperationProductOutComponent(operationComponent, new BigDecimal(RANDOM.nextInt(50) + 5),
                    getRandomProduct());
        }
        for (int i = 0; i < productInComponentQuantity; i++) {
            generateAndAddOperationProductInComponent(operationComponent, new BigDecimal(RANDOM.nextInt(50) + 5),
                    getRandomProduct());
        }

        return operationComponent;
    }

    private void generateAndAddOperationProductInComponent(final Entity operationComponent, final BigDecimal quantity,
            final Entity product) {
        Entity productComponent = dataDefinitionService.get(TECHNOLOGIES_PLUGIN_IDENTIFIER, "operationProductInComponent")
                .create();

        productComponent.setField("operationComponent", operationComponent);
        productComponent.setField(BASIC_MODEL_PRODUCT, product);
        productComponent.setField("quantity", quantity);
        productComponent.setField("batchRequired", true);
        productComponent.setField("productBatchRequired", true);

        productComponent = productComponent.getDataDefinition().save(productComponent);

        operationComponent.setField("operationProductInComponents", productComponent);

        validateEntity(productComponent);
    }

    private void generateAndAddTechnologyOperationComponent(final Entity technology) {
        List<Entity> operations = new LinkedList<Entity>();
        Entity operation = null;
        for (int i = 0; i < 4; i++) {
            if (operations.isEmpty()) {
                operation = addOperationComponent(technology, null, getRandomOperation(), RANDOM.nextInt(3) + 3);
            } else {
                operation = addOperationComponent(technology, operations.get(RANDOM.nextInt(operations.size())),
                        getRandomOperation(), RANDOM.nextInt(3) + 3);
            }
            operations.add(operation);
        }
    }

    private Entity getRandomEntity(final String pluginIdentifier, final String modelName) {
        SearchCriteriaBuilder searchBuilder = dataDefinitionService.get(pluginIdentifier, modelName).find();
        int totalNumberOfEntities = searchBuilder.list().getTotalNumberOfEntities();
        return searchBuilder.setMaxResults(1).setFirstResult(RANDOM.nextInt(totalNumberOfEntities)).uniqueResult();
    }

    private Entity getRandomOperation() {
        return getRandomEntity(TECHNOLOGIES_PLUGIN_IDENTIFIER, TECHNOLOGY_MODEL_OPERATION);
    }

    private void generateAndAddDictionary() {
        for (int i = 0; i < ACCEPTABLE_DICTIONARIES.length; i++) {
            generateDictionaryItem(ACCEPTABLE_DICTIONARIES[i]);
        }
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
        }
        return technologies.get(0);
    }

    private void generateAndAddOrder() {
        Entity order = dataDefinitionService.get(ORDERS_PLUGIN_IDENTIFIER, ORDERS_MODEL_ORDER).create();

        long dateFrom = generateRandomDate();
        long dateTo = generateRandomDate(dateFrom);

        Preconditions.checkArgument(dateTo > dateFrom, "Order was finished before it was started !");

        Entity product = getRandomProduct();
        Entity technology = (getDefaultTechnologyForProduct(product) == null) ? getRandomProduct()
                : getDefaultTechnologyForProduct(product);

        String number = generateString(CHARS_AND_DIGITS, RANDOM.nextInt(34) + 5);
        order.setField(L_NUMBER, number);
        order.setField(L_NAME, getNameFromNumberAndPrefix("Order-", number));
        order.setField("dateFrom", new Date(dateFrom));
        order.setField("dateTo", new Date(dateTo));
        order.setField(STATE_L, "01pending");
        order.setField(BASIC_MODEL_PRODUCT, product);
        order.setField("plannedQuantity", RANDOM.nextInt(100) + 100);
        order.setField("doneQuantity", RANDOM.nextInt(100) + 1);
        order.setField(TECHNOLOGY_MODEL_TECHNOLOGY, technology);
        order.setField("externalSynchronized", true);
        order.setField("typeOfProductionRecording", "01basic");
        order.setField("trackingRecordTreatment", "01duringProduction");

        order = dataDefinitionService.get(ORDERS_PLUGIN_IDENTIFIER, ORDERS_MODEL_ORDER).save(order);

        validateEntity(order);
    }

    private Long generateRandomDate(final Long dateFrom) {
        long dateOffset = RANDOM.nextInt();
        return dateFrom + ((dateOffset > 0) ? dateOffset : -dateOffset);
    }

    private Long generateRandomDate() {
        long date = new Date().getTime() - RANDOM.nextInt();
        return ((date > 0) ? date : -date);
    }

    private void generateAndAddStaff() {
        Entity staff = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_STAFF).create();

        String number = generateString(DIGITS_ONLY, RANDOM.nextInt(40) + 5);

        staff.setField(L_NUMBER, number);
        staff.setField(L_NAME, getNameFromNumberAndPrefix("Staff-", number));
        staff.setField("surname", generateString(CHARS_ONLY, RANDOM.nextInt(12)));
        staff.setField("post", generateString(CHARS_ONLY, RANDOM.nextInt(5)));

        staff = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_STAFF).save(staff);
        validateEntity(staff);
    }

    private String getNameFromNumberAndPrefix(final String prefix, final String number) {
        StringBuilder nameBuilder = new StringBuilder();

        nameBuilder.append(prefix).append(number);

        return nameBuilder.toString();
    }

    private void generateAndAddWorkstationType() {
        Entity machine = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_WORKSTATION_TYPE).create();

        String number = generateString(CHARS_AND_DIGITS, RANDOM.nextInt(40) + 5);

        machine.setField(L_NAME, getNameFromNumberAndPrefix("Workstation type-", number));
        machine.setField(L_NUMBER, number);
        machine.setField("description", generateString(CHARS_ONLY, RANDOM.nextInt(100)));

        machine = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_WORKSTATION_TYPE).save(machine);
        validateEntity(machine);
    }

    private String generateWorkingHours() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long minHours = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long maxHours = calendar.getTimeInMillis();
        long workBeginHours = (long) (RANDOM.nextDouble() * (maxHours / 2 - minHours) + minHours);
        long workEndHours = (long) (RANDOM.nextDouble() * (maxHours - workBeginHours) + workBeginHours);

        Date workBeginDate = new Date(workBeginHours);
        Date workEndDate = new Date(workEndHours);
        StringBuilder workingHours = new StringBuilder();
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
        workingHours.append(hourFormat.format(workBeginDate)).append("-").append(hourFormat.format(workEndDate));
        return workingHours.toString();
    }

    private void generateAndAddShift() {
        Entity shift = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, "shift").create();

        shift.setField(L_NAME, getNameFromNumberAndPrefix("Shift-", generateString(CHARS_ONLY, RANDOM.nextInt(40) + 5)));

        for (int i = 0; i < SHIFT_HOURS.length; i++) {
            shift.setField(WORK_SHIFT[i], RANDOM.nextBoolean());
            shift.setField(SHIFT_HOURS[i], generateWorkingHours());
        }

        shift = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, "shift").save(shift);

        validateEntity(shift);
    }

    private void generateAndAddProduct() {
        Entity product = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_PRODUCT).create();

        String number = generateString(DIGITS_ONLY, RANDOM.nextInt(34) + 5);

        product.setField("category", getRandomDictionaryItem("categories"));
        product.setField("ean", generateString(DIGITS_ONLY, 13));
        product.setField(L_NAME, getNameFromNumberAndPrefix("Product-", number));
        product.setField("unit", getRandomDictionaryItem("units"));
        product.setField("globalTypeOfMaterial", generateTypeOfProduct());
        product.setField(L_NUMBER, number);

        product = product.getDataDefinition().save(product);

        validateEntity(product);

        addSubstituteToProduct(product);
    }

    private void addSubstituteToProduct(final Entity product) {
        Entity substitute = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, "substitute").create();

        String number = generateString(DIGITS_ONLY, RANDOM.nextInt(34) + 5);

        substitute.setField(L_NUMBER, number);
        substitute.setField(L_NAME, getNameFromNumberAndPrefix("ProductSubstitute-", number));
        substitute.setField(BASIC_MODEL_PRODUCT, product);
        substitute.setField("priority", RANDOM.nextInt(7));

        substitute = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, "substitute").save(substitute);

        validateEntity(substitute);

        // for(int i = 0; i < 5; i++)
        addSubstituteComponent(substitute, getRandomProduct(), RANDOM.nextInt(997) * RANDOM.nextDouble());
    }

    private Entity getRandomProduct() {
        return getRandomEntity(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_PRODUCT);
    }

    private void addSubstituteComponent(final Entity substitute, final Entity product, final double quantity) {
        Entity substituteComponent = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, "substituteComponent").create();

        substituteComponent.setField("quantity", numberService.setScale(new BigDecimal(quantity + 1).abs()));
        substituteComponent.setField(BASIC_MODEL_PRODUCT, product);
        substituteComponent.setField("substitute", substitute);

        substituteComponent = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, "substituteComponent").save(substituteComponent);

        validateEntity(substituteComponent);
    }

    private void generateAndAddUser() {
        Entity user = dataDefinitionService.get("qcadooSecurity", "user").create();

        user.setField("userName", generateString(CHARS_ONLY, RANDOM.nextInt(4) + 3));
        user.setField("email", generateRandomEmail());
        user.setField("firstname", generateString(CHARS_ONLY, RANDOM.nextInt(4) + 3));
        user.setField("lastname", generateString(CHARS_ONLY, RANDOM.nextInt(4) + 3));
        SecurityRole role = securityRolesService.getRoleByIdentifier("ROLE_USER");
        user.setField("role", role.getName());
        user.setField("password", "123");
        user.setField("passwordConfirmation", "123");
        user.setField("enabled", true);

        user = dataDefinitionService.get("qcadooSecurity", "user").save(user);

        validateEntity(user);
    }

    private void generateAndAddCompany() {
        Map<String, String> values = new HashMap<String, String>();
        values.put(L_NUMBER, generateString(DIGITS_ONLY, RANDOM.nextInt(34) + 5));
        values.put(L_NAME, generateString(CHARS_ONLY, RANDOM.nextInt(4) + 3));
        values.put("tax", generateString(CHARS_ONLY, RANDOM.nextInt(4) + 3));
        values.put("street", generateString(CHARS_ONLY, RANDOM.nextInt(4) + 3));
        values.put("house", generateString(CHARS_ONLY, RANDOM.nextInt(4) + 3));
        values.put("flat", generateString(CHARS_ONLY, RANDOM.nextInt(4) + 3));
        values.put("zipCode", generateString(CHARS_ONLY, RANDOM.nextInt(4) + 3));
        values.put("city", generateString(CHARS_ONLY, RANDOM.nextInt(4) + 3));
        values.put(STATE_L, generateString(CHARS_ONLY, RANDOM.nextInt(4) + 3));
        values.put("country", generateString(CHARS_ONLY, RANDOM.nextInt(4) + 3));
        values.put("email", generateRandomEmail());
        values.put("website", generateString(CHARS_ONLY, RANDOM.nextInt(4) + 3));
        values.put("phone", generateString(CHARS_ONLY, RANDOM.nextInt(4) + 3));
        values.put("owner", generateString(CHARS_ONLY, RANDOM.nextInt(4) + 3));

        addCompany(values);
    }

    private String generateRandomEmail() {
        String email;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(generateString(CHARS_AND_DIGITS, RANDOM.nextInt(3) + 3));
        stringBuilder.append("@").append(generateString(CHARS_AND_DIGITS, 4)).append(".");
        stringBuilder.append("org");
        email = stringBuilder.toString();
        return email;
    }

    private void generateDictionaryItem(final String name) {
        Entity dictionary = getDictionaryByName(name);

        Entity item = dataDefinitionService.get("qcadooModel", "dictionaryItem").create();
        item.setField("dictionary", dictionary);
        item.setField(L_NAME, generateString(CHARS_ONLY, 8));

        item = dataDefinitionService.get("qcadooModel", "dictionaryItem").save(item);

        validateEntity(item);
    }

    private String generateTypeOfProduct() {
        return ACCEPTABLE_PRODUCT_TYPE[RANDOM.nextInt(ACCEPTABLE_PRODUCT_TYPE.length)];
    }

    private String generateString(final String allowedChars, final int stringLength) {
        int stringLen = stringLength;
        String generatedString;
        if (stringLen <= 0) {
            stringLen = 1;
        }
        char[] chars = new char[stringLen];
        for (int i = 0; i < stringLen; i++) {
            chars[i] = allowedChars.charAt(RANDOM.nextInt(allowedChars.length()));
        }
        generatedString = new String(chars);
        return generatedString;
    }

}
