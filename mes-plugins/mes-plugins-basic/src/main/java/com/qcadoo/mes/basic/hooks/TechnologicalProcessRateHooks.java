package com.qcadoo.mes.basic.hooks;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

import java.util.Collections;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TechnologicalProcessRateHooks {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        setNumber(entity);
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

}
