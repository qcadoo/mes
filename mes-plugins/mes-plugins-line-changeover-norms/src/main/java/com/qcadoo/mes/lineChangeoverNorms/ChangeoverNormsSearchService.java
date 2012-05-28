package com.qcadoo.mes.lineChangeoverNorms;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;

@Service
public interface ChangeoverNormsSearchService {

    Entity searchMatchingChangeroverNormsForTechnology(final Entity fromTechnology, final Entity toTechnology,
            final Entity productionLine);

    Entity searchMatchingChangeroverNormsForTechnologyGroupWithLine(final Entity fromTechnologyGroup,
            final Entity toTechnologyGroup, final Entity productionLine);

}
