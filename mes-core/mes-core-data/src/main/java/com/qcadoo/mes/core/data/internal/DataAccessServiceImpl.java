package com.qcadoo.mes.core.data.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.search.ResultSet;
import com.qcadoo.mes.core.data.search.SearchCriteria;

public final class DataAccessServiceImpl implements DataAccessService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private HibernateTemplate hibernateTemplate;

    @Override
    public void save(final String entityName, final Entity entity) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public Entity get(final String entityName, final Long entityId) {
        DataDefinition dataDefinition = getDataDefinitionForEntity(entityName);
        Class<?> entityClass = getClassForEntity(dataDefinition);

        Object entity = hibernateTemplate.get(entityClass, entityId);

        if (entity == null) {
            return null;
        }

        Entity genericEntity = new Entity((Long) getProperty(entity, "id"));

        for (FieldDefinition fieldDefinition : dataDefinition.getFields()) {
            genericEntity.setField(fieldDefinition.getName(), getProperty(entity, fieldDefinition.getName()));
        }

        return genericEntity;
    }

    @Override
    public void delete(final String entityName, final Long entityId) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public ResultSet find(final String entityName, final SearchCriteria searchCriteria) {
        throw new UnsupportedOperationException("implement me");
    }

    private Object getProperty(final Object entity, final String property) {
        try {
            return PropertyUtils.getProperty(entity, property);
        } catch (Exception e) {
            throw new IllegalStateException("cannot get value of the property: " + entity.getClass().getSimpleName() + ", "
                    + property, e);
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
