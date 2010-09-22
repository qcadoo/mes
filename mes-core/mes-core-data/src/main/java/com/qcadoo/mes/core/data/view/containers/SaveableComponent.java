package com.qcadoo.mes.core.data.view.containers;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.view.Component;
import com.qcadoo.mes.core.data.view.ViewValue;

public interface SaveableComponent extends Component<Long> {

    Entity getSaveableEntity(final ViewValue<Object> viewValue);

}