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

@Service
public final class UserService {

    public void setPasswordAndOldPasswordAdRequired(final ViewDefinitionState state, final Locale locale) {
        // SimpleValue oldPasswordValue = (SimpleValue) value.lookupValue("mainWindow.userDetailsForm.oldPassword").getValue();
        // SimpleValue passwordConfirmationValue = (SimpleValue) value
        // .lookupValue("mainWindow.userDetailsForm.passwordConfirmation").getValue();
        // SimpleValue passwordValue = (SimpleValue) value.lookupValue("mainWindow.userDetailsForm.password").getValue();
        //
        // oldPasswordValue.setRequired(true);
        // passwordConfirmationValue.setRequired(true);
        // passwordValue.setRequired(true);
        //
        // SimpleValue viewIdentifier = (SimpleValue) value.lookupValue("mainWindow.userDetailsForm.viewIdentifier").getValue();
        // viewIdentifier.setValue("userProfileChangePasswordView");
    }

    public void setPasswordAsRequired(final ViewDefinitionState state, final Locale locale) {
        // SimpleValue passwordConfirmationValue = (SimpleValue) value
        // .lookupValue("mainWindow.userDetailsForm.passwordConfirmation").getValue();
        // SimpleValue passwordValue = (SimpleValue) value.lookupValue("mainWindow.userDetailsForm.password").getValue();
        //
        // passwordConfirmationValue.setRequired(true);
        // passwordValue.setRequired(true);
        //
        // SimpleValue viewIdentifier = (SimpleValue) value.lookupValue("mainWindow.userDetailsForm.viewIdentifier").getValue();
        // viewIdentifier.setValue("userDetailsChangePasswordView");
    }

    public void hidePasswordOnUpdateForm(final ViewDefinitionState state, final Locale locale) {
        // if (value == null || value.lookupValue("mainWindow.userDetailsForm") == null) {
        // return;
        // }
        //
        // FormValue formValue = (FormValue) value.lookupValue("mainWindow.userDetailsForm").getValue();
        //
        // if (formValue.getId() == null) {
        // value.lookupValue("mainWindow.userDetailsForm.password").setVisible(true);
        // value.lookupValue("mainWindow.userDetailsForm.passwordConfirmation").setVisible(true);
        // value.lookupValue("mainWindow.changePasswordButton").setVisible(false);
        // } else {
        // value.lookupValue("mainWindow.userDetailsForm.password").setVisible(false);
        // value.lookupValue("mainWindow.userDetailsForm.passwordConfirmation").setVisible(false);
        // value.lookupValue("mainWindow.changePasswordButton").setVisible(true);
        // }
    }

    public boolean checkPassword(final DataDefinition dataDefinition, final Entity entity) {
        String password = entity.getStringField("password");
        String passwordConfirmation = entity.getStringField("passwordConfirmation");
        String oldPassword = entity.getStringField("oldPassword");
        String viewIdentifier = entity.getStringField("viewIdentifier");

        if (!"userProfileChangePasswordView".equals(viewIdentifier) && !"userDetailsChangePasswordView".equals(viewIdentifier)) {
            return true;
        }

        if ("userProfileChangePasswordView".equals(viewIdentifier)) {
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
