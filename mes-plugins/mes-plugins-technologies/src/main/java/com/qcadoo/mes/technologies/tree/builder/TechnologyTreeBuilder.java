package com.qcadoo.mes.technologies.tree.builder;

import java.util.List;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.tree.builder.api.InternalOperationProductComponent;
import com.qcadoo.mes.technologies.tree.builder.api.InternalTechnologyOperationComponent;
import com.qcadoo.mes.technologies.tree.builder.api.ItemWithQuantity;
import com.qcadoo.mes.technologies.tree.builder.api.OperationProductComponent;
import com.qcadoo.mes.technologies.tree.builder.api.TechnologyOperationComponent;
import com.qcadoo.mes.technologies.tree.builder.api.TechnologyTreeAdapter;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

public class TechnologyTreeBuilder<T, P> {

    private final TechnologyTreeAdapter<T, P> adapter;

    private final TechnologyTreeComponentsFactory compsFactory;

    public TechnologyTreeBuilder(final TechnologyTreeComponentsFactory opBuilder, final TechnologyTreeAdapter<T, P> transformer) {
        this.adapter = transformer;
        this.compsFactory = opBuilder;
    }

    public Entity build(final T from, final NumberService numberService) {
        TechnologyOperationComponent root = buildOp(from, numberService);
        return root.getWrappedEntity();
    }

    private TechnologyOperationComponent buildOp(final T from, final NumberService numberService) {
        InternalTechnologyOperationComponent toc = compsFactory.buildToc();
        adapter.setOpCompCustomFields(toc, from);
        toc.addInputProducts(getProductComponents(OperationProductComponent.OperationCompType.INPUT,
                adapter.extractInputProducts(from), numberService));
        toc.addOutputProducts(getProductComponents(OperationProductComponent.OperationCompType.OUTPUT,
                adapter.extractOutputProducts(from), numberService));
        toc.setOperation(adapter.buildOperationEntity(from));
        for (T subOperation : adapter.extractSubOperations(from)) {
            toc.addSubOperation(buildOp(subOperation, numberService));
        }
        return toc;
    }

    private List<OperationProductComponent> getProductComponents(final OperationProductComponent.OperationCompType opcType,
            final Iterable<ItemWithQuantity<P>> productComponents, final NumberService numberService) {
        final List<OperationProductComponent> operationProductComponents = Lists.newArrayList();
        for (ItemWithQuantity<P> productAndQuantity : productComponents) {
            InternalOperationProductComponent opc = compsFactory.buildOpc(opcType);
            adapter.setOpProductCompCustomFields(opc, productAndQuantity.getItem());
            opc.setQuantity(numberService.setScale(productAndQuantity.getQuantity()));
            opc.setProduct(adapter.buildProductEntity(productAndQuantity.getItem()));
            operationProductComponents.add(opc);
        }
        return operationProductComponents;
    }

}
