/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.timeNormsForOperations;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

public class NormServiceTest {

    private NormService normService;

    @Mock
    private TechnologyService technologyService;

    @Mock
    private TranslationService translationService;

    @Mock
    private Entity technology, operComp1;

    private static EntityTree mockEntityTreeIterator(List<Entity> list) {
        EntityTree entityTree = mock(EntityTree.class);
        when(entityTree.iterator()).thenReturn(list.iterator());
        return entityTree;
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        normService = new NormService();

        ReflectionTestUtils.setField(normService, "technologyService", technologyService);

        given(operComp1.getStringField("entityType")).willReturn("operation");
    }

    @Test
    public void shouldOmmitCheckingIfTheTreeIsntCompleteYet() {
        // given
        EntityTree tree = mockEntityTreeIterator(new LinkedList<Entity>());
        given(technology.getTreeField("operationComponents")).willReturn(tree);

        // when
        Map<String, String> messages = normService.checkOperationOutputQuantities(technology);

        // then
        assertTrue(messages.isEmpty());
    }

    @Test
    public void shouldNotFreakOutIfItCantFindOutputProductForAnOperationComponent() {
        // given
        EntityTree tree = mockEntityTreeIterator(asList(operComp1));
        given(technology.getTreeField("operationComponents")).willReturn(tree);
        given(technologyService.getProductCountForOperationComponent(operComp1)).willThrow(new IllegalStateException());

        // when
        Map<String, String> messages = normService.checkOperationOutputQuantities(technology);

        // then
        assertTrue(messages.isEmpty());
    }

    @Ignore
    @Test
    public void shouldReturnAnErrorMessageIfTheQuantitiesDontMatch() {
        // given
        EntityTree tree = mockEntityTreeIterator(asList(operComp1));
        given(technology.getTreeField("operationComponents")).willReturn(tree);
        given(technologyService.getProductCountForOperationComponent(operComp1)).willReturn(new BigDecimal(13.5));
        given(operComp1.getDecimalField("productionInOneCycle")).willReturn(new BigDecimal(13.51));

        Locale locale = LocaleContextHolder.getLocale();

        given(translationService.translate("technologies.technology.validate.error.invalidQuantity1", locale)).willReturn(
                "message1");
        given(operComp1.getStringField("nodeNumber")).willReturn("1");
        given(translationService.translate("technologies.technology.validate.error.invalidQuantity2", locale)).willReturn(
                "message2");

        // when
        Map<String, String> messages = normService.checkOperationOutputQuantities(technology);

        // then
        assertEquals(1, messages.size());
        assertEquals("message1 1 message2", messages.get(0));
    }

    @Ignore
    @Test
    public void shouldReturnNoErrorsIfTheQuantitiesDoMatch() {
        // given
        EntityTree tree = mockEntityTreeIterator(asList(operComp1));
        given(technology.getTreeField("operationComponents")).willReturn(tree);
        given(technologyService.getProductCountForOperationComponent(operComp1)).willReturn(new BigDecimal(13.5));
        given(operComp1.getDecimalField("productionInOneCycle")).willReturn(new BigDecimal(13.500));

        // when
        Map<String, String> messages = normService.checkOperationOutputQuantities(technology);

        // then
        assertEquals(0, messages.size());
    }
}
