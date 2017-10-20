package com.qcadoo.mes.basic.activityStream;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.activityStream.model.ActivityDto;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.UserService;

@Service
public class ActivityStreamService {

    @Value("${activityStreamLimit:20}")
    private int activityStreamLimit;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private UserService userService;

    public List<ActivityDto> getActivityStream() {
        Entity currentUser = userService.getCurrentUserEntity();

        StringBuilder sql = new StringBuilder();

        sql.append("WITH viewed_activities(user_id, log_id) AS ( ");
        sql.append("    SELECT user_id, log_id FROM basic_viewedactivity ");
        sql.append("    WHERE user_id = :userId) ");
        sql.append(
                "SELECT log.id, log.logType AS type, log.createTime AS \"date\", log.message, (va.user_id IS NOT NULL) as viewed ");
        sql.append("FROM basic_log log ");
        sql.append("LEFT JOIN viewed_activities va ON log.id = va.log_id ");
        sql.append("WHERE log.logLevel = '07activity' ");
        sql.append("ORDER BY createtime DESC ");
        sql.append("LIMIT :activityStreamLimit");

        Map<String, Object> params = Maps.newHashMap();

        params.put("userId", currentUser.getId());
        params.put("activityStreamLimit", activityStreamLimit);

        List<ActivityDto> activities = jdbcTemplate.query(sql.toString(), params,
                BeanPropertyRowMapper.newInstance(ActivityDto.class));

        return activities;
    }

    public void markActivityAsViewed(final List<Integer> viewedActivities) {
        Entity currentUser = userService.getCurrentUserEntity();

        Long currentUserId = currentUser.getId();

        String values = viewedActivities.stream().map(va -> "(" + currentUserId.toString() + "," + va.toString() + ")")
                .collect(Collectors.toList()).stream().collect(Collectors.joining(","));

        StringBuilder sql = new StringBuilder();

        sql.append("WITH data(user_id, log_id) AS ( ");
        sql.append("VALUES ").append(values).append(" ) ");
        sql.append("INSERT INTO basic_viewedactivity (user_id, log_id) ");
        sql.append("SELECT d.user_id, d.log_id FROM data d ");
        sql.append(
                "WHERE NOT EXISTS (SELECT 1 FROM basic_viewedactivity va WHERE va.user_id = d.user_id AND va.log_id = d.log_id) ");

        Map<String, Object> params = Maps.newHashMap();

        jdbcTemplate.update(sql.toString(), params);
    }

}
