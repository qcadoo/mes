package com.qcadoo.mes.lineChangeoverNorms;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;

@Service
public interface ChangeoverNormsService {

    Entity matchingChangeoverNorms(final Entity technologyFrom, final Entity toTechnology, final Entity productionLine);

    Entity searchMatchingChangeroverNorms(final Entity fromTechnology, final Entity toTechnology,
            final Entity fromTechnologyGroup, final Entity toTechnologyGroup, final Entity producionLine);
}
