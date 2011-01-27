package com.qcadoo.mes.products.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.form.FormComponentState;

@Service
public class NumberGeneratorService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void generateAndInsertNumber(final ViewDefinitionState state, final String entityName) {
        FieldComponentState number = (FieldComponentState) state.getComponentByReference("number");

        if (!checkIfShouldInsertNumber(state)) {
            return;
        }

        number.setFieldValue(generateNumber(state, entityName));
    }

    public boolean checkIfShouldInsertNumber(final ViewDefinitionState state) {
        FormComponentState form = (FormComponentState) state.getComponentByReference("form");
        FieldComponentState number = (FieldComponentState) state.getComponentByReference("number");
        if (form.getEntityId() != null) {
            // form is already saved
            return false;
        }
        if (StringUtils.hasText((String) number.getFieldValue())) {
            // number is already choosen
            return false;
        }
        if (number.isHasError()) {
            // there is a validation message for that field
            return false;
        }
        return true;
    }

    public String generateNumber(final ViewDefinitionState state, final String entityName) {

        SearchResult results = dataDefinitionService.get("products", entityName).find().withMaxResults(1).orderDescBy("id")
                .list();

        long longValue = 0;

        if (results.getEntities().isEmpty()) {
            longValue++;
        } else {
            longValue = results.getEntities().get(0).getId() + 1;
        }

        return String.format("%06d", longValue);
    }

    public String generateNumber(final ViewDefinitionState state, final String entityName, int digitsNumber) {

        SearchResult results = dataDefinitionService.get("products", entityName).find().withMaxResults(1).orderDescBy("id")
                .list();

        long longValue = 0;

        if (results.getEntities().isEmpty()) {
            longValue++;
        } else {
            longValue = results.getEntities().get(0).getId() + 1;
        }

        return String.format("%0" + digitsNumber + "d", longValue);
    }

}
