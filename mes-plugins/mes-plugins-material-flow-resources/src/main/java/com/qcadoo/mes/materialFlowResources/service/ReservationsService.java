package com.qcadoo.mes.materialFlowResources.service;

import java.util.Map;

import com.qcadoo.model.api.Entity;

/**
 * Created by kama on 30.06.2016.
 */
public interface ReservationsService {
    boolean reservationsEnabled();

    boolean reservationsEnabled(Entity document);

    void createReservation(Map<String, Object> params);

    void createReservation(Entity position);

    void updateReservation(Map<String, Object> params);

    void updateReservation(Entity position);

    void deleteReservation(Map<String, Object> params);

    void deleteReservation(Entity position);

    Boolean reservationsEnabled(Map<String, Object> params);

    Entity getReservationForPosition(Entity position);
}
