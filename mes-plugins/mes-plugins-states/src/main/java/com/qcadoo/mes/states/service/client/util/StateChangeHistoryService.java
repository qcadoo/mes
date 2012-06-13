package com.qcadoo.mes.states.service.client.util;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class StateChangeHistoryService {

    public final CustomRestriction buildStatusRestriction(final String statusFieldName, final List<String> allowedStatuses) {

        return new CustomRestriction() {

            @Override
            public void addRestriction(final SearchCriteriaBuilder scb) {
                scb.add(SearchRestrictions.in(statusFieldName, allowedStatuses));
            }
        };
    }

}
