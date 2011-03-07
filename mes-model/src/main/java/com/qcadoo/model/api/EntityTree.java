package com.qcadoo.model.api;

import java.util.List;

import com.qcadoo.model.api.search.SearchCriteriaBuilder;

public interface EntityTree extends List<Entity> {

    SearchCriteriaBuilder find();

    EntityTreeNode getRoot();

}