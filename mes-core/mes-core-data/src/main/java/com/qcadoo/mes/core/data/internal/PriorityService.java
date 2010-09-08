package com.qcadoo.mes.core.data.internal;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.internal.types.PriorityType;

@Service
public final class PriorityService {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private EntityService entityService;

    public void prioritizeEntity(final DataDefinition dataDefinition, final Object databaseEntity) {
        if (!dataDefinition.isPrioritizable()) {
            return;
        }

        DataFieldDefinition fieldDefinition = dataDefinition.getPriorityField();

        int totalNumberOfEntities = getTotalNumberOfEntities(dataDefinition, fieldDefinition, databaseEntity);

        entityService.setField(databaseEntity, fieldDefinition, totalNumberOfEntities + 1);
    }

    public void deprioritizeEntity(final DataDefinition dataDefinition, final Object databaseEntity) {
        if (!dataDefinition.isPrioritizable()) {
            return;
        }

        DataFieldDefinition fieldDefinition = dataDefinition.getPriorityField();

        int currentPriority = (Integer) entityService.getField(databaseEntity, fieldDefinition);

        changePriority(dataDefinition, fieldDefinition, databaseEntity, currentPriority + 1, Integer.MAX_VALUE, -1);
    }

    private DataFieldDefinition getScopeForPriority(final DataFieldDefinition fieldDefinition) {
        return ((PriorityType) fieldDefinition.getType()).getScopeFieldDefinition();
    }

    public void move(final DataDefinition dataDefinition, final Object databaseEntity, final int position, final int offset) {
        DataFieldDefinition fieldDefinition = dataDefinition.getPriorityField();

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

    private int getIfTargetPriorityIsNotTooHigh(final DataDefinition dataDefinition, final Object databaseEntity,
            final DataFieldDefinition fieldDefinition, final int targetPriority) {
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
    private void changePriority(final DataDefinition dataDefinition, final DataFieldDefinition fieldDefinition,
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

    private int getTotalNumberOfEntities(final DataDefinition dataDefinition, final DataFieldDefinition fieldDefinition,
            final Object databaseEntity) {
        Criteria criteria = getCriteria(dataDefinition, fieldDefinition, databaseEntity).setProjection(Projections.rowCount());

        return Integer.valueOf(criteria.uniqueResult().toString());
    }

    private Criteria getCriteria(final DataDefinition dataDefinition, final DataFieldDefinition fieldDefinition,
            final Object databaseEntity) {
        DataFieldDefinition scopeFieldDefinition = getScopeForPriority(fieldDefinition);
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
