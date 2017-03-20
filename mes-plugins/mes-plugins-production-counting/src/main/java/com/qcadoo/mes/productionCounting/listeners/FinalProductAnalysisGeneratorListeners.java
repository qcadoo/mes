package com.qcadoo.mes.productionCounting.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.analysis.FinalProductAnalysisService;

@Service
public class FinalProductAnalysisGeneratorListeners {

    @Autowired
    private FinalProductAnalysisService finalProductAnalysisService;

}
