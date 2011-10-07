package com.qcadoo.mes.orders.states;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;

@Service
public class OrderStatesService {

    private List<OrderStateListener> listeners = new LinkedList<OrderStateListener>();

    public void addOrderStateListener(OrderStateListener listener) {
        listeners.add(listener);
    }

    public void removeOrderStateListener(OrderStateListener listener) {
        listeners.remove(listener);
    }

    boolean performChangeState(Entity newEntity, Entity oldEntity) {
        // walidacja przejscia na stan
        // np:
        if (oldEntity == null && !newEntity.getField("state").equals("pending")) {
            throw new IllegalStateException();
        }
        if (oldEntity != null && newEntity.getField("state").equals("open") && !oldEntity.getField("state").equals("pending")) {
            throw new IllegalStateException();
        }

        // walidacja poprawnosci encji
        if (newEntity.getField("name").equals("")) {
            throw new IllegalStateException();
        }

        // logika w zaleznosci od przejscia na stan
        if (newEntity.getField("state").equals("pending")) {
            // walidacja specyficzna dla stanu
            // np
            if (newEntity.getField("dateTo") != null) {
                throw new IllegalStateException();
            }

            // wywolanie listenerow
            for (OrderStateListener listener : listeners) {
                String errorMessage = listener.onPending(newEntity);
                if (errorMessage != null) {
                    // komunikat bledu z tym messagem
                    return false;
                }
            }

        } else if (newEntity.getField("status").equals("inProgress")) {

        } else if (newEntity.getField("status").equals("closed")) {

        }
        return true;
    }
}
