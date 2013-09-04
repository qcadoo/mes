package com.qcadoo.mes.productionCounting.hooks.helpers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductionCountingQuantityFetcher {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public List<Entity> getQuantities(final Entity order, final Entity toc) {
        Preconditions.checkArgument(order != null);
        String typeOfProdRecordingString = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        TypeOfProductionRecording typeOfProdRecording = TypeOfProductionRecording.parseString(typeOfProdRecordingString);
        if (typeOfProdRecording == TypeOfProductionRecording.CUMULATED) {
            return getAllQuantities(order);
        } else if (typeOfProdRecording == TypeOfProductionRecording.FOR_EACH) {
            return getQuantitiesForOperation(order, toc);
        } else {
            // that will never happen
            return Lists.newArrayList();
        }
    }

    private List<Entity> getAllQuantities(final Entity order) {
        SearchCriteriaBuilder scb = getQuantitiesDataDef().find();
        scb.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order));
        return scb.list().getEntities();
    }

    private List<Entity> getQuantitiesForOperation(final Entity order, final Entity toc) {
        SearchCriteriaBuilder scb = getQuantitiesDataDef().find();
        scb.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order));
        scb.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT, toc));
        return scb.list().getEntities();
    }

    private DataDefinition getQuantitiesDataDef() {
        return dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY);
    }

}
