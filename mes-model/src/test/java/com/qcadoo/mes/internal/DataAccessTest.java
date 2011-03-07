/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.internal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.junit.Before;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.beans.sample.SampleParentDatabaseObject;
import com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject;
import com.qcadoo.mes.beans.sample.SampleTreeDatabaseObject;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.DictionaryService;
import com.qcadoo.model.api.types.HasManyType;
import com.qcadoo.model.api.types.TreeType;
import com.qcadoo.model.internal.DataAccessServiceImpl;
import com.qcadoo.model.internal.DataDefinitionImpl;
import com.qcadoo.model.internal.EntityService;
import com.qcadoo.model.internal.FieldDefinitionImpl;
import com.qcadoo.model.internal.PriorityService;
import com.qcadoo.model.internal.ValidationService;
import com.qcadoo.model.internal.api.DataAccessService;
import com.qcadoo.model.internal.types.BelongsToEntityType;
import com.qcadoo.model.internal.types.BooleanType;
import com.qcadoo.model.internal.types.DateType;
import com.qcadoo.model.internal.types.DecimalType;
import com.qcadoo.model.internal.types.HasManyEntitiesType;
import com.qcadoo.model.internal.types.IntegerType;
import com.qcadoo.model.internal.types.PriorityType;
import com.qcadoo.model.internal.types.StringType;
import com.qcadoo.model.internal.types.TreeEntitiesType;

public abstract class DataAccessTest {

    protected final DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);

    protected final SessionFactory sessionFactory = mock(SessionFactory.class);

    protected final Session session = mock(Session.class);

    protected final Criteria criteria = mock(Criteria.class, RETURNS_DEEP_STUBS);

    protected final DictionaryService dictionaryService = mock(DictionaryService.class);

    protected final ApplicationContext applicationContext = mock(ApplicationContext.class);

    protected final DataAccessService dataAccessServiceMock = mock(DataAccessService.class);

    protected EntityService entityService = null;

    protected ValidationService validationService = null;

    protected PriorityService priorityService = null;

    protected DataAccessService dataAccessService = null;

    protected DataDefinitionImpl parentDataDefinition = null;

    protected DataDefinitionImpl treeDataDefinition = null;

    protected DataDefinitionImpl dataDefinition = null;

    protected FieldDefinitionImpl fieldDefinitionPriority = null;

    protected FieldDefinitionImpl fieldDefinitionBelongsTo = null;

    protected FieldDefinitionImpl fieldDefinitionLazyBelongsTo = null;

    protected FieldDefinitionImpl fieldDefinitionAge = null;

    protected FieldDefinitionImpl fieldDefinitionMoney = null;

    protected FieldDefinitionImpl fieldDefinitionRetired = null;

    protected FieldDefinitionImpl fieldDefinitionBirthDate = null;

    protected FieldDefinitionImpl fieldDefinitionName = null;

    protected FieldDefinitionImpl parentFieldDefinitionName = null;

    protected FieldDefinitionImpl parentFieldDefinitionHasMany = null;

    protected FieldDefinitionImpl parentFieldDefinitionTree = null;

    protected FieldDefinitionImpl treeFieldDefinitionName = null;

    protected FieldDefinitionImpl treeFieldDefinitionChildren = null;

    protected FieldDefinitionImpl treeFieldDefinitionParent = null;

    protected FieldDefinitionImpl treeFieldDefinitionOwner = null;

    @Before
    public void superInit() {
        validationService = new ValidationService();

        entityService = new EntityService();
        ReflectionTestUtils.setField(entityService, "sessionFactory", sessionFactory);

        priorityService = new PriorityService();
        ReflectionTestUtils.setField(priorityService, "entityService", entityService);
        ReflectionTestUtils.setField(priorityService, "sessionFactory", sessionFactory);

        dataAccessService = new DataAccessServiceImpl();
        ReflectionTestUtils.setField(dataAccessService, "entityService", entityService);
        ReflectionTestUtils.setField(dataAccessService, "sessionFactory", sessionFactory);
        ReflectionTestUtils.setField(dataAccessService, "priorityService", priorityService);
        ReflectionTestUtils.setField(dataAccessService, "validationService", validationService);

        treeDataDefinition = new DataDefinitionImpl("tree", "tree.entity", dataAccessService);
        given(dataDefinitionService.get("tree", "entity")).willReturn(treeDataDefinition);

        parentDataDefinition = new DataDefinitionImpl("parent", "parent.entity", dataAccessService);
        given(dataDefinitionService.get("parent", "entity")).willReturn(parentDataDefinition);

        dataDefinition = new DataDefinitionImpl("simple", "simple.entity", dataAccessService);
        given(dataDefinitionService.get("simple", "entity")).willReturn(dataDefinition);

        parentFieldDefinitionName = new FieldDefinitionImpl(null, "name");
        parentFieldDefinitionName.withType(new StringType());

        parentFieldDefinitionHasMany = new FieldDefinitionImpl(null, "entities");
        parentFieldDefinitionHasMany.withType(new HasManyEntitiesType("simple", "entity", "belongsTo",
                HasManyType.Cascade.DELETE, false, dataDefinitionService));

        parentFieldDefinitionTree = new FieldDefinitionImpl(null, "tree");
        parentFieldDefinitionTree.withType(new TreeEntitiesType("tree", "entity", "owner", TreeType.Cascade.DELETE, false,
                dataDefinitionService));

        parentDataDefinition.withField(parentFieldDefinitionName);
        parentDataDefinition.withField(parentFieldDefinitionHasMany);
        parentDataDefinition.withField(parentFieldDefinitionTree);
        parentDataDefinition.setFullyQualifiedClassName(SampleParentDatabaseObject.class.getCanonicalName());

        treeFieldDefinitionName = new FieldDefinitionImpl(null, "name");
        treeFieldDefinitionName.withType(new StringType());

        treeFieldDefinitionChildren = new FieldDefinitionImpl(null, "children");
        treeFieldDefinitionChildren.withType(new HasManyEntitiesType("tree", "entity", "parent", HasManyType.Cascade.DELETE,
                false, dataDefinitionService));

        treeFieldDefinitionParent = new FieldDefinitionImpl(null, "parent");
        treeFieldDefinitionParent.withType(new BelongsToEntityType("tree", "entity", dataDefinitionService, false));

        treeFieldDefinitionOwner = new FieldDefinitionImpl(null, "owner");
        treeFieldDefinitionOwner.withType(new BelongsToEntityType("parent", "entity", dataDefinitionService, false));

        treeDataDefinition.withField(treeFieldDefinitionName);
        treeDataDefinition.withField(treeFieldDefinitionChildren);
        treeDataDefinition.withField(treeFieldDefinitionParent);
        treeDataDefinition.withField(treeFieldDefinitionOwner);
        treeDataDefinition.setFullyQualifiedClassName(SampleTreeDatabaseObject.class.getCanonicalName());

        fieldDefinitionBelongsTo = new FieldDefinitionImpl(null, "belongsTo");
        fieldDefinitionBelongsTo.withType(new BelongsToEntityType("parent", "entity", dataDefinitionService, false));

        fieldDefinitionLazyBelongsTo = new FieldDefinitionImpl(null, "lazyBelongsTo");
        fieldDefinitionLazyBelongsTo.withType(new BelongsToEntityType("parent", "entity", dataDefinitionService, true));

        fieldDefinitionName = new FieldDefinitionImpl(null, "name");
        fieldDefinitionName.withType(new StringType());

        fieldDefinitionAge = new FieldDefinitionImpl(null, "age");
        fieldDefinitionAge.withType(new IntegerType());

        fieldDefinitionPriority = new FieldDefinitionImpl(null, "priority");
        fieldDefinitionPriority.withType(new PriorityType(fieldDefinitionBelongsTo));
        fieldDefinitionPriority.withReadOnly(true);

        fieldDefinitionMoney = new FieldDefinitionImpl(null, "money");
        fieldDefinitionMoney.withType(new DecimalType());

        fieldDefinitionRetired = new FieldDefinitionImpl(null, "retired");
        fieldDefinitionRetired.withType(new BooleanType());

        fieldDefinitionBirthDate = new FieldDefinitionImpl(null, "birthDate");
        fieldDefinitionBirthDate.withType(new DateType());

        dataDefinition.withField(fieldDefinitionName);
        dataDefinition.withField(fieldDefinitionAge);
        dataDefinition.withField(fieldDefinitionMoney);
        dataDefinition.withField(fieldDefinitionRetired);
        dataDefinition.withField(fieldDefinitionBirthDate);
        dataDefinition.withField(fieldDefinitionBelongsTo);
        dataDefinition.withField(fieldDefinitionLazyBelongsTo);
        dataDefinition.setFullyQualifiedClassName(SampleSimpleDatabaseObject.class.getCanonicalName());

        given(sessionFactory.getCurrentSession()).willReturn(session);

        given(session.createCriteria(any(Class.class))).willReturn(criteria);

        given(criteria.add(any(Criterion.class))).willReturn(criteria);
        given(criteria.setProjection(any(Projection.class))).willReturn(criteria);
        given(criteria.setFirstResult(anyInt())).willReturn(criteria);
        given(criteria.setMaxResults(anyInt())).willReturn(criteria);
        given(criteria.addOrder(any(Order.class))).willReturn(criteria);
    }

}
