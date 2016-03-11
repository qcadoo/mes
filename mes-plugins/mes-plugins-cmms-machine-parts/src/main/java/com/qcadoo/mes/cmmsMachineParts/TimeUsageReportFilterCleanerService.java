package com.qcadoo.mes.cmmsMachineParts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.plugin.api.RunIfEnabled;
import com.qcadoo.tenant.api.MultiTenantCallback;
import com.qcadoo.tenant.api.MultiTenantService;

@Service
@RunIfEnabled(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER)
public class TimeUsageReportFilterCleanerService {

    private static final Logger LOG = LoggerFactory.getLogger(TimeUsageReportFilterCleanerService.class);

    @Autowired
    private MultiTenantService multiTenantService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void cleanOldFilters() {
        multiTenantService.doInMultiTenantContext(new MultiTenantCallback() {

            @Override
            public void invoke() {
                LOG.info("Removing old time usage report filters");
                Date dayBeforeNow = new DateTime().minusDays(1).toDate();
                Map<String, Date> paramMap = new HashMap<>();
                paramMap.put("date", dayBeforeNow);
                jdbcTemplate
                        .update("DELETE FROM jointable_staff_timeusagereportfilter WHERE timeusagereportfilter_id in (SELECT id FROM cmmsmachineparts_timeusagereportfilter WHERE createdate < :date)",
                                paramMap);
                jdbcTemplate.update("DELETE FROM cmmsmachineparts_timeusagereportfilter WHERE createdate < :date", paramMap);
            }
        });
    }
}
