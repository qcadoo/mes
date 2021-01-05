package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.mes.technologies.constants.TechnologicalProcessListFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;

@Service
public class TechnologicalProcessListHooks {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        setNumber(entity);
    }

    private void setNumber(final Entity entity) {
        if (checkIfShouldInsertNumber(entity)) {
            String number = jdbcTemplate.queryForObject("select generate_technological_process_list_number()",
                    Collections.emptyMap(), String.class);
            entity.setField(TechnologicalProcessListFields.NUMBER, number);
        }
    }

    private boolean checkIfShouldInsertNumber(final Entity entity) {
        if (!Objects.isNull(entity.getId())) {
            return false;
        }
        return !StringUtils.isNotBlank(entity.getStringField(TechnologicalProcessListFields.NUMBER));
    }

    public boolean onDelete(final DataDefinition dataDefinition, final Entity entity) {
        boolean canDelete = entity.getHasManyField(TechnologicalProcessListFields.OPERATION_COMPONENTS).isEmpty();
        if (!canDelete) {
            entity.addGlobalError("technologies.technologicalProcessList.delete.hasOperationComponents");
        }
        return canDelete;
    }
}
