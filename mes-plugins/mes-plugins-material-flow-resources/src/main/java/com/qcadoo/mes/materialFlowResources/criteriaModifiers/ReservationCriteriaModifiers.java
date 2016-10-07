package com.qcadoo.mes.materialFlowResources.criteriaModifiers;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.ReservationFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ReservationCriteriaModifiers {

    public void showReservationsBiggerThanZero(SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.gt(ReservationFields.QUANTITY, BigDecimal.ZERO));
    }
}
