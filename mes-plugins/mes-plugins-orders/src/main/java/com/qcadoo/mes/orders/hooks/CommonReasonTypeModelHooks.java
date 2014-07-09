package com.qcadoo.mes.orders.hooks;

import static com.qcadoo.model.api.search.SearchProjections.id;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;
import static com.qcadoo.model.api.search.SearchRestrictions.idEq;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.CommonReasonTypeFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.deviations.constants.DeviationType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;

@Service
public class CommonReasonTypeModelHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void updateDate(final Entity reasonTypeEntity, final DeviationType deviationType) {
        if (reasonHasNotBeenSavedYet(reasonTypeEntity) || reasonHasChanged(reasonTypeEntity, deviationType)) {
            reasonTypeEntity.setField(CommonReasonTypeFields.DATE, new Date());
        }
    }

    private boolean reasonHasNotBeenSavedYet(final Entity reasonTypeEntity) {
        return reasonTypeEntity.getId() == null;
    }

    private boolean reasonHasChanged(final Entity reasonTypeEntity, final DeviationType deviationType) {
        String reasonFieldName = deviationType.getReasonTypeInReasonModelFieldName();
        String reason = reasonTypeEntity.getStringField(reasonFieldName);
        DataDefinition reasonDD = dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, deviationType.getReasonModelName());
        SearchCriteriaBuilder scb = reasonDD.find();
        scb.add(idEq(reasonTypeEntity.getId()));
        scb.add(eq(reasonFieldName, reason));
        scb.setProjection(id());
        return scb.setMaxResults(1).list().getTotalNumberOfEntities() == 0;
    }

}
