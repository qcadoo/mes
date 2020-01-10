package com.qcadoo.mes.basic.hooks;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.NumberPatternFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;

@Service
public class NumberPatternHooks {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getStringField(NumberPatternFields.NUMBER) == null) {
            entity.setField(NumberPatternFields.NUMBER, setNumberFromSequence());
        }
    }

    public boolean onDelete(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getBooleanField(NumberPatternFields.USED)) {
            dropSequence(entity.getStringField(NumberPatternFields.NUMBER));
        }
        return true;
    }

    private String setNumberFromSequence() {
        return jdbcTemplate.queryForObject("select generate_number_pattern_number()", Maps.newHashMap(), String.class);
    }

    private void dropSequence(String number) {
        jdbcTemplate.execute("DROP SEQUENCE number_pattern_" + number + "_seq", (PreparedStatementCallback) PreparedStatement::executeUpdate);
    }
}
