package com.qcadoo.mes.core.internal;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.internal.model.InternalDataDefinition;
import com.qcadoo.mes.core.internal.types.PriorityType;
import com.qcadoo.mes.core.model.FieldDefinition;

@Service
public final class PriorityService {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private EntityService entityService;

    public void prioritizeEntity(final InternalDataDefinition dataDefinition, final Object databaseEntity) {
        if (!dataDefinition.isPrioritizable()) {
            return;
        }

        FieldDefinition fieldDefinition = dataDefinition.getPriorityField();

        int totalNumberOfEntities = getTotalNumberOfEntities(dataDefinition, fieldDefinition, databaseEntity);

        entityService.setField(databaseEntity, fieldDefinition, totalNumberOfEntities + 1);
    }

    public void deprioritizeEntity(final InternalDataDefinition dataDefinition, final Object databaseEntity) {
        if (!dataDefinition.isPrioritizable()) {
            return;
        }

        FieldDefinition fieldDefinition = dataDefinition.getPriorityField();

        int currentPriority = (Integer) entityService.getField(databaseEntity, fieldDefinition);

        changePriority(dataDefinition, fieldDefinition, databaseEntity, currentPriority + 1, Integer.MAX_VALUE, -1);
    }

    private FieldDefinition getScopeForPriority(final FieldDefinition fieldDefinition) {
        return ((PriorityType) fieldDefinition.getType()).getScopeFieldDefinition();
    }

    public void move(final InternalDataDefinition dataDefinition, final Object databaseEntity, final int position, final int offset) {
        FieldDefinition fieldDefinition = dataDefinition.getPriorityField();

        int currentPriority = (Integer) entityService.getField(databaseEntity, fieldDefinition);

        int targetPriority = getTargetPriority(position, offset, currentPriority);

        targetPriority = checkIfTargetPriorityIsNotTooLow(targetPriority);

        targetPriority = getIfTargetPriorityIsNotTooHigh(dataDefinition, databaseEntity, fieldDefinition, targetPriority);

        if (currentPriority < targetPriority) {
            changePriority(dataDefinition, fieldDefinition, databaseEntity, currentPriority + 1, targetPriority, -1);
        } else if (currentPriority > targetPriority) {
            changePriority(dataDefinition, fieldDefinition, databaseEntity, targetPriority, currentPriority - 1, 1);
        } else {
            return;
        }

        entityService.setField(databaseEntity, fieldDefinition, targetPriority);

        getCurrentSession().update(databaseEntity);
    }

    private int getIfTargetPriorityIsNotTooHigh(final InternalDataDefinition dataDefinition, final Object databaseEntity,
            final FieldDefinition fieldDefinition, final int targetPriority) {
        if (targetPriority > 1) {
            int totalNumberOfEntities = getTotalNumberOfEntities(dataDefinition, fieldDefinition, databaseEntity);

            if (targetPriority > totalNumberOfEntities) {
                return totalNumberOfEntities;
            }
        }
        return targetPriority;
    }

    private int checkIfTargetPriorityIsNotTooLow(final int targetPriority) {
        if (targetPriority < 1) {
            return 1;
        }
        return targetPriority;
    }

    private int getTargetPriority(final int position, final int offset, final int currentPriority) {
        int targetPriority = 0;

        if (offset != 0) {
            targetPriority = currentPriority + offset;
        } else {
            targetPriority = position;
        }
        return targetPriority;
    }

    @SuppressWarnings("unchecked")
    private void changePriority(final InternalDataDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final Object databaseEntity, final int fromPriority, final int toPriority, final int diff) {
        Criteria criteria = getCriteria(dataDefinition, fieldDefinition, databaseEntity).add(
                Restrictions.ge(fieldDefinition.getName(), fromPriority)).add(
                Restrictions.le(fieldDefinition.getName(), toPriority));

        List<Object> entitiesToDecrement = criteria.list();

        for (Object entity : entitiesToDecrement) {
            int priority = (Integer) entityService.getField(entity, fieldDefinition);
            entityService.setField(entity, fieldDefinition, priority + diff);
            getCurrentSession().update(entity);
        }
    }

    private org.hibernate.classic.Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    private int getTotalNumberOfEntities(final InternalDataDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final Object databaseEntity) {
        Criteria criteria = getCriteria(dataDefinition, fieldDefinition, databaseEntity).setProjection(Projections.rowCount());

        return Integer.valueOf(criteria.uniqueResult().toString());
    }

    private Criteria getCriteria(final InternalDataDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final Object databaseEntity) {
        FieldDefinition scopeFieldDefinition = getScopeForPriority(fieldDefinition);
        Object scopeValue = entityService.getField(databaseEntity, scopeFieldDefinition);

        Criteria criteria = getCurrentSession().createCriteria(dataDefinition.getClassForEntity());

        if (dataDefinition.isDeletable()) {
            entityService.addDeletedRestriction(criteria);
        }

        if (scopeValue instanceof Entity) {
            return criteria.add(Restrictions.eq(scopeFieldDefinition.getName() + ".id", ((Entity) scopeValue).getId()));
        } else {
            return criteria.add(Restrictions.eq(scopeFieldDefinition.getName(), scopeValue));
        }
    }

}
