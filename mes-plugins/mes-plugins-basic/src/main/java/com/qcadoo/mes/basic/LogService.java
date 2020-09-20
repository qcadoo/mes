package com.qcadoo.mes.basic;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.LogFields;
import com.qcadoo.mes.basic.constants.LogLevel;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.constants.QcadooSecurityConstants;
import com.qcadoo.security.constants.UserFields;
import com.qcadoo.tenant.api.MultiTenantCallback;
import com.qcadoo.tenant.api.MultiTenantService;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogService {

    private static final Logger LOGGER = Logger.getLogger(LogService.class);

    public static final String QCADOO_BOT = "qcadoo_bot";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MultiTenantService multiTenantService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public final static class Builder {

        private Builder() {
        }

        private String type;

        private String action;

        private String item1;

        private String item2;

        private String item3;

        private String message;

        private String details;

        private Date createTime;

        private LogLevel logLevel = LogLevel.TRACE;

        public static Builder newBuilder(LogLevel logLevel, String type, String action) {
            return new Builder().withLogLevel(logLevel).withType(type).withAction(action);
        }

        public static Builder error(String type, String action) {
            return new Builder().withLogLevel(LogLevel.ERROR).withType(type).withAction(action);
        }

        public static Builder debug(String type, String action) {
            return new Builder().withLogLevel(LogLevel.DEBUG).withType(type).withAction(action);
        }

        public static Builder info(String type, String action) {
            return new Builder().withLogLevel(LogLevel.INFO).withType(type).withAction(action);
        }

        public static Builder activity(String type, String action) {
            return new Builder().withLogLevel(LogLevel.ACTIVITY).withType(type).withAction(action);
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withAction(String action) {
            this.action = action;
            return this;
        }

        public Builder withCreateTime(Date createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder withItem1(String item1) {
            this.item1 = item1;
            return this;
        }

        public Builder withItem2(String item2) {
            this.item2 = item2;
            return this;
        }

        public Builder withItem3(String item3) {
            this.item3 = item3;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withDetails(String details) {
            this.details = details;
            return this;
        }

        public Builder withLogLevel(LogLevel logLevel) {
            this.logLevel = logLevel;
            return this;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Entity add(Builder builder) {
        validate(builder);

        DataDefinition logDD = getLogDD();
        Entity logEntity = logDD.create();

        logEntity.setField(LogFields.ACTION, builder.action);
        if (builder.createTime == null) {
            logEntity.setField(LogFields.CREATE_TIME, new Date());
        } else {
            logEntity.setField(LogFields.CREATE_TIME, builder.createTime);
        }
        logEntity.setField(LogFields.DETAILS, builder.details);
        logEntity.setField(LogFields.ITEM_1, builder.item1);
        logEntity.setField(LogFields.ITEM_2, builder.item2);
        logEntity.setField(LogFields.ITEM_3, builder.item3);
        logEntity.setField(LogFields.LOG_LEVEL, builder.logLevel.getCode());
        logEntity.setField(LogFields.MESSAGE, builder.message);
        logEntity.setField(LogFields.LOG_TYPE, builder.type);
        Long userId = securityService.getCurrentUserOrQcadooBotId();
        logEntity.setField(LogFields.USER, userId);
        Entity saved = logDD.save(logEntity);
        if(!saved.isValid()) {
            StringBuilder logErrors = new StringBuilder();
            for (Map.Entry<String, ErrorMessage> errorEntry : saved.getErrors().entrySet()) {
                logErrors.append(errorEntry.getKey() + " : " + errorEntry.getValue().getMessage());
            }
            LOGGER.warn(saved.toString());
            LOGGER.warn(logErrors.toString());
        }
        return saved;
    }

    private DataDefinition getLogDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_LOG);
    }

    private Entity findBotUser() {
        DataDefinition userDD = dataDefinitionService.get(QcadooSecurityConstants.PLUGIN_IDENTIFIER,
                QcadooSecurityConstants.MODEL_USER);

        Entity user = userDD.find().add(SearchRestrictions.eq(UserFields.USER_NAME, QCADOO_BOT)).uniqueResult();

        if (user == null) {
            throw new RuntimeException("User qcadoo_bot not found.");
        }

        return user;
    }

    private void validate(Builder builder) {
        if (Strings.isNullOrEmpty(builder.type) || Strings.isNullOrEmpty(builder.action) || builder.logLevel == null) {
            throw new IllegalStateException("Type, action and logLevel is required!");
        }
    }

    public void deleteOldLogsTrigger() {
        multiTenantService.doInMultiTenantContext(new MultiTenantCallback() {

            @Override
            public void invoke() {
                deleteOldLogs();
            }

        });
    }

    private void deleteOldLogs() {
        String sqlForViewed = "DELETE FROM basic_viewedactivity " +
                "WHERE log_id IN (SELECT id FROM basic_log WHERE createtime < (now() - interval '1 month'))";

        String sql = "DELETE FROM basic_log WHERE createtime < (now() - interval '1 month');";

        Map<String, Object> params = Maps.newHashMap();

        jdbcTemplate.update(sqlForViewed, params);
        jdbcTemplate.update(sql, params);
    }

}
