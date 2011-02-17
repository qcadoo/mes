package com.qcadoo.mes.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.search.SearchResult;

@Service
@Transactional
public class ParameterService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Long getParameterId() {

        DataDefinition dataDefinition = dataDefinitionService.get("basic", "parameter");
        SearchResult searchResult = dataDefinition.find().withMaxResults(1).list();

        if (searchResult.getEntities().size() > 0) {
            return searchResult.getEntities().get(0).getId();
        } else {

            Entity newParameter = new DefaultEntity("basic", "parameter");

            newParameter.setField("checkDoneOrderForQuality", false);
            newParameter.setField("batchForDoneOrder", "01none");

            Entity savedParameter = dataDefinition.save(newParameter);

            return savedParameter.getId();

        }

    }

}
