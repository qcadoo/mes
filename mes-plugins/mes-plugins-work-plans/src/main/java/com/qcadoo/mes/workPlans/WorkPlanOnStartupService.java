package com.qcadoo.mes.workPlans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.workPlans.workPlansColumnExtension.WorkPlansColumnLoader;
import com.qcadoo.plugin.api.Module;

@Component
public class WorkPlanOnStartupService extends Module {

    @Autowired
    private WorkPlansColumnLoader workPlansColumnLoader;

    @Override
    @Transactional
    public void multiTenantEnable() {
        workPlansColumnLoader.setDefaulValues();

        workPlansColumnLoader.addWorkPlansColumnsForProducts();
    }

    @Override
    @Transactional
    public void multiTenantDisable() {
        workPlansColumnLoader.setDefaulValues();

        workPlansColumnLoader.deleteWorkPlansColumnsForProducts();
    }
}
