package com.qcadoo.mes.technologies.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.mes.technologies.TechnologyNameAndNumberGenerator;
import com.qcadoo.mes.technologies.constants.ModifyTechnologyAddProductHelperFields;
import com.qcadoo.mes.technologies.constants.ModifyTechnologyHelperFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.ModifyTechnologyResult;
import com.qcadoo.mes.technologies.states.TechnologyStateChangeViewClient;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityOpResult;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModifyTechnologyListeners {

    private static final Logger LOG = LoggerFactory.getLogger(ModifyTechnologyListeners.class);

    private static final String NODE_NUMBER = "nodeNumber";

    private static final String L_TECHNOLOGY_ID = "technologyId";

    private static final String L_PRODUCT_ID = "productId";

    private static final String L_COMPONENTS_LOCATION = "componentsLocation";

    private static final String COMPONENTS_OUTPUT_LOCATION = "componentsOutputLocation";

    private static final String L_PRODUCTS_FLOW_LOCATION = "productsFlowLocation";

    private static final String L_PRODUCTION_FLOW = "productionFlow";

    public static final String L_SAVE = "save";

    public static final String L_GENERATED = "generated";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyNameAndNumberGenerator technologyNameAndNumberGenerator;

    @Autowired
    private TechnologyStateChangeViewClient technologyStateChangeViewClient;

    public void createTechnologies(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        componentState.performEvent(view, L_SAVE, args);
        CheckBoxComponent generated = (CheckBoxComponent) view.getComponentByReference(L_GENERATED);

        FormComponent formComponent = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (!formComponent.isValid()) {
            formComponent.getEntityId();
            return;
        }

        Entity mt = formComponent.getEntity().getDataDefinition().get(formComponent.getEntityId());
        ModifyTechnologyResult modifyTechnologyResult = new ModifyTechnologyResult();
        createModifiedTechnology(view, componentState, mt, modifyTechnologyResult);

        if (!modifyTechnologyResult.getCreatedTechnologies().isEmpty()) {
            view.addMessage("technologies.modifyTechnology.createdTechnologies", ComponentState.MessageType.SUCCESS,
                    String.join(",", modifyTechnologyResult.getCreatedTechnologies()));
        }
        if (!modifyTechnologyResult.getNotCreatedTechnologies().isEmpty()) {
            view.addMessage("technologies.modifyTechnology.notCreatedTechnologies", ComponentState.MessageType.FAILURE,
                    String.join(",", modifyTechnologyResult.getNotCreatedTechnologies()));
        }
        generated.setChecked(true);
    }

    private void createModifiedTechnology(final ViewDefinitionState view, final ComponentState componentState, Entity mt,
            ModifyTechnologyResult modifyTechnologyResult) {

        String selectedEntities = mt.getStringField(ModifyTechnologyHelperFields.SELECTED_ENTITIES);
        boolean sizeProduct = mt.getBooleanField("sizeProduct");

        List<Long> ids = Lists.newArrayList(selectedEntities.split(",")).stream().map(Long::valueOf).collect(Collectors.toList());
        Map<Long, List<Entity>> opicsByTechnology = Maps.newHashMap();

        if(sizeProduct) {

            List<Entity> opicDtos = dataDefinitionService
                    .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT_DTO)
                    .find().add(SearchRestrictions.in("id", ids)).list().getEntities();
            for (Entity opicDto : opicDtos) {
                if (opicsByTechnology.containsKey(opicDto.getIntegerField(L_TECHNOLOGY_ID).longValue())) {
                    opicsByTechnology.get(opicDto.getIntegerField(L_TECHNOLOGY_ID).longValue()).add(opicDto);
                } else {
                    List<Entity> opiIds = Lists.newArrayList();
                    opiIds.add(opicDto);
                    opicsByTechnology.put(opicDto.getIntegerField(L_TECHNOLOGY_ID).longValue(), opiIds);
                }
            }

        } else {

        }

        for (Map.Entry<Long, List<Entity>> entry : opicsByTechnology.entrySet()) {
            Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY).get(entry.getKey());
            try {
                customizeTechnology(view, componentState, technology, entry.getValue(), mt, modifyTechnologyResult);
            } catch (Exception exc) {
                modifyTechnologyResult.addNotCreatedTechnologies(technology.getStringField(TechnologyFields.NUMBER));
                LOG.warn("Error when create technology.", exc);
            }
        }
    }

    @Transactional
    private void customizeTechnology(final ViewDefinitionState view, final ComponentState state, Entity technology,
            List<Entity> opics, Entity mt, ModifyTechnologyResult modifyTechnologyResult) {

        technology.setField(TechnologyFields.MASTER, Boolean.FALSE);
        technology = technology.getDataDefinition().save(technology);
        if (technology.isValid()) {
            Entity copyTechnology = technology.getDataDefinition().copy(technology.getId()).get(0);
            Entity product = technology.getBelongsToField(TechnologyFields.PRODUCT);

            copyTechnology.setField(TechnologyFields.NUMBER, technologyNameAndNumberGenerator.generateNumber(product));
            copyTechnology.setField(TechnologyFields.NAME, technologyNameAndNumberGenerator.generateName(product));

            copyTechnology = copyTechnology.getDataDefinition().save(copyTechnology);

            for (Entity opicDto : opics) {
                modifyOperation(mt, copyTechnology, opicDto);
            }

            copyTechnology = copyTechnology.getDataDefinition().get(copyTechnology.getId());

            technologyStateChangeViewClient.changeState(new ViewContextHolder(view, state), TechnologyStateStringValues.ACCEPTED,
                    copyTechnology);
            Entity tech = copyTechnology.getDataDefinition().get(copyTechnology.getId());
            tech.setField(TechnologyFields.MASTER, Boolean.TRUE);
            tech.getDataDefinition().save(tech);
            modifyTechnologyResult.addCreatedTechnology(tech.getStringField(TechnologyFields.NUMBER));

        } else {
            throw new IllegalStateException("There was a problem creating the technology");
        }

    }

    private void modifyOperation(Entity mt, Entity copyTechnology, Entity opicDto) {
        Entity toc = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)
                .find()
                .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, copyTechnology))
                .add(SearchRestrictions.eq(TechnologyOperationComponentFields.NODE_NUMBER,
                        opicDto.getStringField(NODE_NUMBER))).setMaxResults(1).uniqueResult();

        Entity opic = toc
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS)
                .stream()
                .filter(opc -> opc.getBelongsToField(OperationProductInComponentFields.PRODUCT).getId()
                        .equals(opicDto.getIntegerField(L_PRODUCT_ID).longValue())).findFirst()
                .orElseThrow(() -> new IllegalStateException("Product not found"));

        if (mt.getBooleanField(ModifyTechnologyHelperFields.ADD_NEW)) {
            addNewProducts(mt, toc, opic);
        }

        if (mt.getBooleanField(ModifyTechnologyHelperFields.REPLACE)) {
            replaceProduct(mt, toc, opic);
        } else if (mt.getBooleanField(ModifyTechnologyHelperFields.REMOVE)) {
            removeProduct(opic);
        }
    }

    private void removeProduct(Entity opic) {
        EntityOpResult entityOpResult = opic.getDataDefinition().delete(opic.getId());
        if (!entityOpResult.isSuccessfull()) {
            throw new IllegalStateException("Error while deleting opic");
        }
    }

    private void replaceProduct(Entity mt, Entity toc, Entity opic) {
        removeProduct(opic);

        Entity newOpic = createOpic(toc, opic, mt.getBelongsToField(ModifyTechnologyHelperFields.REPLACE_PRODUCT),
                mt.getDecimalField(ModifyTechnologyHelperFields.REPLACE_PRODUCT_QUANTITY));
        if (!newOpic.isValid()) {
            throw new IllegalStateException("Error while saving opic");
        }
    }

    private void addNewProducts(Entity mt, Entity toc, Entity opic) {
        mt.getHasManyField(ModifyTechnologyHelperFields.MODIFY_TECHNOLOGY_ADD_PRODUCTS).forEach(
                pr -> {
                    Entity newOpic = createOpic(toc, opic,
                            pr.getBelongsToField(ModifyTechnologyAddProductHelperFields.PRODUCT),
                            pr.getDecimalField(ModifyTechnologyAddProductHelperFields.QUANTITY));
                    if (!newOpic.isValid()) {
                        throw new IllegalStateException("Error while saving opic");
                    }

                });
    }

    private Entity createOpic(Entity toc, Entity opic, Entity product, BigDecimal quantity) {
        Entity newOpic = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).create();

        newOpic.setField(OperationProductInComponentFields.OPERATION_COMPONENT, toc.getId());
        newOpic.setField(OperationProductInComponentFields.PRODUCT, product.getId());
        newOpic.setField(OperationProductInComponentFields.QUANTITY, quantity);

        if (Objects.nonNull(opic.getBelongsToField(L_COMPONENTS_LOCATION))) {
            newOpic.setField(L_COMPONENTS_LOCATION, opic.getBelongsToField(L_COMPONENTS_LOCATION).getId());
        }
        if (Objects.nonNull(opic.getBelongsToField(COMPONENTS_OUTPUT_LOCATION))) {
            newOpic.setField(COMPONENTS_OUTPUT_LOCATION, opic.getBelongsToField(COMPONENTS_OUTPUT_LOCATION).getId());
        }
        if (Objects.nonNull(opic.getBelongsToField(L_PRODUCTS_FLOW_LOCATION))) {
            newOpic.setField(L_PRODUCTS_FLOW_LOCATION, opic.getBelongsToField(L_PRODUCTS_FLOW_LOCATION).getId());
        }
        newOpic.setField(L_PRODUCTION_FLOW, opic.getStringField(L_PRODUCTION_FLOW));

        newOpic = newOpic.getDataDefinition().save(newOpic);
        return newOpic;
    }

}
