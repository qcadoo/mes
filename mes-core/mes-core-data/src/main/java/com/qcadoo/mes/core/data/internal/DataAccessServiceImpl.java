package com.qcadoo.mes.core.data.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.internal.search.SearchResultImpl;
import com.qcadoo.mes.core.data.search.HibernateRestriction;
import com.qcadoo.mes.core.data.search.Order;
import com.qcadoo.mes.core.data.search.Restriction;
import com.qcadoo.mes.core.data.search.SearchCriteria;
import com.qcadoo.mes.core.data.search.SearchResult;
import com.qcadoo.mes.core.data.validation.ValidationResults;

@Service
public final class DataAccessServiceImpl implements DataAccessService {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private EntityService entityService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private PriorityService priorityService;

    private static final Logger LOG = LoggerFactory.getLogger(DataAccessServiceImpl.class);

    @Override
    @Transactional
    public ValidationResults save(final DataDefinition dataDefinition, final Entity genericEntity) {
        checkNotNull(dataDefinition, "dataDefinition must be given");
        checkNotNull(genericEntity, "entity must be given");

        Object existingDatabaseEntity = getExistingDatabaseEntity(dataDefinition, genericEntity);

        Entity existingGenericEntity = null;

        if (existingDatabaseEntity != null) {
            existingGenericEntity = entityService.convertToGenericEntity(dataDefinition, existingDatabaseEntity);
        }

        ValidationResults validationResults = validationService.validateGenericEntity(dataDefinition, genericEntity,
                existingGenericEntity);

        if (validationResults.isValid()) {
            Object databaseEntity = entityService.convertToDatabaseEntity(dataDefinition, genericEntity, existingDatabaseEntity);

            if (genericEntity.getId() == null) {
                priorityService.prioritizeEntity(dataDefinition, databaseEntity);
            }

            getCurrentSession().save(databaseEntity);

            if (LOG.isDebugEnabled()) {
                LOG.debug("entity " + dataDefinition.getName() + "#" + genericEntity.getId() + " has been saved");
            }

            validationResults.setEntity(entityService.convertToGenericEntity(dataDefinition, databaseEntity));
        }

        return validationResults;
    }

    @Override
    @Transactional(readOnly = true)
    public Entity get(final DataDefinition dataDefinition, final Long entityId) {
        checkNotNull(dataDefinition, "dataDefinition must be given");
        checkNotNull(entityId, "entityId must be given");

        Object databaseEntity = getDatabaseEntity(dataDefinition, entityId, false);

        if (databaseEntity == null) {
            return null;
        }

        return entityService.convertToGenericEntity(dataDefinition, databaseEntity);
    }

    @Override
    @Transactional
    public void delete(final DataDefinition dataDefinition, final Long... entityIds) {
        checkNotNull(dataDefinition, "dataDefinition must be given");
        checkState(dataDefinition.isDeletable(), "entity must be deletable");
        checkState(entityIds.length > 0, "entityIds must be given");

        for (Long entityId : entityIds) {
            deleteEntity(dataDefinition, entityId);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("entities " + dataDefinition.getName() + "#" + Arrays.toString(entityIds) + " marked as deleted");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult find(final SearchCriteria searchCriteria) {
        checkArgument(searchCriteria != null, "searchCriteria must be given");

        DataDefinition dataDefinition = searchCriteria.getDataDefinition();

        int totalNumberOfEntities = getTotalNumberOfEntities(getCriteria(searchCriteria));

        if (totalNumberOfEntities == 0 || searchCriteria.getRestrictions().contains(null)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("find 0 elements");
            }
            return getResultSet(searchCriteria, dataDefinition, totalNumberOfEntities, Collections.emptyList());
        }

        Criteria criteria = getCriteria(searchCriteria).setFirstResult(searchCriteria.getFirstResult()).setMaxResults(
                searchCriteria.getMaxResults());

        addOrderToCriteria(searchCriteria.getOrder(), criteria);

        if (LOG.isDebugEnabled()) {
            LOG.debug("searching with criteria " + searchCriteria);
        }

        List<?> results = criteria.list();

        if (LOG.isDebugEnabled()) {
            LOG.debug("find " + results.size() + " elements");
        }

        return getResultSet(searchCriteria, dataDefinition, totalNumberOfEntities, results);
    }

    @Override
    @Transactional
    public void moveTo(final DataDefinition dataDefinition, final Long entityId, final int position) {
        checkNotNull(dataDefinition, "dataDefinition must be given");
        checkState(dataDefinition.isPrioritizable(), "entity must be prioritizable");
        checkNotNull(entityId, "entityId must be given");
        checkState(position > 0, "position must be greaten than 0");

        Object databaseEntity = getDatabaseEntity(dataDefinition, entityId, false);

        if (databaseEntity == null) {
            return;
        }

        priorityService.move(dataDefinition, databaseEntity, position, 0);
    }

    @Override
    @Transactional
    public void move(final DataDefinition dataDefinition, final Long entityId, final int offset) {
        checkNotNull(dataDefinition, "dataDefinition must be given");
        checkState(dataDefinition.isPrioritizable(), "entity must be prioritizable");
        checkNotNull(entityId, "entityId must be given");
        checkState(offset != 0, "offset must be different than 0");

        Object databaseEntity = getDatabaseEntity(dataDefinition, entityId, false);

        if (databaseEntity == null) {
            return;
        }

        priorityService.move(dataDefinition, databaseEntity, 0, offset);
    }

    private Object getExistingDatabaseEntity(final DataDefinition dataDefinition, final Entity entity) {
        Object existingDatabaseEntity = null;

        if (entity.getId() != null) {
            existingDatabaseEntity = getDatabaseEntity(dataDefinition, entity.getId(), false);
            checkState(existingDatabaseEntity != null, "entity %s#%s cannot be found", dataDefinition.getName(), entity.getId());
        }

        return existingDatabaseEntity;
    }

    private void deleteEntity(final DataDefinition dataDefinition, final Long entityId) {
        Object databaseEntity = getDatabaseEntity(dataDefinition, entityId, true);

        checkNotNull(databaseEntity, "entity %s#%s cannot be found", dataDefinition.getName(), entityId);

        priorityService.deprioritizeEntity(dataDefinition, databaseEntity);

        entityService.setDeleted(databaseEntity);

        getCurrentSession().update(databaseEntity);
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    private Criteria getCriteria(final SearchCriteria searchCriteria) {
        Criteria criteria = getCurrentSession().createCriteria(searchCriteria.getDataDefinition().getClassForEntity());

        if (searchCriteria.getDataDefinition().isDeletable()) {
            entityService.addDeletedRestriction(criteria);
        }

        if (searchCriteria != null) {
            for (Restriction restriction : searchCriteria.getRestrictions()) {
                addRestrictionToCriteria(restriction, criteria);
            }
        }

        return criteria;
    }

    private int getTotalNumberOfEntities(final Criteria criteria) {
        return Integer.valueOf(criteria.setProjection(Projections.rowCount()).uniqueResult().toString());
    }

    private SearchResultImpl getResultSet(final SearchCriteria searchCriteria, final DataDefinition dataDefinition,
            final int totalNumberOfEntities, final List<?> results) {
        List<Entity> genericResults = new ArrayList<Entity>();

        for (Object databaseEntity : results) {
            genericResults.add(entityService.convertToGenericEntity(dataDefinition, databaseEntity));
        }

        SearchResultImpl resultSet = new SearchResultImpl();
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

    private Object getDatabaseEntity(final DataDefinition dataDefinition, final Long entityId, final boolean withDeleted) {
        if (withDeleted || !dataDefinition.isDeletable()) {
            return getCurrentSession().get(dataDefinition.getClassForEntity(), entityId);
        } else {
            Criteria criteria = getCurrentSession().createCriteria(dataDefinition.getClassForEntity()).add(
                    Restrictions.idEq(entityId));
            if (dataDefinition.isDeletable()) {
                entityService.addDeletedRestriction(criteria);
            }
            return criteria.uniqueResult();
        }
    }

}
