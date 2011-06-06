package com.qcadoo.mes.stoppage;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class StoppageService {

    public void showStoppage(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState, final String[] args) {
        Long orderId = (Long) triggerState.getFieldValue();

        if (orderId != null) {
            String url = "../page/stoppage/stoppage.html?context={\"order.id\":\"" + orderId + "\"}";
            viewDefinitionState.openModal(url);
        }
    }

}