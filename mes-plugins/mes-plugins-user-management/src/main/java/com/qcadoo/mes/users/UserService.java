/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${showChangePasswordButton}")
    private boolean showChangePasswordButton;

    public void setPasswordAndOldPasswordAdRequired(final ViewDefinitionState state, final Locale locale) {
        FieldComponentState viewIdentifier = (FieldComponentState) state
                .getComponentByFunctionalPath("window.user.viewIdentifier");
        PasswordComponentState oldPassword = (PasswordComponentState) state
                .getComponentByFunctionalPath("window.user.oldPassword");
        PasswordComponentState password = (PasswordComponentState) state.getComponentByFunctionalPath("window.user.password");
        PasswordComponentState passwordConfirmation = (PasswordComponentState) state
                .getComponentByFunctionalPath("window.user.passwordConfirmation");
        ButtonComponentState changePasswordButton = (ButtonComponentState) state
                .getComponentByFunctionalPath("window.changePasswordButton");

        oldPassword.setRequired(true);
        password.setRequired(true);
        passwordConfirmation.setRequired(true);
        viewIdentifier.setFieldValue("profileChangePassword");

        if (!showChangePasswordButton) {
            changePasswordButton.setVisible(false);
        }
    }

    public void setPasswordAsRequired(final ViewDefinitionState state, final Locale locale) {
        FieldComponentState viewIdentifier = (FieldComponentState) state
                .getComponentByFunctionalPath("window.user.viewIdentifier");
        PasswordComponentState password = (PasswordComponentState) state.getComponentByFunctionalPath("window.user.password");
        PasswordComponentState passwordConfirmation = (PasswordComponentState) state
                .getComponentByFunctionalPath("window.user.passwordConfirmation");

        password.setRequired(true);
        passwordConfirmation.setRequired(true);
        viewIdentifier.setFieldValue("userChangePassword");
    }

    public void hidePasswordOnUpdateForm(final ViewDefinitionState state, final Locale locale) {
        FormComponentState form = (FormComponentState) state.getComponentByFunctionalPath("window.user");
        PasswordComponentState password = (PasswordComponentState) state.getComponentByFunctionalPath("window.user.password");
        PasswordComponentState passwordConfirmation = (PasswordComponentState) state
                .getComponentByFunctionalPath("window.user.passwordConfirmation");
        ButtonComponentState changePasswordButton = (ButtonComponentState) state
                .getComponentByFunctionalPath("window.changePasswordButton");

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
