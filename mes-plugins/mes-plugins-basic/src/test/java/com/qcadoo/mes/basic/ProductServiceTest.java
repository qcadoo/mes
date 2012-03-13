package com.qcadoo.mes.basic;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.utils.NumberGeneratorService;

public class ProductServiceTest {

    private ProductService productService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private NumberGeneratorService numberGeneratorService;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity product;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(productService, "numberGeneratorService", numberGeneratorService);
        ReflectionTestUtils.setField(productService, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public void shouldGenerateProductNumber() throws Exception {
        // when
        numberGeneratorService.generateAndInsertNumber(view, "basic", "product", "form", "number");
    }
}
