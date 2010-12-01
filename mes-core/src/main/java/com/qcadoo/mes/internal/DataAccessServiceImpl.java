/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.aop.internal.Monitorable;
import com.qcadoo.mes.model.internal.InternalDataDefinition;
import com.qcadoo.mes.model.search.Order;
import com.qcadoo.mes.model.search.Restriction;
import com.qcadoo.mes.model.search.SearchCriteria;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.model.search.internal.SearchResultImpl;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.model.validators.ErrorMessage;

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
    @Monitorable
    public Entity save(final InternalDataDefinition dataDefinition, final Entity genericEntity) {
        checkNotNull(dataDefinition, "DataDefinition must be given");
        checkNotNull(genericEntity, "Entity must be given");

        Entity genericEntityToSave = genericEntity.copy();

        Object existingDatabaseEntity = getExistingDatabaseEntity(dataDefinition, genericEntity);

        Entity existingGenericEntity = null;

        if (existingDatabaseEntity != null) {
            existingGenericEntity = entityService.convertToGenericEntity(dataDefinition, existingDatabaseEntity);
        }

        validationService.validateGenericEntity(dataDefinition, genericEntity, existingGenericEntity);

        if (!genericEntity.isValid()) {
            copyValidationErrors(dataDefinition, genericEntityToSave, genericEntity);
            if (existingGenericEntity != null) {
                copyMissingFields(genericEntityToSave, existingGenericEntity);
            }

            LOG.info(genericEntityToSave + " hasn't been saved, bacause of validation errors");

            return genericEntityToSave;
        }

        Object databaseEntity = entityService.convertToDatabaseEntity(dataDefinition, genericEntity, existingDatabaseEntity);

        if (genericEntity.getId() == null) {
            priorityService.prioritizeEntity(dataDefinition, databaseEntity);
        }

        getCurrentSession().save(databaseEntity);

        Entity savedEntity = entityService.convertToGenericEntity(dataDefinition, databaseEntity);

        LOG.info(savedEntity + " has been saved");

        return savedEntity;
    }

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    public Entity get(final InternalDataDefinition dataDefinition, final Long entityId) {
        checkNotNull(dataDefinition, "DataDefinition must be given");
        checkNotNull(entityId, "EntityId must be given");

        Object databaseEntity = getDatabaseEntity(dataDefinition, entityId, false);

        if (databaseEntity == null) {
            LOG.info("Entity[" + dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName() + "][id=" + entityId
                    + "] hasn't been retrieved, because it doesn't exist");
            return null;
        }

        Entity entity = entityService.convertToGenericEntity(dataDefinition, databaseEntity);

        LOG.info(entity + " has been retrieved");

        return entity;
    }

    @Override
    @Transactional
    @Monitorable
    public void delete(final InternalDataDefinition dataDefinition, final Long... entityIds) {
        checkNotNull(dataDefinition, "DataDefinition must be given");
        checkState(dataDefinition.isDeletable(), "Entity must be deletable");
        checkState(entityIds.length > 0, "EntityIds must be given");

        for (Long entityId : entityIds) {
            deleteEntity(dataDefinition, entityId, false);
        }
    }

    @Override
    @Transactional
    @Monitorable
    public void deleteHard(final InternalDataDefinition dataDefinition, final Long... entityIds) {
        checkNotNull(dataDefinition, "DataDefinition must be given");
        checkState(dataDefinition.isDeletable(), "Entity must be deletable");
        checkState(entityIds.length > 0, "EntityIds must be given");

        for (Long entityId : entityIds) {
            deleteEntity(dataDefinition, entityId, true);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    public SearchResult find(final SearchCriteria searchCriteria) {
        checkArgument(searchCriteria != null, "SearchCriteria must be given");

        InternalDataDefinition dataDefinition = (InternalDataDefinition) searchCriteria.getDataDefinition();

        int totalNumberOfEntities = getTotalNumberOfEntities(getCriteria(searchCriteria));

        if (totalNumberOfEntities == 0 || searchCriteria.getRestrictions().contains(null)) {
            LOG.info("There is no entity matching criteria " + searchCriteria);
            return getResultSet(searchCriteria, dataDefinition, totalNumberOfEntities, Collections.emptyList());
        }

        Criteria criteria = getCriteria(searchCriteria).setFirstResult(searchCriteria.getFirstResult()).setMaxResults(
                searchCriteria.getMaxResults());

        addOrderToCriteria(searchCriteria.getOrder(), criteria);

        List<?> results = criteria.list();

        LOG.info("There are " + totalNumberOfEntities + " entities matching criteria " + searchCriteria);

        return getResultSet(searchCriteria, dataDefinition, totalNumberOfEntities, results);
    }

    @Override
    @Transactional
    @Monitorable
    public void moveTo(final InternalDataDefinition dataDefinition, final Long entityId, final int position) {
        checkNotNull(dataDefinition, "DataDefinition must be given");
        checkState(dataDefinition.isPrioritizable(), "Entity must be prioritizable");
        checkNotNull(entityId, "EntityId must be given");
        checkState(position > 0, "Position must be greaten than 0");

        Object databaseEntity = getDatabaseEntity(dataDefinition, entityId, false);

        if (databaseEntity == null) {
            LOG.info("Entity[" + dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName() + "][id=" + entityId
                    + "] hasn't been prioritized, because it doesn't exist");
            return;
        }

        priorityService.move(dataDefinition, databaseEntity, position, 0);

        LOG.info("Entity[" + dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName() + "][id=" + entityId
                + "] has been prioritized");
    }

    @Override
    @Transactional
    @Monitorable
    public void move(final InternalDataDefinition dataDefinition, final Long entityId, final int offset) {
        checkNotNull(dataDefinition, "DataDefinition must be given");
        checkState(dataDefinition.isPrioritizable(), "Entity must be prioritizable");
        checkNotNull(entityId, "EntityId must be given");
        checkState(offset != 0, "Offset must be different than 0");

        Object databaseEntity = getDatabaseEntity(dataDefinition, entityId, false);

        if (databaseEntity == null) {
            LOG.info("Entity[" + dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName() + "][id=" + entityId
                    + "] hasn't been prioritized, because it doesn't exist");
            return;
        }

        priorityService.move(dataDefinition, databaseEntity, 0, offset);

        LOG.info("Entity[" + dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName() + "][id=" + entityId
                + "] has been prioritized");
    }

    private Object getExistingDatabaseEntity(final InternalDataDefinition dataDefinition, final Entity entity) {
        Object existingDatabaseEntity = null;

        if (entity.getId() != null) {
            existingDatabaseEntity = getDatabaseEntity(dataDefinition, entity.getId(), false);
            checkState(existingDatabaseEntity != null, "Entity[%s][id=%s] cannot be found", dataDefinition.getPluginIdentifier()
                    + "." + dataDefinition.getName(), entity.getId());
        }

        return existingDatabaseEntity;
    }

    private void deleteEntity(final InternalDataDefinition dataDefinition, final Long entityId, final boolean hardDelete) {
        Object databaseEntity = getDatabaseEntity(dataDefinition, entityId, true);

        checkNotNull(databaseEntity, "Entity[%s][id=%s] cannot be found", dataDefinition.getPluginIdentifier() + "."
                + dataDefinition.getName(), entityId);

        priorityService.deprioritizeEntity(dataDefinition, databaseEntity);

        Map<String, FieldDefinition> fields = dataDefinition.getFields();

        for (FieldDefinition fieldDefinition : fields.values()) {
            if (fieldDefinition.getType() instanceof HasManyType) {
                HasManyType hasManyFieldType = (HasManyType) fieldDefinition.getType();
                EntityList children = (EntityList) entityService.getField(databaseEntity, fieldDefinition);
                InternalDataDefinition childDataDefinition = (InternalDataDefinition) hasManyFieldType.getDataDefinition();
                if (HasManyType.Cascade.NULLIFY.equals(hasManyFieldType.getCascade())) {
                    for (Object child : children) {
                        DefaultEntity defaultEntity = (DefaultEntity) child;
                        defaultEntity.setField(hasManyFieldType.getJoinFieldName(), null);
                        Entity genericEntity = save(childDataDefinition, defaultEntity);
                        if (!genericEntity.isValid()) {
                            throw new IllegalStateException("Trying delete entity in use");
                        }
                    }
                } else {
                    for (Object child : children.find().includeDeleted().list().getEntities()) {
                        deleteEntity(childDataDefinition, entityService.getId(child), hardDelete);
                    }
                }
            }
        }

        if (hardDelete) {
            getCurrentSession().delete(databaseEntity);
        } else {
            entityService.setDeleted(databaseEntity);
            getCurrentSession().update(databaseEntity);
        }

        LOG.info("Entity[" + dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName() + "][id=" + entityId
                + "] has been deleted");
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    private Criteria getCriteria(final SearchCriteria searchCriteria) {
        InternalDataDefinition dataDefinition = (InternalDataDefinition) searchCriteria.getDataDefinition();
        Criteria criteria = getCurrentSession().createCriteria(dataDefinition.getClassForEntity());

        if (dataDefinition.isDeletable() && !searchCriteria.isIncludeDeleted()) {
            entityService.addDeletedRestriction(criteria);
        }

        for (Restriction restriction : searchCriteria.getRestrictions()) {
            addRestrictionToCriteria(restriction, criteria);
        }

        return criteria;
    }

    private int getTotalNumberOfEntities(final Criteria criteria) {
        return Integer.valueOf(criteria.setProjection(Projections.rowCount()).uniqueResult().toString());
    }

    private SearchResultImpl getResultSet(final SearchCriteria searchCriteria, final InternalDataDefinition dataDefinition,
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
        return restriction.addToHibernateCriteria(criteria);
    }

    private Criteria addOrderToCriteria(final Order order, final Criteria criteria) {
        String[] path = order.getFieldName().split("\\.");

        if (path.length > 2) {
            throw new IllegalStateException("Cannot order using multiple assosiations - " + order.getFieldName());
        } else if (path.length == 2 && !criteria.toString().matches(".*Subcriteria\\(" + path[0] + ":" + path[0] + "\\).*")) {
            criteria.createAlias(path[0], path[0]);
        }

        if (order.isAsc()) {
            return criteria.addOrder(org.hibernate.criterion.Order.asc(order.getFieldName()).ignoreCase());
        } else {
            return criteria.addOrder(org.hibernate.criterion.Order.desc(order.getFieldName()).ignoreCase());
        }
    }

    private Object getDatabaseEntity(final InternalDataDefinition dataDefinition, final Long entityId, final boolean withDeleted) {
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

    private void copyMissingFields(final Entity genericEntityToSave, final Entity existingGenericEntity) {
        for (Map.Entry<String, Object> field : existingGenericEntity.getFields().entrySet()) {
            if (!genericEntityToSave.getFields().containsKey(field.getKey())) {
                genericEntityToSave.setField(field.getKey(), field.getValue());
            }
        }
    }

    private void copyValidationErrors(final DataDefinition dataDefinition, final Entity genericEntityToSave,
            final Entity genericEntity) {
        for (ErrorMessage error : genericEntity.getGlobalErrors()) {
            genericEntityToSave.addGlobalError(error.getMessage(), error.getVars());
        }
        for (Map.Entry<String, ErrorMessage> error : genericEntity.getErrors().entrySet()) {
            genericEntityToSave.addError(dataDefinition.getField(error.getKey()), error.getValue().getMessage(), error.getValue()
                    .getVars());
        }
    }

}
