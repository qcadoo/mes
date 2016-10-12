package com.qcadoo.mes.productionPerShift.services;

import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.model.api.Entity;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkersOnLineService {

    /**
     * Get workers on line for shift and date
     * @param productionLine
     * @param shift
     * @param date
     * @return number of workers
     */
    public Integer getWorkersOnLine(final Entity productionLine, final Entity shift, final DateTime date) {
        List<Entity> shifts = productionLine.getManyToManyField(ProductionLineFields.SHIFTS);
        Optional<Entity> isShift = shifts.stream()
                .filter(user -> user.getId() == 1)
                .collect(Collectors.reducing((a, b) -> null));
        if(!isShift.isPresent()){
            return 0;
        }
        Optional<Entity> isFactory = findFactory(productionLine);
        return 0;
    }

    private Optional<Entity> findFactory(Entity productionLine) {
        return null;
    }
}
