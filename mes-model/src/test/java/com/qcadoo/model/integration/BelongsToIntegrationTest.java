package com.qcadoo.model.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.Test;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class BelongsToIntegrationTest extends IntegrationTest {

    @Test
    public void shouldSaveBelongsToField() throws Exception {
        // given
        DataDefinition productDao = dataDefinitionService.get(PLUGIN_NAME, ENTITY_NAME_PRODUCT);
        DataDefinition machineDao = dataDefinitionService.get(PLUGIN_NAME, ENTITY_NAME_MACHINE);
        DataDefinition componentDao = dataDefinitionService.get(PLUGIN_NAME, ENTITY_NAME_COMPONENT);

        Entity machine = machineDao.save(createMachine("asd"));
        Entity product = productDao.save(createProduct("asd", "asd"));
        Entity component = createComponent("name", product, machine);

        // when
        component = componentDao.save(component);

        // then
        assertNotNull(component);

        assertEquals(product.getId(), ((Entity) component.getField("product")).getId());

        Map<String, Object> result = jdbcTemplate.queryForMap("select * from " + TABLE_NAME_COMPONENT);

        assertNotNull(result);
        assertEquals(product.getId(), result.get("product"));
    }

    @Test
    public void shouldSaveBelongsToFieldWithId() throws Exception {
        // given
        DataDefinition productDao = dataDefinitionService.get(PLUGIN_NAME, ENTITY_NAME_PRODUCT);
        DataDefinition machineDao = dataDefinitionService.get(PLUGIN_NAME, ENTITY_NAME_MACHINE);
        DataDefinition componentDao = dataDefinitionService.get(PLUGIN_NAME, ENTITY_NAME_COMPONENT);

        Entity machine = machineDao.save(createMachine("asd"));
        Entity product = productDao.save(createProduct("asd", "asd"));
        Entity component = createComponent("name", product.getId(), machine.getId());

        // when
        component = componentDao.save(component);

        // then
        assertNotNull(component);

        assertEquals(product.getId(), ((Entity) component.getField("product")).getId());

        Map<String, Object> result = jdbcTemplate.queryForMap("select * from " + TABLE_NAME_COMPONENT);

        assertNotNull(result);
        assertEquals(product.getId(), result.get("product"));
    }

    @Test
    public void shouldGetEagerBelongsToField() throws Exception {
        // given
        DataDefinition productDao = dataDefinitionService.get(PLUGIN_NAME, ENTITY_NAME_PRODUCT);
        DataDefinition machineDao = dataDefinitionService.get(PLUGIN_NAME, ENTITY_NAME_MACHINE);
        DataDefinition componentDao = dataDefinitionService.get(PLUGIN_NAME, ENTITY_NAME_COMPONENT);

        Entity machine = machineDao.save(createMachine("asd"));
        Entity product = productDao.save(createProduct("asd", "asd"));
        Entity component = componentDao.save(createComponent("name", product, machine));

        // when
        component = componentDao.get(component.getId());

        // then
        assertNotNull(component);
        assertEquals(product.getId(), ((Entity) component.getField("product")).getId());
    }

    @Test
    public void shouldGetLazyBelongsToField() throws Exception {
        // given
        DataDefinition productDataDefinition = dataDefinitionService.get(PLUGIN_NAME, ENTITY_NAME_PRODUCT);
        DataDefinition machineDataDefinition = dataDefinitionService.get(PLUGIN_NAME, ENTITY_NAME_MACHINE);
        DataDefinition componentDataDefinition = dataDefinitionService.get(PLUGIN_NAME, ENTITY_NAME_COMPONENT);

        Entity machine = machineDataDefinition.save(createMachine("asd"));
        Entity product = productDataDefinition.save(createProduct("asd", "asd"));
        Entity component = componentDataDefinition.save(createComponent("name", product, machine));

        // when
        component = componentDataDefinition.get(component.getId());

        // then
        assertNotNull(component);
        assertEquals(machine.getId(), ((Entity) component.getField("machine")).getId());
    }

}
