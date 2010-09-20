package com.qcadoo.mes.products;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.search.SearchCriteriaBuilder;
import com.qcadoo.mes.core.data.internal.search.restrictions.RestrictionOperator;
import com.qcadoo.mes.core.data.model.ModelDefinition;
import com.qcadoo.mes.core.data.search.Restrictions;
import com.qcadoo.mes.core.data.search.SearchResult;

@Service
public class ProductService {

    public boolean checkInstructionDefault(final ModelDefinition dataDefinition, final Entity entity) {
        Boolean master = (Boolean) entity.getField("master");

        if (!master) {
            return true;
        }

        SearchCriteriaBuilder searchCriteria = dataDefinition.find().withMaxResults(1)
                .restrictedWith(Restrictions.eq(dataDefinition.getField("master"), true))
                .restrictedWith(Restrictions.belongsTo(dataDefinition.getField("product"), entity.getField("product")));

        if (entity.getId() != null) {
            searchCriteria.restrictedWith(Restrictions.idRestriction(entity.getId(), RestrictionOperator.NE));
        }

        SearchResult searchResult = searchCriteria.list();

        return searchResult.getTotalNumberOfEntities() == 0;
    }

    public boolean checkSubstituteDates(final ModelDefinition dataDefinition, final Entity entity) {
        Date dateFrom = (Date) entity.getField("effectiveDateFrom");
        Date dateTo = (Date) entity.getField("effectiveDateTo");

        return compareDates(dateFrom, dateTo);
    }

    public boolean checkOrderDates(final ModelDefinition dataDefinition, final Entity entity) {
        Date dateFrom = (Date) entity.getField("dateFrom");
        Date dateTo = (Date) entity.getField("dateTo");

        return compareDates(dateFrom, dateTo);
    }

    public boolean checkInstructionDates(final ModelDefinition dataDefinition, final Entity entity) {
        Date dateFrom = (Date) entity.getField("dateFrom");
        Date dateTo = (Date) entity.getField("dateTo");

        return compareDates(dateFrom, dateTo);
    }

    public void fillOrderDatesAndWorkers(final ModelDefinition dataDefinition, final Entity entity) {
        if (("pending".equals(entity.getField("state")) || "done".equals(entity.getField("state")))
                && entity.getField("effectiveDateFrom") == null) {
            entity.setField("effectiveDateFrom", new Date());
            entity.setField("startWorker", "Jan Kowalski"); // TODO masz - fill field with current user
        }
        if ("done".equals(entity.getField("state")) && entity.getField("effectiveDateTo") == null) {
            entity.setField("effectiveDateTo", new Date());
            entity.setField("endWorker", "Jan Nowak"); // TODO masz - fill field with current user

        }

        if (entity.getField("effectiveDateTo") != null) {
            entity.setField("state", "done");
        } else if (entity.getField("effectiveDateFrom") != null) {
            entity.setField("state", "pending");
        }
    }

    private boolean compareDates(final Date dateFrom, final Date dateTo) {
        if (dateFrom == null || dateTo == null) {
            return true;
        }

        if (dateFrom.after(dateTo)) {
            return false;
        } else {
            return true;
        }
    }

}
