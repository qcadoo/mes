package com.qcadoo.mes.masterOrders;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.lineChangeoverNorms.ChangeoverNormsService;
import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields;
import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderPositionDtoFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.masterOrders.constants.ParameterFieldsMO;
import com.qcadoo.mes.masterOrders.hooks.MasterOrderPositionStatus;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.DictionaryService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.exception.EntityRuntimeException;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.constants.DictionaryItemFields;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.view.api.utils.NumberGeneratorService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import static com.qcadoo.mes.orders.constants.OrderFields.PRODUCTION_LINE;

@Service
public class OrdersFromMOProductsGenerationService {

    private static final String IS_SUBCONTRACTED = "isSubcontracted";

    private static final String CREATE_COLLECTIVE_ORDERS = "createCollectiveOrders";

    private static final String L_MASTER_ORDER_POSITION_STATUS = "masterOrderPositionStatus";

    private static final List<String> L_TECHNOLOGY_FIELD_NAMES = Lists.newArrayList("registerQuantityInProduct",
            "registerQuantityOutProduct", "registerProductionTime", "registerPiecework", "justOne", "allowToClose",
            "autoCloseOrder", "typeOfProductionRecording");

    private static final String ORDERS_GENERATION_NOT_COMPLETE_DATES = "ordersGenerationNotCompleteDates";

    private static final String CUMULATED_MASTER_ORDER_QUANTITY = "cumulatedMasterOrderQuantity";

    private static final String COPY_NOTES_FROM_MASTER_ORDER_POSITION = "copyNotesFromMasterOrderPosition";

    private static final String PPS_IS_AUTOMATIC = "ppsIsAutomatic";

    private static final String IGNORE_MISSING_COMPONENTS = "ignoreMissingComponents";

    private static final String PARAMETER_AUTOMATICALLY_GENERATE_ORDERS_FOR_COMPONENTS = "automaticallyGenerateOrdersForComponents";

    private static final String PARAMETER_ORDERS_GENERATED_BY_COVERAGE = "ordersGeneratedByCoverage";

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ChangeoverNormsService changeoverNormsService;

    @Autowired
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private DictionaryService dictionaryService;

    public GenerationOrderResult generateOrders(List<Entity> masterOrderProducts, Date start, Date finish, boolean generatePPS) {
        GenerationOrderResult result = new GenerationOrderResult(translationService);
        boolean automaticPps = parameterService.getParameter().getBooleanField(PPS_IS_AUTOMATIC);

        List<Entity> masterOrderProductsEntities = Lists.newArrayList();

        masterOrderProducts.forEach(masterOrderProduct -> {
            Optional<Entity> dtoEntity = Optional.ofNullable(masterOrderProduct.getDataDefinition().getMasterModelEntity(
                    masterOrderProduct.getId()));
            if (dtoEntity.isPresent()) {
                masterOrderProductsEntities.add(dtoEntity.get());
            } else {
                masterOrderProductsEntities.add(masterOrderProduct);
            }
        });

        boolean createCollectiveOrders = parameterService.getParameter().getBooleanField(CREATE_COLLECTIVE_ORDERS);
        if (createCollectiveOrders) {
            Map<ProductTechnologyKey, List<Entity>> groupedMap = groupPositions(masterOrderProductsEntities);

            for (Map.Entry<ProductTechnologyKey, List<Entity>> entry : groupedMap.entrySet()) {
                String hql = "SELECT SUM(mop.quantityRemainingToOrderWithoutStock) AS quantityRemainingToOrder FROM #masterOrders_masterOrderPositionDto mop "
                        + " WHERE mop.id IN (:ids)";

                SearchQueryBuilder scb = dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                        MasterOrdersConstants.MODEL_MASTER_ORDER_POSITION_DTO).find(hql);

                scb.setParameterList("ids", entry.getValue().stream().map(e -> e.getId()).collect(Collectors.toList()));

                Entity quantityRemainingToOrderResult = scb.setMaxResults(1).uniqueResult();

                MasterOrderProduct masterOrderProduct = MasterOrderProduct.newMasterOrderProduct()
                        .createCollectiveOrders(createCollectiveOrders).product(entry.getKey().getProduct())
                        .technology(entry.getKey().getTechnology()).groupedMasterOrderProduct(entry.getValue())
                        .quantityRemainingToOrder(quantityRemainingToOrderResult.getDecimalField("quantityRemainingToOrder"))
                        .build();
                generateOrder(generatePPS, automaticPps, result, masterOrderProduct, start, finish);

            }

        } else {
            masterOrderProductsEntities
                    .forEach(mop -> {
                        BigDecimal quantityRemainingToOrder = dataDefinitionService
                                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                                        MasterOrdersConstants.MODEL_MASTER_ORDER_POSITION_DTO).get(mop.getId())
                                .getDecimalField(MasterOrderPositionDtoFields.QUANTITY_REMAINING_TO_ORDER_WITHOUT_STOCK);
                        MasterOrderProduct masterOrderProduct = MasterOrderProduct.newMasterOrderProduct()
                                .createCollectiveOrders(createCollectiveOrders)
                                .product(mop.getBelongsToField(MasterOrderProductFields.PRODUCT))
                                .technology(mop.getBelongsToField(MasterOrderProductFields.TECHNOLOGY))
                                .masterOrder(mop.getBelongsToField(MasterOrderProductFields.MASTER_ORDER))
                                .comments(mop.getStringField(MasterOrderProductFields.COMMENTS))
                                .quantityRemainingToOrder(quantityRemainingToOrder).masterOrderProduct(mop).build();
                        generateOrder(generatePPS, automaticPps, result, masterOrderProduct, start, finish);

                    });
        }
        MasterOrderProduct.newMasterOrderProduct().createCollectiveOrders(createCollectiveOrders);
        return result;
    }

    private Map<ProductTechnologyKey, List<Entity>> groupPositions(List<Entity> masterOrderProductsEntities) {
        Map<ProductTechnologyKey, List<Entity>> groupedMap = Maps.newHashMap();

        masterOrderProductsEntities.forEach(mop -> {
            ProductTechnologyKey key = new ProductTechnologyKey(mop);
            if (Objects.isNull(key.getTechnology())) {
                Entity technology = technologyServiceO.getDefaultTechnology(key.getProduct());
                if (Objects.nonNull(technology)) {
                    key.setTechnology(technology);
                    key.setTechnologyId(technology.getId());
                }
            }
            if (groupedMap.containsKey(key)) {
                List<Entity> values = groupedMap.get(key);
                values.add(mop);
                groupedMap.put(key, values);
            } else {
                List<Entity> values = Lists.newArrayList(mop);
                groupedMap.put(key, values);
            }
        });
        return groupedMap;
    }

    private void generateOrder(boolean generatePPS, boolean automaticPps, GenerationOrderResult result,
            MasterOrderProduct masterOrderProduct, Date start, Date finish) {

        if (PluginUtils.isEnabled("integrationBaseLinker")) {
            createDocuments();
        }

        Entity parameter = parameterService.getParameter();

        boolean realizationFromStock = parameter.getBooleanField(ParameterFieldsO.REALIZATION_FROM_STOCK);
        boolean alwaysOrderItemsWithPersonalization = parameter
                .getBooleanField(ParameterFieldsO.ALWAYS_ORDER_ITEMS_WITH_PERSONALIZATION)
                && StringUtils.isNoneEmpty(masterOrderProduct.getComments());
        if (alwaysOrderItemsWithPersonalization) {
            realizationFromStock = false;
        }

        BigDecimal stockQuantity = BigDecimal.ZERO;
        BigDecimal quantityRemainingToOrder = masterOrderProduct.getQuantityRemainingToOrder();
        if (realizationFromStock) {
            List<Entity> locations = parameter.getHasManyField(ParameterFieldsO.REALIZATION_LOCATIONS);
            Entity product = masterOrderProduct.getProduct();
            for (Entity location : locations) {
                Map<Long, BigDecimal> productQuantity = materialFlowResourcesService.getQuantitiesForProductsAndLocation(
                        Lists.newArrayList(product), location);
                if (productQuantity.containsKey(product.getId())) {
                    stockQuantity = stockQuantity.add(productQuantity.get(product.getId()), numberService.getMathContext());
                }
            }

        }

        if (realizationFromStock && stockQuantity.compareTo(quantityRemainingToOrder) >= 0
                && quantityRemainingToOrder.compareTo(BigDecimal.ZERO) > 0) {
            if (!masterOrderProduct.isCreateCollectiveOrders()) {
                Entity masterOrderProductEntity = masterOrderProduct.getMasterOrderProduct();

                masterOrderProductEntity.setField(MasterOrderProductFields.QUANTITY_TAKEN_FROM_WAREHOUSE,
                        quantityRemainingToOrder);
                setMasterOrderPositionStatus(masterOrderProductEntity, MasterOrderPositionStatus.ORDERED);
                masterOrderProductEntity.getDataDefinition().save(masterOrderProductEntity);
            }
            result.addRealizationFromStock(masterOrderProduct.getProduct().getStringField(ProductFields.NUMBER));
        } else {
            Entity order = createOrder(masterOrderProduct, realizationFromStock, quantityRemainingToOrder, stockQuantity, start,
                    finish);
            order = getOrderDD().save(order);
            if (!order.isValid()) {
                MasterOrderProductErrorContainer productErrorContainer = new MasterOrderProductErrorContainer();
                productErrorContainer.setProduct(masterOrderProduct.getProduct().getStringField(ProductFields.NUMBER));
                if (Objects.nonNull(masterOrderProduct.getMasterOrder())) {
                    productErrorContainer.setMasterOrder(masterOrderProduct.getMasterOrder().getStringField(
                            MasterOrderFields.NUMBER));
                } else {
                    productErrorContainer.setMasterOrder(extractMasterOrdersNumbers(masterOrderProduct));
                }
                productErrorContainer.setQuantity(order.getDecimalField(OrderFields.PLANNED_QUANTITY));
                productErrorContainer.setErrorMessages(order.getGlobalErrors());
                result.addNotGeneratedProductError(productErrorContainer);
            } else {
                result.addGeneratedOrderNumber(order.getStringField(OrderFields.NUMBER));
            }

            if (order.isValid()) {
                if (Objects.isNull(order.getBelongsToField(OrderFields.TECHNOLOGY))) {
                    result.addOrderWithoutGeneratedSubOrders(new SubOrderErrorHolder(order.getStringField(OrderFields.NUMBER),
                            "masterOrders.masterOrder.generationOrder.ordersWithoutGeneratedSubOrders.technologyNotSet"));
                } else if (parameter.getBooleanField(PARAMETER_AUTOMATICALLY_GENERATE_ORDERS_FOR_COMPONENTS)
                        && parameter.getBooleanField(PARAMETER_ORDERS_GENERATED_BY_COVERAGE)
                        && Objects.nonNull(order.getDateField(OrderFields.DATE_FROM))
                        && order.getDateField(OrderFields.DATE_FROM).before(new Date())) {
                    result.addOrderWithoutGeneratedSubOrders(new SubOrderErrorHolder(order.getStringField(OrderFields.NUMBER),
                            "masterOrders.masterOrder.generationOrder.ordersWithoutGeneratedSubOrders.orderStartDateEarlierThanToday"));
                } else {
                    generateSubOrders(result, order);
                }
            }

            if (order.isValid() && generatePPS && automaticPps
                    && !parameter.getBooleanField(ORDERS_GENERATION_NOT_COMPLETE_DATES)) {
                List<Entity> orders = getOrderAndSubOrders(order.getId());
                Collections.reverse(orders);
                Integer lastLevel = null;
                Date lastDate = null;
                for (Entity ord : orders) {

                    Date calculatedOrderStartDate = null;
                    if (Objects.isNull(ord.getDateField(OrderFields.DATE_FROM))) {
                        Optional<Entity> maybeOrder = findLastOrder(ord);
                        if (maybeOrder.isPresent()) {
                            calculatedOrderStartDate = ord.getDateField(OrderFields.FINISH_DATE);
                        } else {
                            calculatedOrderStartDate = new DateTime().toDate();
                        }
                    } else {
                        Optional<Entity> maybeOrder = findPreviousOrder(ord);
                        if (maybeOrder.isPresent()) {
                            calculatedOrderStartDate = maybeOrder.get().getDateField(OrderFields.FINISH_DATE);

                        } else {
                            calculatedOrderStartDate = ord.getDateField(OrderFields.FINISH_DATE);
                        }
                    }

                    if (Objects.isNull(calculatedOrderStartDate)) {
                        calculatedOrderStartDate = new DateTime().toDate();
                    }

                    if (Objects.nonNull(lastLevel) && !Objects.equals(lastLevel, ord.getIntegerField("level"))) {
                        if (Objects.nonNull(lastDate) && calculatedOrderStartDate.before(lastDate)) {
                            calculatedOrderStartDate = lastDate;
                        }
                    }

                    try {
                        Date finishDate = tryGeneratePPS(ord, calculatedOrderStartDate);
                        if (Objects.nonNull(lastDate) && finishDate.after(lastDate)) {
                            lastDate = finishDate;
                        } else if (Objects.isNull(lastDate)) {
                            lastDate = finishDate;
                        }
                    } catch (Exception ex) {
                        result.addOrderWithoutPps(ord.getStringField(OrderFields.NUMBER));
                        break;
                    }
                    lastLevel = ord.getIntegerField("level");

                }

            }

        }

    }

    public Optional<Entity> findLastOrder(final Entity order) {
        Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);
        Entity lastOrder = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.isNotNull(OrderFields.FINISH_DATE))
                .add(SearchRestrictions.belongsTo(OrderFields.PRODUCTION_LINE, productionLine))
                .add(SearchRestrictions.ne(OrderFields.STATE, OrderState.ABANDONED.getStringValue()))
                .addOrder(SearchOrders.desc(OrderFields.FINISH_DATE)).setMaxResults(1).uniqueResult();
        return Optional.ofNullable(lastOrder);
    }

    public Optional<Entity> findNextOrder(final Entity order) {
        Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);
        Entity nextOrder = dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)
                .find()
                .add(SearchRestrictions.belongsTo(OrderFields.PRODUCTION_LINE, productionLine))
                .add(SearchRestrictions.or(SearchRestrictions.eq(OrderFields.STATE, OrderState.PENDING.getStringValue()),
                        SearchRestrictions.eq(OrderFields.STATE, OrderState.IN_PROGRESS.getStringValue()),
                        SearchRestrictions.eq(OrderFields.STATE, OrderState.ACCEPTED.getStringValue())))
                .add(SearchRestrictions.gt(OrderFields.START_DATE, order.getDateField(OrderFields.START_DATE)))
                .addOrder(SearchOrders.asc(OrderFields.START_DATE)).setMaxResults(1).uniqueResult();
        return Optional.ofNullable(nextOrder);
    }

    /*
     * override by aspect
     */
    public void generateSubOrders(GenerationOrderResult result, Entity order) {

    }

    /*
     * override by aspect
     */
    public void createDocuments() {

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void tryGeneratePPS(final Entity order) {
        Date startDate = findStartDate(order);
        generateEmptyPpsForOrder(order);
        order.setField("generatePPS", true);
        order.setField(OrderFields.START_DATE, startDate);
        order.setField(OrderFields.FINISH_DATE, new DateTime(order.getDateField(OrderFields.START_DATE)).plusDays(1).toDate());
        Entity storedOrder = order.getDataDefinition().save(order);
        if (!storedOrder.isValid()) {
            throw new EntityRuntimeException(storedOrder);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Date tryGeneratePPS(final Entity order, Date date) {
        Date startDate = findStartDate(order, date);
        generateEmptyPpsForOrder(order);
        order.setField("generatePPS", true);
        order.setField(OrderFields.START_DATE, startDate);
        order.setField(OrderFields.FINISH_DATE, new DateTime(order.getDateField(OrderFields.START_DATE)).plusDays(1).toDate());
        Entity storedOrder = order.getDataDefinition().save(order);
        if (!storedOrder.isValid()) {
            throw new EntityRuntimeException(storedOrder);
        }
        return order.getDateField(OrderFields.FINISH_DATE);
    }

    private void generateEmptyPpsForOrder(Entity order) {
        Entity productionPerShift = dataDefinitionService.get("productionPerShift", "productionPerShift").find()
                .add(SearchRestrictions.belongsTo("order", order)).setMaxResults(1).uniqueResult();
        if (productionPerShift != null) {
            return;
        }
        boolean shouldBeCorrected = OrderState.of(order).compareTo(OrderState.PENDING) != 0;
        productionPerShift = dataDefinitionService.get("productionPerShift", "productionPerShift").create();
        productionPerShift.setField("order", order);
        if (shouldBeCorrected) {
            productionPerShift.setField("plannedProgressType", "02corrected");
        } else {
            productionPerShift.setField("plannedProgressType", "01planned");
        }
        productionPerShift.getDataDefinition().save(productionPerShift);
    }

    private Date findStartDate(final Entity order) {
        if (Objects.nonNull(order.getDateField(OrderFields.START_DATE))) {
            return order.getDateField(OrderFields.START_DATE);
        }

        Optional<Entity> previousOrder = findPreviousOrder(order);
        if (previousOrder.isPresent()) {
            Integer changeoverDurationInMillis = getChangeoverDurationInMillis(previousOrder.get(), order);
            Optional<DateTime> maybeDate = shiftsService.getNearestWorkingDate(
                    new DateTime(previousOrder.get().getDateField(OrderFields.FINISH_DATE)),
                    order.getBelongsToField(OrderFields.PRODUCTION_LINE));
            if (maybeDate.isPresent()) {
                return calculateOrderStartDate(maybeDate.get().toDate(), changeoverDurationInMillis);
            }
        }

        return DateTime.now().toDate();
    }

    private Date findStartDate(final Entity order, Date startDate) {

        Optional<Entity> previousOrder = findPreviousOrder(order);
        if (previousOrder.isPresent()) {
            Integer changeoverDurationInMillis = getChangeoverDurationInMillis(previousOrder.get(), order);
            Optional<DateTime> maybeDate = shiftsService.getNearestWorkingDate(new DateTime(startDate),
                    order.getBelongsToField(OrderFields.PRODUCTION_LINE));
            if (maybeDate.isPresent()) {
                return calculateOrderStartDate(maybeDate.get().toDate(), changeoverDurationInMillis);
            }
        }

        return startDate;
    }

    private Date calculateOrderStartDate(Date finishDate, Integer changeoverDurationInMillis) {
        DateTime finishDateTime = new DateTime(finishDate);
        finishDateTime = finishDateTime.plusMillis(changeoverDurationInMillis);
        return finishDateTime.toDate();
    }

    public Optional<Entity> findPreviousOrder(final Entity order) {
        Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);
        Entity nextOrder = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.belongsTo(OrderFields.PRODUCTION_LINE, productionLine))
                .add(SearchRestrictions.isNotNull(OrderFields.START_DATE)).addOrder(SearchOrders.desc(OrderFields.START_DATE))
                .setMaxResults(1).uniqueResult();
        return Optional.ofNullable(nextOrder);
    }

    public Integer getChangeoverDurationInMillis(Entity previousOrder, final Entity nextOrder) {
        Entity fromTechnology = previousOrder.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);
        Entity toTechnology = nextOrder.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);
        Entity productionLine = nextOrder.getBelongsToField(PRODUCTION_LINE);
        Entity changeover = changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology, productionLine);
        if (changeover != null) {
            Integer duration = changeover.getIntegerField(LineChangeoverNormsFields.DURATION);
            if (duration == null) {
                return 0;
            }
            return duration * 1000;
        }
        return 0;
    }

    private Entity createOrder(final MasterOrderProduct masterOrderProduct, final boolean realizationFromStock,
            BigDecimal quantityRemainingToOrder, final BigDecimal stockQuantity, Date start, Date finish) {
        Entity parameter = parameterService.getParameter();

        Entity product = masterOrderProduct.getProduct();
        Entity technology = getTechnology(masterOrderProduct);

        Entity order = getOrderDD().create();

        if (masterOrderProduct.isCreateCollectiveOrders()) {
            fillDates(parameter, order, null, start, finish);
        } else {
            Entity masterOrder = masterOrderProduct.getMasterOrder();
            Date masterOrderDeadline = masterOrder.getDateField(MasterOrderFields.DEADLINE);
            Date masterOrderStartDate = masterOrder.getDateField(MasterOrderFields.START_DATE);
            Date masterOrderFinishDate = masterOrder.getDateField(MasterOrderFields.FINISH_DATE);
            order.setField(OrderFields.COMPANY, masterOrder.getBelongsToField(MasterOrderFields.COMPANY));
            order.setField(OrderFields.ADDRESS, masterOrder.getBelongsToField(MasterOrderFields.ADDRESS));
            order.setField(OrderFieldsMO.MASTER_ORDER, masterOrder);
            fillDates(parameter, order, masterOrderDeadline, masterOrderStartDate, masterOrderFinishDate);
        }

        order.setField(OrderFields.NUMBER, generateOrderNumber(masterOrderProduct));
        order.setField(OrderFields.NAME, generateOrderName(product, technology));

        order.setField(OrderFields.PRODUCT, product);
        order.setField(OrderFields.TECHNOLOGY_PROTOTYPE, technology);
        order.setField(OrderFields.PRODUCTION_LINE, orderService.getProductionLine(technology));
        order.setField(OrderFields.DIVISION, orderService.getDivision(technology));

        order.setField(OrderFields.EXTERNAL_SYNCHRONIZED, true);
        order.setField(IS_SUBCONTRACTED, false);
        order.setField(OrderFields.STATE, OrderStateStringValues.PENDING);
        order.setField(OrderFields.ORDER_TYPE, OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue());
        if (realizationFromStock) {
            if (stockQuantity.compareTo(BigDecimal.ZERO) > 0 && stockQuantity.compareTo(quantityRemainingToOrder) < 0) {
                if (!masterOrderProduct.isCreateCollectiveOrders()) {
                    Entity masterOrderProductEntity = masterOrderProduct.getMasterOrderProduct();
                    masterOrderProductEntity.setField(MasterOrderProductFields.QUANTITY_TAKEN_FROM_WAREHOUSE, stockQuantity);
                    masterOrderProductEntity.getDataDefinition().save(masterOrderProductEntity);
                }
                order.setField(OrderFields.PLANNED_QUANTITY,
                        quantityRemainingToOrder.subtract(stockQuantity, numberService.getMathContext()));
            } else {
                if (!masterOrderProduct.isCreateCollectiveOrders()) {
                    Entity masterOrderProductEntity = masterOrderProduct.getMasterOrderProduct();
                    masterOrderProductEntity.setField(MasterOrderProductFields.QUANTITY_TAKEN_FROM_WAREHOUSE, BigDecimal.ZERO);
                    masterOrderProductEntity.getDataDefinition().save(masterOrderProductEntity);
                }
                order.setField(OrderFields.PLANNED_QUANTITY, quantityRemainingToOrder);

            }
        } else {
            order.setField(OrderFields.PLANNED_QUANTITY, quantityRemainingToOrder);

        }
        if (!masterOrderProduct.isCreateCollectiveOrders()) {
            Entity masterOrderProductEntity = masterOrderProduct.getMasterOrderProduct();
            setMasterOrderPositionStatus(masterOrderProductEntity, MasterOrderPositionStatus.ORDERED);
            masterOrderProductEntity.getDataDefinition().save(masterOrderProductEntity);
        } else {
            for (Entity mop : masterOrderProduct.getGroupedMasterOrderProduct()) {
                setMasterOrderPositionStatus(mop, MasterOrderPositionStatus.ORDERED);
                mop = mop.getDataDefinition().save(mop);
                mop.isValid();
            }
        }

        order.setField(IGNORE_MISSING_COMPONENTS, parameter.getBooleanField(IGNORE_MISSING_COMPONENTS));

        String orderDescription = buildDescription(parameter, masterOrderProduct, technology);
        order.setField(OrderFields.DESCRIPTION, orderDescription);
        fillPCParametersForOrder(order, technology);
        return order;
    }

    private void fillDates(Entity parameter, Entity order, Date masterOrderDeadline, Date masterOrderStartDate,
            Date masterOrderFinishDate) {
        if (!parameter.getBooleanField(ORDERS_GENERATION_NOT_COMPLETE_DATES)) {
            order.setField(OrderFields.DATE_FROM, masterOrderStartDate);
            order.setField(OrderFields.DATE_TO, masterOrderFinishDate);
            order.setField(OrderFields.DEADLINE, masterOrderDeadline);
        }
    }

    public String buildDescription(Entity parameter, MasterOrderProduct masterOrderProduct, Entity technology) {
        boolean copyDescription = parameter.getBooleanField(ParameterFieldsMO.COPY_DESCRIPTION);
        boolean copyNotesFromMasterOrderPosition = parameter.getBooleanField(COPY_NOTES_FROM_MASTER_ORDER_POSITION);
        boolean fillOrderDescriptionBasedOnTechnology = parameter
                .getBooleanField(ParameterFieldsO.FILL_ORDER_DESCRIPTION_BASED_ON_TECHNOLOGY_DESCRIPTION);

        StringBuilder descriptionBuilder = new StringBuilder();

        if (!masterOrderProduct.isCreateCollectiveOrders()) {

            if (copyDescription
                    && StringUtils.isNoneBlank(masterOrderProduct.getMasterOrder().getStringField(MasterOrderFields.DESCRIPTION))) {
                descriptionBuilder.append(masterOrderProduct.getMasterOrder().getStringField(MasterOrderFields.DESCRIPTION));
            }

            if (copyNotesFromMasterOrderPosition && StringUtils.isNoneBlank(masterOrderProduct.getComments())) {
                if (StringUtils.isNoneBlank(descriptionBuilder.toString())) {
                    descriptionBuilder.append("\n");
                }
                descriptionBuilder.append(masterOrderProduct.getComments());
            }
        } else {
            descriptionBuilder.append(translationService.translate("masterOrders.generatingOrders.basedOnMasterOrders",
                    LocaleContextHolder.getLocale(), extractMasterOrdersNumbers(masterOrderProduct)));
        }

        if (fillOrderDescriptionBasedOnTechnology && Objects.nonNull(technology)
                && StringUtils.isNoneBlank(technology.getStringField(TechnologyFields.DESCRIPTION))) {
            if (StringUtils.isNoneBlank(descriptionBuilder.toString())) {
                descriptionBuilder.append("\n");
            }
            descriptionBuilder.append(technology.getStringField(TechnologyFields.DESCRIPTION));

        }

        return descriptionBuilder.toString();
    }

    private String extractMasterOrdersNumbers(MasterOrderProduct masterOrderProduct) {
        return String.join(
                ", ",
                masterOrderProduct
                        .getGroupedMasterOrderProduct()
                        .stream()
                        .map(mop -> mop.getBelongsToField(MasterOrderProductFields.MASTER_ORDER).getStringField(
                                MasterOrderFields.NUMBER)).collect(Collectors.toSet()));
    }

    public String buildDescription(Entity parameter, Entity masterOrder, Entity masterOrderProduct, Entity technology) {
        boolean copyDescription = parameter.getBooleanField(ParameterFieldsMO.COPY_DESCRIPTION);
        boolean copyNotesFromMasterOrderPosition = parameter.getBooleanField(COPY_NOTES_FROM_MASTER_ORDER_POSITION);
        boolean fillOrderDescriptionBasedOnTechnology = parameter
                .getBooleanField(ParameterFieldsO.FILL_ORDER_DESCRIPTION_BASED_ON_TECHNOLOGY_DESCRIPTION);

        StringBuilder descriptionBuilder = new StringBuilder();

        if (copyDescription && StringUtils.isNoneBlank(masterOrder.getStringField(MasterOrderFields.DESCRIPTION))) {
            descriptionBuilder.append(masterOrder.getStringField(MasterOrderFields.DESCRIPTION));
        }

        if (copyNotesFromMasterOrderPosition
                && StringUtils.isNoneBlank(masterOrderProduct.getStringField(MasterOrderProductFields.COMMENTS))) {
            if (StringUtils.isNoneBlank(descriptionBuilder.toString())) {
                descriptionBuilder.append("\n");
            }
            descriptionBuilder.append(masterOrderProduct.getStringField(MasterOrderProductFields.COMMENTS));
        }

        if (fillOrderDescriptionBasedOnTechnology && Objects.nonNull(technology)
                && StringUtils.isNoneBlank(technology.getStringField(TechnologyFields.DESCRIPTION))) {
            if (StringUtils.isNoneBlank(descriptionBuilder.toString())) {
                descriptionBuilder.append("\n");
            }
            descriptionBuilder.append(technology.getStringField(TechnologyFields.DESCRIPTION));

        }

        return descriptionBuilder.toString();
    }

    private String generateOrderName(final Entity product, final Entity technology) {
        return orderService.makeDefaultName(product, technology, LocaleContextHolder.getLocale());
    }

    private String generateOrderNumber(final MasterOrderProduct masterOrderProduct) {
        if (masterOrderProduct.isCreateCollectiveOrders()) {
            return numberGeneratorService.generateNumber(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER, 6);
        } else {
            return numberGeneratorService.generateNumberWithPrefix(OrdersConstants.PLUGIN_IDENTIFIER,
                    OrdersConstants.MODEL_ORDER, 3, masterOrderProduct.getMasterOrder().getStringField(MasterOrderFields.NUMBER)
                            + "-");
        }

    }

    private void setMasterOrderPositionStatus(final Entity masterOrderProduct, MasterOrderPositionStatus status) {
        Entity item = dictionaryService.getItemEntityByTechnicalCode(L_MASTER_ORDER_POSITION_STATUS, status.getStringValue());
        if (Objects.nonNull(item)) {
            masterOrderProduct.setField(MasterOrderProductFields.MASTER_ORDER_POSITION_STATUS,
                    item.getStringField(DictionaryItemFields.NAME));
        }

    }

    private Entity getTechnology(final MasterOrderProduct masterOrderProduct) {
        Entity technology;
        technology = masterOrderProduct.getTechnology();
        if (Objects.isNull(technology)) {
            Entity product = masterOrderProduct.getProduct();
            technology = technologyServiceO.getDefaultTechnology(product);
        }

        return technology;
    }

    private void fillPCParametersForOrder(final Entity orderEntity, final Entity technology) {
        if (Objects.nonNull(technology)) {
            for (String field : L_TECHNOLOGY_FIELD_NAMES) {
                orderEntity.setField(field, getDefaultValueForProductionCounting(technology, field));
            }
        }
    }

    private List<Entity> getOrderAndSubOrders(final Long orderID) {
        String sql = "SELECT o FROM #orders_order AS o WHERE o.root = :orderID OR o.id = :orderID";
        return getOrderDD().find(sql).setLong("orderID", orderID).list().getEntities();
    }

    private Object getDefaultValueForProductionCounting(final Entity technology, final String fieldName) {
        return technology.getField(fieldName);
    }

    private DataDefinition getOrderDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }

    private DataDefinition getMasterOrderProductDtoDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_MASTER_ORDER_POSITION_DTO);
    }
}
