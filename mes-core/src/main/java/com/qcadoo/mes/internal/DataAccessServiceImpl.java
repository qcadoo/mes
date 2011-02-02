/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.beans.genealogies.GenealogiesGenealogy;
import com.qcadoo.mes.beans.genealogies.GenealogiesProductInBatch;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.aop.internal.Monitorable;
import com.qcadoo.mes.model.internal.InternalDataDefinition;
import com.qcadoo.mes.model.search.Order;
import com.qcadoo.mes.model.search.Restriction;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteria;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.model.search.internal.SearchResultImpl;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.model.types.TreeType;
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
    public Entity save(final InternalDataDefinition dataDefinition, final Entity genericEntity) {
        Set<Entity> newlySavedEntities = new HashSet<Entity>();
        Entity resultEntity = performSave(dataDefinition, genericEntity, new HashSet<Entity>(), newlySavedEntities);
        try {
            if (TransactionAspectSupport.currentTransactionStatus().isRollbackOnly()) {
                resultEntity.setNotValid();
                for (Entity e : newlySavedEntities) {
                    e.setId(null);
                }
            }
        } catch (NoTransactionException e) {
            // nothing - test purpose only
        }
        return resultEntity;
    }

    @SuppressWarnings("unchecked")
    @Monitorable
    private Entity performSave(final InternalDataDefinition dataDefinition, final Entity genericEntity,
            final Set<Entity> alreadySavedEntities, final Set<Entity> newlySavedEntities) {
        checkNotNull(dataDefinition, "DataDefinition must be given");
        checkNotNull(genericEntity, "Entity must be given");

        if (alreadySavedEntities.contains(genericEntity)) {
            return genericEntity;
        }

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

            if (LOG.isDebugEnabled()) {
                for (ErrorMessage error : genericEntityToSave.getGlobalErrors()) {
                    LOG.debug(" --- " + error.getMessage());
                }
                for (Map.Entry<String, ErrorMessage> error : genericEntityToSave.getErrors().entrySet()) {
                    LOG.debug(" --- " + error.getKey() + ": " + error.getValue().getMessage());
                }
            }

            return genericEntityToSave;
        }

        Object databaseEntity = entityService.convertToDatabaseEntity(dataDefinition, genericEntity, existingDatabaseEntity);

        if (genericEntity.getId() == null) {
            priorityService.prioritizeEntity(dataDefinition, databaseEntity);
        }

        getCurrentSession().save(databaseEntity);

        Entity savedEntity = entityService.convertToGenericEntity(dataDefinition, databaseEntity);

        LOG.info(savedEntity + " has been saved");

        for (Entry<String, FieldDefinition> fieldEntry : dataDefinition.getFields().entrySet()) {
            if (fieldEntry.getValue().getType() instanceof HasManyType) {
                List<Entity> entities = (List<Entity>) genericEntityToSave.getField(fieldEntry.getKey());

                if (entities == null || entities instanceof EntityList) {
                    savedEntity.setField(fieldEntry.getKey(), entities);
                    continue;
                }

                List<Entity> savedEntities = new LinkedList<Entity>();

                HasManyType hasManyType = (HasManyType) fieldEntry.getValue().getType();

                saveInnerEntities(alreadySavedEntities, newlySavedEntities, hasManyType.getJoinFieldName(), savedEntity.getId(),
                        entities, savedEntities, (InternalDataDefinition) hasManyType.getDataDefinition());

                EntityList dbEntities = savedEntity.getHasManyField(fieldEntry.getKey());

                removeOrphans(savedEntities, (InternalDataDefinition) hasManyType.getDataDefinition(), dbEntities);

                savedEntity.setField(fieldEntry.getKey(), savedEntities);
            } else if (fieldEntry.getValue().getType() instanceof TreeType) {
                List<Entity> entities = (List<Entity>) genericEntityToSave.getField(fieldEntry.getKey());

                if (entities == null || entities instanceof EntityTree) {
                    savedEntity.setField(fieldEntry.getKey(), entities);
                    continue;
                }

                List<Entity> savedEntities = new LinkedList<Entity>();

                TreeType treeType = (TreeType) fieldEntry.getValue().getType();

                saveInnerEntities(alreadySavedEntities, newlySavedEntities, treeType.getJoinFieldName(), savedEntity.getId(),
                        entities, savedEntities, (InternalDataDefinition) treeType.getDataDefinition());

                EntityTree dbEntities = savedEntity.getTreeField(fieldEntry.getKey());

                removeOrphans(savedEntities, (InternalDataDefinition) treeType.getDataDefinition(), dbEntities);

                System.out.println(" ^^ " + dbEntities.getRoot());
                System.out.println(" ^^ " + dbEntities.getRoot().getHasManyField("children"));

                updateEntityTreeNodeJoinField(dbEntities.getRoot().getHasManyField("children"),
                        (InternalDataDefinition) treeType.getDataDefinition(), treeType.getJoinFieldName(), savedEntity.getId());

            }
        }

        alreadySavedEntities.add(savedEntity);

        if (genericEntity.getId() == null && savedEntity.getId() != null) {
            newlySavedEntities.add(savedEntity);
        }

        return savedEntity;
    }

    private void saveInnerEntities(final Set<Entity> alreadySavedEntities, final Set<Entity> newlySavedEntities,
            final String joinFieldName, final Long id, final List<Entity> entities, final List<Entity> savedEntities,
            final InternalDataDefinition dataDefinition) {
        for (Entity innerEntity : entities) {
            innerEntity.setField(joinFieldName, id);
            Entity savedInnerEntity = performSave(dataDefinition, innerEntity, alreadySavedEntities, newlySavedEntities);
            savedEntities.add(savedInnerEntity);
            if (!savedInnerEntity.isValid()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        }
    }

    private void removeOrphans(final List<Entity> savedEntities, final InternalDataDefinition dataDefinition,
            final List<Entity> dbEntities) {
        for (Entity dbEntity : dbEntities) {
            boolean exists = false;
            for (Entity exisingEntity : savedEntities) {
                if (dbEntity.getId().equals(exisingEntity.getId())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                delete(dataDefinition, dbEntity.getId());
            }
        }
    }

    private void updateEntityTreeNodeJoinField(final List<Entity> list, final InternalDataDefinition dataDefinition,
            final String joinFieldName, final Long id) {
        for (Entity node : list) {
            System.out.println(" ^^^^ " + node + " -> " + joinFieldName + "=" + id);
            node.setField(joinFieldName, id);
            Entity savedNode = performSave(dataDefinition, node, new HashSet<Entity>(), new HashSet<Entity>());
            System.out.println(" ^^^^ " + savedNode.getHasManyField("children"));
            System.out.println(" ^^^^ " + savedNode.getHasManyField("children").size());
            updateEntityTreeNodeJoinField(savedNode.getHasManyField("children"), dataDefinition, joinFieldName, id);
        }
    }

    @Override
    @Transactional
    @Monitorable
    public Entity copy(final InternalDataDefinition dataDefinition, final Long entityId) {
        Entity sourceEntity = get(dataDefinition, entityId);
        Entity targetEntity = copy(dataDefinition, sourceEntity);

        if (targetEntity == null) {
            throw new IllegalStateException("Cannot copy " + sourceEntity);
        }

        LOG.info(sourceEntity + " has been copied to " + targetEntity);

        targetEntity = save(dataDefinition, targetEntity);

        if (!targetEntity.isValid()) {
            throw new CopyException(targetEntity);
        }

        return targetEntity;
    }

    public Entity copy(final InternalDataDefinition dataDefinition, final Entity sourceEntity) {
        Entity targetEntity = new DefaultEntity(sourceEntity.getPluginIdentifier(), sourceEntity.getName());

        for (String fieldName : dataDefinition.getFields().keySet()) {
            targetEntity.setField(fieldName, getCopyValueOfSimpleField(sourceEntity, dataDefinition, fieldName));
        }

        if (!dataDefinition.callCopyHook(targetEntity)) {
            return null;
        }

        for (String fieldName : dataDefinition.getFields().keySet()) {
            copyHasManyField(sourceEntity, targetEntity, dataDefinition, fieldName);
        }

        for (String fieldName : dataDefinition.getFields().keySet()) {
            copyTreeField(sourceEntity, targetEntity, dataDefinition, fieldName);
        }

        return targetEntity;
    }

    private void copyTreeField(final Entity sourceEntity, final Entity targetEntity, final DataDefinition dataDefinition,
            final String fieldName) {
        FieldDefinition fieldDefinition = dataDefinition.getField(fieldName);

        if (!(fieldDefinition.getType() instanceof TreeType) || !((TreeType) fieldDefinition.getType()).isCopyable()) {
            return;
        }

        TreeType treeType = ((TreeType) fieldDefinition.getType());

        List<Entity> entities = new ArrayList<Entity>();

        Entity root = sourceEntity.getTreeField(fieldName).getRoot();

        if (root != null) {
            root.setField(treeType.getJoinFieldName(), null);
            root = copy((InternalDataDefinition) treeType.getDataDefinition(), root);

            if (root != null) {
                entities.add(root);
            }
        }

        targetEntity.setField(fieldName, entities);
    }

    private void copyHasManyField(final Entity sourceEntity, final Entity targetEntity, final DataDefinition dataDefinition,
            final String fieldName) {
        FieldDefinition fieldDefinition = dataDefinition.getField(fieldName);

        if (!(fieldDefinition.getType() instanceof HasManyType) || !((HasManyType) fieldDefinition.getType()).isCopyable()) {
            return;
        }

        HasManyType hasManyType = ((HasManyType) fieldDefinition.getType());

        List<Entity> entities = new ArrayList<Entity>();

        for (Entity childEntity : sourceEntity.getHasManyField(fieldName)) {
            childEntity.setField(hasManyType.getJoinFieldName(), null);

            Entity savedChildEntity = copy((InternalDataDefinition) hasManyType.getDataDefinition(), childEntity);

            if (savedChildEntity != null) {
                entities.add(savedChildEntity);
            }
        }

        targetEntity.setField(fieldName, entities);
    }

    private Object getCopyValueOfSimpleField(final Entity sourceEntity, final DataDefinition dataDefinition,
            final String fieldName) {
        FieldDefinition fieldDefinition = dataDefinition.getField(fieldName);
        if (fieldDefinition.isUnique()) {
            if (fieldDefinition.getType().getType().equals(String.class)) {
                return getCopyValueOfUniqueField(dataDefinition, fieldDefinition, sourceEntity.getStringField(fieldName));
            } else {
                sourceEntity.addError(fieldDefinition, "core.validate.field.error.invalidUniqueType");
                throw new CopyException(sourceEntity);
            }
        } else if (fieldDefinition.getType() instanceof HasManyType) {
            return null;
        } else if (fieldDefinition.getType() instanceof TreeType) {
            return null;
        } else {
            return sourceEntity.getField(fieldName);
        }
    }

    private String getCopyValueOfUniqueField(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final String value) {
        Matcher matcher = Pattern.compile("(.+)\\((\\d+)\\)").matcher(value);

        String oldValue = value;
        int index = 1;

        if (matcher.matches()) {
            oldValue = matcher.group(1);
            index = Integer.valueOf(matcher.group(2)) + 1;
        }

        while (true) {
            String newValue = oldValue + "(" + (index++) + ")";

            int matches = dataDefinition.find().withMaxResults(1).restrictedWith(Restrictions.eq(fieldDefinition, newValue))
                    .list().getTotalNumberOfEntities();

            if (matches == 0) {
                return newValue;
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    public Entity get(final InternalDataDefinition dataDefinition, final Long entityId) {
        checkNotNull(dataDefinition, "DataDefinition must be given");
        checkNotNull(entityId, "EntityId must be given");

        Object databaseEntity = getDatabaseEntity(dataDefinition, entityId);

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
            deleteEntity(dataDefinition, entityId);
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

        // FIXME masz

        if (searchCriteria.getDistinctProperty() != null) {
            Class<?> entityClass = ((InternalDataDefinition) searchCriteria.getDataDefinition()).getClassForEntity();
            Set<String> batches = new HashSet<String>();
            List<Object> uniqueResults = new ArrayList<Object>();

            if (GenealogiesGenealogy.class.equals(entityClass)) {
                for (Object o : results) {
                    String batch = ((GenealogiesGenealogy) o).getBatch();

                    if (!batches.contains(batch)) {
                        uniqueResults.add(o);
                        batches.add(batch);
                    }
                }

                results = uniqueResults;
                totalNumberOfEntities = results.size();
            } else if (GenealogiesProductInBatch.class.equals(entityClass)) {
                for (Object o : results) {
                    String batch = ((GenealogiesProductInBatch) o).getBatch();

                    if (!batches.contains(batch)) {
                        uniqueResults.add(o);
                        batches.add(batch);
                    }
                }

                results = uniqueResults;
                totalNumberOfEntities = results.size();
            }
        }

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

        Object databaseEntity = getDatabaseEntity(dataDefinition, entityId);

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

        Object databaseEntity = getDatabaseEntity(dataDefinition, entityId);

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
            existingDatabaseEntity = getDatabaseEntity(dataDefinition, entity.getId());
            checkState(existingDatabaseEntity != null, "Entity[%s][id=%s] cannot be found", dataDefinition.getPluginIdentifier()
                    + "." + dataDefinition.getName(), entity.getId());
        }

        return existingDatabaseEntity;
    }

    private void deleteEntity(final InternalDataDefinition dataDefinition, final Long entityId) {
        Object databaseEntity = getDatabaseEntity(dataDefinition, entityId);

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
                        save(childDataDefinition, defaultEntity);
                    }
                }
            }

            // TODO delete tree fields
        }

        getCurrentSession().delete(databaseEntity);

        LOG.info("Entity[" + dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName() + "][id=" + entityId
                + "] has been deleted");
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    private Criteria getCriteria(final SearchCriteria searchCriteria) {
        InternalDataDefinition dataDefinition = (InternalDataDefinition) searchCriteria.getDataDefinition();
        Criteria criteria = getCurrentSession().createCriteria(dataDefinition.getClassForEntity());

        for (Restriction restriction : searchCriteria.getRestrictions()) {
            criteria.add(addRestrictionToCriteria(restriction, criteria));
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

    private Criterion addRestrictionToCriteria(final Restriction restriction, final Criteria criteria) {
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

    private Object getDatabaseEntity(final InternalDataDefinition dataDefinition, final Long entityId) {
        return getCurrentSession().get(dataDefinition.getClassForEntity(), entityId);
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
