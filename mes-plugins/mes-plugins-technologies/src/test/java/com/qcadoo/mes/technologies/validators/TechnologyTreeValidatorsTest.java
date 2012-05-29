package com.qcadoo.mes.technologies.validators;

import static org.mockito.BDDMockito.given;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.technologies.tree.TechnologyTreeValidationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

public class TechnologyTreeValidatorsTest {

    private TechnologyTreeValidators technologyTreeValidators;

    @Mock
    private TechnologyTreeValidationService technologyTreeValidationService;

    @Mock
    private DataDefinition dd;

    @Mock
    private Entity tech, product;

    @Mock
    private EntityTree tree;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        technologyTreeValidators = new TechnologyTreeValidators();

        ReflectionTestUtils
                .setField(technologyTreeValidators, "technologyTreeValidationService", technologyTreeValidationService);
    }

    @Test
    public void shouldAddMessagesCorrectly() {
        // given
        String messageKey = "technologies.technology.validate.global.error.subOperationsProduceTheSameProductThatIsConsumed";
        String parentNode = "1.";
        String productName = "name";
        String productNumber = "abc123";

        Long techId = 1L;
        given(tech.getStringField("state")).willReturn("02accepted");
        given(tech.getId()).willReturn(techId);
        given(tech.getDataDefinition()).willReturn(dd);
        given(dd.get(techId)).willReturn(tech);
        given(tech.getTreeField("operationComponents")).willReturn(tree);

        given(product.getStringField("name")).willReturn(productName);
        given(product.getStringField("number")).willReturn(productNumber);

        Map<String, Set<Entity>> nodesMap = Maps.newHashMap();
        Set<Entity> productSet = Sets.newHashSet();
        productSet.add(product);
        nodesMap.put("1.", productSet);

        given(technologyTreeValidationService.checkConsumingTheSameProductFromManySubOperations(tree)).willReturn(nodesMap);

        // when
        technologyTreeValidators.checkConsumingTheSameProductFromManySubOperations(dd, tech);

        // then
        Mockito.verify(tech).addGlobalError(messageKey, parentNode, productName, productNumber);
    }
}
