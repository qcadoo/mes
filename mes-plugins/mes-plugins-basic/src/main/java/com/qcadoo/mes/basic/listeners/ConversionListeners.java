package com.qcadoo.mes.basic.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ConversionListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void redirectToDictionary(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        Long dictionaryId = getDictionaryId("units");

        if (dictionaryId != null) {
            String url = "../page/qcadooDictionaries/dictionaryDetails.html?context={\"form.id\":\"" + dictionaryId + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }

    }

    private Long getDictionaryId(final String name) {
        Entity dictionary = dataDefinitionService.get("qcadooModel", "dictionary").find()
                .add(SearchRestrictions.eq("name", name)).setMaxResults(1).uniqueResult();
        if (dictionary == null) {
            return null;
        } else {
            return dictionary.getId();
        }
    }
}
