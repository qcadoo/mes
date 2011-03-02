package com.qcadoo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;

public class HasManyToIntegrationTest extends IntegrationTest {

    // http://docs.jboss.org/hibernate/core/3.3/reference/en/html/performance.html#performance-collections

    @Test
    @SuppressWarnings("unchecked")
    public void shouldSaveBelongsToField() throws Exception {
        // given
        DataDefinition productDataDefinition = dataDefinitionService.get(PLUGIN_NAME, ENTITY_NAME_PRODUCT);
        DataDefinition machineDataDefinition = dataDefinitionService.get(PLUGIN_NAME, ENTITY_NAME_MACHINE);
        DataDefinition componentDataDefinition = dataDefinitionService.get(PLUGIN_NAME, ENTITY_NAME_COMPONENT);

        Entity product = productDataDefinition.save(createProduct("asd", "asd"));
        Entity machine = machineDataDefinition.save(createMachine("asd"));
        Entity component1 = componentDataDefinition.save(createComponent("name1", product, machine));
        Entity component2 = componentDataDefinition.save(createComponent("name2", product, machine));

        // when
        product = productDataDefinition.get(product.getId());

        // then
        List<Entity> components = (List<Entity>) product.getField("components");

        assertNotNull(components);

        assertEquals(2, components.size());

        assertTrue(components.contains(component1));
        assertTrue(components.contains(component2));
    }

}
