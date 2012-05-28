package com.qcadoo.mes.lineChangeoverNormsForOrders;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public interface LineChangeoverNormsForOrdersService {

    void fillOrderForm(final ViewDefinitionState view, final List<String> orderFields);

    boolean checkIfOrderHasCorrectStateAndIsPrevious(final Entity previousOrder, final Entity order);

    Entity getOrderFromDB(final Long orderId);

    Entity getTechnologyFromDB(final Long technologyId);

    Entity getPreviousOrderFromDB(final Entity order);

}
