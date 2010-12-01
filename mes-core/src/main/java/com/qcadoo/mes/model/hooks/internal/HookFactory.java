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

package com.qcadoo.mes.model.hooks.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.model.HookDefinition;

@Service
public final class HookFactory {

    @Autowired
    private ApplicationContext applicationContext;

    public HookDefinition getHook(final String fullyQualifiedClassName, final String methodName) {
        Class<?> beanClass;
        try {
            beanClass = HookFactory.class.getClassLoader().loadClass(fullyQualifiedClassName);
            Object bean = applicationContext.getBean(beanClass);
            if (bean != null) {
                return new HookDefinitionImpl(bean, methodName);
            } else {
                throw new IllegalStateException("Cannot find bean for hook: " + fullyQualifiedClassName);
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find mapping class for hook: " + fullyQualifiedClassName, e);
        }
    }

}
