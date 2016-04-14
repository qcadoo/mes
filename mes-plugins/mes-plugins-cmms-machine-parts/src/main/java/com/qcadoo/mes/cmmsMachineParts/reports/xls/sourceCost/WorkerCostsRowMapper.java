package com.qcadoo.mes.cmmsMachineParts.reports.xls.sourceCost;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.qcadoo.mes.cmmsMachineParts.reports.xls.sourceCost.dto.WorkerCostsDTO;


public class WorkerCostsRowMapper implements RowMapper<WorkerCostsDTO> {

    @Override
    public WorkerCostsDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        WorkerCostsDTO workerCosts = new WorkerCostsDTO();
        workerCosts.setSourceCost(rs.getString("sourcecost"));
        workerCosts.setWorker(rs.getString("worker"));
        workerCosts.setEvent(rs.getString("number"));
        workerCosts.setType(rs.getString("type"));
        workerCosts.setWorkTime(rs.getInt("worktime"));

        return workerCosts;
    }

}
