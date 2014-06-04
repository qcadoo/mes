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
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.view.api.crud.CrudService;

public class BasicControllerTest {

    @Test
    public void shouldPrepareViewForParameters() throws Exception {
        // // given
        Map<String, String> arguments = ImmutableMap.of("context", "{\"form.id\":\"13\"}");
        ModelAndView expectedMav = mock(ModelAndView.class);
        CrudService crudController = mock(CrudService.class);
        given(
                crudController.prepareView(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.VIEW_PARAMETERS, arguments,
                        Locale.ENGLISH)).willReturn(expectedMav);

        ParameterService parameterService = mock(ParameterService.class);
        given(parameterService.getParameterId()).willReturn(13L);

        BasicController basicController = new BasicController();
        setField(basicController, "crudService", crudController);
        setField(basicController, "parameterService", parameterService);

        // // when
        ModelAndView mav = basicController.getParameterPageView(Locale.ENGLISH);

        // // then
        assertEquals(expectedMav, mav);
    }

}
