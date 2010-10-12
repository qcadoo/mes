package com.qcadoo.mes.view;

import com.qcadoo.mes.api.Entity;

public interface SaveableComponent {

    Entity getSaveableEntity(final ViewValue<Long> viewValue);

}
