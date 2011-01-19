package com.qcadoo.mes.genealogies;

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
public class GenealogyAttributeService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Long getGenealogyAttributeId() {

        SearchResult searchResult = dataDefinitionService.get("genealogies", "currentAttribute").find().withMaxResults(1).list();

        if (searchResult.getEntities().size() > 0) {
            return searchResult.getEntities().get(0).getId();
        } else {

            Entity newAttribute = new DefaultEntity("genealogies", "currentAttribute");

            newAttribute.setField("shift", "");
            newAttribute.setField("post", "");
            newAttribute.setField("other", "");

            DataDefinition dataDefinition = dataDefinitionService.get("genealogies", "currentAttribute");
            Entity savedAttribute = dataDefinition.save(newAttribute);

            return savedAttribute.getId();

        }

    }

}
