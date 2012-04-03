package com.qcadoo.mes.costNormsForMaterials;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.NumberService;

public class ProductsCostCalculationServiceTest {

    private ProductsCostCalculationService productsCostCalculationService;

    @Mock
    private ProductQuantitiesService productQuantitiesService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private NumberService numberService;

    @Before
    public void init() {
        productsCostCalculationService = new ProductsCostCalculationServiceImpl();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(productsCostCalculationService, "productQuantitiesService", productQuantitiesService);
        ReflectionTestUtils.setField(productsCostCalculationService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(productsCostCalculationService, "numberService", numberService);

    }

}
