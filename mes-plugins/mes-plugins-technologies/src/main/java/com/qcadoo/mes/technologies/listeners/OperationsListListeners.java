package com.qcadoo.mes.technologies.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class OperationsListListeners {

    public void openOperationsImportPage(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        StringBuilder url = new StringBuilder("../page/technologies/operationsImport.html");

        view.openModal(url.toString());
    }

}
