package com.qcadoo.mes.lineChangeoverNorms;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;

@Service
public interface ChangeoverNormsService {

    Entity matchingChangeoverNorms(final Entity technologyFrom, final Entity toTechnology, final Entity productionLine);
}
