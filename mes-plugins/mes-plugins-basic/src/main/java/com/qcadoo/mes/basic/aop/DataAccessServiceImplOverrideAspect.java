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
package com.qcadoo.mes.basic.aop;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.types.HasManyType;
import com.qcadoo.plugin.api.RunIfEnabled;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Aspect
@Configurable
@RunIfEnabled(BasicConstants.PLUGIN_IDENTIFIER)
public class DataAccessServiceImplOverrideAspect {

    @Autowired
    private ParameterService parameterService;

    @Pointcut("execution(private boolean com.qcadoo.model.internal.DataAccessServiceImpl.isFieldCopyable(..)) "
            + "&& args(fieldTypeClass, fieldDefinition, dataDefinition)")
    public void isFieldCopyable(final Class fieldTypeClass, final FieldDefinition fieldDefinition, final DataDefinition dataDefinition) {
    }

    @Around("isFieldCopyable(fieldTypeClass, fieldDefinition, dataDefinition)")
    public boolean aroundIsFieldCopyable(final ProceedingJoinPoint pjp,
                                         final Class fieldTypeClass, final FieldDefinition fieldDefinition, final DataDefinition dataDefinition) throws Throwable {
        if (fieldTypeClass.equals(HasManyType.class) && BasicConstants.MODEL_PRODUCT.equals(dataDefinition.getName()) && ProductFields.PRODUCT_ATTRIBUTE_VALUES.equals(fieldDefinition.getName())) {
            return parameterService.getParameter().getBooleanField(ParameterFields.COPY_ATTRIBUTES_TO_PRODUCTS);
        } else {
            return (boolean) pjp.proceed();
        }
    }

}
