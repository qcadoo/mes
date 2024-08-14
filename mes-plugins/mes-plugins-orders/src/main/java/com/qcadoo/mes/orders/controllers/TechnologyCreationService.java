package com.qcadoo.mes.orders.controllers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.*;
import com.qcadoo.mes.orders.controllers.dto.TechnologyOperationDto;
import com.qcadoo.mes.orders.controllers.requests.TechnologyCreationRequest;
import com.qcadoo.mes.orders.controllers.responses.TechnologyCreationResponse;
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
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.ALL;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.NEXT_OPERATION_AFTER_PRODUCED_QUANTITY;

@Service
public class TechnologyCreationService {

    private static final String L_RANGE = "range";

    private static final Set<String> FIELDS_OPERATION = Sets.newHashSet("tpz", "tj", "productionInOneCycle",
            "nextOperationAfterProducedType", "nextOperationAfterProducedQuantity", "nextOperationAfterProducedQuantityUNIT",
            "timeNextOperation", "machineUtilization", "laborUtilization", "productionInOneCycleUNIT",
            "areProductQuantitiesDivisible", "isTjDivisible", TechnologyOperationComponentFieldsTNFO.MIN_STAFF,
            TechnologyOperationComponentFieldsTNFO.TJ_DECREASES_FOR_ENLARGED_STAFF, TechnologyOperationComponentFieldsTNFO.OPTIMAL_STAFF);

    private static final String NEXT_OPERATION_AFTER_PRODUCED_TYPE = "nextOperationAfterProducedType";

    private static final String PRODUCTION_IN_ONE_CYCLE = "productionInOneCycle";

    private static final String L_OPERATION = "operation";

    public static final String COMPONENTS_LOCATION = "componentsLocation";

    public static final String COMPONENTS_OUTPUT_LOCATION = "componentsOutputLocation";

    public static final String PRODUCTS_INPUT_LOCATION = "productsInputLocation";

    public static final String PRODUCTS_FLOW_LOCATION = "productsFlowLocation";

    public static final String WASTE_RECEPTION_WAREHOUSE = "wasteReceptionWarehouse";

    public static final String PRODUCTION_FLOW = "productionFlow";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private TechnologyStateChangeAspect technologyStateChangeAspect;

    @Autowired
    private StateChangeContextBuilder stateChangeContextBuilder;

    @Autowired
    private TechnologyNameAndNumberGenerator technologyNameAndNumberGenerator;

    public TechnologyCreationResponse createTechnology(final TechnologyCreationRequest technologyCreationRequest) {
        Either<String, Entity> result = createTechnologyForEachOperation(technologyCreationRequest);
        if (result.isLeft()) {
            return new TechnologyCreationResponse(result.getLeft());
        } else {
            TechnologyCreationResponse technologyCreationResponse = new TechnologyCreationResponse(TechnologyCreationResponse.StatusCode.OK);
            technologyCreationResponse.setMessage(translationService.translate("orders.technologyDefinitionWizard.createTechnology.success", LocaleContextHolder.getLocale(), result.getRight().getStringField(TechnologyFields.NUMBER)));
            return technologyCreationResponse;
        }
    }

    private Either<String, Entity> createTechnologyForEachOperation(final TechnologyCreationRequest technologyCreationRequest) {
        Entity product = getProduct(technologyCreationRequest.getProductId());
        Entity parameter = parameterService.getParameter();

        technologyCreationRequest.getTechnologyOperations().sort(Comparator.comparing(TechnologyOperationDto::getNode));

        String range = parameter.getStringField(L_RANGE);
        Entity technology = getTechnologyDD().create();

        technology.setField(TechnologyFields.NUMBER, technologyNameAndNumberGenerator.generateNumber(product));
        technology.setField(TechnologyFields.NAME, technologyNameAndNumberGenerator.generateName(product));
        technology.setField(TechnologyFields.PRODUCT, product);
        technology.setField(TechnologyFields.EXTERNAL_SYNCHRONIZED, true);
        technology.setField(L_RANGE, range);
        technology.setField(TechnologyFields.DESCRIPTION, technologyCreationRequest.getDescription());
        technology.setField("typeOfProductionRecording", parameter.getStringField("typeOfProductionRecording"));

        technology = technology.getDataDefinition().save(technology);

        if (!technology.isValid()) {
            return Either.left(translationService.translate(
                    "basic.dashboard.orderDefinitionWizard.createTechnology.validationError", LocaleContextHolder.getLocale()));
        }

        if ("01oneDivision".equals(range)) {
            Entity division = technology.getBelongsToField("division");
            if (division != null) {
                Entity componentsLocation = division.getBelongsToField(COMPONENTS_LOCATION);
                technology.setField(COMPONENTS_LOCATION, componentsLocation);

                Entity componentsOutput = division.getBelongsToField(COMPONENTS_OUTPUT_LOCATION);
                technology.setField(COMPONENTS_OUTPUT_LOCATION, componentsOutput);

                Entity productsInput = division.getBelongsToField(PRODUCTS_INPUT_LOCATION);
                technology.setField(PRODUCTS_INPUT_LOCATION, productsInput);

                Entity productsWaste = division.getBelongsToField(WASTE_RECEPTION_WAREHOUSE);
                technology.setField(WASTE_RECEPTION_WAREHOUSE, productsWaste);

                Entity productsFlow = division.getBelongsToField(PRODUCTS_FLOW_LOCATION);
                technology.setField(PRODUCTS_FLOW_LOCATION, productsFlow);

                technology = technology.getDataDefinition().save(technology);
            }
        }

        Entity parent = null;

        for (TechnologyOperationDto technologyOperation : technologyCreationRequest.getTechnologyOperations()) {
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
