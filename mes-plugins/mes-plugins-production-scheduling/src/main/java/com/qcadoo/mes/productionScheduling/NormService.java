package com.qcadoo.mes.productionScheduling;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;


@Service
public class NormService {
	
	
	public void changeEnableNorm(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
		
		FieldComponent enableNorm = (FieldComponent) state;
        FieldComponent tpzNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tpzNorm");
        FieldComponent tjNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tjNorm");
        
        if (enableNorm.isEnabled()){
        	tpzNorm.setVisible(true);
        	
        }
        
        
	}
}
