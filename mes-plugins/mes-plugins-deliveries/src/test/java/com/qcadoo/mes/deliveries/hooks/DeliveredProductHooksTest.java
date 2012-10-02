package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.DELIVERY;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.PRODUCT;
import static org.mockito.Mockito.when;

import org.junit.Assert;
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
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class DeliveredProductHooksTest {

    private DeliveredProductHooks deliveredProductHooks;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity entity, delivery, product;

    @Mock
    private FieldDefinition productField;

    @Mock
    private SearchCriteriaBuilder builder;

    @Before
    public void init() {
        deliveredProductHooks = new DeliveredProductHooks();
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(SearchRestrictions.class);

        when(entity.getBelongsToField("delivery")).thenReturn(delivery);
        when(entity.getBelongsToField("product")).thenReturn(product);
        when(dataDefinition.find()).thenReturn(builder);
        SearchCriterion criterion1 = SearchRestrictions.belongsTo(DELIVERY, delivery);
        SearchCriterion criterion2 = SearchRestrictions.belongsTo(PRODUCT, product);
        when(builder.add(criterion1)).thenReturn(builder);
        when(builder.add(criterion2)).thenReturn(builder);
    }

    @Test
    public void shouldReturnFalseAndAddErrorForEntityWhenOrderedProductAlreadyExists() throws Exception {
        // given
        when(builder.uniqueResult()).thenReturn(delivery);
        when(dataDefinition.getField("product")).thenReturn(productField);
        // when
        boolean result = deliveredProductHooks.checkIfDeliveredProductAlreadyExists(dataDefinition, entity);
        // then
        Assert.assertFalse(result);
        Mockito.verify(entity).addError(productField, "deliveries.delivedProduct.error.alreadyExists");
    }

    @Test
    public void shouldReturnTrue() throws Exception {
        when(builder.uniqueResult()).thenReturn(null);
        when(dataDefinition.getField("product")).thenReturn(productField);
        // when
        boolean result = deliveredProductHooks.checkIfDeliveredProductAlreadyExists(dataDefinition, entity);
        // then
        Assert.assertTrue(result);
    }
}
