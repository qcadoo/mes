package com.qcadoo.mes.technologies.dataProvider;

import java.util.Optional;

import com.qcadoo.model.api.Entity;

public interface TechnologyDataProvider {

    Optional<Entity> tryFind(final Long id);

}
