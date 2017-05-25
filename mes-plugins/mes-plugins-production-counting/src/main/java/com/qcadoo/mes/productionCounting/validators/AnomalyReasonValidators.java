package com.qcadoo.mes.productionCounting.validators;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AnomalyReasonValidators {

    public boolean validatesWith(final DataDefinition anomalyReasonDD, final Entity anomalyReason) {
        boolean valid = true;
        if (!anomalyReason.getBooleanField("defaultReason")) {
            return true;
        }
        SearchCriteriaBuilder criteriaBuilder = anomalyReasonDD.find().add(SearchRestrictions.eq("defaultReason", true));

        if (Objects.nonNull(anomalyReason.getId())) {
            criteriaBuilder = criteriaBuilder.add(SearchRestrictions.idNe(anomalyReason.getId()));

        }

        List<Entity> anomalyReasons = criteriaBuilder.list().getEntities();
        if (!anomalyReasons.isEmpty()) {
            anomalyReason.addError(anomalyReasonDD.getField("defaultReason"),
                    "productionCounting.anomalyReason.defaultReason.error.canMarkOnlyOneDefault");
            valid = false;
        }
        return valid;
    }
}
