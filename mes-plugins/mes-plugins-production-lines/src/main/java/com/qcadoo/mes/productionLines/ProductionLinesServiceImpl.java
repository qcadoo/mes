package com.qcadoo.mes.productionLines;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;

@Service
public class ProductionLinesServiceImpl implements ProductionLinesService {

    public Integer getWorkstationTypesCount(final Entity operationComponent, final Entity productionLine) {
        List<Entity> workComps = productionLine.getHasManyField("workstationTypeComponents");

        Entity desiredWorkstation = operationComponent.getBelongsToField("operation").getBelongsToField("workstationType");

        if (desiredWorkstation != null) {
            for (Entity workComp : workComps) {
                Entity workstation = workComp.getBelongsToField("workstationType");

                // FIXME mici, proxy entity equals thing
                if (desiredWorkstation.getId().equals(workstation.getId())) {
                    return (Integer) workComp.getField("quantity");
                }
            }
        }

        return (Integer) productionLine.getField("quantityForOtherWorkstationTypes");
    }

}
