/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.users;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.components.ButtonComponentState;
import com.qcadoo.view.components.FieldComponentState;
import com.qcadoo.view.components.PasswordComponentState;
import com.qcadoo.view.components.form.FormComponentState;

@Service
public final class UserService {

    @Value("${setAsDemoEnviroment}")
    private boolean setAsDemoEnviroment;

    public void setPasswordAndOldPasswordAdRequired(final ViewDefinitionState state) {
        FieldComponentState viewIdentifier = (FieldComponentState) state.getComponentByReference("viewIdentifierHiddenInput");
        PasswordComponentState oldPassword = (PasswordComponentState) state.getComponentByReference("oldPasswordTextInput");
        PasswordComponentState password = (PasswordComponentState) state.getComponentByReference("passwordTextInput");
        PasswordComponentState passwordConfirmation = (PasswordComponentState) state
                .getComponentByReference("passwordConfirmationTextInput");

        oldPassword.setRequired(true);
        password.setRequired(true);
        passwordConfirmation.setRequired(true);
        viewIdentifier.setFieldValue("profileChangePassword");
    }

    public void removeChangePasswordButtonWhenNotAllowed(final ViewDefinitionState state) {
        if (setAsDemoEnviroment) {
            ButtonComponentState changePasswordButton = (ButtonComponentState) state
                    .getComponentByReference("changePasswordButton");
            changePasswordButton.setVisible(false);
        }
    }

    public void setPasswordAsRequired(final ViewDefinitionState state) {
        FieldComponentState viewIdentifier = (FieldComponentState) state.getComponentByReference("viewIdentifierHiddenInput");
        PasswordComponentState password = (PasswordComponentState) state.getComponentByReference("passwordTextInput");
        PasswordComponentState passwordConfirmation = (PasswordComponentState) state
                .getComponentByReference("passwordConfirmationTextInput");

        password.setRequired(true);
        passwordConfirmation.setRequired(true);
        viewIdentifier.setFieldValue("userChangePassword");
    }

    public void hidePasswordOnUpdateForm(final ViewDefinitionState state) {
        FormComponentState form = (FormComponentState) state.getComponentByReference("form");
        PasswordComponentState password = (PasswordComponentState) state.getComponentByReference("passwordTextInput");
        PasswordComponentState passwordConfirmation = (PasswordComponentState) state
                .getComponentByReference("passwordConfirmationTextInput");
        ButtonComponentState changePasswordButton = (ButtonComponentState) state.getComponentByReference("changePasswordButton");

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
        String viewIdentifier = entity.getId() == null ? "userChangePassword" : entity.getStringField("viewIdentifier");

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
