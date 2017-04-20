package com.qcadoo.mes.materialFlowResources.palletBalance;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.DictionaryService;

@Service
public class PalletBalanceReportHelper {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<String> getTypesOfPallet() {
        return dictionaryService.getKeys("typeOfPallet");
    }

    public Map<Date, List<PalletBalanceRowDto>> getCurrentState() {
        StringBuilder query = new StringBuilder();
        query.append("select r.typeofpallet as typeOfPallet, count(distinct p.number) as palletsCount, current_date AS day ");
        query.append("  from materialflowresources_resource r ");
        query.append("  join basic_palletnumber p on r.palletnumber_id = p.id ");
        query.append("group by r.typeofpallet");

        List<PalletBalanceRowDto> results = jdbcTemplate.query(query.toString(), new BeanPropertyRowMapper<>(
                PalletBalanceRowDto.class));

        Map<Date, List<PalletBalanceRowDto>> map = Maps.newHashMap();
        map.put(new Date(), results);
        return map;
    }

    public Map<Date, List<PalletBalanceRowDto>> getInbounds(final Date dateFrom) {
        StringBuilder query = new StringBuilder();
        query.append("select p.typeofpallet as typeOfPallet, date_trunc('day', d.time) as day, count(distinct pn.number) as palletsCount ");
        query.append("  from materialflowresources_position p ");
        query.append("  join basic_palletnumber pn on p.palletnumber_id = pn.id ");
        query.append("  join materialflowresources_document d on p.document_id = d.id ");
        query.append("      where d.type in ('01receipt','02internalInbound') ");
        query.append("      and date_trunc('day', d.time) >= :dateFrom ");
        query.append("group by p.typeofpallet, date_trunc('day',d.time)");

        Map<String, Object> params = Maps.newHashMap();
        params.put("dateFrom", dateFrom);

        List<PalletBalanceRowDto> results = jdbcTemplate.query(query.toString(), params, new BeanPropertyRowMapper<>(
                PalletBalanceRowDto.class));

        return mapQueryResults(results);
    }

    // TODO - zmienić metodę po zmianach w numerach palet
    public Map<Date, List<PalletBalanceRowDto>> getOutbounds(final Date dateFrom) {
        StringBuilder query = new StringBuilder();
        query.append("select p.typeofpallet as typeOfPallet, date_trunc('day', d.time) as day, count(distinct pn.number) as palletsCount ");
        query.append("  from materialflowresources_position p ");
        query.append("  join basic_palletnumber pn on p.palletnumber_id = pn.id ");
        query.append("  join materialflowresources_document d on p.document_id = d.id ");
        query.append("      where d.type in ('03internalOutbound','04release') ");
        query.append("      and date_trunc('day', d.time) >= :dateFrom ");
        query.append("group by p.typeofpallet, date_trunc('day',d.time)");

        Map<String, Object> params = Maps.newHashMap();
        params.put("dateFrom", dateFrom);

        List<PalletBalanceRowDto> results = jdbcTemplate.query(query.toString(), params, new BeanPropertyRowMapper<>(
                PalletBalanceRowDto.class));

        return mapQueryResults(results);
    }

    private Map<Date, List<PalletBalanceRowDto>> mapQueryResults(List<PalletBalanceRowDto> results) {
        Map<Date, List<PalletBalanceRowDto>> map = Maps.newHashMap();
        for (PalletBalanceRowDto dto : results) {
            Date day = dto.getDay();
            if (map.containsKey(day)) {
                map.get(day).add(dto);
            } else {
                map.put(day, Lists.newArrayList(dto));
            }
        }
        return map;
    }

}
