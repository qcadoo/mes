package com.qcadoo.mes.core.view.containers;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.view.Component;
import com.qcadoo.mes.core.view.ViewValue;

public interface SaveableComponent extends Component<Long> {

    Entity getSaveableEntity(final ViewValue<Object> viewValue);

}