/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

public class CompanyServiceTest {

    private CompanyService companyService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private DataDefinition companyDD;

    @Mock
    private Entity company;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Before
    public final void init() {
        companyService = new CompanyService();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(companyService, "dataDefinitionService", dataDefinitionService);

        given(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY)).willReturn(companyDD);
        given(companyDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(Mockito.anyInt())).willReturn(searchCriteriaBuilder);
    }

    @Test
    public void shouldReturnExistingCompanyEntityId() throws Exception {
        // given
        Entity company = Mockito.mock(Entity.class);
        given(company.getId()).willReturn(13L);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(company);

        // when
        Long id = companyService.getCompanyId();

        // then
        assertEquals(Long.valueOf(13L), id);
    }

    @Test
    public void shouldReturnExistingCompanyEntity() throws Exception {
        // given
        Entity company = Mockito.mock(Entity.class);
        given(company.getId()).willReturn(13L);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(company);
        given(company.isValid()).willReturn(true);

        // when
        Entity existingcompany = companyService.getCompany();

        // then
        assertEquals(company, existingcompany);
    }

    @Test
    public void shouldReturnNewCompanyEntity() throws Exception {
        // given
        Entity company = mock(Entity.class);
        given(company.isValid()).willReturn(true);
        given(companyDD.create()).willReturn(company);

        Entity savedCompany = mock(Entity.class);
        given(savedCompany.isValid()).willReturn(true);
        given(savedCompany.getId()).willReturn(15L);

        given(companyDD.save(company)).willReturn(savedCompany);

        // when
        Entity returnedcompany = companyService.getCompany();

        // then
        verify(companyDD).save(company);
        assertEquals(Long.valueOf(15L), returnedcompany.getId());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfNewCompanyEntityIsNotValid() throws Exception {
        // given
        Entity company = mock(Entity.class);
        given(company.isValid()).willReturn(true);
        given(companyDD.create()).willReturn(company);

        Entity savedCompany = mock(Entity.class);
        given(savedCompany.isValid()).willReturn(false);

        given(companyDD.save(company)).willReturn(savedCompany);

        // when
        companyService.getCompany();
    }

    @Test
    public void shouldReturnOwnerTrue() throws Exception {
        // given
        FormComponent form = mock(FormComponent.class);
        final Boolean expectedOwner = Boolean.TRUE;

        Long companyId = 1L;
        given(form.getEntityId()).willReturn(companyId);
        given(companyDD.get(companyId)).willReturn(company);
        given(company.getField("owner")).willReturn(expectedOwner);

        // when
        Boolean owner = companyService.getOwning(form);

        // then
        Assert.assertEquals(expectedOwner, owner);
    }

    @Test
    public void shouldReturnOwnerFalse() throws Exception {
        // given
        FormComponent form = mock(FormComponent.class);
        final Boolean expectedOwner = Boolean.FALSE;

        Long companyId = 1L;
        given(form.getEntityId()).willReturn(companyId);
        given(companyDD.get(companyId)).willReturn(company);
        given(company.getField("owner")).willReturn(expectedOwner);

        // when
        Boolean owner = companyService.getOwning(form);

        // then
        Assert.assertEquals(expectedOwner, owner);
    }

    @Test
    public void shouldDisableCompanyFormForOwner() throws Exception {
        // given
        FormComponent form = mock(FormComponent.class);
        Long companyId = 1L;

        given(view.getComponentByReference("form")).willReturn(form);
        given(form.getEntityId()).willReturn(companyId);
        given(companyDD.get(companyId)).willReturn(company);
        given(company.getField("owner")).willReturn(true);

        // when
        companyService.disableCompanyFormForOwner(view);

        // then
        verify(form).setFormEnabled(false);
        verify(form, Mockito.never()).setFormEnabled(true);
    }

    public void shouldNotDisableCompanyFormForNonowner() throws Exception {
        // given
        FormComponent form = mock(FormComponent.class);
        Long companyId = 1L;

        given(view.getComponentByReference("form")).willReturn(form);
        given(form.getEntityId()).willReturn(companyId);
        given(companyDD.get(companyId)).willReturn(company);
        given(company.getField("owner")).willReturn(false);

        // when
        companyService.disableCompanyFormForOwner(view);

        // then
        verify(form, Mockito.never()).setFormEnabled(Mockito.anyBoolean());
    }

    @Test
    public void shouldDisableCompanyGridForOwner() throws Exception {

        // given
        ViewDefinitionState view = mock(ViewDefinitionState.class);
        FormComponent form = mock(FormComponent.class);
        Map<String, GridComponent> componentsMap = new HashMap<String, GridComponent>();
        for (String ref : new String[] { "1", "2", "3" }) {
            componentsMap.put(ref, mock(GridComponent.class));
        }

        given(view.getComponentByReference("form")).willReturn(form);
        given(form.getEntityId()).willReturn(1L);
        given(companyDD.get(Mockito.anyLong())).willReturn(company);
        given(company.getField("owner")).willReturn(true);

        for (Map.Entry<String, GridComponent> mockEntry : componentsMap.entrySet()) {

            given(view.getComponentByReference(mockEntry.getKey())).willReturn((ComponentState) mockEntry.getValue());
        }

        // when
        companyService.disabledGridWhenCompanyIsAnOwner(view, componentsMap.keySet().toArray(new String[0]));

        // then
        for (GridComponent gridComponent : componentsMap.values()) {
            verify(gridComponent).setEditable(false);
        }
    }

    @Test
    public void shouldNotDisableCompanyGridForNonOwner() throws Exception {

        // given
        ViewDefinitionState view = mock(ViewDefinitionState.class);
        FormComponent form = mock(FormComponent.class);
        Map<String, GridComponent> componentsMap = new HashMap<String, GridComponent>();
        for (String ref : new String[] { "1", "2", "3" }) {
            componentsMap.put(ref, mock(GridComponent.class));
        }

        given(view.getComponentByReference("form")).willReturn(form);
        given(form.getEntityId()).willReturn(1L);
        given(companyDD.get(Mockito.anyLong())).willReturn(company);
        given(company.getField("owner")).willReturn(false);

        // when
        companyService.disabledGridWhenCompanyIsAnOwner(view, componentsMap.keySet().toArray(new String[0]));

        // then
        for (GridComponent gridComponent : componentsMap.values()) {
            verify(gridComponent, Mockito.never()).setEditable(Mockito.anyBoolean());
        }
    }

}
