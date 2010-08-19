package com.qcadoo.mes.core.data.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.internal.search.ResultSetImpl;
import com.qcadoo.mes.core.data.search.ResultSet;
import com.qcadoo.mes.core.data.search.SearchCriteria;

@Service
public final class DataAccessServiceImpl implements DataAccessService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    @Transactional
    public void save(final String entityName, final Entity entity) {
        checkArgument(entity != null, "entity must be given");
        DataDefinition dataDefinition = getDataDefinitionForEntity(entityName);
        Class<?> entityClass = getClassForEntity(dataDefinition);

        Object existingDatabaseEntity = null;

        if (entity.getId() != null) {
            existingDatabaseEntity = getDatabaseEntity(entityClass, entity.getId());
            checkState(existingDatabaseEntity != null, "cannot find entity %s with id=%s", entityClass.getSimpleName(),
                    entity.getId());
        }

        Object databaseEntity = convertToDatabaseEntity(dataDefinition, entity, existingDatabaseEntity);

        sessionFactory.getCurrentSession().save(databaseEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Entity get(final String entityName, final Long entityId) {
        checkArgument(entityId != null, "entityId must be given");
        DataDefinition dataDefinition = getDataDefinitionForEntity(entityName);
        Class<?> entityClass = getClassForEntity(dataDefinition);

        Object databaseEntity = getDatabaseEntity(entityClass, entityId);

        if (databaseEntity == null) {
            return null;
        }

        return convertToGenericEntity(dataDefinition, databaseEntity);
    }

    @Override
    @Transactional
    public void delete(final String entityName, final Long entityId) {
        checkArgument(entityId != null, "entityId must be given");
        DataDefinition dataDefinition = getDataDefinitionForEntity(entityName);
        Class<?> entityClass = getClassForEntity(dataDefinition);

        Object databaseEntity = sessionFactory.getCurrentSession().get(entityClass, entityId);

        if (databaseEntity == null) {
            return;
        }

        FieldUtils.setDeleted(databaseEntity);

        sessionFactory.getCurrentSession().update(databaseEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public ResultSet find(final String entityName, final SearchCriteria searchCriteria) {
        checkArgument(searchCriteria != null, "searchCriteria must be given");
        DataDefinition dataDefinition = getDataDefinitionForEntity(entityName);
        Class<?> entityClass = getClassForEntity(dataDefinition);

        Long totalNumberOfEntities = (Long) sessionFactory.getCurrentSession().createCriteria(entityClass)
                .setProjection(Projections.rowCount()).uniqueResult();

        List<?> results = sessionFactory.getCurrentSession().createCriteria(entityClass)
                .setFirstResult(searchCriteria.getFirstResult()).setMaxResults(searchCriteria.getMaxResults())
                .add(Restrictions.ne(FieldUtils.FIELD_DELETED, true)).list();

        List<Entity> genericResults = new ArrayList<Entity>();

        for (Object databaseEntity : results) {
            genericResults.add(convertToGenericEntity(dataDefinition, databaseEntity));
        }

        ResultSetImpl resultSet = new ResultSetImpl();
        resultSet.setResults(genericResults);
        resultSet.setCriteria(searchCriteria);
        resultSet.setTotalNumberOfEntities(totalNumberOfEntities.intValue());

        return resultSet;
    }

    private Object getDatabaseEntity(final Class<?> entityClass, final Long entityId) {
        return sessionFactory.getCurrentSession().createCriteria(entityClass).add(Restrictions.idEq(entityId))
                .add(Restrictions.ne(FieldUtils.FIELD_DELETED, true)).uniqueResult();
    }

    private Entity convertToGenericEntity(final DataDefinition dataDefinition, final Object entity) {
        Entity genericEntity = new Entity(FieldUtils.getId(entity));

        for (FieldDefinition fieldDefinition : dataDefinition.getFields()) {
            genericEntity.setField(fieldDefinition.getName(), FieldUtils.getField(entity, fieldDefinition));
        }

        return genericEntity;
    }

    private Object convertToDatabaseEntity(final DataDefinition dataDefinition, final Entity genericEntity,
            final Object existingDatabaseEntity) {
        Object databaseEntity = null;

        if (existingDatabaseEntity != null) {
            databaseEntity = existingDatabaseEntity;
        } else {
            databaseEntity = getInstanceForEntity(dataDefinition);
            FieldUtils.setId(databaseEntity, genericEntity.getId());
        }

        for (FieldDefinition fieldDefinition : dataDefinition.getFields()) {
            FieldUtils.setField(databaseEntity, fieldDefinition, genericEntity.getField(fieldDefinition.getName()));
        }

        return databaseEntity;
    }

    private Class<?> getClassForEntity(final DataDefinition dataDefinition) {
        if (dataDefinition.isVirtualTable()) {
            throw new UnsupportedOperationException("virtual tables are not supported");
        } else {
            String fullyQualifiedClassName = dataDefinition.getFullyQualifiedClassName();

            try {
                return getClass().getClassLoader().loadClass(fullyQualifiedClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("cannot find mapping class for definition: "
                        + dataDefinition.getFullyQualifiedClassName(), e);
            }
        }
    }

    private Object getInstanceForEntity(DataDefinition dataDefinition) {
        Class<?> entityClass = getClassForEntity(dataDefinition);
        try {
            return entityClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("cannot instantiate class: " + dataDefinition.getFullyQualifiedClassName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("cannot instantiate class: " + dataDefinition.getFullyQualifiedClassName(), e);
        }
    }

    private DataDefinition getDataDefinitionForEntity(final String entityName) {
        DataDefinition dataDefinition = dataDefinitionService.get(entityName);
        checkNotNull(dataDefinition, "data definition has't been found");
        return dataDefinition;
    }

}
