package com.qcadoo.mes.core.data.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.internal.search.SearchResultImpl;
import com.qcadoo.mes.core.data.internal.types.PriorityFieldType;
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

    private static final Logger LOG = LoggerFactory.getLogger(DataAccessServiceImpl.class);

    @Override
    @Transactional
    public ValidationResults save(final DataDefinition dataDefinition, final Entity entity) {
        checkNotNull(dataDefinition, "dataDefinition must be given");
        checkNotNull(entity, "entity must be given");

        Object existingDatabaseEntity = getExistingDatabaseEntity(dataDefinition, entity);

        ValidationResults validationResults = new ValidationResults();

        Object databaseEntity = entityService.convertToDatabaseEntity(dataDefinition, entity, existingDatabaseEntity,
                validationResults);

        if (validationResults.isValid()) {
            if (entity.getId() == null) {
                prioritizeEntity(dataDefinition, databaseEntity);
            }

            sessionFactory.getCurrentSession().save(databaseEntity);

            if (LOG.isDebugEnabled()) {
                LOG.debug("entity " + dataDefinition.getEntityName() + "#" + entity.getId() + " has been saved");
            }

            validationResults.setEntity(entityService.convertToGenericEntity(dataDefinition, databaseEntity));
        }

        return validationResults;
    }

    private Object getExistingDatabaseEntity(final DataDefinition dataDefinition, final Entity entity) {
        Object existingDatabaseEntity = null;

        if (entity.getId() != null) {
            existingDatabaseEntity = getDatabaseEntity(dataDefinition, entity.getId(), false);
            checkState(existingDatabaseEntity != null, "entity %s#%s cannot be found", dataDefinition.getEntityName(),
                    entity.getId());
        }

        return existingDatabaseEntity;
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
        checkState(entityIds.length > 0, "entityIds must be given");

        for (Long entityId : entityIds) {
            deleteEntity(dataDefinition, entityId);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("entities " + dataDefinition.getEntityName() + "#" + Arrays.toString(entityIds) + " marked as deleted");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult find(final SearchCriteria searchCriteria) {
        checkArgument(searchCriteria != null, "searchCriteria must be given");
        DataDefinition dataDefinition = searchCriteria.getDataDefinition();

        int totalNumberOfEntities = getTotalNumberOfEntities(getCriteriaWithRestriction(searchCriteria, dataDefinition));

        if (totalNumberOfEntities == 0 || searchCriteria.getRestrictions().contains(null)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("find 0 elements");
            }
            return getResultSet(searchCriteria, dataDefinition, totalNumberOfEntities, Collections.emptyList());
        }

        Criteria criteria = getCriteriaWithRestriction(searchCriteria, dataDefinition).setFirstResult(
                searchCriteria.getFirstResult()).setMaxResults(searchCriteria.getMaxResults());

        addOrderToCriteria(searchCriteria.getOrder(), criteria);

        if (LOG.isDebugEnabled()) {
            LOG.debug("searching with criteria: firstResults = " + searchCriteria.getFirstResult() + ", maxResults = "
                    + searchCriteria.getMaxResults() + ", order is asc = " + searchCriteria.getOrder().isAsc());
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
        move(dataDefinition, entityId, position, 0);
    }

    @Override
    @Transactional
    public void move(final DataDefinition dataDefinition, final Long entityId, final int offset) {
        move(dataDefinition, entityId, 0, offset);
    }

    private void deleteEntity(final DataDefinition dataDefinition, final Long entityId) {
        Object databaseEntity = getDatabaseEntity(dataDefinition, entityId, true);

        checkNotNull(databaseEntity, "entity %s#%s cannot be found", dataDefinition.getEntityName(), entityId);

        deprioritizeEntity(dataDefinition, databaseEntity);

        entityService.setDeleted(databaseEntity);

        sessionFactory.getCurrentSession().update(databaseEntity);
    }

    private void prioritizeEntity(final DataDefinition dataDefinition, final Object databaseEntity) {
        if (!dataDefinition.isPrioritizable()) {
            return;
        }

        FieldDefinition fieldDefinition = dataDefinition.getPriorityField();

        FieldDefinition scopeFieldDefinition = getScopeForPriority(fieldDefinition);

        int totalNumberOfEntities = getScopedTotalNumberOfEntities(dataDefinition, scopeFieldDefinition,
                entityService.getField(databaseEntity, scopeFieldDefinition));

        entityService.setField(databaseEntity, fieldDefinition, totalNumberOfEntities + 1);
    }

    private FieldDefinition getScopeForPriority(final FieldDefinition fieldDefinition) {
        return ((PriorityFieldType) fieldDefinition.getType()).getScopeFieldDefinition();
    }

    private void deprioritizeEntity(final DataDefinition dataDefinition, final Object databaseEntity) {
        if (!dataDefinition.isPrioritizable()) {
            return;
        }

        FieldDefinition fieldDefinition = dataDefinition.getPriorityField();

        FieldDefinition scopeFieldDefinition = getScopeForPriority(fieldDefinition);

        int currentPriority = (Integer) entityService.getField(databaseEntity, fieldDefinition);

        changePriority(dataDefinition, fieldDefinition, scopeFieldDefinition,
                entityService.getField(databaseEntity, scopeFieldDefinition), currentPriority + 1, Integer.MAX_VALUE, -1);
    }

    @SuppressWarnings("unchecked")
    private void move(final DataDefinition dataDefinition, final Long entityId, final int position, final int offset) {
        checkNotNull(dataDefinition, "dataDefinition must be given");
        checkNotNull(entityId, "entityId must be given");

        if (!dataDefinition.isPrioritizable()) {
            return;
        }

        FieldDefinition fieldDefinition = dataDefinition.getPriorityField();

        Object databaseEntity = getDatabaseEntity(dataDefinition, entityId, false);

        if (databaseEntity == null) {
            return;
        }

        int currentPriority = (Integer) entityService.getField(databaseEntity, fieldDefinition);

        int targetPriority = 0;

        if (offset != 0) {
            targetPriority = currentPriority + offset;
        } else {
            targetPriority = position;
        }

        if (targetPriority < 1) {
            targetPriority = 1;
        }

        if (currentPriority == targetPriority) {
            return;
        }

        FieldDefinition scopeFieldDefinition = getScopeForPriority(fieldDefinition);
        Object scopeValue = entityService.getField(databaseEntity, scopeFieldDefinition);

        if (targetPriority > 1) {
            int totalNumberOfEntities = getScopedTotalNumberOfEntities(dataDefinition, scopeFieldDefinition, scopeValue);

            if (targetPriority > totalNumberOfEntities) {
                targetPriority = totalNumberOfEntities;
            }
        }

        if (currentPriority == targetPriority) {
            return;
        }

        if (currentPriority < targetPriority) {
            changePriority(dataDefinition, fieldDefinition, scopeFieldDefinition, scopeValue, currentPriority + 1,
                    targetPriority, -1);
        } else {
            changePriority(dataDefinition, fieldDefinition, scopeFieldDefinition, scopeValue, targetPriority,
                    currentPriority - 1, 1);
        }

        entityService.setField(databaseEntity, fieldDefinition, targetPriority);

        sessionFactory.getCurrentSession().update(databaseEntity);
    }

    @SuppressWarnings("unchecked")
    private void changePriority(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final FieldDefinition scopeFieldDefinition, final Object scopeValue, final int fromPriority, final int toPriority,
            final int diff) {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = getCriteria(dataDefinition).add(Restrictions.ge(fieldDefinition.getName(), fromPriority)).add(
                Restrictions.le(fieldDefinition.getName(), toPriority));

        addScopeToCriteria(criteria, scopeFieldDefinition, scopeValue);

        List<Object> entitiesToDecrement = criteria.list();

        for (Object entity : entitiesToDecrement) {
            int priority = (Integer) entityService.getField(entity, fieldDefinition);
            entityService.setField(entity, fieldDefinition, priority + diff);
            session.update(entity);
        }
    }

    private int getScopedTotalNumberOfEntities(final DataDefinition dataDefinition, final FieldDefinition scopeFieldDefinition,
            final Object scopeValue) {
        Criteria criteria = getCriteria(dataDefinition).setProjection(Projections.rowCount());

        addScopeToCriteria(criteria, scopeFieldDefinition, scopeValue);

        return Integer.valueOf(criteria.uniqueResult().toString());
    }

    private Criteria addScopeToCriteria(final Criteria criteria, final FieldDefinition scopeFieldDefinition,
            final Object scopeValue) {
        if (scopeValue instanceof Entity) {
            return criteria.add(Restrictions.eq(scopeFieldDefinition.getName() + ".id", ((Entity) scopeValue).getId()));
        } else {
            return criteria.add(Restrictions.eq(scopeFieldDefinition.getName(), scopeValue));
        }
    }

    private Criteria getCriteriaWithRestriction(final SearchCriteria searchCriteria, final DataDefinition dataDefinition) {
        Criteria criteria = getCriteria(dataDefinition);

        for (Restriction restriction : searchCriteria.getRestrictions()) {
            addRestrictionToCriteria(restriction, criteria);
        }
        return criteria;
    }

    private Criteria getCriteria(final DataDefinition dataDefinition) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(dataDefinition.getClassForEntity());

        if (dataDefinition.isDeletable()) {
            criteria.add(Restrictions.ne(EntityService.FIELD_DELETED, true));
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
            return sessionFactory.getCurrentSession().get(dataDefinition.getClassForEntity(), entityId);
        } else {
            Criteria criteria = sessionFactory.getCurrentSession().createCriteria(dataDefinition.getClassForEntity())
                    .add(Restrictions.idEq(entityId));
            if (dataDefinition.isDeletable()) {
                criteria.add(Restrictions.ne(EntityService.FIELD_DELETED, true));
            }
            return criteria.uniqueResult();
        }
    }
}
