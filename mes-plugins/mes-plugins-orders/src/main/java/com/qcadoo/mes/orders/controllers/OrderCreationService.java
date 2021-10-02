package com.qcadoo.mes.orders.controllers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.GlobalTypeOfMaterial;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
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
import com.qcadoo.mes.productionLines.constants.ParameterFieldsPL;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.service.StateChangeContextBuilder;
import com.qcadoo.mes.technologies.TechnologyNameAndNumberGenerator;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.ParameterFieldsT;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.controller.dataProvider.MaterialDto;
import com.qcadoo.mes.technologies.states.aop.TechnologyStateChangeAspect;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.GlobalMessage;
import com.qcadoo.view.api.utils.NumberGeneratorService;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class OrderCreationService {

    private static final String IS_SUBCONTRACTED = "isSubcontracted";

    private static final String IGNORE_MISSING_COMPONENTS = "ignoreMissingComponents";

    private static final String L_RANGE = "range";

    private static final String L_DASHBOARD_OPERATION = "dashboardOperation";

    private static final Set<String> FIELDS_OPERATION = Sets.newHashSet("tpz", "tj", "productionInOneCycle",
            "nextOperationAfterProducedType", "nextOperationAfterProducedQuantity", "nextOperationAfterProducedQuantityUNIT",
            "timeNextOperation", "machineUtilization", "laborUtilization", "productionInOneCycleUNIT",
            "areProductQuantitiesDivisible", "isTjDivisible");

    private static final String NEXT_OPERATION_AFTER_PRODUCED_TYPE = "nextOperationAfterProducedType";

    private static final String PRODUCTION_IN_ONE_CYCLE = "productionInOneCycle";

    private static final String NEXT_OPERATION_AFTER_PRODUCED_QUANTITY = "nextOperationAfterProducedQuantity";

    private static final String L_PRODUCT = "product";

    private static final String L_DASHBOARD_COMPONENTS_LOCATION = "dashboardComponentsLocation";

    private static final String L_DASHBOARD_PRODUCTS_INPUT_LOCATION = "dashboardProductsInputLocation";

    private static final String L_BASIC_PRODUCTION_COUNTING = "basicProductionCounting";

    private static final String L_PRODUCTION_COUNTING_QUANTITY = "productionCountingQuantity";

    public static final String L_ORDER = "order";

    private static final String L_PLANNED_QUANTITY = "plannedQuantity";

    private static final String L_ROLE = "role";

    private static final String L_USED = "01used";

    private static final String L_TYPE_OF_MATERIAL = "typeOfMaterial";

    private static final String L_COMPONENT = "01component";

    private static final String L_FLOW_FILLED = "flowFilled";

    private static final String L_PRODUCTS_INPUT_LOCATION = "productsInputLocation";

    private static final String L_COMPONENTS_LOCATION = "componentsLocation";

    private static final String L_PRODUCTION_COUNTING_QUANTITIES = "productionCountingQuantities";

    private static final String L_OPERATION = "operation";

    private static final String L_ALL = "01all";

    private static final String L_TECHNOLOGY_OPERATION_COMPONENT = "technologyOperationComponent";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private OrderStateChangeAspect orderStateChangeAspect;

    @Autowired
    private TechnologyStateChangeAspect technologyStateChangeAspect;

    @Autowired
    private StateChangeContextBuilder stateChangeContextBuilder;

    @Autowired
    private TechnologyNameAndNumberGenerator technologyNameAndNumberGenerator;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DashboardKanbanDataProvider dashboardKanbanDataProvider;

    @Autowired
    private OrderDetailsListeners orderDetailsListeners;

    @Autowired
    private NumberService numberService;

    public OrderCreationResponse createOrder(OrderCreationRequest orderCreationRequest) {

        Entity parameter = parameterService.getParameter();
        boolean createOperationalTasks = !orderCreationRequest.getTechnologyOperations().isEmpty();
        if (Objects.isNull(orderCreationRequest.getTechnologyId())) {
            if (!isParameterSet(parameter, createOperationalTasks)) {
                return new OrderCreationResponse(translationService.translate(
                        "basic.dashboard.orderDefinitionWizard.createOrder.parameterNotSet", LocaleContextHolder.getLocale()));
            }
        }
        Entity product = getProduct(orderCreationRequest.getProductId());
        Entity productionLine = getProductionLine(orderCreationRequest.getProductionLineId());
        Either<String, Entity> isTechnology = getOrCreateTechnology(orderCreationRequest);
        if (isTechnology.isLeft()) {
            return new OrderCreationResponse(isTechnology.getLeft());
        }
        Entity technology = isTechnology.getRight();
        OrderCreationResponse response = new OrderCreationResponse(OrderCreationResponse.StatusCode.OK);

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).create();

        order.setField(OrderFields.NUMBER,
                numberGeneratorService.generateNumber(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER, 6));
        order.setField(OrderFields.NAME, orderService.makeDefaultName(product, technology, LocaleContextHolder.getLocale()));

        order.setField(OrderFields.PRODUCT, product);
        order.setField(OrderFields.TECHNOLOGY_PROTOTYPE, technology);
        order.setField(OrderFields.PRODUCTION_LINE, productionLine);
        order.setField(OrderFields.DIVISION, orderService.getDivision(technology));

        order.setField(OrderFields.DATE_FROM, orderCreationRequest.getStartDate());
        order.setField(OrderFields.DATE_TO, orderCreationRequest.getFinishDate());

        order.setField(OrderFields.EXTERNAL_SYNCHRONIZED, true);
        order.setField(IS_SUBCONTRACTED, false);
        order.setField(OrderFields.STATE, OrderStateStringValues.PENDING);
        order.setField(OrderFields.PLANNED_QUANTITY, orderCreationRequest.getQuantity());
        order.setField(OrderFields.DESCRIPTION, buildDescription(parameter, orderCreationRequest.getDescription(), technology));

        order.setField(IGNORE_MISSING_COMPONENTS, parameter.getBooleanField(IGNORE_MISSING_COMPONENTS));

        order.setField("typeOfProductionRecording", orderCreationRequest.getTypeOfProductionRecording());

        order = order.getDataDefinition().save(order);
        if (order.isValid()) {
            if (!order.getGlobalMessages().isEmpty()) {
                Optional<GlobalMessage> message = order.getGlobalMessages().stream()
                        .filter(gm -> gm.getMessage().equals("orders.order.message.plannedQuantityChanged")).findFirst();
                message.ifPresent(m -> {
                    response.setAdditionalInformation(translationService.translate(m.getMessage(),
                            LocaleContextHolder.getLocale(), m.getVars()[0], m.getVars()[1]));
                });
            }
            final StateChangeContext orderStateChangeContext = stateChangeContextBuilder.build(
                    orderStateChangeAspect.getChangeEntityDescriber(), order, OrderState.ACCEPTED.getStringValue());
            orderStateChangeAspect.changeState(orderStateChangeContext);
            order = order.getDataDefinition().get(order.getId());
            if (!order.getStringField(OrderFields.STATE).equals(OrderStateStringValues.ACCEPTED)) {
                return new OrderCreationResponse(translationService.translate(
                        "basic.dashboard.orderDefinitionWizard.createOrder.acceptError", LocaleContextHolder.getLocale(),
                        order.getStringField(OrderFields.NUMBER)));
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
            response.setOrder(dashboardKanbanDataProvider.getOrder(order.getId()));
        }
        return response;
    }

    private void createOperationalTasks(Entity order, OrderCreationRequest orderCreationRequest) {
        orderDetailsListeners.createOperationalTasksForOrder(order);
        Map<Long, TechnologyOperationDto> operationsById = orderCreationRequest.getTechnologyOperations().stream()
                .collect(Collectors.toMap(TechnologyOperationDto::getId, x -> x));
        List<Entity> operationalTasks = order.getHasManyField(OrderFields.OPERATIONAL_TASKS);
        for (Entity operationalTask : operationalTasks) {
            Long workstation = operationsById.get(
                    operationalTask.getBelongsToField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT).getId())
                    .getWorkstationId();
            if (Objects.nonNull(workstation)) {
                operationalTask.setField(OperationalTaskFields.WORKSTATION, workstation);
                operationalTask.getDataDefinition().save(operationalTask);
            }

        }
    }

    private void modifyProductionCountingQuantityForEach(Entity order, List<TechnologyOperationDto> technologyOperations) {
        Entity parameter = parameterService.getParameter();

        for (TechnologyOperationDto technologyOperation : technologyOperations) {
            Entity toc = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(technologyOperation.getId());
            List<Entity> materialsFromOrderPCQ = getMaterialsFromOrder(order, toc);
            List<MaterialDto> materials = technologyOperation.getMaterials();
            List<MaterialDto> addedMaterials = materials.stream().filter(m -> Objects.isNull(m.getProductInId()))
                    .collect(Collectors.toList());
            List<Long> technologyMaterials = materials.stream().filter(m -> Objects.nonNull(m.getProductInId()))
                    .map(m -> m.getProductId()).collect(Collectors.toList());
            Map<Long, Entity> pacqByProductId = materialsFromOrderPCQ.stream().collect(
                    Collectors.toMap(pcq -> pcq.getBelongsToField(L_PRODUCT).getId(), pcq -> pcq));
            for (Map.Entry<Long, Entity> entry : pacqByProductId.entrySet()) {
                if (!technologyMaterials.contains(entry.getKey())) {
                    Entity pcq = entry.getValue();
                    pcq.getDataDefinition().delete(pcq.getId());
                }
            }

            List<Entity> pcqs = Lists.newArrayList();
            for (MaterialDto material : addedMaterials) {
                Entity pcq = createProductionCoutingQuantity(order, material, toc);
                pcqs.add(pcq);
            }

            fillFlow(pcqs, order);
            pcqs.forEach(p -> p.getDataDefinition().save(p));
        }

    }

    private void modifyProductionCountingQuantity(Entity order, List<MaterialDto> materials) {
        List<MaterialDto> addedMaterials = materials.stream().filter(m -> Objects.isNull(m.getProductInId()))
                .collect(Collectors.toList());
        List<Long> technologyMaterials = materials.stream().filter(m -> Objects.nonNull(m.getProductInId()))
                .map(m -> m.getProductId()).collect(Collectors.toList());
        List<Entity> materialsFromOrderPCQ = getMaterialsFromOrder(order, null);

        Map<Long, Entity> pacqByProductId = materialsFromOrderPCQ.stream().collect(
                Collectors.toMap(pcq -> pcq.getBelongsToField(L_PRODUCT).getId(), pcq -> pcq));

        for (Map.Entry<Long, Entity> entry : pacqByProductId.entrySet()) {
            if (!technologyMaterials.contains(entry.getKey())) {
                Entity pcq = entry.getValue();
                pcq.getDataDefinition().delete(pcq.getId());
            }
        }

        List<Entity> pcqs = Lists.newArrayList();
        for (MaterialDto material : addedMaterials) {
            Entity toc = order.getBelongsToField(OrderFields.TECHNOLOGY).getTreeField(TechnologyFields.OPERATION_COMPONENTS)
                    .getRoot();
            Entity pcq = createProductionCoutingQuantity(order, material, toc);
            pcqs.add(pcq);
        }

        fillFlow(pcqs, order);
        pcqs.forEach(p -> p.getDataDefinition().save(p));
    }

    //override be aspect
    public void fillFlow(final List<Entity> productionCountingQuantities, final Entity order) {


    }

    private Entity createProductionCoutingQuantity(Entity order, MaterialDto material, Entity toc) {
        Entity productionCountingQuantity = dataDefinitionService
                .get(L_BASIC_PRODUCTION_COUNTING, L_PRODUCTION_COUNTING_QUANTITY).create();
        productionCountingQuantity.setField(L_ORDER, order.getId());
        productionCountingQuantity.setField(L_TECHNOLOGY_OPERATION_COMPONENT, toc.getId());

        BigDecimal q = order.getDecimalField(OrderFields.PLANNED_QUANTITY).multiply(material.getQuantityPerUnit(),
                numberService.getMathContext());
        productionCountingQuantity.setField(L_PLANNED_QUANTITY, q);
        productionCountingQuantity.setField(OrderCreationService.L_PRODUCT, material.getProductId());
        productionCountingQuantity.setField(L_ROLE, L_USED);
        productionCountingQuantity.setField(L_TYPE_OF_MATERIAL, L_COMPONENT);
        productionCountingQuantity.setField(L_FLOW_FILLED, Boolean.TRUE);
        return productionCountingQuantity;
    }

    private List<Entity> getMaterialsFromOrder(Entity order, Entity toc) {
        SearchCriteriaBuilder scb = order.getHasManyField(L_PRODUCTION_COUNTING_QUANTITIES).find()
                .add(SearchRestrictions.eq(OrderCreationService.L_ROLE, OrderCreationService.L_USED));
        if (Objects.nonNull(toc)) {
            scb.add(SearchRestrictions.belongsTo("technologyOperationComponent", toc));
        }
        scb.add(SearchRestrictions.eq(OrderCreationService.L_TYPE_OF_MATERIAL, OrderCreationService.L_COMPONENT));

        return scb.list().getEntities();
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

    private String buildDescription(Entity parameter, String description, Entity technology) {
        boolean fillOrderDescriptionBasedOnTechnology = parameter
                .getBooleanField(ParameterFieldsO.FILL_ORDER_DESCRIPTION_BASED_ON_TECHNOLOGY_DESCRIPTION);

        StringBuilder descriptionBuilder = new StringBuilder();

        descriptionBuilder.append(description);

        if (fillOrderDescriptionBasedOnTechnology && Objects.nonNull(technology)
                && StringUtils.isNoneBlank(technology.getStringField(TechnologyFields.DESCRIPTION))) {
            if (StringUtils.isNoneBlank(descriptionBuilder.toString())) {
                descriptionBuilder.append("\n");
            }
            descriptionBuilder.append(technology.getStringField(TechnologyFields.DESCRIPTION));

        }

        return descriptionBuilder.toString();
    }

    private Either<String, Entity> getOrCreateTechnology(OrderCreationRequest orderCreationRequest) {
        if (Objects.isNull(orderCreationRequest.getTechnologyId())) {
            if (orderCreationRequest.getTechnologyOperations().isEmpty()) {
                return createTechnology(orderCreationRequest);
            } else {
                return createTechnologyForEachOperation(orderCreationRequest);
            }
        } else {
            return Either.right(dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY).get(orderCreationRequest.getTechnologyId()));
        }
    }

    private Either<String, Entity> createTechnologyForEachOperation(OrderCreationRequest orderCreationRequest) {
        Entity product = getProduct(orderCreationRequest.getProductId());
        Entity parameter = parameterService.getParameter();
        Entity dashboardComponentsLocation = parameter.getBelongsToField(OrderCreationService.L_DASHBOARD_COMPONENTS_LOCATION);
        Entity dashboardProductsInputLocation = parameter
                .getBelongsToField(OrderCreationService.L_DASHBOARD_PRODUCTS_INPUT_LOCATION);
        orderCreationRequest.getTechnologyOperations().sort(Comparator.comparing(TechnologyOperationDto::getNode));

        String range = parameter.getStringField(L_RANGE);
        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).create();
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
            Entity operation = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_OPERATION).get(technologyOperation.getOperationId());

            Entity toc = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).create();
            toc.setField(TechnologyOperationComponentFields.OPERATION, operation);
            toc.setField(TechnologyOperationComponentFields.ENTITY_TYPE, L_OPERATION);
            for (String fieldName : FIELDS_OPERATION) {
                toc.setField(fieldName, operation.getField(fieldName));
            }
            if (operation.getField(NEXT_OPERATION_AFTER_PRODUCED_TYPE) == null) {
                toc.setField(NEXT_OPERATION_AFTER_PRODUCED_TYPE, L_ALL);
            }

            if (operation.getField(PRODUCTION_IN_ONE_CYCLE) == null) {
                toc.setField(PRODUCTION_IN_ONE_CYCLE, "1");
            }

            if (operation.getField(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY) == null) {
                toc.setField(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY, "0");
            }

            if (Objects.isNull(parent)) {
                Entity topoc = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT).create();
                topoc.setField(OperationProductOutComponentFields.QUANTITY, BigDecimal.ONE);
                topoc.setField(OperationProductOutComponentFields.PRODUCT, product);
                toc.setField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS, Lists.newArrayList(topoc));
            }

            List<Entity> topics = Lists.newArrayList();
            for (MaterialDto material : technologyOperation.getMaterials()) {
                Entity inProduct = getProduct(material.getProductId());

                Entity topic = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).create();
                topic.setField(OperationProductInComponentFields.PRODUCT, inProduct);
                topic.setField(OperationProductInComponentFields.QUANTITY, material.getQuantityPerUnit());
                topics.add(topic);
            }
            toc.setField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS, topics);
            if (Objects.nonNull(parent)) {
                toc.setField(TechnologyOperationComponentFields.PARENT, parent.getId());
            }
            toc.setField(TechnologyOperationComponentFields.TECHNOLOGY, technology.getId());
            toc = toc.getDataDefinition().save(toc);
            if (Objects.nonNull(technologyOperation.getWorkstationId())) {
                List<Entity> workstations = Lists.newArrayList(toc
                        .getHasManyField(TechnologyOperationComponentFields.WORKSTATIONS));

                Entity workstation = dataDefinitionService
                        .get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_WORKSTATION).get(
                                technologyOperation.getWorkstationId());
                workstations.add(workstation);
                toc.setField(TechnologyOperationComponentFields.WORKSTATIONS, workstations);
            }
            toc = toc.getDataDefinition().save(toc);
            technologyOperation.setId(toc.getId());
            if (toc.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS).isEmpty()) {
                Entity topoc = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT).create();
                topoc.setField(OperationProductOutComponentFields.QUANTITY, BigDecimal.ONE);
                topoc.setField(OperationProductOutComponentFields.PRODUCT, getOrCreateProduct(operation));
                topoc.setField(OperationProductOutComponentFields.OPERATION_COMPONENT, toc);
                topoc.getDataDefinition().save(topoc);
            }
            parent = toc;

        }

        if (technology.isValid()) {
            final StateChangeContext technologyStateChangeContext = stateChangeContextBuilder
                    .build(technologyStateChangeAspect.getChangeEntityDescriber(), technology,
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

    private Either<String, Entity> createTechnology(OrderCreationRequest orderCreationRequest) {
        Entity product = getProduct(orderCreationRequest.getProductId());
        Entity parameter = parameterService.getParameter();
        Entity operation = parameter.getBelongsToField(L_DASHBOARD_OPERATION);
        Entity dashboardComponentsLocation = parameter.getBelongsToField(OrderCreationService.L_DASHBOARD_COMPONENTS_LOCATION);
        Entity dashboardProductsInputLocation = parameter
                .getBelongsToField(OrderCreationService.L_DASHBOARD_PRODUCTS_INPUT_LOCATION);

        Entity toc = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).create();
        toc.setField(TechnologyOperationComponentFields.OPERATION, operation);
        toc.setField(TechnologyOperationComponentFields.ENTITY_TYPE, L_OPERATION);
        for (String fieldName : FIELDS_OPERATION) {
            toc.setField(fieldName, operation.getField(fieldName));
        }
        if (operation.getField(NEXT_OPERATION_AFTER_PRODUCED_TYPE) == null) {
            toc.setField(NEXT_OPERATION_AFTER_PRODUCED_TYPE, L_ALL);
        }

        if (operation.getField(PRODUCTION_IN_ONE_CYCLE) == null) {
            toc.setField(PRODUCTION_IN_ONE_CYCLE, "1");
        }

        if (operation.getField(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY) == null) {
            toc.setField(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY, "0");
        }
        Entity topoc = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT).create();
        topoc.setField(OperationProductOutComponentFields.PRODUCT, product);
        topoc.setField(OperationProductOutComponentFields.QUANTITY, BigDecimal.ONE);
        toc.setField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS, Lists.newArrayList(topoc));

        List<Entity> topics = Lists.newArrayList();
        for (MaterialDto material : orderCreationRequest.getMaterials()) {
            Entity inProduct = getProduct(material.getProductId());

            Entity topic = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).create();
            topic.setField(OperationProductInComponentFields.PRODUCT, inProduct);
            topic.setField(OperationProductInComponentFields.QUANTITY, material.getQuantityPerUnit());
            topics.add(topic);
        }
        toc.setField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS, topics);

        String range = parameter.getStringField(L_RANGE);
        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).create();
        technology.setField(TechnologyFields.NUMBER, technologyNameAndNumberGenerator.generateNumber(product));
        technology.setField(TechnologyFields.NAME, technologyNameAndNumberGenerator.generateName(product));
        technology.setField(TechnologyFields.PRODUCT, product);
        technology.setField(TechnologyFields.EXTERNAL_SYNCHRONIZED, true);
        technology.setField(TechnologyFields.OPERATION_COMPONENTS, Lists.newArrayList(toc));
        technology.setField(L_RANGE, range);
        technology.setField("componentsLocation", dashboardComponentsLocation);
        technology.setField("productsInputLocation", dashboardProductsInputLocation);
        technology.setField("typeOfProductionRecording", "02cumulated");
        technology = technology.getDataDefinition().save(technology);
        if (technology.isValid()) {
            final StateChangeContext technologyStateChangeContext = stateChangeContextBuilder
                    .build(technologyStateChangeAspect.getChangeEntityDescriber(), technology,
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

    private Entity getProductionLine(Long productionLineId) {
        if (Objects.nonNull(productionLineId)) {
            return dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                    ProductionLinesConstants.MODEL_PRODUCTION_LINE).get(productionLineId);
        } else {
            return parameterService.getParameter().getBelongsToField(ParameterFieldsPL.DEFAULT_PRODUCTION_LINE);
        }
    }

    private Entity getProduct(Long productId) {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(productId);
    }

    private Entity getOrCreateProduct(Entity operation) {
        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).find()
                .add(SearchRestrictions.eq(ProductFields.NUMBER, operation.getStringField(OperationFields.NUMBER)))
                .setMaxResults(1).uniqueResult();
        if (Objects.nonNull(product)) {
            return product;
        } else {
            Entity newProduct = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT)
                    .create();
            newProduct.setField(ProductFields.NUMBER, operation.getStringField(OperationFields.NUMBER));
            newProduct.setField(ProductFields.NAME, operation.getStringField(OperationFields.NAME));
            newProduct.setField(ProductFields.GLOBAL_TYPE_OF_MATERIAL, GlobalTypeOfMaterial.INTERMEDIATE.getStringValue());
            newProduct.setField(ProductFields.ENTITY_TYPE, ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue());
            newProduct.setField(ProductFields.UNIT, parameterService.getParameter().getStringField(ParameterFields.UNIT));
            return newProduct.getDataDefinition().save(newProduct);
        }
    }
}
