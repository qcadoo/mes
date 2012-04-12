package com.qcadoo.mes.technologies.hooks;

import static com.qcadoo.mes.basic.constants.ProductFields.NUMBER;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Maps;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

public class ProductDetailsViewHooksTest {

    private ProductDetailsViewHooks productDetailsViewHooks;

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent productForm;

    @Mock
    private Entity product;

    @Mock
    private Entity technologyGroup;

    private Map<String, Object> parameters = Maps.newHashMap();

    private Map<String, String> filters = Maps.newHashMap();

    private Map<String, Object> gridOptions = Maps.newHashMap();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productDetailsViewHooks = new ProductDetailsViewHooks();

        given(view.getComponentByReference("form")).willReturn(productForm);
        given(productForm.getEntity()).willReturn(product);
    }

    @Test
    public void shouldAddTechnologyGroupIfProductIsSaved() {
        // given
        given(product.getId()).willReturn(1L);

        parameters.put("product.id", 1L);

        parameters.put(L_WINDOW_ACTIVE_MENU, "technology.technologyGroups");

        String url = "../page/technologies/technologyGroupDetails.html";

        // when
        productDetailsViewHooks.addTechnologyGroup(view, null, null);

        // then
        verify(view).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntAddTechnologyGroupIfProductIsntSaved() {
        // given
        given(product.getId()).willReturn(null);

        String url = "../page/technologies/technologyGroupDetails.html";

        // when
        productDetailsViewHooks.addTechnologyGroup(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldShowTechnologiesWithTechnologyGroupIfProductIsSaved() {
        // given
        given(product.getId()).willReturn(1L);

        given(product.getBelongsToField("technologyGroup")).willReturn(technologyGroup);

        given(technologyGroup.getStringField(NUMBER)).willReturn("000001");

        filters.put("technologyGroupNumber", "000001");

        gridOptions.put(L_FILTERS, filters);

        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "technology.technologies");

        String url = "../page/technologies/technologiesList.html";

        // when
        productDetailsViewHooks.showTechnologiesWithTechnologyGroup(view, null, null);

        // then
        verify(view).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntShowTechnologiesWithTechnologyGroupIfProductIsntSaved() {
        // given
        given(product.getId()).willReturn(null);

        String url = "../page/technologies/technologiesList.html";

        // when
        productDetailsViewHooks.showTechnologiesWithTechnologyGroup(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntShowTechnologiesWithProductIfProductIsntSaved() {
        // given
        given(product.getId()).willReturn(null);

        String url = "../page/technologies/technologiesList.html";

        // when
        productDetailsViewHooks.showTechnologiesWithProduct(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntShowOrdersWithProductMainIfProductIsntSaved() {
        // given
        given(product.getId()).willReturn(null);

        String url = "../page/orders/ordersList.html";

        // when
        productDetailsViewHooks.showOrdersWithProductMain(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntShowOrdersWithProductPlannedIfProductIsntSaved() {
        // given
        given(product.getId()).willReturn(null);

        String url = "../page/orders/ordersList.html";

        // when
        productDetailsViewHooks.showOrdersWithProductPlanned(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

}
