package com.qcadoo.mes.view;

import com.qcadoo.mes.api.Entity;

public interface SaveableComponent extends Component<Long> {

    Entity getSaveableEntity(final ViewValue<Object> viewValue);

}
