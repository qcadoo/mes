package com.qcadoo.mes.users;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;

@Service
public final class UserService {

    public boolean checkPassword(final DataDefinition dataDefinition, final Entity entity) {
        String password = entity.getStringField("password");
        String passwordConfirmation = entity.getStringField("passwordConfirmation");

        if (password == null && passwordConfirmation == null) {
            return true;
        }

        if (password == null || passwordConfirmation == null || !password.equals(passwordConfirmation)) {
            entity.addError(dataDefinition.getField("password"), "commons.validate.field.error.notMatch");
            entity.addError(dataDefinition.getField("passwordConfirmation"), "commons.validate.field.error.notMatch");

            return false;
        } else {
            return true;
        }
    }

}
