/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.productFlowThruDivision;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.MethodUtils;
import org.hibernate.classic.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.util.FieldUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.plugin.api.Module;

@Service
public class ProductFlowThruDivisionOnStartupService extends Module {

    protected static final Logger LOG = LoggerFactory.getLogger(ProductFlowThruDivisionOnStartupService.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    @Transactional
    public final void multiTenantEnable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Required fields default values from productFlowThruDivision plugin will be populated...");
        }

        Session currentSession = getCurrentSession();
        currentSession
                .createQuery(
                        "UPDATE com.qcadoo.model.beans.technologies.TechnologiesTechnology "
                                + "SET range = '02manyDivisions' WHERE range IS NULL"
                ).executeUpdate();

    }

    private Session getCurrentSession() {
        DataDefinition dataDefinition = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
        Object dataAccessService = FieldUtils.getProtectedFieldValue("dataAccessService", dataDefinition);
        Object hibernateService = FieldUtils.getProtectedFieldValue("hibernateService", dataAccessService);

        try {
            return (Session) MethodUtils.invokeExactMethod(hibernateService, "getCurrentSession", new Object[0]);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
