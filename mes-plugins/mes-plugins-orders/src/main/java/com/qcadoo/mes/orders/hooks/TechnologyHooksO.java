package com.qcadoo.mes.orders.hooks;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.TechnologyFieldsO;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyHooksO {

    public boolean checkIfTechnologyIsPrototypeForOrders(final DataDefinition technologyDD, final Entity technology) {
        List<Entity> orders = technology.getHasManyField(TechnologyFieldsO.ORDERS_USING_PROTOTYPE);

        if (!orders.isEmpty()) {
            technology.addGlobalError("orders.technology.hasOrdersAsPrototype",
                    technology.getStringField(TechnologyFields.NUMBER));
            return false;
        }
        return true;
    }
}
