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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.orderGroups.constants.OrderGroupsConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.utils.TreeNumberingService;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.plugin.api.Module;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.security.api.SecurityRole;
import com.qcadoo.security.api.SecurityRolesService;

@Component
public class SamplesGeneratorModule extends Module {

    private static final String charsOnly = "QWERTYUIOPLKJHGFDSAZXCVBNMmnbvcxzasdfghjklpoiuytrewq";

    private static final String digitsOnly = "0123456789";

    private static final String charsAndDigits = charsOnly + digitsOnly;

    private static final String[] acceptableTypeOfProduct = { "01component", "02intermediate", "03product", "04waste" };

    private static final String[] shiftsWorking = { "mondayWorking", "tuesdayWorking", "wensdayWorking", "thursdayWorking",
            "fridayWorking", "saturdayWorking", "sundayWorking" };

    private static final String[] shiftsHours = { "mondayHours", "tuesdayHours", "wensdayHours", "thursdayHours", "fridayHours",
            "saturdayHours", "sundayHours", };

    private static final String[] acceptableDictionaries = { "categories", "posts", "units" };

    private static final String[] acceptableOrderState = { "01pending", "02accepted", "03inProgress", "04completed",
            "05declined", "06interrupted", "07abandoned" };

    private static final String[] acceptableTechnologyComponentQuantityAlgorithm = { "01perProductOut", "02perTechnology" };

    private static final Random RANDOM = new Random();

    @Autowired
    private PluginAccessor pluginAccessor;

    @Autowired
    private SecurityRolesService securityRolesService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TreeNumberingService treeNumberingService;

    @Value("${generatorIterations}")
    int iterations;

    @Override
    @Transactional
    public void multiTenantEnable() {

        addParameters();
        generateAndAddUser();
        int range = iterations;
        generateAndAddDictionary();
        if (isEnabled("basic")) {
            for (int i = 0; i < range; i++) {
                generateAndAddProduct();
                generateAndAddMachine();
                generateAndAddContractor();
                generateAndAddStaff();
            }
            for (int i = 0; i < 10; i++) {
                generateAndAddShift();
            }
        }
        if (isEnabled(TechnologiesConstants.PLUGIN_IDENTIFIER)) {
            for (int i = 0; i < range; i++) {
                generateAndAddOperation();
            }
            generateAndAddTechnologies();
        }
        if (isEnabled(OrdersConstants.PLUGIN_IDENTIFIER)) {
            for (int i = 0; i < range; i++) {
                generateAndAddOrder();
            }
            if (isEnabled(OrderGroupsConstants.PLUGIN_IDENTIFIER)) {
                for (int i = 0; i < 10; i++) {
                    generateAndAddOrderGroup();
                }
            }
        }
        if (isEnabled("usedProducts")) {
            for (int i = 0; i < range; i++) {
                generateAndAddUsedProduct();
            }
        }
        if (isEnabled("workPlans")) {
            for (int i = 0; i < (range / 40); i++) {
                generateAndAddWorkPlan();
            }
        }
        if (isEnabled("producedProducts")) {
            for (int i = 0; i < range; i++) {
                generateAndAddProducedProducts();
            }
        }

    }

    private void generateAndAddProducedProducts() {
        Entity producedProduct = dataDefinitionService.get("producedProducts", "producedProducts").create();

        producedProduct.setField("product", getRandomProduct());
        producedProduct.setField("order", getRandomOrder());
        producedProduct.setField("plannedQuantity", RANDOM.nextInt(20) + 20);
        producedProduct.setField("producedQuantity", RANDOM.nextInt(20));

        producedProduct = producedProduct.getDataDefinition().save(producedProduct);

        validateEntity(producedProduct);
    }

    private void generateAndAddWorkPlan() {
        Entity workPlan = dataDefinitionService.get("workPlans", "workPlan").create();

        workPlan.setField("name", getNameFromNumberAndPrefix("WorkPlan-", 5 + generateString(charsAndDigits, RANDOM.nextInt(45))));
        workPlan.setField("date", new Date(generateRandomDate()));
        workPlan.setField("worker", getNameFromNumberAndPrefix("Worker-", 5 + generateString(charsAndDigits, RANDOM.nextInt(45))));
        workPlan.setField("generated", false);

        workPlan = workPlan.getDataDefinition().save(workPlan);

        List<Entity> allOrders = dataDefinitionService.get("orders", "order").find().list().getEntities();

        int iters = RANDOM.nextInt(allOrders.size() / 30 + 1);
        for (int i = 0; i < iters; i++) {
            addWorkPlanComponent(workPlan, allOrders);
        }

        validateEntity(workPlan);
    }

    private void addWorkPlanComponent(final Entity workPlan, final List<Entity> orders) {
        Entity workPlanComponent = dataDefinitionService.get("workPlans", "workPlanComponent").create();

        Entity order = orders.get(0);
        orders.remove(order);
        workPlanComponent.setField("workPlan", workPlan);
        workPlanComponent.setField("order", order);

        workPlanComponent = workPlanComponent.getDataDefinition().save(workPlanComponent);

        validateEntity(workPlanComponent);
    }

    private void generateAndAddOrderGroup() {
        Entity orderGroup = dataDefinitionService.get(OrderGroupsConstants.PLUGIN_IDENTIFIER,
                OrderGroupsConstants.MODEL_ORDERGROUP).create();

        String number = generateString(charsAndDigits, RANDOM.nextInt(34) + 5);

        orderGroup.setField("number", number);
        orderGroup.setField("name", getNameFromNumberAndPrefix("OrderGroup-", number));

        orderGroup = orderGroup.getDataDefinition().save(orderGroup);

        addOrdersToOrderGroup(orderGroup);

        validateEntity(orderGroup);
    }

    private void addOrdersToOrderGroup(final Entity orderGroup) {
        List<Entity> orders;
        SearchCriteriaBuilder searchBuilder = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.MODEL_ORDER).find();
        int ordersLeft = searchBuilder.add(SearchRestrictions.isNull("orderGroup")).list().getTotalNumberOfEntities();
        if (ordersLeft >= 0) {
            orders = searchBuilder.add(SearchRestrictions.isNull("orderGroup")).setMaxResults(10).list().getEntities();
            for (Entity order : orders) {
                order.setField("orderGroup", orderGroup);
                order.setField("doneQuantity", RANDOM.nextInt(10) + 1);
                order.getDataDefinition().save(order);
                validateEntity(order);
            }
        }
    }

    private void generateAndAddTechnologies() {
        List<Entity> products = dataDefinitionService.get("basic", "product").find().list().getEntities();
        for (Entity product : products) {
            generateAndAddTechnology(product);
        }
    }

    private void generateAndAddOperationProductOutComponent(Entity operationComponent, final BigDecimal quantity,
            final Entity product) {

        Preconditions.checkArgument(operationComponent != null, "operation component is null");

        Entity productComponent = dataDefinitionService.get("technologies", "operationProductOutComponent").create();

        productComponent.setField("operationComponent", operationComponent);
        productComponent.setField("product", product);
        productComponent.setField("quantity", quantity);

        productComponent = productComponent.getDataDefinition().save(productComponent);

        operationComponent.setField("operationProductOutComponents", productComponent);

        validateEntity(operationComponent);
        validateEntity(productComponent);
    }

    private void generateAndAddOperation() {
        Entity operation = dataDefinitionService.get("technologies", "operation").create();

        String number = generateString(charsOnly, RANDOM.nextInt(40) + 5);

        operation.setField("number", number);
        operation.setField("name", getNameFromNumberAndPrefix("Operation-", number));
        operation.setField("staff", getRandomStaff());
        operation.setField("machine", getRandomMachine());

        operation.setField("tpz", RANDOM.nextInt(1000));
        operation.setField("tj", RANDOM.nextInt(1000));
        operation.setField("productionInOneCycle", RANDOM.nextInt(20));
        operation.setField("countRealized", RANDOM.nextInt(10));
        operation.setField("machineUtilization", new BigDecimal(RANDOM.nextDouble()).abs().setScale(3, RoundingMode.HALF_EVEN));
        operation.setField("laborUtilization", new BigDecimal(RANDOM.nextDouble()).abs().setScale(3, RoundingMode.HALF_EVEN));
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
        operation = dataDefinitionService.get("technologies", "operation").save(operation);

        validateEntity(operation);

    }

    private Entity getRandomMachine() {
        return getRandomEntity("basic", "machine");
    }

    private Object getRandomStaff() {
        return getRandomEntity("basic", "staff");
    }

    private void validateEntity(final Entity entity) {
        if (!entity.isValid()) {
            Map<String, ErrorMessage> errors = entity.getErrors();
            Set<String> keys = errors.keySet();
            StringBuilder stringError = new StringBuilder();
            for (String key : keys) {
                stringError.append("\t").append(key).append("  -  ").append(errors.get(key).getMessage()).append("\n");
            }
            Map<String, Object> fields = entity.getFields();
            for (Entry<String, Object> entry : fields.entrySet()) {
                if (entry.getValue() == null) {
                    stringError.append("\t\t");
                }
                stringError.append(entry.getKey()).append(" - ").append(entry.getValue()).append("\n");
            }
            throw new IllegalStateException("Saved entity is invalid\n" + stringError.toString());
        }
    }

    private void generateAndAddTechnology(final Entity product) {
        Entity technology = dataDefinitionService.get("technologies", "technology").create();

        Entity defaultTechnology = getDefaultTechnologyForProduct(product);

        String number = generateString(digitsOnly, RANDOM.nextInt(40) + 5);

        technology.setField("master", defaultTechnology == null);
        technology.setField("name", getNameFromNumberAndPrefix("Technology-", number));
        technology.setField("number", number);
        technology.setField("product", product);
        technology.setField("batchRequired", true);
        technology.setField("postFeatureRequired", false);
        technology.setField("otherFeatureRequired", false);
        technology.setField("shiftFeatureRequired", false);
        technology.setField("minimalQuantity", RANDOM.nextInt(40) + 10);

        technology.setField("qualityControlType", "02forUnit");
        technology.setField("unitSamplingNr", "123");
        technology.setField("qualityControlInstruction", "asd23");

        technology.setField("componentQuantityAlgorithm", acceptableTechnologyComponentQuantityAlgorithm[RANDOM
                .nextInt(acceptableTechnologyComponentQuantityAlgorithm.length)]);

        technology = dataDefinitionService.get("technologies", "technology").save(technology);
        validateEntity(technology);

        generateAndAddTechnologyOperationComponent(technology);

        treeNumberingService.generateNumbersAndUpdateTree(
                dataDefinitionService.get("technologies", "technologyOperationComponent"), "technology", technology.getId());
    }

    private Entity addOperationComponent(final Entity technology, final Entity parent, Entity operation,
            final int productsComponentsQuantity) {
        Preconditions.checkNotNull(technology, "Technology entity is null");
        Entity operationComponent = dataDefinitionService.get("technologies", "technologyOperationComponent").create();

        int productInComponentQuantity = RANDOM.nextInt(productsComponentsQuantity);
        int productOutComponentQuantity = productsComponentsQuantity - productInComponentQuantity;

        operationComponent.setField("name", "operationComponent" + generateString(charsAndDigits, 15));
        operationComponent.setField("number", generateString(charsAndDigits, 20));
        operationComponent.setField("technology", technology);
        operationComponent.setField("parent", parent);
        operationComponent.setField("operation", operation);
        operationComponent.setField("entityType", "operation");
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
        Entity productComponent = dataDefinitionService.get("technologies", "operationProductInComponent").create();

        productComponent.setField("operationComponent", operationComponent);
        productComponent.setField("product", product);
        productComponent.setField("quantity", quantity);

        productComponent = productComponent.getDataDefinition().save(productComponent);

        operationComponent.setField("operationProductInComponents", productComponent);

        validateEntity(productComponent);
    }

    private void generateAndAddTechnologyOperationComponent(final Entity technology) {
        List<Entity> operations = new LinkedList<Entity>();
        Entity operation = null;
        for (int i = 0; i < 4; i++) {
            if (!operations.isEmpty()) {
                operation = addOperationComponent(technology, operations.get(RANDOM.nextInt(operations.size())),
                        getRandomOperation(), RANDOM.nextInt(3) + 3);
            } else {
                operation = addOperationComponent(technology, null, getRandomOperation(), RANDOM.nextInt(3) + 3);
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
        return getRandomEntity("technologies", "operation");
    }

    private void generateAndAddDictionary() {
        for (int i = 0; i < acceptableDictionaries.length; i++) {
            addDictionary(acceptableDictionaries[i]);
        }
    }

    private void generateAndAddUsedProduct() {
        Entity usedProduct = dataDefinitionService.get("usedProducts", "usedProducts").create();

        usedProduct.setField("order", getRandomOrder());
        usedProduct.setField("product", getRandomProduct());
        usedProduct.setField("plannedQuantity", RANDOM.nextInt(200) + 1);
        usedProduct.setField("usedQuantity", RANDOM.nextInt(200) + 1);

        usedProduct = dataDefinitionService.get("usedProducts", "usedProducts").save(usedProduct);

        validateEntity(usedProduct);
    }

    private void generateAndAddContractor() {
        Entity contractor = dataDefinitionService.get("basic", "contractor").create();

        String number = generateString(digitsOnly, RANDOM.nextInt(40) + 5);

        contractor.setField("externalNumber", generateString(charsAndDigits, 10));
        contractor.setField("number", number);
        contractor.setField("name", getNameFromNumberAndPrefix("Contractor-", number));

        contractor = dataDefinitionService.get("basic", "contractor").save(contractor);

        validateEntity(contractor);
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

    private void generateAndAddOrder() {
        Entity order = dataDefinitionService.get("orders", "order").create();

        long dateFrom = generateRandomDate();
        long dateTo = generateRandomDate(dateFrom);

        Preconditions.checkArgument(dateTo > dateFrom, "Order was finished before it was started !");

        Entity product = getRandomProduct();
        Entity technology = (getDefaultTechnologyForProduct(product) == null) ? getRandomProduct()
                : getDefaultTechnologyForProduct(product);

        String number = generateString(charsAndDigits, RANDOM.nextInt(34) + 5);
        order.setField("number", number);
        order.setField("name", getNameFromNumberAndPrefix("Order-", number));
        order.setField("dateFrom", new Date(dateFrom));
        order.setField("dateTo", new Date(dateTo));
        order.setField("state", "01pending");
        order.setField("contractor", getRandomContractor());
        order.setField("product", product);
        order.setField("plannedQuantity", RANDOM.nextInt(100) + 100);
        order.setField("doneQuantity", RANDOM.nextInt(100) + 1);
        order.setField("technology", technology);
        order.setField("externalSynchronized", true);
        order.setField("typeOfProductionRecording", "01basic");

        order = dataDefinitionService.get("orders", "order").save(order);

        validateEntity(order);
    }

    private Entity getRandomContractor() {
        return getRandomEntity("basic", "contractor");
    }

    private Long generateRandomDate(final Long dateFrom) {
        long dateOffset = RANDOM.nextInt();
        return dateFrom + ((dateOffset > 0) ? dateOffset : -dateOffset);
    }

    private Long generateRandomDate() {
        long date = new Date().getTime() - RANDOM.nextInt();
        return ((date > 0) ? date : -date);
    }

    private Entity getRandomOrder() {
        return getRandomEntity("orders", "order");
    }

    private void generateAndAddStaff() {
        Entity staff = dataDefinitionService.get("basic", "staff").create();

        String number = generateString(digitsOnly, RANDOM.nextInt(40) + 5);

        staff.setField("number", number);
        staff.setField("name", getNameFromNumberAndPrefix("Staff-", number));
        staff.setField("surname", generateString(charsOnly, RANDOM.nextInt(12)));
        staff.setField("post", generateString(charsOnly, RANDOM.nextInt(5)));

        staff = dataDefinitionService.get("basic", "staff").save(staff);
        validateEntity(staff);
    }

    private String getNameFromNumberAndPrefix(final String prefix, final String number) {
        StringBuilder nameBuilder = new StringBuilder();

        nameBuilder.append(prefix).append(number);

        return nameBuilder.toString();
    }

    private void generateAndAddMachine() {
        Entity machine = dataDefinitionService.get("basic", "machine").create();

        String number = generateString(charsAndDigits, RANDOM.nextInt(40) + 5);

        machine.setField("name", getNameFromNumberAndPrefix("Machine-", number));
        machine.setField("number", number);
        machine.setField("description", generateString(charsOnly, RANDOM.nextInt(100)));

        machine = dataDefinitionService.get("basic", "machine").save(machine);
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
        Entity shift = dataDefinitionService.get("basic", "shift").create();

        shift.setField("name", getNameFromNumberAndPrefix("Shift-", generateString(charsOnly, RANDOM.nextInt(40) + 5)));

        for (int i = 0; i < shiftsHours.length; i++) {
            shift.setField(shiftsWorking[i], RANDOM.nextBoolean());
            shift.setField(shiftsHours[i], generateWorkingHours());
        }

        shift = dataDefinitionService.get("basic", "shift").save(shift);

        validateEntity(shift);
    }

    private void generateAndAddProduct() {
        Entity product = dataDefinitionService.get("basic", "product").create();

        String number = generateString(digitsOnly, RANDOM.nextInt(34) + 5);

        product.setField("category", getRandomDictionaryItem("categories"));
        product.setField("ean", generateString(digitsOnly, 13));
        product.setField("name", getNameFromNumberAndPrefix("Product-", number));
        product.setField("unit", getRandomDictionaryItem("units"));
        product.setField("typeOfMaterial", generateTypeOfProduct());
        product.setField("number", number);

        product = product.getDataDefinition().save(product);

        validateEntity(product);

        addSubstituteToProduct(product);
    }

    private void addSubstituteToProduct(final Entity product) {
        Entity substitute = dataDefinitionService.get("basic", "substitute").create();

        String number = generateString(digitsOnly, RANDOM.nextInt(34) + 5);

        substitute.setField("number", number);
        substitute.setField("name", getNameFromNumberAndPrefix("ProductSubstitute-", number));
        substitute.setField("product", product);
        substitute.setField("priority", RANDOM.nextInt(7));

        substitute = dataDefinitionService.get("basic", "substitute").save(substitute);

        validateEntity(substitute);

        // for(int i = 0; i < 5; i++)
        addSubstituteComponent(substitute, getRandomProduct(), RANDOM.nextInt(997) * RANDOM.nextDouble());
    }

    private Entity getRandomProduct() {
        return getRandomEntity("basic", "product");
    }

    private void addSubstituteComponent(final Entity substitute, final Entity product, final double quantity) {
        Entity substituteComponent = dataDefinitionService.get("basic", "substituteComponent").create();

        substituteComponent.setField("quantity", new BigDecimal(quantity + 1).abs().setScale(3, RoundingMode.HALF_EVEN));
        substituteComponent.setField("product", product);
        substituteComponent.setField("substitute", substitute);

        substituteComponent = dataDefinitionService.get("basic", "substituteComponent").save(substituteComponent);

        validateEntity(substituteComponent);
    }

    private String getRandomDictionaryItem(final String dictionaryName) {
        Entity dictionary = getDictionaryByName(dictionaryName);
        SearchCriteriaBuilder searchBuilder = dataDefinitionService.get("qcadooModel", "dictionaryItem").find();
        searchBuilder.add(SearchRestrictions.belongsTo("dictionary", dictionary));
        int total = searchBuilder.list().getTotalNumberOfEntities();
        return searchBuilder.setMaxResults(1).setFirstResult(RANDOM.nextInt(total)).uniqueResult().getField("name").toString();
    }

    private Entity getDictionaryByName(final String dictionaryName) {
        return dataDefinitionService.get("qcadooModel", "dictionary").find().add(SearchRestrictions.eq("name", dictionaryName))
                .setMaxResults(1).uniqueResult();
    }

    private void generateAndAddUser() {
        Entity user = dataDefinitionService.get("qcadooSecurity", "user").create();

        user.setField("userName", generateString(charsOnly, RANDOM.nextInt(4) + 3));
        user.setField("email", generateRandomEmail());
        user.setField("firstname", generateString(charsOnly, RANDOM.nextInt(4) + 3));
        user.setField("lastname", generateString(charsOnly, RANDOM.nextInt(4) + 3));
        SecurityRole role = securityRolesService.getRoleByIdentifier("ROLE_USER");
        user.setField("role", role.getName());
        user.setField("password", "123");
        user.setField("passwordConfirmation", "123");
        user.setField("enabled", true);

        user = dataDefinitionService.get("qcadooSecurity", "user").save(user);

        validateEntity(user);
    }

    private String generateRandomEmail() {
        String email;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(generateString(charsAndDigits, RANDOM.nextInt(3) + 3));
        stringBuilder.append("@").append(generateString(charsAndDigits, 4)).append(".");
        stringBuilder.append("org");
        email = stringBuilder.toString();
        return email;
    }

    private void addDictionary(final String name) {
        Entity dictionary = getDictionaryByName(name);

        Entity item = dataDefinitionService.get("qcadooModel", "dictionaryItem").create();
        item.setField("dictionary", dictionary);
        item.setField("name", generateString(charsOnly, 8));

        item = dataDefinitionService.get("qcadooModel", "dictionaryItem").save(item);

        validateEntity(item);
    }

    private String generateTypeOfProduct() {
        return acceptableTypeOfProduct[RANDOM.nextInt(acceptableTypeOfProduct.length)];
    }

    private boolean isEnabled(final String pluginIdentifier) {
        return pluginAccessor.getPlugin(pluginIdentifier) != null;
    }

    private boolean databaseHasToBePrepared() {
        return dataDefinitionService.get("basic", "parameter").find().list().getTotalNumberOfEntities() == 0;
    }

    private void addParameters() {
        Entity parameter = dataDefinitionService.get("basic", "parameter").create();
        parameter.setField("checkDoneOrderForQuality", false);
        parameter.setField("autoGenerateQualityControl", false);
        parameter.setField("batchForDoneOrder", "01none");

        if (isEnabled("productionCounting")) {
            parameter.setField("registerQuantityInProduct", true);
            parameter.setField("registerQuantityOutProduct", true);
            parameter.setField("registerProductionTime", true);
            parameter.setField("justOne", false);
            parameter.setField("allowToClose", false);
            parameter.setField("autoCloseOrder", false);
        }

        dataDefinitionService.get("basic", "parameter").save(parameter);

        validateEntity(parameter);
    }

    private static String generateString(final String allowedChars, final int stringLength) {
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
