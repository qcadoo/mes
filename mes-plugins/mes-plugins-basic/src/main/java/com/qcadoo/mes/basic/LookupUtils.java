package com.qcadoo.mes.basic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

@Service
public class LookupUtils {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public <R> GridResponse<R> getGridResponse(String query, String sidx, String sord, Integer page, int perPage, R recordExample) {
        return getGridResponse(query, sidx, sord, page, perPage, recordExample, new HashMap<>());
    }

    public <R> GridResponse<R> getGridResponse(String query, String sidx, String sord, Integer page, int perPage, R recordExample, Map<String, Object> parameters) {
        sidx = sidx != null ? sidx.toLowerCase() : "";
        sord = sord != null ? sord.toLowerCase() : "";

        Preconditions.checkState(Arrays.asList("asc", "desc", "").contains(sord));
        Preconditions.checkState(Arrays.asList(recordExample.getClass().getDeclaredFields()).stream().map(Field::getName).map(String::toLowerCase).collect(Collectors.toList()).contains(sidx));

        query += addQueryWhereForObject(recordExample);

        parameters.putAll(getParametersForObject(recordExample));

        String queryCount = String.format(query, "COUNT(*)", "");
        String queryRecords = String.format(query, "*", "ORDER BY " + sidx + " " + sord) + String.format(" LIMIT %d OFFSET %d", perPage, perPage * (page - 1));

        Integer countRecords = jdbcTemplate.queryForObject(queryCount, parameters, Long.class).intValue();
        List<R> records = jdbcTemplate.query(queryRecords, parameters, new BeanPropertyRowMapper(recordExample.getClass()));

        return new GridResponse<>(page, Double.valueOf(Math.ceil((1.0 * countRecords) / perPage)).intValue(), countRecords, records);
    }

    private String addQueryWhereForObject(Object object) {
        List<String> items = new ArrayList<>();

        if (object != null) {
            for (Field field : object.getClass().getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(object);
                    if (value != null) {
                        if (value instanceof Number) {
                            items.add(String.format("%s = :%s", field.getName(), field.getName()));

                        } else if (value instanceof Date) {
                            items.add(String.format("%s = :%s", field.getName(), field.getName()));

                        } else if (value instanceof Boolean) {
                            items.add(String.format("%s = :%s", field.getName(), field.getName()));

                        } else if (value instanceof String) {
                            SearchAttribute.SEARCH_TYPE searchType = field.isAnnotationPresent(SearchAttribute.class) ? field.getAnnotation(SearchAttribute.class).searchType() : SearchAttribute.SEARCH_TYPE.LIKE;

                            if (searchType == SearchAttribute.SEARCH_TYPE.EXACT_MATCH) {
                                items.add(String.format("lower(%s) = lower(:%s)", field.getName(), field.getName()));

                            } else {
                                items.add(String.format("lower(%s) like lower(:%s)", field.getName(), field.getName()));
                            }
                        }
                    }

                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        String where = "";

        if (!items.isEmpty()) {
            where = " WHERE " + items.stream().collect(Collectors.joining(" AND "));
        }

        return where;
    }

    private Map<String, Object> getParametersForObject(Object object) {
        Map<String, Object> parameters = new HashMap<>();

        if (object != null) {
            for (Field field : object.getClass().getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(object);
                    if (value != null) {
                        if (value instanceof String) {
                            SearchAttribute.SEARCH_TYPE searchType = field.isAnnotationPresent(SearchAttribute.class) ? field.getAnnotation(SearchAttribute.class).searchType() : SearchAttribute.SEARCH_TYPE.LIKE;

                            if (searchType == SearchAttribute.SEARCH_TYPE.EXACT_MATCH) {
                                parameters.put(field.getName(), value);

                            } else {
                                parameters.put(field.getName(), "%" + value + "%");
                            }

                        } else {
                            parameters.put(field.getName(), value);
                        }
                    }

                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        return parameters;
    }
}
