package com.qcadoo.mes.technologies.tree.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.tree.builder.api.TechnologyTreeAdapter;
import com.qcadoo.mes.technologies.tree.builder.api.TechnologyTreeBuildService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;

@Service
public class TechnologyTreeBuildServiceImpl implements TechnologyTreeBuildService {

    @Autowired
    private TechnologyTreeComponentsFactory componentsFactory;

    @Autowired
    private NumberService numberService;

    @Override
    public <T, P> EntityTree build(final T from, final TechnologyTreeAdapter<T, P> transformer) {
        TechnologyTreeBuilder<T, P> builder = new TechnologyTreeBuilder<T, P>(componentsFactory, transformer);
        Entity root = builder.build(from, numberService);
        return EntityTreeUtilsService.getDetachedEntityTree(Lists.newArrayList(root));
    }
}
