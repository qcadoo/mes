package com.qcadoo.mes.basic.services;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.basic.constants.TypeTerminalLicenses;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.tenant.api.MultiTenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LicenseUsageService {

    @Autowired
    private MultiTenantService multiTenantService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ParameterService parameterService;

    public boolean checkLicences(final Long staffId) {
        Entity parameter = parameterService.getParameter();

        Integer numberTerminalLicenses = parameter.getIntegerField(ParameterFields.NUMBER_TERMINAL_LICENSES);
        String typeTerminalLicenses = parameter.getStringField(ParameterFields.TYPE_TERMINAL_LICENSES);

        Long currentUserId = securityService.getCurrentUserId();

        Integer licenseUsageCount = getLicenseUsageCount();

        if (numberTerminalLicenses.compareTo(licenseUsageCount) < 0) {
            Integer licenseUsageCountOther = getLicenseUsageCountForOther(currentUserId);

            if (numberTerminalLicenses.compareTo(licenseUsageCountOther + 1) < 0) {
                return false;
            }
        }

        Integer licenseUsageCountForUser = getLicenseUsageCountForUser(currentUserId);

        if (licenseUsageCountForUser == 0) {
            if (numberTerminalLicenses.compareTo(licenseUsageCount + 1) < 0) {
                return false;
            } else {
                createLicenseUsage(currentUserId, staffId);
            }
        } else {
            Integer licenseUsageCountForUserAndStaff = getLicenseUsageCountForUserAndStaff(currentUserId, staffId);

            if (TypeTerminalLicenses.UP_TO_TEN_EMPLOYEES.getStringValue().equals(typeTerminalLicenses)) {
                if (licenseUsageCountForUserAndStaff == 0) {
                    if ((licenseUsageCountForUser + 1) > 10) {
                        return false;
                    } else {
                        createLicenseUsage(currentUserId, staffId);
                    }
                }
            } else if (TypeTerminalLicenses.FROM_11_TO_50_EMPLOYEES.getStringValue().equals(typeTerminalLicenses)) {
                if (licenseUsageCountForUserAndStaff == 0) {
                    if ((licenseUsageCountForUser + 1) > 50) {
                        return false;
                    } else {
                        createLicenseUsage(currentUserId, staffId);
                    }
                }
            } else {
                if (licenseUsageCountForUserAndStaff == 0) {
                    createLicenseUsage(currentUserId, staffId);
                }
            }
        }

        return true;
    }

    private Integer getLicenseUsageCount() {
        String sql = "SELECT COUNT(DISTINCT(user_id)) FROM basic_licenseusage "
                + "WHERE createtime::date = now()::date";

        Map<String, Object> parameters = Maps.newHashMap();

        return jdbcTemplate.queryForObject(sql, parameters, Integer.class);
    }

    private Integer getLicenseUsageCountForOther(final Long userId) {
        String sql = "SELECT COUNT(DISTINCT(user_id)) FROM basic_licenseusage "
                + "WHERE createtime::date = now()::date AND createtime < ("
                + "SELECT createtime FROM basic_licenseusage "
                + "WHERE createtime::date = now()::date AND user_id = :userId "
                + "ORDER BY id "
                + "LIMIT 1"
                + ")";

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("userId", userId);

        return jdbcTemplate.queryForObject(sql, parameters, Integer.class);
    }

    private Integer getLicenseUsageCountForUser(final Long userId) {
        String sql = "SELECT COUNT(DISTINCT(staff_id)) FROM basic_licenseusage "
                + "WHERE createtime::date = now()::date AND user_id = :userId";

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("userId", userId);

        return jdbcTemplate.queryForObject(sql, parameters, Integer.class);
    }

    private Integer getLicenseUsageCountForUserAndStaff(final Long userId, final Long staffId) {
        String sql = "SELECT COUNT(DISTINCT(user_id)) FROM basic_licenseusage "
                + "WHERE createtime::date = now()::date AND user_id = :userId AND staff_id = :staffId";

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("userId", userId);
        parameters.put("staffId", staffId);

        return jdbcTemplate.queryForObject(sql, parameters, Integer.class);
    }

    private void createLicenseUsage(final Long userId, final Long staffId) {
        String sql = "INSERT INTO basic_licenseusage (createtime, user_id, staff_id) "
                + "VALUES (now(), :userId, :staffId)";

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("userId", userId);
        parameters.put("staffId", staffId);

        SqlParameterSource namedParameters = new MapSqlParameterSource(parameters);

        jdbcTemplate.update(sql, namedParameters);
    }

    public void deleteOldLicenseUsagesTrigger() {
        multiTenantService.doInMultiTenantContext(this::deleteOldLicenseUsages);
    }

    private void deleteOldLicenseUsages() {
        String sql = "DELETE FROM basic_licenseusage WHERE createtime < (now() - interval '7 day');";

        Map<String, Object> params = Maps.newHashMap();

        jdbcTemplate.update(sql, params);
    }

}
