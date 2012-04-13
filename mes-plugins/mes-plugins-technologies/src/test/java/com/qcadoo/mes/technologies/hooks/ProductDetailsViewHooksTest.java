package com.qcadoo.mes.technologies.hooks;

import static com.qcadoo.mes.basic.constants.ProductFields.NAME;
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
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.internal.components.window.WindowComponentState;

public class ProductDetailsViewHooksTest {

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    private static final String L_TECHNOLOGY_GROUP_NUMBER = "000001";

    private static final String L_PRODUCT_NAME = "ProductName";

    private static final String L_PRODUCT_NUMBER = "000001";

    private static final long L_ID = 1L;

    private ProductDetailsViewHooks productDetailsViewHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent productForm;

    @Mock
    private WindowComponentState window;

    @Mock
    private Ribbon ribbon;

    @Mock
    private RibbonGroup technologies, orders;

    @Mock
    private RibbonActionItem addTechnologyGroup, showTechnologiesWithTechnologyGroup, showTechnologiesWithProduct,
            showOrdersWithProductMain, showOrdersWithProductPlanned;

    @Mock
    private Entity product, technologyGroup;

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
    public void shouldAddTechnologyGroupIfProductIsSaved() {
        // given
        given(product.getId()).willReturn(L_ID);

        parameters.put("product.id", L_ID);

        parameters.put(L_WINDOW_ACTIVE_MENU, "technology.technologyGroups");

        String url = "../page/technologies/technologyGroupDetails.html";

        // when
        productDetailsViewHooks.addTechnologyGroup(view, null, null);

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
    public void shouldntShowTechnologiesWithTechnologyGroupIfProductIsSavedAndTechnologyGroupIsNull() {
        // given
        given(product.getId()).willReturn(L_ID);

        given(product.getBelongsToField("technologyGroup")).willReturn(null);

        String url = "../page/technologies/technologiesList.html";

        // when
        productDetailsViewHooks.showTechnologiesWithTechnologyGroup(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldShowTechnologiesWithTechnologyGroupIfProductIsSavedAndTechnologyGroupIsntNull() {
        // given
        given(product.getId()).willReturn(L_ID);

        given(product.getBelongsToField("technologyGroup")).willReturn(technologyGroup);

        given(technologyGroup.getStringField(NUMBER)).willReturn(L_TECHNOLOGY_GROUP_NUMBER);

        filters.put("technologyGroupNumber", L_TECHNOLOGY_GROUP_NUMBER);

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
    public void shouldntShowTechnologiesWithProductIfProductIsntSavedAndProductNameIsNull() {
        // given
        given(product.getId()).willReturn(1L);

        given(product.getStringField(NAME)).willReturn(null);

        String url = "../page/technologies/technologiesList.html";

        // when
        productDetailsViewHooks.showTechnologiesWithProduct(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldShowTechnologiesWithProductIfProductIsSavedAndProductNameIsntNull() {
        // given
        given(product.getId()).willReturn(L_ID);

        given(product.getStringField(NAME)).willReturn(L_PRODUCT_NAME);

        filters.put("productName", L_PRODUCT_NAME);

        gridOptions.put(L_FILTERS, filters);

        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "technology.technologies");

        String url = "../page/technologies/technologiesList.html";

        // when
        productDetailsViewHooks.showTechnologiesWithProduct(view, null, null);

        // then
        verify(view).redirectTo(url, false, true, parameters);
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
    public void shouldntShowOrdersWithProductMainIfProductIsSavedAndProductNumberIsNull() {
        // given
        given(product.getId()).willReturn(L_ID);

        given(product.getStringField(NUMBER)).willReturn(null);

        String url = "../page/orders/ordersList.html";

        // when
        productDetailsViewHooks.showOrdersWithProductMain(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldShowOrdersWithProductMainIfProductIsSavedAndProductNumberIsntNull() {
        // given
        given(product.getId()).willReturn(L_ID);

        given(product.getStringField(NUMBER)).willReturn(L_PRODUCT_NUMBER);

        filters.put("productNumber", L_PRODUCT_NUMBER);

        gridOptions.put(L_FILTERS, filters);

        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "orders.productionOrders");

        String url = "../page/orders/ordersList.html";

        // when
        productDetailsViewHooks.showOrdersWithProductMain(view, null, null);

        // then
        verify(view).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntShowOrdersWithProductPlannedIfProductIsntSaved() {
        // given
        given(product.getId()).willReturn(null);

        String url = "../page/orders/ordersPlanningList.html";

        // when
        productDetailsViewHooks.showOrdersWithProductPlanned(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntShowOrdersWithProductPlannedIfProductIsSavedAndProductNumberIsNull() {
        // given
        given(product.getId()).willReturn(L_ID);

        given(product.getStringField(NUMBER)).willReturn(null);

        String url = "../page/orders/ordersPlanningList.html";

        // when
        productDetailsViewHooks.showOrdersWithProductPlanned(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldShowOrdersWithProductPlannedIfProductIsSavedAndProductNumberIsntNull() {
        // given
        given(product.getId()).willReturn(L_ID);

        given(product.getStringField(NUMBER)).willReturn(L_PRODUCT_NUMBER);

        filters.put("productNumber", L_PRODUCT_NUMBER);

        gridOptions.put(L_FILTERS, filters);

        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "orders.productionOrdersPlanning");

        String url = "../page/orders/ordersPlanningList.html";

        // when
        productDetailsViewHooks.showOrdersWithProductPlanned(view, null, null);

        // then
        verify(view).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntUpdateRibbonStateIfProductIsntSaved() {
        // given
        given(product.getId()).willReturn(null);

        given(view.getComponentByReference("window")).willReturn((ComponentState) window);

        given(window.getRibbon()).willReturn(ribbon);

        given(ribbon.getGroupByName("technologies")).willReturn(technologies);
        given(ribbon.getGroupByName("orders")).willReturn(orders);

        given(technologies.getItemByName("addTechnologyGroup")).willReturn(addTechnologyGroup);
        given(technologies.getItemByName("showTechnologiesWithTechnologyGroup")).willReturn(showTechnologiesWithTechnologyGroup);
        given(technologies.getItemByName("showTechnologiesWithProduct")).willReturn(showTechnologiesWithProduct);

        given(orders.getItemByName("showOrdersWithProductMain")).willReturn(showOrdersWithProductMain);
        given(orders.getItemByName("showOrdersWithProductPlanned")).willReturn(showOrdersWithProductPlanned);

        // when
        productDetailsViewHooks.updateRibbonState(view);

        // then
        verify(addTechnologyGroup).setEnabled(false);
        verify(showTechnologiesWithTechnologyGroup).setEnabled(false);
        verify(showTechnologiesWithProduct).setEnabled(false);
        verify(showOrdersWithProductMain).setEnabled(false);
        verify(showOrdersWithProductPlanned).setEnabled(false);
    }

    @Test
    public void shouldUpdateRibbonStateIfProductIsSavedAndTechnologyGroupIsNull() {
        // given
        given(product.getId()).willReturn(L_ID);

        given(product.getBelongsToField("technologyGroup")).willReturn(null);

        given(view.getComponentByReference("window")).willReturn((ComponentState) window);

        given(window.getRibbon()).willReturn(ribbon);

        given(ribbon.getGroupByName("technologies")).willReturn(technologies);
        given(ribbon.getGroupByName("orders")).willReturn(orders);

        given(technologies.getItemByName("addTechnologyGroup")).willReturn(addTechnologyGroup);
        given(technologies.getItemByName("showTechnologiesWithTechnologyGroup")).willReturn(showTechnologiesWithTechnologyGroup);
        given(technologies.getItemByName("showTechnologiesWithProduct")).willReturn(showTechnologiesWithProduct);

        given(orders.getItemByName("showOrdersWithProductMain")).willReturn(showOrdersWithProductMain);
        given(orders.getItemByName("showOrdersWithProductPlanned")).willReturn(showOrdersWithProductPlanned);

        // when
        productDetailsViewHooks.updateRibbonState(view);

        // then
        verify(addTechnologyGroup).setEnabled(true);
        verify(showTechnologiesWithTechnologyGroup).setEnabled(false);
        verify(showTechnologiesWithProduct).setEnabled(true);
        verify(showOrdersWithProductMain).setEnabled(true);
        verify(showOrdersWithProductPlanned).setEnabled(true);
    }

    @Test
    public void shouldUpdateRibbonStateIfProductIsSavedAndTechnologyGroupIsntNull() {
        // given
        given(product.getId()).willReturn(L_ID);

        given(product.getBelongsToField("technologyGroup")).willReturn(technologyGroup);

        given(view.getComponentByReference("window")).willReturn((ComponentState) window);

        given(window.getRibbon()).willReturn(ribbon);

        given(ribbon.getGroupByName("technologies")).willReturn(technologies);
        given(ribbon.getGroupByName("orders")).willReturn(orders);

        given(technologies.getItemByName("addTechnologyGroup")).willReturn(addTechnologyGroup);
        given(technologies.getItemByName("showTechnologiesWithTechnologyGroup")).willReturn(showTechnologiesWithTechnologyGroup);
        given(technologies.getItemByName("showTechnologiesWithProduct")).willReturn(showTechnologiesWithProduct);

        given(orders.getItemByName("showOrdersWithProductMain")).willReturn(showOrdersWithProductMain);
        given(orders.getItemByName("showOrdersWithProductPlanned")).willReturn(showOrdersWithProductPlanned);

        // when
        productDetailsViewHooks.updateRibbonState(view);

        // then
        verify(addTechnologyGroup).setEnabled(true);
        verify(showTechnologiesWithTechnologyGroup).setEnabled(true);
        verify(showTechnologiesWithProduct).setEnabled(true);
        verify(showOrdersWithProductMain).setEnabled(true);
        verify(showOrdersWithProductPlanned).setEnabled(true);
    }
}
