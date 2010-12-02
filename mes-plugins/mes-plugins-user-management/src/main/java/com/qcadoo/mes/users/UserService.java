/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
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

import java.io.IOException;
import java.util.Locale;

import org.dom4j.DocumentException;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.view.ViewValue;
import com.qcadoo.mes.view.components.SimpleValue;
import com.qcadoo.mes.view.containers.FormValue;

@Service
public final class UserService {

    public void onUserProfileChangePasswordView(final ViewValue<Long> value, final String triggerComponentName,
            final Entity entity, final Locale locale) throws IOException, DocumentException {
        SimpleValue oldPasswordValue = (SimpleValue) value.lookupValue("mainWindow.userDetailsForm.oldPassword").getValue();
        SimpleValue passwordConfirmationValue = (SimpleValue) value
                .lookupValue("mainWindow.userDetailsForm.passwordConfirmation").getValue();
        SimpleValue passwordValue = (SimpleValue) value.lookupValue("mainWindow.userDetailsForm.password").getValue();

        oldPasswordValue.setRequired(true);
        passwordConfirmationValue.setRequired(true);
        passwordValue.setRequired(true);

        SimpleValue viewIdentifier = (SimpleValue) value.lookupValue("mainWindow.userDetailsForm.viewIdentifier").getValue();
        viewIdentifier.setValue("userProfileChangePasswordView");
    }

    public void onUserDetailsChangePasswordView(final ViewValue<Long> value, final String triggerComponentName,
            final Entity entity, final Locale locale) throws IOException, DocumentException {
        SimpleValue passwordConfirmationValue = (SimpleValue) value
                .lookupValue("mainWindow.userDetailsForm.passwordConfirmation").getValue();
        SimpleValue passwordValue = (SimpleValue) value.lookupValue("mainWindow.userDetailsForm.password").getValue();

        passwordConfirmationValue.setRequired(true);
        passwordValue.setRequired(true);

        SimpleValue viewIdentifier = (SimpleValue) value.lookupValue("mainWindow.userDetailsForm.viewIdentifier").getValue();
        viewIdentifier.setValue("userDetailsChangePasswordView");
    }

    public void onUserDetailsViewView(final ViewValue<Long> value, final String triggerComponentName, final Entity entity,
            final Locale locale) throws IOException, DocumentException {

        if (value == null || value.lookupValue("mainWindow.userDetailsForm") == null) {
            return;
        }

        FormValue formValue = (FormValue) value.lookupValue("mainWindow.userDetailsForm").getValue();

        if (formValue.getId() == null) {
            if (value.lookupValue("mainWindow.userDetailsForm.password") != null) {
                value.lookupValue("mainWindow.userDetailsForm.password").setVisible(true);
            } else {
                return;
            }
            value.lookupValue("mainWindow.userDetailsForm.passwordConfirmation").setVisible(true);
            value.lookupValue("mainWindow.changePasswordButton").setVisible(false);
        } else {
            value.lookupValue("mainWindow.userDetailsForm.password").setVisible(false);
            value.lookupValue("mainWindow.userDetailsForm.passwordConfirmation").setVisible(false);
            value.lookupValue("mainWindow.changePasswordButton").setVisible(true);
        }

    }

    public void onUserProfileView(final ViewValue<Long> value, final String triggerComponentName, final Entity entity,
            final Locale locale) throws IOException, DocumentException {
        if (value == null || value.lookupValue("mainWindow.userDetailsForm") == null) {
            return;
        }
        String envHomeProperty = System.getenv("ENV_HOME");
        if (envHomeProperty != null && envHomeProperty.equals("amazon")) {
            value.lookupValue("mainWindow.changePasswordButton").setVisible(false);
        }
    }

    public boolean checkPassword(final DataDefinition dataDefinition, final Entity entity) {

        String password = entity.getStringField("password");
        String passwordConfirmation = entity.getStringField("passwordConfirmation");
        String oldPassword = entity.getStringField("oldPassword");
        String viewIdentifier = entity.getStringField("viewIdentifier");

        System.out.println("ABC");
        System.out.println(entity.getFields().containsKey("passwordConfirmation"));
        System.out.println(password);
        System.out.println(passwordConfirmation);
        System.out.println(oldPassword);
        System.out.println(viewIdentifier);

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
