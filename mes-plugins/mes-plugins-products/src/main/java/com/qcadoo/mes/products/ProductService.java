package com.qcadoo.mes.products;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.search.SearchCriteriaBuilder;
import com.qcadoo.mes.core.data.internal.search.restrictions.RestrictionOperator;
import com.qcadoo.mes.core.data.model.DataDefinition;
import com.qcadoo.mes.core.data.search.Restrictions;
import com.qcadoo.mes.core.data.search.SearchResult;
import com.qcadoo.mes.core.data.view.ViewValue;
import com.qcadoo.mes.core.data.view.elements.grid.ListData;

@Service
public class ProductService {

    @SuppressWarnings("unchecked")
    public void getBeanAName(final ViewValue<Object> value) {
        ViewValue<String> valueNameM = (ViewValue<String>) value.lookupValue("mainWindow.beanAForm.nameM");
        ViewValue<String> valueNameA = (ViewValue<String>) value.lookupValue("mainWindow.beanAForm.name");
        ViewValue<String> valueNameB = (ViewValue<String>) value.lookupValue("mainWindow.beanAForm.nameB");
        ViewValue<ListData> valueNameC = (ViewValue<ListData>) value.lookupValue("mainWindow.beanAForm.beansCGrig");
        valueNameM
                .setValue((valueNameA != null ? valueNameA.getValue() : null)
                        + " - "
                        + (valueNameB != null ? valueNameB.getValue() : null)
                        + " - "
                        + (valueNameC != null && valueNameC.getValue() != null
                                && valueNameC.getValue().getSelectedEntityId() != null ? valueNameC.getValue()
                                .getSelectedEntityId() : null));
    }

    public boolean checkInstructionDefault(final DataDefinition dataDefinition, final Entity entity) {
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

    public boolean checkSubstituteDates(final DataDefinition dataDefinition, final Entity entity) {
        Date dateFrom = (Date) entity.getField("effectiveDateFrom");
        Date dateTo = (Date) entity.getField("effectiveDateTo");

        return compareDates(dateFrom, dateTo);
    }

    public boolean checkOrderDates(final DataDefinition dataDefinition, final Entity entity) {
        Date dateFrom = (Date) entity.getField("dateFrom");
        Date dateTo = (Date) entity.getField("dateTo");

        return compareDates(dateFrom, dateTo);
    }

    public boolean checkInstructionDates(final DataDefinition dataDefinition, final Entity entity) {
        Date dateFrom = (Date) entity.getField("dateFrom");
        Date dateTo = (Date) entity.getField("dateTo");

        return compareDates(dateFrom, dateTo);
    }

    public void fillOrderDatesAndWorkers(final DataDefinition dataDefinition, final Entity entity) {
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

        return !dateFrom.after(dateTo);
    }

}
