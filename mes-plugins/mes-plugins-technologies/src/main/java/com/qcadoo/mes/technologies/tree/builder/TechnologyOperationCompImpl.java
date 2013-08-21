package com.qcadoo.mes.technologies.tree.builder;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentType;
import com.qcadoo.mes.technologies.tree.builder.api.InternalTechnologyOperationComponent;
import com.qcadoo.mes.technologies.tree.builder.api.OperationProductComponent;
import com.qcadoo.mes.technologies.tree.builder.api.TechnologyOperationComponent;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class TechnologyOperationCompImpl implements InternalTechnologyOperationComponent {

    private final Entity entity;

    private final Collection<TechnologyOperationComponent> subOperations;

    private final Collection<OperationProductComponent> inputPoducts;

    private final Collection<OperationProductComponent> outputPoducts;

    public TechnologyOperationCompImpl(final DataDefinition tocDataDef) {
        this.entity = tocDataDef.create();
        entity.setField(TechnologyOperationComponentFields.ENTITY_TYPE,
                TechnologyOperationComponentType.OPERATION.getStringValue());
        this.inputPoducts = Lists.newArrayList();
        this.outputPoducts = Lists.newArrayList();
        this.subOperations = Lists.newArrayList();
    }

    @Override
    public void setOperation(final Entity operation) {
        Preconditions.checkArgument(hasCorrectOperationDataDefinition(operation));
        setField(TechnologyOperationComponentFields.OPERATION, operation);
    }

    private boolean hasCorrectOperationDataDefinition(final Entity operation) {
        DataDefinition dataDef = operation.getDataDefinition();
        return TechnologiesConstants.MODEL_OPERATION.equals(dataDef.getName())
                && TechnologiesConstants.PLUGIN_IDENTIFIER.equals(dataDef.getPluginIdentifier());
    }

    @Override
    public void setField(final String name, final Object value) {
        entity.setField(name, value);
    }

    @Override
    public void addSubOperation(final TechnologyOperationComponent operation) {
        subOperations.add(operation);
    }

    @Override
    public void addInputProducts(final Collection<OperationProductComponent> productComponents) {
        inputPoducts.addAll(productComponents);
    }

    @Override
    public void addOutputProducts(final Collection<OperationProductComponent> productComponents) {
        outputPoducts.addAll(productComponents);
    }

    @Override
    public Entity getWrappedEntity() {
        final Entity resultEntity = entity.copy();
        resultEntity.setField(TechnologyOperationComponentFields.CHILDREN, subOperationsAsEntities());
        resultEntity.setField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS,
                productComponentsToEntities(inputPoducts));
        resultEntity.setField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS,
                productComponentsToEntities(outputPoducts));
        return resultEntity;
    }

    private List<Entity> productComponentsToEntities(final Iterable<OperationProductComponent> productCompoents) {
        final List<Entity> entities = Lists.newArrayList();
        for (OperationProductComponent productComponent : productCompoents) {
            entities.add(productComponent.getWrappedEntity());
        }
        return entities;
    }

    private List<Entity> subOperationsAsEntities() {
        List<Entity> childrenEntities = Lists.newArrayList();
        for (TechnologyOperationComponent child : subOperations) {
            childrenEntities.add(child.getWrappedEntity());
        }
        return childrenEntities;
    }

}
