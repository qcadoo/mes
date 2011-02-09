package com.qcadoo.mes.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;

import java.util.List;

import org.junit.Test;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.emory.mathcs.backport.java.util.Collections;

public class EntityTreeTest {

    @Test
    public void shouldBeEmptyIfParentIdIsNull() throws Exception {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);
        EntityList list = new EntityList(dataDefinition, "tree", null);

        // then
        assertTrue(list.isEmpty());
    }

    @Test
    public void shouldLoadEntities() throws Exception {
        // given
        Entity entity = mock(Entity.class);
        List<Entity> entities = Collections.singletonList(entity);

        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        given(dataDefinition.getField("tree")).willReturn(fieldDefinition);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(fieldDefinition, 1L)).orderAscBy("priority").list()
                        .getEntities()).willReturn(entities);

        EntityTree tree = new EntityTree(dataDefinition, "tree", 1L);

        // then
        assertEquals(1, tree.size());
        assertEquals(entity, tree.get(0));
        assertEquals(entity, getField(tree.getRoot(), "entity"));
    }

    @Test
    public void shouldBuildTree() throws Exception {
        // given
        Entity entity1 = mock(Entity.class);
        given(entity1.getId()).willReturn(1L);

        Entity entity2 = mock(Entity.class);
        given(entity2.getId()).willReturn(2L);
        given(entity2.getBelongsToField("parent")).willReturn(entity1);

        Entity entity3 = mock(Entity.class);
        given(entity3.getId()).willReturn(3L);
        given(entity3.getBelongsToField("parent")).willReturn(entity1);

        Entity entity4 = mock(Entity.class);
        given(entity4.getId()).willReturn(4L);
        given(entity4.getBelongsToField("parent")).willReturn(entity2);

        List<Entity> entities = Arrays.asList(new Entity[] { entity1, entity2, entity3, entity4 });

        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        given(dataDefinition.getField("tree")).willReturn(fieldDefinition);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(fieldDefinition, 1L)).orderAscBy("priority").list()
                        .getEntities()).willReturn(entities);

        EntityTree tree = new EntityTree(dataDefinition, "tree", 1L);

        // when
        EntityTreeNode root = tree.getRoot();

        // then
        assertEquals(4, tree.size());
        assertEquals(Long.valueOf(1L), root.getId());
        assertEquals(Long.valueOf(2L), root.getChildren().get(0).getId());
        assertEquals(Long.valueOf(3L), root.getChildren().get(1).getId());
        assertEquals(Long.valueOf(4L), root.getChildren().get(0).getChildren().get(0).getId());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfThereAreMultipleRoots() throws Exception {
        // given
        Entity entity1 = mock(Entity.class);
        given(entity1.getId()).willReturn(1L);
        Entity entity2 = mock(Entity.class);
        given(entity2.getId()).willReturn(2L);
        List<Entity> entities = Arrays.asList(new Entity[] { entity1, entity2 });

        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        given(dataDefinition.getField("tree")).willReturn(fieldDefinition);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(fieldDefinition, 1L)).orderAscBy("priority").list()
                        .getEntities()).willReturn(entities);

        EntityTree tree = new EntityTree(dataDefinition, "tree", 1L);

        // when
        tree.size();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfThereIsNoRoot() throws Exception {
        // given
        Entity entity = mock(Entity.class);
        given(entity.getBelongsToField("parent")).willReturn(entity);
        List<Entity> entities = Collections.singletonList(entity);

        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        given(dataDefinition.getField("tree")).willReturn(fieldDefinition);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(fieldDefinition, 1L)).orderAscBy("priority").list()
                        .getEntities()).willReturn(entities);

        EntityTree tree = new EntityTree(dataDefinition, "tree", 1L);

        // when
        tree.size();
    }

    @Test
    public void shouldReturnCriteriaBuilder() throws Exception {
        // given
        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        given(dataDefinition.getField("tree")).willReturn(fieldDefinition);
        SearchCriteriaBuilder searchCriteriaBuilder = mock(SearchCriteriaBuilder.class);
        given(dataDefinition.find().restrictedWith(Restrictions.belongsTo(fieldDefinition, 1L)))
                .willReturn(searchCriteriaBuilder);

        EntityList list = new EntityList(dataDefinition, "tree", 1L);

        // then
        assertEquals(searchCriteriaBuilder, list.find());
    }

    @Test
    public void shouldDelegateMethods() throws Exception {
        // given
        Entity entity = mock(Entity.class);
        given(entity.getStringField("entityType")).willReturn("entityType1");

        EntityTreeNode child = mock(EntityTreeNode.class);
        EntityTreeNode node = new EntityTreeNode(entity);
        node.addChild(child);

        // then
        assertEquals(1, node.getChildren().size());
        assertEquals(child, node.getChildren().get(0));
        assertEquals("entityType1", node.getEntityType());
    }

}
