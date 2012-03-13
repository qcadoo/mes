package com.qcadoo.mes.basic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

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

    @Before
    public final void init() {
        companyService = new CompanyService();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(companyService, "dataDefinitionService", dataDefinitionService);

        when(dataDefinitionService.get("basic", "company")).thenReturn(companyDD);
    }

    @Test
    public void shouldDisableCompanyFormForOwner() throws Exception {
        // given
        FormComponent form = mock(FormComponent.class);
        Long companyId = 1L;

        when(view.getComponentByReference("form")).thenReturn(form);
        when(form.getEntityId()).thenReturn(companyId);
        when(companyDD.get(companyId)).thenReturn(company);
        when(company.getField("owner")).thenReturn(true);

        // when
        companyService.disableCompanyFormForOwner(view);

        // then
        verify(form).setFormEnabled(false);
    }

    @Test
    public void shouldReturnIdExistsEntity() throws Exception {
        // given
        SearchCriteriaBuilder search = Mockito.mock(SearchCriteriaBuilder.class);
        SearchCriterion criterion = SearchRestrictions.eq("owner", true);
        when(companyDD.find()).thenReturn(search);
        when(search.add(criterion)).thenReturn(search);
        when(search.setMaxResults(1)).thenReturn(search);
        when(search.uniqueResult()).thenReturn(company);
        // when
        long parameterId = companyService.getParameterId();
        // then
        Assert.assertEquals(0L, parameterId);
    }

    @Test
    public void shouldReturnIdCreatedEntity() throws Exception {
        // given
        SearchCriteriaBuilder search = Mockito.mock(SearchCriteriaBuilder.class);
        SearchCriterion criterion = SearchRestrictions.eq("owner", true);
        when(companyDD.find()).thenReturn(search);
        when(search.add(criterion)).thenReturn(search);
        when(search.setMaxResults(1)).thenReturn(search);
        when(search.uniqueResult()).thenReturn(null);

        when(companyDD.create()).thenReturn(company);
        when(companyDD.save(company)).thenReturn(company);
        // when
        long parameterId = companyService.getParameterId();
        // then
        Assert.assertEquals(0L, parameterId);
    }

}
