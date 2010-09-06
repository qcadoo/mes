package com.qcadoo.mes.plugins.products;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.beans.Entity;

@Service
public class ProductService {

    public boolean checkSubstituteDates(final Entity entity) {
        Date dateFrom = (Date) entity.getField("effectiveDateFrom");
        Date dateTo = (Date) entity.getField("effectiveDateTo");

        return compareDates(dateFrom, dateTo);
    }

    public boolean checkOrderDates(final Entity entity) {
        Date dateFrom = (Date) entity.getField("dateFrom");
        Date dateTo = (Date) entity.getField("dateTo");

        return compareDates(dateFrom, dateTo);
    }

    public void fillOrderDatesAndWorkers(final Entity entity) {
        if (("pending".equals(entity.getField("state")) || "done".equals(entity.getField("state")))
                && entity.getField("effectiveDateFrom") == null) {
            entity.setField("effectiveDateFrom", new Date());
            entity.setField("startWorker", "Jan Kowalski"); // TODO masz - fill field with current user
        }
        if ("done".equals(entity.getField("state")) && entity.getField("effectiveDateTo") == null) {
            entity.setField("effectiveDateTo", new Date());
            entity.setField("endWorker", "Jan Nowak"); // TODO masz - fill field with current user

        }

        System.out.println(" -----> " + entity.getField("effectiveDateFrom"));
        System.out.println(" -----> " + entity.getField("effectiveDateTo"));

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
