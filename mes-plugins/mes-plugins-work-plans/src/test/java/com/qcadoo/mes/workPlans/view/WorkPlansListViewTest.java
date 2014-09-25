package com.qcadoo.mes.workPlans.view;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.internal.api.ViewDefinition;

public class WorkPlansListViewTest {

    private WorkPlansListView workPlansListView;

    @Mock
    private WindowComponent windowComponent;

    @Mock
    private GridComponent workPlansGrid;

    @Mock
    private RibbonActionItem deleteButton;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        workPlansListView = new WorkPlansListView(windowComponent, deleteButton, workPlansGrid);
    }

    @Test
    public final void shouldBuildFromViewDefinitionState() {
        // given
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        given(viewDefinitionState.getComponentByReference("window")).willReturn((ComponentState) windowComponent);
        Ribbon ribbon = mock(Ribbon.class);
        given(windowComponent.getRibbon()).willReturn(ribbon);
        RibbonGroup actionsRibbonGroup = mock(RibbonGroup.class);
        given(ribbon.getGroupByName("actions")).willReturn(actionsRibbonGroup);
        given(actionsRibbonGroup.getItemByName("delete")).willReturn(deleteButton);

        given(viewDefinitionState.getComponentByReference("grid")).willReturn(workPlansGrid);

        // when
        WorkPlansListView workPlansListView = WorkPlansListView.from(viewDefinitionState);
        workPlansListView.setUpDeleteButton(true, null);
        workPlansListView.getSelectedWorkPlans();

        // then
        verify(deleteButton).setEnabled(anyBoolean());
        verify(workPlansGrid).getSelectedEntities();

    }

    @Test
    public final void shouldGetSelectedEntities() {
        // given
        List<Entity> entities = ImmutableList.of(mockEntity(), mockEntity(), mockEntity(), mockEntity());
        given(workPlansGrid.getSelectedEntities()).willReturn(entities);

        // when
        List<Entity> result = workPlansListView.getSelectedWorkPlans();

        // then
        assertEquals(entities, result);
    }

    @Test
    public final void shouldSetUpDeleteButton() {
        // given
        boolean isEnabled = true;
        String message = "some arbitrary mesage";

        // when
        workPlansListView.setUpDeleteButton(isEnabled, message);

        // then
        verify(deleteButton).setEnabled(isEnabled);
        verify(deleteButton).setMessage(message);
        verify(deleteButton).requestUpdate(true);
        verify(windowComponent).requestRibbonRender();
    }

}
