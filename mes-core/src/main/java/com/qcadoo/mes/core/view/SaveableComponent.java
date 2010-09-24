package com.qcadoo.mes.core.view;

import com.qcadoo.mes.core.api.Entity;

public interface SaveableComponent extends Component<Long> {

    Entity getSaveableEntity(final ViewValue<Object> viewValue);

}
