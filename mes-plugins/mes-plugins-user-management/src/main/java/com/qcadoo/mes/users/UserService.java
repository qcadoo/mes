package com.qcadoo.mes.users;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;

@Service
public final class UserService {

    public boolean checkPassword(final DataDefinition dataDefinition, final Entity entity) {

        String password = entity.getStringField("password");
        String passwordConfirmation = entity.getStringField("passwordConfirmation");
        String oldPassword = entity.getStringField("oldPassword");

        System.out.println("ABC");
        System.out.println(entity.getFields().containsKey("passwordConfirmation"));
        System.out.println(password);
        System.out.println(passwordConfirmation);
        System.out.println(oldPassword);

        if (oldPassword != null) {
            Object currentPassword = dataDefinition.get(entity.getId()).getField("password");
            if (!currentPassword.equals(oldPassword)) {
                entity.addError(dataDefinition.getField("oldPassword"), "users.validate.global.error.wrongOldPassword");
                return false;
            }
        } else {
            if (password == null && passwordConfirmation == null) {
                return true;
            }
        }

        if (password == null || passwordConfirmation == null || !password.equals(passwordConfirmation)) {
            entity.addError(dataDefinition.getField("password"), "users.validate.global.error.notMatch");
            entity.addError(dataDefinition.getField("passwordConfirmation"), "users.validate.global.error.notMatch");
            return false;
        } else {
            return true;
        }
    }
}
