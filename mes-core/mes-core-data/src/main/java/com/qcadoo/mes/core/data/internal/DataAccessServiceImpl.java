package com.qcadoo.mes.core.data.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.internal.search.ResultSetImpl;
import com.qcadoo.mes.core.data.search.HibernateRestriction;
import com.qcadoo.mes.core.data.search.Order;
import com.qcadoo.mes.core.data.search.Restriction;
import com.qcadoo.mes.core.data.search.ResultSet;
import com.qcadoo.mes.core.data.search.SearchCriteria;

@Service
public final class DataAccessServiceImpl implements DataAccessService {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private EntityServiceImpl entityService;

    @Override
    @Transactional
    public void save(final String entityName, final Entity entity) {
        checkArgument(entity != null, "entity must be given");
        DataDefinition dataDefinition = entityService.getDataDefinitionForEntity(entityName);
        Class<?> entityClass = entityService.getClassForEntity(dataDefinition);

        Object existingDatabaseEntity = null;

        if (entity.getId() != null) {
            existingDatabaseEntity = getDatabaseEntity(entityClass, entity.getId());
            checkState(existingDatabaseEntity != null, "cannot find entity %s with id=%s", entityClass.getSimpleName(),
                    entity.getId());
        }

        Object databaseEntity = entityService.convertToDatabaseEntity(dataDefinition, entity, existingDatabaseEntity);

        sessionFactory.getCurrentSession().save(databaseEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Entity get(final String entityName, final Long entityId) {
        checkArgument(entityId != null, "entityId must be given");
        DataDefinition dataDefinition = entityService.getDataDefinitionForEntity(entityName);
        Class<?> entityClass = entityService.getClassForEntity(dataDefinition);

        Object databaseEntity = getDatabaseEntity(entityClass, entityId);

        if (databaseEntity == null) {
            return null;
        }

        return entityService.convertToGenericEntity(dataDefinition, databaseEntity);
    }

    @Override
    @Transactional
    public void delete(final String entityName, final Long entityId) {
        checkArgument(entityId != null, "entityId must be given");
        DataDefinition dataDefinition = entityService.getDataDefinitionForEntity(entityName);
        Class<?> entityClass = entityService.getClassForEntity(dataDefinition);

        Object databaseEntity = sessionFactory.getCurrentSession().get(entityClass, entityId);

        if (databaseEntity == null) {
            return;
        }

        entityService.setDeleted(databaseEntity);

        sessionFactory.getCurrentSession().update(databaseEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public ResultSet find(final String entityName, final SearchCriteria searchCriteria) {
        checkArgument(searchCriteria != null, "searchCriteria must be given");
        DataDefinition dataDefinition = entityService.getDataDefinitionForEntity(entityName);
        Class<?> entityClass = entityService.getClassForEntity(dataDefinition);

        int totalNumberOfEntities = getTotalNumberOfEntities(getCriteriaWithRestriction(searchCriteria, entityClass));

        Criteria criteria = getCriteriaWithRestriction(searchCriteria, entityClass).setFirstResult(
                searchCriteria.getFirstResult()).setMaxResults(searchCriteria.getMaxResults());

        criteria = addOrderToCriteria(searchCriteria.getOrder(), criteria);

        List<?> results = criteria.list();

        return getResultSet(searchCriteria, dataDefinition, totalNumberOfEntities, results);
    }

    private Criteria getCriteriaWithRestriction(final SearchCriteria searchCriteria, Class<?> entityClass) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(entityClass)
                .add(Restrictions.ne(EntityServiceImpl.FIELD_DELETED, true));

        for (Restriction restriction : searchCriteria.getRestrictions()) {
            criteria = addRestrictionToCriteria(restriction, criteria);
        }
        return criteria;
    }

    private int getTotalNumberOfEntities(Criteria criteria) {
        return Integer.valueOf(criteria.setProjection(Projections.rowCount()).uniqueResult().toString());
    }

    private ResultSetImpl getResultSet(final SearchCriteria searchCriteria, DataDefinition dataDefinition,
            int totalNumberOfEntities, List<?> results) {
        List<Entity> genericResults = new ArrayList<Entity>();

        for (Object databaseEntity : results) {
            genericResults.add(entityService.convertToGenericEntity(dataDefinition, databaseEntity));
        }

        ResultSetImpl resultSet = new ResultSetImpl();
        resultSet.setResults(genericResults);
        resultSet.setCriteria(searchCriteria);
        resultSet.setTotalNumberOfEntities(totalNumberOfEntities);
        return resultSet;
    }

    private Criteria addRestrictionToCriteria(final Restriction restriction, final Criteria criteria) {
        return ((HibernateRestriction) restriction).addToHibernateCriteria(criteria);
    }

    private Criteria addOrderToCriteria(final Order order, final Criteria criteria) {
        if (order.isAsc()) {
            return criteria.addOrder(org.hibernate.criterion.Order.asc(order.getFieldName()));
        } else {
            return criteria.addOrder(org.hibernate.criterion.Order.desc(order.getFieldName()));
        }
    }

    private Object getDatabaseEntity(final Class<?> entityClass, final Long entityId) {
        return sessionFactory.getCurrentSession().createCriteria(entityClass).add(Restrictions.idEq(entityId))
                .add(Restrictions.ne(EntityServiceImpl.FIELD_DELETED, true)).uniqueResult();
    }

}
