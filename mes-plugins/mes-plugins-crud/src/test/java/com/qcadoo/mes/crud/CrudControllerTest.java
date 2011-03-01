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

package com.qcadoo.mes.crud;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.ViewDefinition;

public class CrudControllerTest {

    @Test
    public void shouldReturnValidView() throws Exception {
        // given
        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        ViewDefinitionService viewDefinitionService = mock(ViewDefinitionService.class);
        given(viewDefinitionService.get("testPlugin", "testView")).willReturn(viewDefinition);

        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put("context", "");

        CrudController crud = new CrudController();
        ReflectionTestUtils.setField(crud, "viewDefinitionService", viewDefinitionService);

        // when
        ModelAndView mav = crud.prepareView("testPlugin", "testView", arguments, Locale.ENGLISH);

        // then
        assertEquals("crud/crudView", mav.getViewName());
        assertEquals("testView", mav.getModel().get("viewName"));
        assertEquals("testPlugin", mav.getModel().get("pluginIdentifier"));
        assertEquals(Locale.ENGLISH.getLanguage(), mav.getModel().get("locale"));
        assertNull(mav.getModel().get("context"));
        assertEquals(false, mav.getModel().get("popup"));
    }

    @Test
    public void shouldReturnValidViewWithContextAndPopup() throws Exception {
        // given
        ViewDefinition viewDefinition = mock(ViewDefinition.class);
        given(viewDefinition.translateContextReferences("testContext")).willReturn("{context: translatedTestContext}");

        ViewDefinitionService viewDefinitionService = mock(ViewDefinitionService.class);
        given(viewDefinitionService.get("testPlugin", "testView")).willReturn(viewDefinition);

        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put("context", "testContext");
        arguments.put("popup", "true");

        CrudController crud = new CrudController();
        ReflectionTestUtils.setField(crud, "viewDefinitionService", viewDefinitionService);

        // when
        ModelAndView mav = crud.prepareView("testPlugin", "testView", arguments, Locale.ENGLISH);

        // then
        assertEquals("crud/crudView", mav.getViewName());
        assertEquals("testView", mav.getModel().get("viewName"));
        assertEquals("testPlugin", mav.getModel().get("pluginIdentifier"));
        assertEquals(Locale.ENGLISH.getLanguage(), mav.getModel().get("locale"));
        assertEquals("{context: translatedTestContext}", mav.getModel().get("context"));
        assertEquals(true, mav.getModel().get("popup"));
    }

    @Test
    public void shouldPerformEvent() throws Exception {
        // given
        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        ViewDefinitionService viewDefinitionService = mock(ViewDefinitionService.class);
        given(viewDefinitionService.get("testPlugin", "testView")).willReturn(viewDefinition);

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("test", "testVal1");
        JSONObject jsonResult = new JSONObject();
        jsonResult.put("test", "testVal2");

        CrudController crud = new CrudController();
        ReflectionTestUtils.setField(crud, "viewDefinitionService", viewDefinitionService);

        given(viewDefinition.performEvent(jsonBody, Locale.ENGLISH)).willReturn(jsonResult);

        // when
        Object result = crud.performEvent("testPlugin", "testView", jsonBody, Locale.ENGLISH);

        // then
        assertEquals(jsonResult, result);
    }
}
