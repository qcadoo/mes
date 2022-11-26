package com.qcadoo.mes.basicProductionCounting;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.PCQChangeType;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityChangeFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class ProductionCountingQuantityChangeService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    public void addEntry(Entity productionCountingQuantity) {
        if (Objects.isNull(productionCountingQuantity.getId())) {
            if (!productionCountingQuantity.getBooleanField(ProductionCountingQuantityFields.IS_ORDER_CREATE)) {
                addProductEntry(productionCountingQuantity, PCQChangeType.ADDING_PRODUCT, null);
            }
        } else {
            Entity productionCountingQuantityDb = productionCountingQuantity.getDataDefinition().get(productionCountingQuantity.getId());
            BigDecimal plannedQuantity = productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);
            BigDecimal plannedQuantityDb = productionCountingQuantityDb.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);
            if(plannedQuantity.compareTo(plannedQuantityDb) != 0) {
                addProductEntry(productionCountingQuantity, PCQChangeType.CHANGE_QUANTITY, plannedQuantityDb);
            }
        }
    }

    public void addRemoveEntry(Entity productionCountingQuantity) {
        addProductEntry(productionCountingQuantity, PCQChangeType.PRODUCT_REMOVAL, null);
    }

    private void addProductEntry(Entity pcq, PCQChangeType changeType, BigDecimal beforeQuantity) {
        Entity entry = getDD().create();
        entry.setField(ProductionCountingQuantityChangeFields.ORDER, pcq.getBelongsToField(ProductionCountingQuantityFields.ORDER));
        entry.setField(ProductionCountingQuantityChangeFields.PRODUCT, pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT));
        entry.setField(ProductionCountingQuantityChangeFields.WORKER, securityService.getCurrentUserName());
        entry.setField(ProductionCountingQuantityChangeFields.CHANGE_TYPE, changeType.getStringValue());
        entry.setField(ProductionCountingQuantityChangeFields.DATE_AND_TIME, DateTime.now().toDate());
        if(Objects.nonNull(beforeQuantity)) {
            entry.setField(ProductionCountingQuantityChangeFields.PLANNED_QUANTITY_AFTER, pcq.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY));
            entry.setField(ProductionCountingQuantityChangeFields.PLANNED_QUANTITY_BEFORE, beforeQuantity);
        }
        getDD().save(entry);
    }

    private DataDefinition getDD() {
        return dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER, BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY_CHANGE);
    }


}

