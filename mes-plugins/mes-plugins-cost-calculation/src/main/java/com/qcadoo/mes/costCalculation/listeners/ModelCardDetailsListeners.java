package com.qcadoo.mes.costCalculation.listeners;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ModelCardDetailsListeners {

    public void printModelCard(final ViewDefinitionState view, final ComponentState state, final String[] args) {

    }

    @Transactional
    public void generateModelCard(final ViewDefinitionState view, final ComponentState state, final String[] args) {

    }
}
