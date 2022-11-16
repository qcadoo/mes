package com.qcadoo.mes.basic.hooks;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TechnologicalProcessRateHooks {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        setNumber(entity);
    }

    public void onView(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("currentRate", findCurrentRate(entity));

    }

    private void setNumber(final Entity entity) {
        if (checkIfShouldInsertNumber(entity)) {
            String number = jdbcTemplate.queryForObject("select generate_technological_process_rate_number()", Collections.emptyMap(),
                    String.class);
            entity.setField("number", number);
        }
    }

    private boolean checkIfShouldInsertNumber(final Entity entity) {
        if (!Objects.isNull(entity.getId())) {
            return false;
        }
        return !StringUtils.isNotBlank(entity.getStringField("number"));
    }


    private BigDecimal findCurrentRate(Entity technologicalProcessRate) {
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(BasicConstants.PLUGIN_IDENTIFIER, "technologicalProcessRateItem")
                .find().addOrder(SearchOrders.desc("dateFrom"))
                .add(SearchRestrictions.belongsTo("technologicalProcessRate", technologicalProcessRate))
                .add(SearchRestrictions.le("dateFrom", new Date()));
        Entity technologicalProcessRateItem = scb.setMaxResults(1).uniqueResult();
        if(Objects.isNull(technologicalProcessRateItem)) {
            return null;
        } else {
            return technologicalProcessRateItem.getDecimalField("actualRate");
        }

    }

}
