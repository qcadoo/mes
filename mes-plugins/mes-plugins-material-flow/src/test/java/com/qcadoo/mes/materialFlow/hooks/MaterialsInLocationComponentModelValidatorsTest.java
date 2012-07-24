package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.MaterialsInLocationComponentFields.LOCATION;
import static com.qcadoo.mes.materialFlow.constants.MaterialsInLocationComponentFields.MATERIALS_IN_LOCATION;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class MaterialsInLocationComponentModelValidatorsTest {

    private MaterialsInLocationComponentModelValidators materialsInLocationComponentModelValidators;

    private static final Long L_ID = 1L;

    private static final Long L_ID_OTHER = 2L;

    @Mock
    private DataDefinition materialsInLocationComponentDD;

    @Mock
    private Entity materialsInLocationComponent, materialsInLocationComponentOther, location, materialsInLocation;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private SearchResult searchResult;

    @Mock
    private List<Entity> entities;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        materialsInLocationComponentModelValidators = new MaterialsInLocationComponentModelValidators();

        PowerMockito.mockStatic(SearchRestrictions.class);
    }

    @Test
    public void shouldReturnFalseWhenCheckMaterialFlowComponentUniquenessAndMaterialsInLocationOrLocationIsNull() {
        // given
        given(materialsInLocationComponent.getBelongsToField(LOCATION)).willReturn(null);
        given(materialsInLocationComponent.getBelongsToField(MATERIALS_IN_LOCATION)).willReturn(null);

        // when
        boolean result = materialsInLocationComponentModelValidators.checkMaterialFlowComponentUniqueness(
                materialsInLocationComponentDD, materialsInLocationComponent);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseAndAddErrorWhenCheckMaterialFlowComponentUniquenessAndMaterialFlowComponentIsntUnique() {
        // given
        given(materialsInLocationComponent.getBelongsToField(LOCATION)).willReturn(location);
        given(materialsInLocationComponent.getBelongsToField(MATERIALS_IN_LOCATION)).willReturn(materialsInLocation);

        given(materialsInLocationComponentDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);

        given(searchResult.getTotalNumberOfEntities()).willReturn(1);
        given(searchResult.getEntities()).willReturn(entities);
        given(entities.get(0)).willReturn(materialsInLocationComponentOther);

        given(materialsInLocationComponent.getId()).willReturn(L_ID);
        given(materialsInLocationComponentOther.getId()).willReturn(L_ID_OTHER);

        // when
        boolean result = materialsInLocationComponentModelValidators.checkMaterialFlowComponentUniqueness(
                materialsInLocationComponentDD, materialsInLocationComponent);

        // then
        assertFalse(result);

        Mockito.verify(materialsInLocationComponent).addError(Mockito.eq(materialsInLocationComponentDD.getField(LOCATION)),
                Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueWhenCheckMaterialFlowComponentUniquenessAndMaterialFlowComponentIsUnique() {
        // given
        given(materialsInLocationComponent.getBelongsToField(LOCATION)).willReturn(location);
        given(materialsInLocationComponent.getBelongsToField(MATERIALS_IN_LOCATION)).willReturn(materialsInLocation);

        given(materialsInLocationComponentDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);

        given(searchResult.getTotalNumberOfEntities()).willReturn(0);

        // when
        boolean result = materialsInLocationComponentModelValidators.checkMaterialFlowComponentUniqueness(
                materialsInLocationComponentDD, materialsInLocationComponent);

        // then
        assertTrue(result);
    }

}
