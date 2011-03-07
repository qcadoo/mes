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

package com.qcadoo.mes.security.internal;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.aop.Monitorable;
import com.qcadoo.model.api.search.Restrictions;

@Service
public final class SecurityServiceImpl implements SecurityService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    public String getCurrentUserName() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Entity> users = dataDefinitionService.get("users", "user").find().restrictedWith(Restrictions.eq("userName", login))
                .withMaxResults(1).list().getEntities();
        checkState(users.size() > 0, "Current user with login %s cannot be found", login);
        return users.get(0).getStringField("userName");
    }

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    public Long getCurrentUserId() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Entity> users = dataDefinitionService.get("users", "user").find().restrictedWith(Restrictions.eq("userName", login))
                .withMaxResults(1).list().getEntities();
        checkState(users.size() > 0, "Current user with login %s cannot be found", login);
        return users.get(0).getId();
    }

}
