package com.qcadoo.mes.technologies.grouping;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentMergeProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OperationMergeServiceImpl implements OperationMergeService {

    private DataDefinitionService dataDefinitionService;

    @Autowired
    public OperationMergeServiceImpl(DataDefinitionService dataDefinitionService) {
        this.dataDefinitionService = dataDefinitionService;
    }

    @Override
    public void mergeProductIn(Entity existingOperationComponent, Entity operationProductIn, BigDecimal quantity) {
        mergeProduct(mergesProductInDD(), existingOperationComponent, operationProductIn, quantity);
    }

    @Override
    public void storeProductIn(Entity operationComponent, Entity mergeOperationComponent, Entity operationProductIn, BigDecimal quantity) {
        persistMerge(mergesProductInDD(), operationComponent, mergeOperationComponent, operationProductIn, quantity);
    }

    @Override
    public void mergeProductOut(Entity existingOperationComponent, Entity operationProductOut, BigDecimal quantity) {
        mergeProduct(mergesProductOutDD(), existingOperationComponent, operationProductOut, quantity);
    }

    @Override
    public void storeProductOut(Entity operationComponent, Entity mergeOperationComponent, Entity operationProductIn, BigDecimal quantity) {
        persistMerge(mergesProductInDD(), operationComponent, mergeOperationComponent, operationProductIn, quantity);
    }

    private void mergeProduct(DataDefinition dataDefinition, Entity existingOperationComponent, Entity operationProduct, BigDecimal quantity) {
        Entity alreadyMergedProductComponentForOperation = findAlreadyMergedProductComponentForOperation(dataDefinition, existingOperationComponent, operationProduct);
        if (alreadyMergedProductComponentForOperation != null) {
            alreadyMergedProductComponentForOperation.setField(TechnologyOperationComponentMergeProductFields.QUANTITY_CHANGE, quantity);
            dataDefinition.save(alreadyMergedProductComponentForOperation);
        } else
            persistMerge(mergesProductInDD(), existingOperationComponent, existingOperationComponent, operationProduct, quantity);
    }

    private void persistMerge(DataDefinition dataDefinition, Entity operationComponent, Entity mergeOperationComponent, Entity operationProduct, BigDecimal quantity) {
        Entity merge = dataDefinition.create();
        merge.setField(TechnologyOperationComponentMergeProductFields.OPERATION_COMPONENT, operationComponent);
        merge.setField(TechnologyOperationComponentMergeProductFields.MERGED_OPERATION_COMPONENT, mergeOperationComponent);
        merge.setField(TechnologyOperationComponentMergeProductFields.MERGED_OPERATION_PRODUCT_COMPONENT, operationProduct);
        merge.setField(TechnologyOperationComponentMergeProductFields.QUANTITY_CHANGE, quantity);
        dataDefinition.save(merge);
    }

    private Entity findAlreadyMergedProductComponentForOperation(DataDefinition dataDefinition, Entity existingOperationComponent, Entity operationProductIn) {
        return dataDefinition.find()
                .add(SearchRestrictions.eq(TechnologyOperationComponentMergeProductFields.OPERATION_COMPONENT, existingOperationComponent))
                .add(SearchRestrictions.eq(TechnologyOperationComponentMergeProductFields.MERGED_OPERATION_COMPONENT, existingOperationComponent))
                .add(SearchRestrictions.eq(TechnologyOperationComponentMergeProductFields.MERGED_OPERATION_PRODUCT_COMPONENT + "." + OperationProductInComponentFields.PRODUCT , operationProductIn.getBelongsToField(OperationProductInComponentFields.PRODUCT))).setMaxResults(1).uniqueResult();
    }

    private DataDefinition mergesProductInDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT_MERGE_PRODUCT_IN);
    }

    private DataDefinition mergesProductOutDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT_MERGE_PRODUCT_OUT);
    }

}
