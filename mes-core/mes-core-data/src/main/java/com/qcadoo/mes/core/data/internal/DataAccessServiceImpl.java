package com.qcadoo.mes.core.data.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.internal.search.ResultSetImpl;
import com.qcadoo.mes.core.data.search.HibernateRestriction;
import com.qcadoo.mes.core.data.search.Order;
import com.qcadoo.mes.core.data.search.Restriction;
import com.qcadoo.mes.core.data.search.ResultSet;
import com.qcadoo.mes.core.data.search.SearchCriteria;
import com.qcadoo.mes.core.data.validation.ValidationResults;

@Service
public final class DataAccessServiceImpl implements DataAccessService {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private EntityService entityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private static final Logger LOG = LoggerFactory.getLogger(DataAccessServiceImpl.class);

    @Override
    @Transactional
    public ValidationResults save(final String entityName, final Entity entity) {
        checkNotNull(entity, "entity must be given");
        DataDefinition dataDefinition = dataDefinitionService.get(entityName);
        Class<?> entityClass = dataDefinition.getClassForEntity();

        Object existingDatabaseEntity = null;

        ValidationResults validationResults = new ValidationResults();

        if (entity.getId() != null) {
            existingDatabaseEntity = getDatabaseEntity(entityClass, entity.getId());
            checkState(existingDatabaseEntity != null, "cannot find entity %s with id=%s", entityClass.getSimpleName(),
                    entity.getId());
        }

        Object databaseEntity = entityService.convertToDatabaseEntity(dataDefinition, entity, existingDatabaseEntity,
                validationResults);

        if (validationResults.isNotValid()) {
            return validationResults;
        }

        sessionFactory.getCurrentSession().save(databaseEntity);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Object with id: " + entity.getId() + " has been saved");
        }

        Entity savedEntity = entityService.convertToGenericEntity(dataDefinition, databaseEntity);

        validationResults.setEntity(savedEntity);

        return validationResults;
    }

    @Override
    @Transactional(readOnly = true)
    public Entity get(final String entityName, final Long entityId) {
        checkArgument(entityId != null, "entityId must be given");
        DataDefinition dataDefinition = dataDefinitionService.get(entityName);
        Class<?> entityClass = dataDefinition.getClassForEntity();

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
        DataDefinition dataDefinition = dataDefinitionService.get(entityName);
        Class<?> entityClass = dataDefinition.getClassForEntity();

        Object databaseEntity = sessionFactory.getCurrentSession().get(entityClass, entityId);

        if (databaseEntity == null) {
            return;
        }

        entityService.setDeleted(databaseEntity);

        sessionFactory.getCurrentSession().update(databaseEntity);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Object with id: " + entityId + " marked as deleted");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResultSet find(final String entityName, final SearchCriteria searchCriteria) {
        checkArgument(searchCriteria != null, "searchCriteria must be given");
        DataDefinition dataDefinition = dataDefinitionService.get(entityName);
        Class<?> entityClass = dataDefinition.getClassForEntity();

        int totalNumberOfEntities = getTotalNumberOfEntities(getCriteriaWithRestriction(searchCriteria, entityClass));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Get total number of entities: " + totalNumberOfEntities);
        }

        if (totalNumberOfEntities == 0) {
            return getResultSet(searchCriteria, dataDefinition, totalNumberOfEntities, Collections.emptyList());
        }

        Criteria criteria = getCriteriaWithRestriction(searchCriteria, entityClass).setFirstResult(
                searchCriteria.getFirstResult()).setMaxResults(searchCriteria.getMaxResults());

        criteria = addOrderToCriteria(searchCriteria.getOrder(), criteria);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching elements with criteria: firstResults = " + searchCriteria.getFirstResult() + ", maxResults = "
                    + searchCriteria.getMaxResults() + ", order is asc = " + searchCriteria.getOrder().isAsc());
        }

        List<?> results = criteria.list();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Find " + results.size() + " elements");
        }

        return getResultSet(searchCriteria, dataDefinition, totalNumberOfEntities, results);
    }

    private Criteria getCriteriaWithRestriction(final SearchCriteria searchCriteria, final Class<?> entityClass) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(entityClass)
                .add(Restrictions.ne(EntityService.FIELD_DELETED, true));

        for (Restriction restriction : searchCriteria.getRestrictions()) {
            criteria = addRestrictionToCriteria(restriction, criteria);
        }
        return criteria;
    }

    private int getTotalNumberOfEntities(final Criteria criteria) {
        return Integer.valueOf(criteria.setProjection(Projections.rowCount()).uniqueResult().toString());
    }

    private ResultSetImpl getResultSet(final SearchCriteria searchCriteria, final DataDefinition dataDefinition,
            final int totalNumberOfEntities, final List<?> results) {
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
                .add(Restrictions.ne(EntityService.FIELD_DELETED, true)).uniqueResult();
    }

}
