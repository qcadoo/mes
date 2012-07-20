package com.qcadoo.mes.costNormsForOperationInOrder.hooks;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class HourlyCostNormsInOrderDetailsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void setLastUpdateDateTioc(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference("form");
        Entity order = orderForm.getEntity().getDataDefinition().get(orderForm.getEntityId());
        List<Entity> tiocs = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)
                .find().add(SearchRestrictions.belongsTo("order", order)).addOrder(SearchOrders.desc("updateDate")).list()
                .getEntities();
        if (tiocs.isEmpty()) {
            return;
        }
        FieldComponent lastUpdate = (FieldComponent) view.getComponentByReference("lastUpdate");
        String updateDate = DateFormat.getDateTimeInstance().format((Date) (tiocs.get(0).getField("updateDate")));
        lastUpdate.setFieldValue(updateDate);
        lastUpdate.requestComponentUpdateState();
    }
}
