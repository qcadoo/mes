package com.qcadoo.model.api;

import java.util.List;

import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.internal.EntityTreeNodeImpl;

public interface EntityTree extends List<Entity> {

    SearchCriteriaBuilder find();

    EntityTreeNodeImpl getRoot();

}