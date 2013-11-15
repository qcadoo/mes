package com.qcadoo.mes.technologies.tree.builder;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.tree.builder.api.InternalOperationProductComponent;
import com.qcadoo.mes.technologies.tree.builder.api.OperationProductComponent;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class OperationProductComponentImplTest {

    private static final String CORRECT_MODEL_NAME = BasicConstants.MODEL_PRODUCT;

    private static final String INCORRECT_MODEL_NAME = BasicConstants.MODEL_PRODUCT + "Incorrect";

    @Mock
    private DataDefinition dataDef;

    @Mock
    private Entity wrappedEntity, product;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        given(dataDef.create()).willReturn(wrappedEntity);
    }

    @Test
    public final void shouldThrowExceptionWhenTryToAddProductWithIncorrectTypeToOutputProductComponent() {
        performWrongTypeProductTest(buildOpoc());
        verify(wrappedEntity, Mockito.never()).setField(OperationProductInComponentFields.PRODUCT, product);
    }

    @Test
    public final void shouldThrowExceptionWhenTryToAddProductWithIncorrectTypeToInputProductComponent() {
        performWrongTypeProductTest(buildOpic());
        verify(wrappedEntity, Mockito.never()).setField(OperationProductOutComponentFields.PRODUCT, product);
    }

    private void performWrongTypeProductTest(final InternalOperationProductComponent opc) {
        // given
        stubProduct(BasicConstants.PLUGIN_IDENTIFIER, INCORRECT_MODEL_NAME);

        // when & then
        try {
            opc.setProduct(product);
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            // success
        }
    }

    @Test
    public final void shouldNotThrowExceptionWhenAddingProductWithCorrectTypeToOutputProductComponent() {
        performCorrectTypeProductTest(buildOpoc());
        verify(wrappedEntity).setField(OperationProductOutComponentFields.PRODUCT, product);
    }

    @Test
    public final void shouldNotThrowExceptionWhenAddingProductWithCorrectTypeToInputProductComponent() {
        performCorrectTypeProductTest(buildOpic());
        verify(wrappedEntity).setField(OperationProductInComponentFields.PRODUCT, product);
    }

    private void performCorrectTypeProductTest(final InternalOperationProductComponent opc) {
        // given
        stubProduct(BasicConstants.PLUGIN_IDENTIFIER, CORRECT_MODEL_NAME);

        // when
        opc.setProduct(product);

    }

    private OperationProductComponentImpl buildOpic() {
        return new OperationProductComponentImpl(OperationProductComponent.OperationCompType.INPUT, dataDef);
    }

    private OperationProductComponentImpl buildOpoc() {
        return new OperationProductComponentImpl(OperationProductComponent.OperationCompType.OUTPUT, dataDef);
    }

    private void stubProduct(final String pluginId, final String modelName) {
        DataDefinition dataDefinition = mock(DataDefinition.class);
        given(product.getDataDefinition()).willReturn(dataDefinition);
        given(dataDefinition.getPluginIdentifier()).willReturn(pluginId);
        given(dataDefinition.getName()).willReturn(modelName);
    }

}
