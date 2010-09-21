package com.qcadoo.mes.core.data.view.containers;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.view.Component;
import com.qcadoo.mes.core.data.view.ViewValue;

public interface SaveableComponent extends Component<Long> {

    public abstract Entity getFormEntity(final ViewValue<Object> viewValue);

    public abstract Object addValidationResults(final ViewValue<Object> viewValue, final String path, final Entity results);

}