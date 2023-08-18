package com.qcadoo.mes.orders.controllers;

import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.ALL;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.*;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.controllers.dataProvider.DashboardKanbanDataProvider;
import com.qcadoo.mes.orders.controllers.dto.TechnologyOperationDto;
import com.qcadoo.mes.orders.controllers.requests.OrderCreationRequest;
import com.qcadoo.mes.orders.controllers.responses.OrderCreationResponse;
import com.qcadoo.mes.orders.listeners.OrderDetailsListeners;
import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.productionLines.constants.UserFieldsPL;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.service.StateChangeContextBuilder;
import com.qcadoo.mes.technologies.TechnologyNameAndNumberGenerator;
import com.qcadoo.mes.technologies.constants.*;
import com.qcadoo.mes.technologies.controller.dataProvider.MaterialDto;
import com.qcadoo.mes.technologies.states.aop.TechnologyStateChangeAspect;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.GlobalMessage;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.constants.QcadooSecurityConstants;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderCreationService {

    private static final String IGNORE_MISSING_COMPONENTS = "ignoreMissingComponents";

    private static final String L_RANGE = "range";

    private static final String L_DASHBOARD_OPERATION = "dashboardOperation";

    private static final Set<String> FIELDS_OPERATION = Sets.newHashSet("tpz", "tj", "productionInOneCycle",
            "nextOperationAfterProducedType", "nextOperationAfterProducedQuantity", "nextOperationAfterProducedQuantityUNIT",
            "timeNextOperation", "machineUtilization", "laborUtilization", "productionInOneCycleUNIT",
            "areProductQuantitiesDivisible", "isTjDivisible", TechnologyOperationComponentFieldsTNFO.MIN_STAFF,
            TechnologyOperationComponentFieldsTNFO.TJ_DECREASES_FOR_ENLARGED_STAFF, TechnologyOperationComponentFieldsTNFO.OPTIMAL_STAFF);

    private static final String NEXT_OPERATION_AFTER_PRODUCED_TYPE = "nextOperationAfterProducedType";

    private static final String PRODUCTION_IN_ONE_CYCLE = "productionInOneCycle";

    private static final String NEXT_OPERATION_AFTER_PRODUCED_QUANTITY = "nextOperationAfterProducedQuantity";

    private static final String L_PRODUCT = "product";

    private static final String L_DASHBOARD_COMPONENTS_LOCATION = "dashboardComponentsLocation";

    private static final String L_DASHBOARD_PRODUCTS_INPUT_LOCATION = "dashboardProductsInputLocation";

    private static final String L_BASIC_PRODUCTION_COUNTING = "basicProductionCounting";

    private static final String L_PRODUCTION_COUNTING_QUANTITY = "productionCountingQuantity";

    private static final String L_ORDER = "order";

    private static final String L_PLANNED_QUANTITY = "plannedQuantity";

    private static final String L_ROLE = "role";

    private static final String L_USED = "01used";

    private static final String L_TYPE_OF_MATERIAL = "typeOfMaterial";

    private static final String L_COMPONENT = "01component";

    private static final String L_FLOW_FILLED = "flowFilled";

    private static final String L_PRODUCTION_COUNTING_QUANTITIES = "productionCountingQuantities";

    private static final String L_OPERATION = "operation";

    private static final String L_TECHNOLOGY_OPERATION_COMPONENT = "technologyOperationComponent";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderStateChangeAspect orderStateChangeAspect;

    @Autowired
    private TechnologyStateChangeAspect technologyStateChangeAspect;

    @Autowired
    private StateChangeContextBuilder stateChangeContextBuilder;

    @Autowired
    private TechnologyNameAndNumberGenerator technologyNameAndNumberGenerator;

    @Autowired
    private DashboardKanbanDataProvider dashboardKanbanDataProvider;

    @Autowired
    private OrderDetailsListeners orderDetailsListeners;

    public OrderCreationResponse createOrder(final OrderCreationRequest orderCreationRequest) {
        Entity parameter = parameterService.getParameter();

        boolean createOperationalTasks = !orderCreationRequest.getTechnologyOperations().isEmpty();

        if (Objects.isNull(orderCreationRequest.getTechnologyId())) {
            if (!isParameterSet(parameter, createOperationalTasks)) {
                return new OrderCreationResponse(translationService.translate(
                        "basic.dashboard.orderDefinitionWizard.createOrder.parameterNotSet", LocaleContextHolder.getLocale()));
            }
        }

        Entity product = getProduct(orderCreationRequest.getProductId());

        Either<String, Entity> isTechnology = getOrCreateTechnology(orderCreationRequest);

        if (isTechnology.isLeft()) {
            return new OrderCreationResponse(isTechnology.getLeft());
        }

        Entity technology = isTechnology.getRight();
        Entity productionLine = getProductionLine(orderCreationRequest.getProductionLineId(), technology);

        OrderCreationResponse response = new OrderCreationResponse(OrderCreationResponse.StatusCode.OK);

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).create();

        order.setField(OrderFields.NUMBER,
                numberGeneratorService.generateNumber(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER, 6));
        order.setField(OrderFields.NAME, orderService.makeDefaultName(product, technology, LocaleContextHolder.getLocale()));

        order.setField(OrderFields.PRODUCT, product);
        order.setField(OrderFields.TECHNOLOGY, technology);
        order.setField(OrderFields.PRODUCTION_LINE, productionLine);
        order.setField(OrderFields.DIVISION, orderService.getDivision(technology));

        order.setField(OrderFields.DATE_FROM, orderCreationRequest.getStartDate());
        order.setField(OrderFields.DATE_TO, orderCreationRequest.getFinishDate());

        order.setField(OrderFields.EXTERNAL_SYNCHRONIZED, true);
        order.setField(OrderFields.STATE, OrderStateStringValues.PENDING);
        order.setField(OrderFields.PLANNED_QUANTITY, orderCreationRequest.getQuantity());
        order.setField(OrderFields.DESCRIPTION,
                buildDescription(parameter, orderCreationRequest.getDescription(), technology, product));

        order.setField(IGNORE_MISSING_COMPONENTS, parameter.getBooleanField(IGNORE_MISSING_COMPONENTS));

        order.setField("typeOfProductionRecording", orderCreationRequest.getTypeOfProductionRecording());

        order = order.getDataDefinition().save(order);

        if (order.isValid()) {
            if (!order.getGlobalMessages().isEmpty()) {
                Optional<GlobalMessage> message = order.getGlobalMessages().stream()
                        .filter(gm -> gm.getMessage().equals("orders.order.message.plannedQuantityChanged")).findFirst();

                message.ifPresent(m -> response.setAdditionalInformation(translationService.translate(m.getMessage(),
                        LocaleContextHolder.getLocale(), m.getVars()[0], m.getVars()[1])));
            }

            final StateChangeContext orderStateChangeContext = stateChangeContextBuilder
                    .build(orderStateChangeAspect.getChangeEntityDescriber(), order, OrderState.ACCEPTED.getStringValue());

            orderStateChangeAspect.changeState(orderStateChangeContext);

            order = order.getDataDefinition().get(order.getId());

            if (!order.getStringField(OrderFields.STATE).equals(OrderStateStringValues.ACCEPTED)) {
                return new OrderCreationResponse(
                        translationService.translate("basic.dashboard.orderDefinitionWizard.createOrder.acceptError",
                                LocaleContextHolder.getLocale(), order.getStringField(OrderFields.NUMBER)));
            }
        } else {
            return new OrderCreationResponse(translationService.translate(
                    "basic.dashboard.orderDefinitionWizard.createOrder.validationError", LocaleContextHolder.getLocale()));
        }

        if (createOperationalTasks) {
            createOperationalTasks(order, orderCreationRequest);
            modifyProductionCountingQuantityForEach(order, orderCreationRequest.getTechnologyOperations());
        } else {
            modifyProductionCountingQuantity(order, orderCreationRequest.getMaterials());
        }

        response.setMessage(translationService.translate("orders.orderCreationService.created", LocaleContextHolder.getLocale(),
                order.getStringField(OrderFields.NUMBER)));

        if (createOperationalTasks) {
            response.setOperationalTasks(dashboardKanbanDataProvider.getOperationalTasksPendingForOrder(order.getId()));
        } else {
            Entity currentUserProductionLine = getCurrentUserProductionLine();

            if (Objects.isNull(currentUserProductionLine)) {
                response.setOrder(dashboardKanbanDataProvider.getOrder(order.getId()));
            } else {
                if (Objects.nonNull(productionLine) && currentUserProductionLine.getId().equals(productionLine.getId())) {
                    response.setOrder(dashboardKanbanDataProvider.getOrder(order.getId()));
                }
            }
        }

        return response;
    }

    private void createOperationalTasks(Entity order, final OrderCreationRequest orderCreationRequest) {
        orderDetailsListeners.createOperationalTasksForOrder(order, false);

        Map<Long, TechnologyOperationDto> operationsById = orderCreationRequest.getTechnologyOperations().stream()
                .collect(Collectors.toMap(TechnologyOperationDto::getId, x -> x));

        order = order.getDataDefinition().get(order.getId());

        List<Entity> operationalTasks = order.getHasManyField(OrderFields.OPERATIONAL_TASKS);

        for (Entity operationalTask : operationalTasks) {
            Entity technologyOperationComponent = operationalTask.getBelongsToField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);

            Long workstation = operationsById
                    .get(technologyOperationComponent.getId()).getWorkstationId();

            if (Objects.nonNull(workstation)) {
                operationalTask.setField(OperationalTaskFields.WORKSTATION, workstation);

                Entity workstationEntity = getWorkstationDD().get(workstation);

                if (Objects.nonNull(workstationEntity.getBelongsToField(WorkstationFields.STAFF))
                        && technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.OPTIMAL_STAFF) == 1) {
                    operationalTask.setField(OperationalTaskFields.STAFF,
                            workstationEntity.getBelongsToField(WorkstationFields.STAFF));
                }

                operationalTask.getDataDefinition().save(operationalTask);
            }
        }
    }

    private void modifyProductionCountingQuantityForEach(Entity order, List<TechnologyOperationDto> technologyOperations) {
        for (TechnologyOperationDto technologyOperation : technologyOperations) {
            Entity technologyOperationComponent = getTechnologyOperationComponentDD().get(technologyOperation.getId());

            List<Entity> materialsFromOrderPCQ = getMaterialsFromOrder(order, technologyOperationComponent);
            List<MaterialDto> materials = technologyOperation.getMaterials();
            List<MaterialDto> addedMaterials = materials.stream().filter(m -> Objects.isNull(m.getProductInId()))
                    .collect(Collectors.toList());

            List<Long> technologyMaterials = materials.stream().filter(m -> Objects.nonNull(m.getProductInId()))
                    .map(MaterialDto::getProductId).collect(Collectors.toList());

            Map<Long, Entity> pacqByProductId = materialsFromOrderPCQ.stream()
                    .collect(Collectors.toMap(pcq -> pcq.getBelongsToField(L_PRODUCT).getId(), pcq -> pcq));

            for (Map.Entry<Long, Entity> entry : pacqByProductId.entrySet()) {
                if (!technologyMaterials.contains(entry.getKey())) {
                    Entity productionCountingQuantity = entry.getValue();

                    productionCountingQuantity.getDataDefinition().delete(productionCountingQuantity.getId());
                }
            }

            List<Entity> productionCountingQuantities = Lists.newArrayList();

            for (MaterialDto material : addedMaterials) {
                Entity productionCountingQuantity = createProductionCountingQuantity(order, material, technologyOperationComponent);

                productionCountingQuantities.add(productionCountingQuantity);
            }

            fillFlow(productionCountingQuantities, order);

            productionCountingQuantities.forEach(productionCountingQuantity -> productionCountingQuantity.getDataDefinition().save(productionCountingQuantity));
        }
    }

    private void modifyProductionCountingQuantity(final Entity order, final List<MaterialDto> materials) {
        List<MaterialDto> addedMaterials = materials.stream().filter(m -> Objects.isNull(m.getProductInId()))
                .collect(Collectors.toList());
        List<Long> technologyMaterials = materials.stream().filter(m -> Objects.nonNull(m.getProductInId()))
                .map(MaterialDto::getProductId).collect(Collectors.toList());
        List<Entity> materialsFromOrderPCQ = getMaterialsFromOrder(order, null);

        Map<Long, Entity> pacqByProductId = materialsFromOrderPCQ.stream()
                .collect(Collectors.toMap(pcq -> pcq.getBelongsToField(L_PRODUCT).getId(), pcq -> pcq));

        for (Map.Entry<Long, Entity> entry : pacqByProductId.entrySet()) {
            if (!technologyMaterials.contains(entry.getKey())) {
                Entity productionCountingQuantity = entry.getValue();

                productionCountingQuantity.getDataDefinition().delete(productionCountingQuantity.getId());
            }
        }

        List<Entity> productionCountingQuantities = Lists.newArrayList();

        for (MaterialDto material : addedMaterials) {
            Entity technologyOperationComponent = order.getBelongsToField(OrderFields.TECHNOLOGY).getTreeField(TechnologyFields.OPERATION_COMPONENTS)
                    .getRoot();

            Entity productionCountingQuantity = createProductionCountingQuantity(order, material, technologyOperationComponent);

            productionCountingQuantities.add(productionCountingQuantity);
        }

        fillFlow(productionCountingQuantities, order);
        productionCountingQuantities.forEach(productionCountingQuantity -> productionCountingQuantity.getDataDefinition().save(productionCountingQuantity));
    }

    // override be aspect
    public void fillFlow(final List<Entity> productionCountingQuantities, final Entity order) {

    }

    private Entity createProductionCountingQuantity(final Entity order, final MaterialDto material, final Entity technologyOperationComponent) {
        Entity productionCountingQuantity = dataDefinitionService.get(L_BASIC_PRODUCTION_COUNTING, L_PRODUCTION_COUNTING_QUANTITY)
                .create();

        productionCountingQuantity.setField(L_ORDER, order.getId());
        productionCountingQuantity.setField(L_TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent.getId());

        BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY).multiply(material.getQuantityPerUnit(),
                numberService.getMathContext());

        productionCountingQuantity.setField(L_PLANNED_QUANTITY, plannedQuantity);
        productionCountingQuantity.setField(OrderCreationService.L_PRODUCT, material.getProductId());
        productionCountingQuantity.setField(L_ROLE, L_USED);
        productionCountingQuantity.setField(L_TYPE_OF_MATERIAL, L_COMPONENT);
        productionCountingQuantity.setField(L_FLOW_FILLED, Boolean.TRUE);

        return productionCountingQuantity;
    }

    private List<Entity> getMaterialsFromOrder(final Entity order, final Entity technologyOperationComponent) {
        SearchCriteriaBuilder searchCriteriaBuilder = order.getHasManyField(L_PRODUCTION_COUNTING_QUANTITIES).find()
                .add(SearchRestrictions.eq(OrderCreationService.L_ROLE, OrderCreationService.L_USED));

        if (Objects.nonNull(technologyOperationComponent)) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo("technologyOperationComponent", technologyOperationComponent));
        }

        searchCriteriaBuilder.add(SearchRestrictions.eq(OrderCreationService.L_TYPE_OF_MATERIAL, OrderCreationService.L_COMPONENT));

        return searchCriteriaBuilder.list().getEntities();
    }

    private boolean isParameterSet(final Entity parameter, boolean createOperationalTasks) {
        Entity operation = parameter.getBelongsToField(L_DASHBOARD_OPERATION);
        Entity dashboardComponentsLocation = parameter.getBelongsToField(OrderCreationService.L_DASHBOARD_COMPONENTS_LOCATION);
        Entity dashboardProductsInputLocation = parameter
                .getBelongsToField(OrderCreationService.L_DASHBOARD_PRODUCTS_INPUT_LOCATION);

        if (createOperationalTasks) {
            return !Objects.isNull(dashboardComponentsLocation) && !Objects.isNull(dashboardProductsInputLocation)
                    && parameter.getBooleanField(ParameterFieldsT.COMPLETE_WAREHOUSES_FLOW_WHILE_CHECKING)
                    && parameter.getBooleanField(ParameterFieldsT.MOVE_PRODUCTS_TO_SUBSEQUENT_OPERATIONS);
        } else {
            return !Objects.isNull(operation) && !Objects.isNull(dashboardComponentsLocation)
                    && !Objects.isNull(dashboardProductsInputLocation)
                    && parameter.getBooleanField(ParameterFieldsT.COMPLETE_WAREHOUSES_FLOW_WHILE_CHECKING);
        }
    }

    private String buildDescription(final Entity parameter, final String description, final Entity technology, final Entity product) {
        boolean fillOrderDescriptionBasedOnTechnology = parameter
                .getBooleanField(ParameterFieldsO.FILL_ORDER_DESCRIPTION_BASED_ON_TECHNOLOGY_DESCRIPTION);
        boolean fillOrderDescriptionBasedOnProductDescription = parameter
                .getBooleanField(ParameterFieldsO.FILL_ORDER_DESCRIPTION_BASED_ON_PRODUCT_DESCRIPTION);

        StringBuilder descriptionBuilder = new StringBuilder();

        descriptionBuilder.append(description);

        if (fillOrderDescriptionBasedOnTechnology && Objects.nonNull(technology)
                && StringUtils.isNoneBlank(technology.getStringField(TechnologyFields.DESCRIPTION))) {
            if (StringUtils.isNoneBlank(descriptionBuilder.toString())) {
                descriptionBuilder.append("\n");
            }

            descriptionBuilder.append(technology.getStringField(TechnologyFields.DESCRIPTION));
        }

        if (fillOrderDescriptionBasedOnProductDescription && Objects.nonNull(product)) {
            String productDescription = product.getStringField(ProductFields.DESCRIPTION);

            if (StringUtils.isNoneBlank(productDescription)) {
                if (StringUtils.isNoneBlank(descriptionBuilder.toString())) {
                    descriptionBuilder.append("\n");
                }

                descriptionBuilder.append(productDescription);
            }
        }

        return descriptionBuilder.toString();
    }

    private Either<String, Entity> getOrCreateTechnology(final OrderCreationRequest orderCreationRequest) {
        if (Objects.isNull(orderCreationRequest.getTechnologyId())) {
            if (orderCreationRequest.getTechnologyOperations().isEmpty()) {
                return createTechnology(orderCreationRequest);
            } else {
                return createTechnologyForEachOperation(orderCreationRequest);
            }
        } else {
            return Either.right(
                    getTechnologyDD()
                            .get(orderCreationRequest.getTechnologyId()));
        }
    }

    private Either<String, Entity> createTechnologyForEachOperation(final OrderCreationRequest orderCreationRequest) {
        Entity product = getProduct(orderCreationRequest.getProductId());
        Entity parameter = parameterService.getParameter();
        Entity dashboardComponentsLocation = parameter.getBelongsToField(OrderCreationService.L_DASHBOARD_COMPONENTS_LOCATION);
        Entity dashboardProductsInputLocation = parameter
                .getBelongsToField(OrderCreationService.L_DASHBOARD_PRODUCTS_INPUT_LOCATION);

        orderCreationRequest.getTechnologyOperations().sort(Comparator.comparing(TechnologyOperationDto::getNode));

        String range = parameter.getStringField(L_RANGE);
        Entity technology = getTechnologyDD().create();

        technology.setField(TechnologyFields.NUMBER, technologyNameAndNumberGenerator.generateNumber(product));
        technology.setField(TechnologyFields.NAME, technologyNameAndNumberGenerator.generateName(product));
        technology.setField(TechnologyFields.PRODUCT, product);
        technology.setField(TechnologyFields.EXTERNAL_SYNCHRONIZED, true);
        technology.setField(L_RANGE, range);
        technology.setField("componentsLocation", dashboardComponentsLocation);
        technology.setField("productsInputLocation", dashboardProductsInputLocation);
        technology.setField("typeOfProductionRecording", orderCreationRequest.getTypeOfProductionRecording());
        technology = technology.getDataDefinition().save(technology);

        if (!technology.isValid()) {
            return Either.left(translationService.translate(
                    "basic.dashboard.orderDefinitionWizard.createTechnology.validationError", LocaleContextHolder.getLocale()));
        }

        Entity parent = null;

        for (TechnologyOperationDto technologyOperation : orderCreationRequest.getTechnologyOperations()) {
            Entity operation = dataDefinitionService
                    .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION)
                    .get(technologyOperation.getOperationId());

            Entity technologyOperationComponent = getTechnologyOperationComponentDD().create();

            technologyOperationComponent.setField(TechnologyOperationComponentFields.OPERATION, operation);
            technologyOperationComponent.setField(TechnologyOperationComponentFields.ENTITY_TYPE, L_OPERATION);

            for (String fieldName : FIELDS_OPERATION) {
                technologyOperationComponent.setField(fieldName, operation.getField(fieldName));
            }

            if (Objects.isNull(operation.getField(NEXT_OPERATION_AFTER_PRODUCED_TYPE))) {
                technologyOperationComponent.setField(NEXT_OPERATION_AFTER_PRODUCED_TYPE, ALL);
            }

            if (Objects.isNull(operation.getField(PRODUCTION_IN_ONE_CYCLE))) {
                technologyOperationComponent.setField(PRODUCTION_IN_ONE_CYCLE, "1");
            }

            if (Objects.isNull(operation.getField(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY))) {
                technologyOperationComponent.setField(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY, "0");
            }

            if (Objects.isNull(parent)) {
                Entity operationProductOutComponent = getOperationProductOutComponentDD().create();

                operationProductOutComponent.setField(OperationProductOutComponentFields.QUANTITY, BigDecimal.ONE);
                operationProductOutComponent.setField(OperationProductOutComponentFields.PRODUCT, product);

                technologyOperationComponent.setField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS, Lists.newArrayList(operationProductOutComponent));
            }

            List<Entity> operationProductInComponents = Lists.newArrayList();

            for (MaterialDto material : technologyOperation.getMaterials()) {
                Entity inProduct = getProduct(material.getProductId());

                Entity operationProductInComponent = getOperationProductInComponentDD()
                        .create();

                operationProductInComponent.setField(OperationProductInComponentFields.PRODUCT, inProduct);
                operationProductInComponent.setField(OperationProductInComponentFields.QUANTITY, material.getQuantityPerUnit());

                operationProductInComponents.add(operationProductInComponent);
            }

            technologyOperationComponent.setField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS, operationProductInComponents);

            if (Objects.nonNull(parent)) {
                technologyOperationComponent.setField(TechnologyOperationComponentFields.PARENT, parent.getId());
            }

            technologyOperationComponent.setField(TechnologyOperationComponentFields.TECHNOLOGY, technology.getId());
            technologyOperationComponent = technologyOperationComponent.getDataDefinition().save(technologyOperationComponent);

            if (Objects.nonNull(technologyOperation.getWorkstationId())) {
                List<Entity> workstations = Lists
                        .newArrayList(technologyOperationComponent.getHasManyField(TechnologyOperationComponentFields.WORKSTATIONS));

                Entity workstation = getWorkstationDD().get(technologyOperation.getWorkstationId());

                workstations.add(workstation);

                technologyOperationComponent.setField(TechnologyOperationComponentFields.WORKSTATIONS, workstations);
            }

            technologyOperationComponent = technologyOperationComponent.getDataDefinition().save(technologyOperationComponent);

            technologyOperation.setId(technologyOperationComponent.getId());

            if (technologyOperationComponent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS).isEmpty()) {
                Entity operationProductOutComponent = getOperationProductOutComponentDD().create();

                operationProductOutComponent.setField(OperationProductOutComponentFields.QUANTITY, BigDecimal.ONE);
                operationProductOutComponent.setField(OperationProductOutComponentFields.PRODUCT, getOrCreateProduct(operation));
                operationProductOutComponent.setField(OperationProductOutComponentFields.OPERATION_COMPONENT, technologyOperationComponent);

                operationProductOutComponent.getDataDefinition().save(operationProductOutComponent);
            }

            parent = technologyOperationComponent;
        }

        if (technology.isValid()) {
            final StateChangeContext technologyStateChangeContext = stateChangeContextBuilder.build(
                    technologyStateChangeAspect.getChangeEntityDescriber(), technology,
                    TechnologyState.ACCEPTED.getStringValue());

            technologyStateChangeAspect.changeState(technologyStateChangeContext);

            technology = technology.getDataDefinition().get(technology.getId());

            if (!technology.getStringField(TechnologyFields.STATE).equals(TechnologyStateStringValues.ACCEPTED)) {
                return Either.left(translationService.translate(
                        "basic.dashboard.orderDefinitionWizard.createTechnology.acceptError", LocaleContextHolder.getLocale()));
            }

            technology.setField(TechnologyFields.MASTER, Boolean.TRUE);
            technology.getDataDefinition().save(technology);
        } else {
            return Either.left(translationService.translate(
                    "basic.dashboard.orderDefinitionWizard.createTechnology.validationError", LocaleContextHolder.getLocale()));
        }

        return Either.right(technology);
    }

    private Either<String, Entity> createTechnology(final OrderCreationRequest orderCreationRequest) {
        Entity product = getProduct(orderCreationRequest.getProductId());
        Entity parameter = parameterService.getParameter();
        Entity operation = parameter.getBelongsToField(L_DASHBOARD_OPERATION);
        Entity dashboardComponentsLocation = parameter.getBelongsToField(OrderCreationService.L_DASHBOARD_COMPONENTS_LOCATION);
        Entity dashboardProductsInputLocation = parameter
                .getBelongsToField(OrderCreationService.L_DASHBOARD_PRODUCTS_INPUT_LOCATION);

        Entity technologyOperationComponent = getTechnologyOperationComponentDD().create();

        technologyOperationComponent.setField(TechnologyOperationComponentFields.OPERATION, operation);
        technologyOperationComponent.setField(TechnologyOperationComponentFields.ENTITY_TYPE, L_OPERATION);

        for (String fieldName : FIELDS_OPERATION) {
            technologyOperationComponent.setField(fieldName, operation.getField(fieldName));
        }

        if (Objects.isNull(operation.getField(NEXT_OPERATION_AFTER_PRODUCED_TYPE))) {
            technologyOperationComponent.setField(NEXT_OPERATION_AFTER_PRODUCED_TYPE, ALL);
        }

        if (Objects.isNull(operation.getField(PRODUCTION_IN_ONE_CYCLE))) {
            technologyOperationComponent.setField(PRODUCTION_IN_ONE_CYCLE, "1");
        }

        if (Objects.isNull(operation.getField(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY))) {
            technologyOperationComponent.setField(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY, "0");
        }

        Entity operationProductOutComponent = getOperationProductOutComponentDD().create();

        operationProductOutComponent.setField(OperationProductOutComponentFields.PRODUCT, product);
        operationProductOutComponent.setField(OperationProductOutComponentFields.QUANTITY, BigDecimal.ONE);

        technologyOperationComponent.setField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS, Lists.newArrayList(operationProductOutComponent));

        List<Entity> operationProductInComponents = Lists.newArrayList();

        for (MaterialDto material : orderCreationRequest.getMaterials()) {
            Entity inProduct = getProduct(material.getProductId());

            Entity operationProductInComponent = getOperationProductInComponentDD()
                    .create();

            operationProductInComponent.setField(OperationProductInComponentFields.PRODUCT, inProduct);
            operationProductInComponent.setField(OperationProductInComponentFields.QUANTITY, material.getQuantityPerUnit());

            operationProductInComponents.add(operationProductInComponent);
        }

        technologyOperationComponent.setField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS, operationProductInComponents);

        String range = parameter.getStringField(L_RANGE);

        Entity technology = getTechnologyDD().create();

        technology.setField(TechnologyFields.NUMBER, technologyNameAndNumberGenerator.generateNumber(product));
        technology.setField(TechnologyFields.NAME, technologyNameAndNumberGenerator.generateName(product));
        technology.setField(TechnologyFields.PRODUCT, product);
        technology.setField(TechnologyFields.EXTERNAL_SYNCHRONIZED, true);
        technology.setField(TechnologyFields.OPERATION_COMPONENTS, Lists.newArrayList(technologyOperationComponent));
        technology.setField(L_RANGE, range);
        technology.setField("componentsLocation", dashboardComponentsLocation);
        technology.setField("productsInputLocation", dashboardProductsInputLocation);
        technology.setField("typeOfProductionRecording", "02cumulated");

        technology = technology.getDataDefinition().save(technology);

        if (technology.isValid()) {
            final StateChangeContext technologyStateChangeContext = stateChangeContextBuilder.build(
                    technologyStateChangeAspect.getChangeEntityDescriber(), technology,
                    TechnologyState.ACCEPTED.getStringValue());

            technologyStateChangeAspect.changeState(technologyStateChangeContext);

            technology = technology.getDataDefinition().get(technology.getId());

            if (!technology.getStringField(TechnologyFields.STATE).equals(TechnologyStateStringValues.ACCEPTED)) {
                return Either.left(translationService.translate(
                        "basic.dashboard.orderDefinitionWizard.createTechnology.acceptError", LocaleContextHolder.getLocale()));
            }

            technology.setField(TechnologyFields.MASTER, Boolean.TRUE);
            technology.getDataDefinition().save(technology);
        } else {
            return Either.left(translationService.translate(
                    "basic.dashboard.orderDefinitionWizard.createTechnology.validationError", LocaleContextHolder.getLocale()));
        }

        return Either.right(technology);
    }

    private Entity getProductionLine(final Long productionLineId, final Entity technology) {
        if (Objects.nonNull(productionLineId)) {
            return dataDefinitionService
                    .get(ProductionLinesConstants.PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_PRODUCTION_LINE)
                    .get(productionLineId);
        } else {
            return orderService.getProductionLine(technology);
        }
    }

    private Entity getProduct(final Long productId) {
        return getProductDD().get(productId);
    }

    private Entity getOrCreateProduct(final Entity operation) {
        Entity product = getProductDD().find().add(SearchRestrictions.eq(ProductFields.NUMBER, operation.getStringField(OperationFields.NUMBER)))
                .setMaxResults(1).uniqueResult();

        if (Objects.nonNull(product)) {
            return product;
        } else {
            Entity newProduct = getProductDD()
                    .create();

            newProduct.setField(ProductFields.NUMBER, operation.getStringField(OperationFields.NUMBER));
            newProduct.setField(ProductFields.NAME, operation.getStringField(OperationFields.NAME));
            newProduct.setField(ProductFields.GLOBAL_TYPE_OF_MATERIAL, GlobalTypeOfMaterial.INTERMEDIATE.getStringValue());
            newProduct.setField(ProductFields.ENTITY_TYPE, ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue());
            newProduct.setField(ProductFields.UNIT, parameterService.getParameter().getStringField(ParameterFields.UNIT));

            return newProduct.getDataDefinition().save(newProduct);
        }
    }

    private Entity getCurrentUserProductionLine() {
        Entity currentUser = dataDefinitionService.get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER)
                .get(securityService.getCurrentUserId());

        return currentUser.getBelongsToField(UserFieldsPL.PRODUCTION_LINE);
    }

    private DataDefinition getProductDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }

    private DataDefinition getWorkstationDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_WORKSTATION);
    }

    private DataDefinition getTechnologyDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }

    private DataDefinition getTechnologyOperationComponentDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
    }

    private DataDefinition getOperationProductInComponentDD() {
        return dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);
    }

    private DataDefinition getOperationProductOutComponentDD() {
        return dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT);
    }

}
