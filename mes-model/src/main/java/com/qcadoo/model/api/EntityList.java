package com.qcadoo.model.api;

import java.util.List;

import com.qcadoo.model.api.search.SearchCriteriaBuilder;

public interface EntityList extends List<Entity> {

    SearchCriteriaBuilder find();

}