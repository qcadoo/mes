package com.qcadoo.mes.core.data.internal;

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

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.api.DictionaryService;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.internal.types.FieldTypeFactoryImpl;
import com.qcadoo.mes.core.data.internal.validators.FieldValidatorFactoryImpl;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.FieldValidatorFactory;

public abstract class DataAccessTest {

    protected final DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);

    protected final SessionFactory sessionFactory = mock(SessionFactory.class);

    protected final Session session = mock(Session.class);

    protected final Criteria criteria = mock(Criteria.class, RETURNS_DEEP_STUBS);

    protected final DictionaryService dictionaryService = mock(DictionaryService.class);

    protected final ApplicationContext applicationContext = mock(ApplicationContext.class);

    protected final DataAccessService dataAccessServiceMock = mock(DataAccessService.class);

    protected FieldTypeFactory fieldTypeFactory = null;

    protected FieldValidatorFactory fieldValidatorFactory = null;

    protected EntityService entityService = null;

    protected ValidationService validationService = null;

    protected CallbackFactory callbackFactory;

    protected DataAccessService dataAccessService = null;

    protected DataDefinition parentDataDefinition = null;

    protected DataDefinition dataDefinition = null;

    protected FieldDefinition fieldDefinitionPriority = null;

    protected FieldDefinition fieldDefinitionBelongsTo = null;

    protected FieldDefinition fieldDefinitionAge = null;

    protected FieldDefinition fieldDefinitionMoney = null;

    protected FieldDefinition fieldDefinitionRetired = null;

    protected FieldDefinition fieldDefinitionBirthDate = null;

    protected FieldDefinition fieldDefinitionName = null;

    protected FieldDefinition parentFieldDefinitionName = null;

    @Before
    public void superInit() {
        validationService = new ValidationService();
        ReflectionTestUtils.setField(validationService, "sessionFactory", sessionFactory);
        ReflectionTestUtils.setField(validationService, "dataDefinitionService", dataDefinitionService);

        entityService = new EntityService();
        ReflectionTestUtils.setField(entityService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(entityService, "validationService", validationService);

        dataAccessService = new DataAccessServiceImpl();
        ReflectionTestUtils.setField(dataAccessService, "entityService", entityService);
        ReflectionTestUtils.setField(dataAccessService, "sessionFactory", sessionFactory);

        fieldTypeFactory = new FieldTypeFactoryImpl();
        ReflectionTestUtils.setField(fieldTypeFactory, "dictionaryService", dictionaryService);
        ReflectionTestUtils.setField(fieldTypeFactory, "dataAccessService", dataAccessService);
        ReflectionTestUtils.setField(fieldTypeFactory, "dataDefinitionService", dataDefinitionService);

        callbackFactory = new CallbackFactory();
        ReflectionTestUtils.setField(callbackFactory, "applicationContext", applicationContext);

        fieldValidatorFactory = new FieldValidatorFactoryImpl();
        ReflectionTestUtils.setField(fieldValidatorFactory, "callbackFactory", callbackFactory);
        ReflectionTestUtils.setField(fieldValidatorFactory, "dataAccessService", dataAccessServiceMock);

        parentFieldDefinitionName = new FieldDefinition("name");
        parentFieldDefinitionName.setType(fieldTypeFactory.stringType());
        parentFieldDefinitionName.setValidators();

        parentDataDefinition = new DataDefinition("parent.entity");
        parentDataDefinition.addField(parentFieldDefinitionName);
        parentDataDefinition.setFullyQualifiedClassName(ParentDatabaseObject.class.getCanonicalName());

        given(dataDefinitionService.get("parent.entity")).willReturn(parentDataDefinition);

        fieldDefinitionBelongsTo = new FieldDefinition("belongsTo");
        fieldDefinitionBelongsTo.setType(fieldTypeFactory.eagerBelongsToType("parent.entity", "name"));
        fieldDefinitionBelongsTo.setValidators();

        fieldDefinitionName = new FieldDefinition("name");
        fieldDefinitionName.setType(fieldTypeFactory.stringType());
        fieldDefinitionName.setValidators();

        fieldDefinitionAge = new FieldDefinition("age");
        fieldDefinitionAge.setType(fieldTypeFactory.integerType());
        fieldDefinitionAge.setValidators();

        fieldDefinitionPriority = new FieldDefinition("priority");
        fieldDefinitionPriority.setType(fieldTypeFactory.priorityType(fieldDefinitionBelongsTo));
        fieldDefinitionPriority.setValidators();
        fieldDefinitionPriority.setReadOnly(true);

        fieldDefinitionMoney = new FieldDefinition("money");
        fieldDefinitionMoney.setType(fieldTypeFactory.decimalType());
        fieldDefinitionMoney.setValidators();

        fieldDefinitionRetired = new FieldDefinition("retired");
        fieldDefinitionRetired.setType(fieldTypeFactory.booleanType());
        fieldDefinitionRetired.setValidators();

        fieldDefinitionBirthDate = new FieldDefinition("birthDate");
        fieldDefinitionBirthDate.setType(fieldTypeFactory.dateType());
        fieldDefinitionBirthDate.setValidators();

        dataDefinition = new DataDefinition("simple.entity");
        dataDefinition.addField(fieldDefinitionName);
        dataDefinition.addField(fieldDefinitionAge);
        dataDefinition.addField(fieldDefinitionMoney);
        dataDefinition.addField(fieldDefinitionRetired);
        dataDefinition.addField(fieldDefinitionBirthDate);
        dataDefinition.addField(fieldDefinitionBelongsTo);
        dataDefinition.setFullyQualifiedClassName(SimpleDatabaseObject.class.getCanonicalName());

        given(dataDefinitionService.get("simple.entity")).willReturn(dataDefinition);

        given(sessionFactory.getCurrentSession()).willReturn(session);

        given(session.createCriteria(any(Class.class))).willReturn(criteria);

        given(criteria.add(any(Criterion.class))).willReturn(criteria);
        given(criteria.setProjection(any(Projection.class))).willReturn(criteria);
        given(criteria.setFirstResult(anyInt())).willReturn(criteria);
        given(criteria.setMaxResults(anyInt())).willReturn(criteria);
        given(criteria.addOrder(any(Order.class))).willReturn(criteria);
    }

}
