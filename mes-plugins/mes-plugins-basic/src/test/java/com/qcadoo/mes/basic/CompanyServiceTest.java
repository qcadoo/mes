/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.basic;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

public class CompanyServiceTest {

    private CompanyService companyService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition companyDD;

    @Mock
    private Entity company, parameter;

    @Mock
    private ParameterService parameterService;

    @Before
    public final void init() {
        companyService = new CompanyService();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(companyService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(companyService, "parameterService", parameterService);

        given(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY)).willReturn(companyDD);

    }

    @Test
    public void shouldReturnExistingCompanyEntityId() throws Exception {
        // given
        Entity company = Mockito.mock(Entity.class);
        given(company.getId()).willReturn(13L);
        given(parameterService.getParameter()).willReturn(parameter);
        given(parameter.getBelongsToField(ParameterFields.COMPANY)).willReturn(company);
        // when
        Long id = companyService.getCompanyId();

        // then
        assertEquals(Long.valueOf(13L), id);
    }

    @Test
    public void shouldReturnExistingCompanyEntity() throws Exception {
        // given
        given(parameterService.getParameter()).willReturn(parameter);
        given(parameter.getBelongsToField(ParameterFields.COMPANY)).willReturn(company);
        given(company.isValid()).willReturn(true);

        // when
        Entity existingcompany = companyService.getCompany();

        // then
        assertEquals(company, existingcompany);
    }

}
