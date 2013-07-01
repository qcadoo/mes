package com.qcadoo.mes.technologies.tree.builder;

import com.qcadoo.mes.technologies.tree.builder.api.InternalOperationProductComponent;
import com.qcadoo.mes.technologies.tree.builder.api.InternalTechnologyOperationComponent;
import com.qcadoo.mes.technologies.tree.builder.api.OperationProductComponent;

public interface TechnologyTreeComponentsFactory {

    InternalTechnologyOperationComponent buildToc();

    InternalOperationProductComponent buildOpc(final OperationProductComponent.OperationCompType type);

}
