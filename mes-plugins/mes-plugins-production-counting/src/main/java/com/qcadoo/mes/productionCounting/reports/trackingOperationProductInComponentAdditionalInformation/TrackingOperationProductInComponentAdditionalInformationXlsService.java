package com.qcadoo.mes.productionCounting.reports.trackingOperationProductInComponentAdditionalInformation;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
final class TrackingOperationProductInComponentAdditionalInformationXlsService {

    @Autowired
    private TrackingOperationProductInComponentAdditionalInformationDataProvider dataProvider;

    List<TrackingOperationProductInComponentAdditionalInformationReportDto> getAdditionalInformationReportData(Date fromDate,
            Date toDate) {
        return dataProvider.getAdditionalInformationReportData(fromDate, toDate);
    }
}
