package com.qcadoo.mes.core.data.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.internal.search.ResultSetImpl;
import com.qcadoo.mes.core.data.search.ResultSet;
import com.qcadoo.mes.core.data.search.SearchCriteria;

public final class DataAccessServiceImpl implements DataAccessService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void save(final String entityName, final Entity entity) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public Entity get(final String entityName, final Long entityId) {
        DataDefinition dataDefinition = getDataDefinitionForEntity(entityName);
        Class<?> entityClass = getClassForEntity(dataDefinition);

        Object databaseEntity = sessionFactory.getCurrentSession().get(entityClass, entityId);

        if (databaseEntity == null) {
            return null;
        }

        return getGenericEntity(dataDefinition, databaseEntity);
    }

    private Entity getGenericEntity(DataDefinition dataDefinition, Object entity) {
        Entity genericEntity = new Entity(getIdProperty(entity));

        for (FieldDefinition fieldDefinition : dataDefinition.getFields()) {
            genericEntity.setField(fieldDefinition.getName(), getProperty(entity, fieldDefinition));
        }
        return genericEntity;
    }

    @Override
    public void delete(final String entityName, final Long entityId) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public ResultSet find(final String entityName, final SearchCriteria searchCriteria) {
        DataDefinition dataDefinition = getDataDefinitionForEntity(entityName);
        Class<?> entityClass = getClassForEntity(dataDefinition);

        List<?> results = sessionFactory.getCurrentSession().createCriteria(entityClass)
                .setFirstResult(searchCriteria.getFirstResult()).setMaxResults(searchCriteria.getMaxResults()).list();

        List<Entity> genericResults = new ArrayList<Entity>();

        for (Object object : results) {

        }

        ResultSetImpl resultSet = new ResultSetImpl();
        resultSet.setResults(genericResults);
        resultSet.setCriteria(searchCriteria);

        return resultSet;
    }

    private Long getIdProperty(final Object entity) {
        try {
            return (Long) PropertyUtils.getProperty(entity, "id");
        } catch (Exception e) {
            throw new IllegalStateException("cannot get value of the id: " + entity.getClass().getSimpleName(), e);
        }
    }

    private Object getProperty(final Object entity, final FieldDefinition fieldDefinition) {
        if (fieldDefinition.isCustomField()) {
            throw new UnsupportedOperationException("custom fields are not supported");
        } else {
            try {
                Object value = PropertyUtils.getProperty(entity, fieldDefinition.getName());
                if (!fieldDefinition.getType().isValidType(value)) {
                    throw new IllegalStateException("value of the property: " + entity.getClass().getSimpleName()
                            + " has value with invalid type: " + value.getClass().getSimpleName());
                }
                return value;
            } catch (Exception e) {
                throw new IllegalStateException("cannot get value of the property: " + entity.getClass().getSimpleName() + ", "
                        + fieldDefinition.getName(), e);
            }
        }
    }

    private Class<?> getClassForEntity(final DataDefinition dataDefinition) {
        if (dataDefinition.isVirtualTable()) {
            throw new UnsupportedOperationException("virtual tables are not supported");
        } else {
            String fullyQualifiedClassName = dataDefinition.getFullyQualifiedClassName();

            try {
                return ClassLoader.getSystemClassLoader().loadClass(fullyQualifiedClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("cannot find mapping class for definition: "
                        + dataDefinition.getFullyQualifiedClassName(), e);
            }
        }
    }

    private DataDefinition getDataDefinitionForEntity(final String entityName) {
        DataDefinition dataDefinition = dataDefinitionService.get(entityName);
        checkNotNull(dataDefinition, "data definition has't been found");
        return dataDefinition;
    }

}
