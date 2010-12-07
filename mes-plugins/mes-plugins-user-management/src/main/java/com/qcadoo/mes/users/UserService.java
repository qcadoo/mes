/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.users;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.ButtonComponentState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.PasswordComponentState;
import com.qcadoo.mes.view.components.form.FormComponentState;

@Service
public final class UserService {

    public void setPasswordAndOldPasswordAdRequired(final ViewDefinitionState state, final Locale locale) {
        FieldComponentState viewIdentifier = (FieldComponentState) state.getComponentByPath("window.user.viewIdentifier");
        PasswordComponentState oldPassword = (PasswordComponentState) state.getComponentByPath("window.user.oldPassword");
        PasswordComponentState password = (PasswordComponentState) state.getComponentByPath("window.user.password");
        PasswordComponentState passwordConfirmation = (PasswordComponentState) state
                .getComponentByPath("window.user.passwordConfirmation");

        oldPassword.setRequired(true);
        password.setRequired(true);
        passwordConfirmation.setRequired(true);
        viewIdentifier.setFieldValue("profileChangePassword");
    }

    public void setPasswordAsRequired(final ViewDefinitionState state, final Locale locale) {
        FieldComponentState viewIdentifier = (FieldComponentState) state.getComponentByPath("window.user.viewIdentifier");
        PasswordComponentState password = (PasswordComponentState) state.getComponentByPath("window.user.password");
        PasswordComponentState passwordConfirmation = (PasswordComponentState) state
                .getComponentByPath("window.user.passwordConfirmation");

        password.setRequired(true);
        passwordConfirmation.setRequired(true);
        viewIdentifier.setFieldValue("userChangePassword");
    }

    public void hidePasswordOnUpdateForm(final ViewDefinitionState state, final Locale locale) {
        FormComponentState form = (FormComponentState) state.getComponentByPath("window.user");
        PasswordComponentState password = (PasswordComponentState) state.getComponentByPath("window.user.password");
        PasswordComponentState passwordConfirmation = (PasswordComponentState) state
                .getComponentByPath("window.user.passwordConfirmation");
        ButtonComponentState changePasswordButton = (ButtonComponentState) state
                .getComponentByPath("window.changePasswordButton");

        password.setRequired(true);
        passwordConfirmation.setRequired(true);

        if (form.getEntityId() != null) {
            password.setVisible(false);
            passwordConfirmation.setVisible(false);
            changePasswordButton.setVisible(true);
        } else {
            password.setVisible(true);
            passwordConfirmation.setVisible(true);
            changePasswordButton.setVisible(false);
        }
    }

    public boolean checkPassword(final DataDefinition dataDefinition, final Entity entity) {
        String password = entity.getStringField("password");
        String passwordConfirmation = entity.getStringField("passwordConfirmation");
        String oldPassword = entity.getStringField("oldPassword");
        String viewIdentifier = entity.getStringField("viewIdentifier");

        if (!"profileChangePassword".equals(viewIdentifier) && !"userChangePassword".equals(viewIdentifier)) {
            return true;
        }

        if ("profileChangePassword".equals(viewIdentifier)) {
            if (oldPassword == null) {
                entity.addError(dataDefinition.getField("oldPassword"), "users.validate.global.error.noOldPassword");
                return false;
            }
            Object currentPassword = dataDefinition.get(entity.getId()).getField("password");
            if (!currentPassword.equals(oldPassword)) {
                entity.addError(dataDefinition.getField("oldPassword"), "users.validate.global.error.wrongOldPassword");
                return false;
            }
        }

        if (password == null) {
            entity.addError(dataDefinition.getField("password"), "users.validate.global.error.noPassword");
            return false;
        }

        if (passwordConfirmation == null) {
            entity.addError(dataDefinition.getField("passwordConfirmation"), "users.validate.global.error.noPasswordConfirmation");
            return false;
        }

        if (!password.equals(passwordConfirmation)) {
            entity.addError(dataDefinition.getField("password"), "users.validate.global.error.notMatch");
            entity.addError(dataDefinition.getField("passwordConfirmation"), "users.validate.global.error.notMatch");
            return false;
        }

        return true;
    }
}
