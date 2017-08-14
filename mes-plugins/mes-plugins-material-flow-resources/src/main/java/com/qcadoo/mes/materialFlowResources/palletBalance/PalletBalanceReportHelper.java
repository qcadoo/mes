package com.qcadoo.mes.materialFlowResources.palletBalance;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
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
        return dictionaryService.getActiveKeys("typeOfPallet");
    }

    public Map<Date, List<PalletBalanceRowDto>> getCurrentState(Date dateTo) {
        StringBuilder query = new StringBuilder();
        query.append("select r.typeofpallet as typeOfPallet, count(distinct p.number) as palletsCount, current_date AS day ");
        query.append("  from materialflowresources_resource r ");
        query.append("  join basic_palletnumber p on r.palletnumber_id = p.id ");
        query.append("group by r.typeofpallet");

        List<PalletBalanceRowDto> results = jdbcTemplate.query(query.toString(), new BeanPropertyRowMapper<>(
                PalletBalanceRowDto.class));

        Map<Date, List<PalletBalanceRowDto>> map = Maps.newHashMap();
        map.put(dateTo, results);
        return map;
    }

    public void fillFinalAndInitialState(List<String> typesOfPallet, Map<Date, List<PalletBalanceRowDto>> finalState,
            Map<Date, List<PalletBalanceRowDto>> initialState, Map<Date, List<PalletBalanceRowDto>> inbounds,
            Map<Date, List<PalletBalanceRowDto>> outbounds, Date dateFrom, Date dateTo) {
        DateTime currentDate = new DateTime(dateTo);

        while (currentDate.toDate().compareTo(dateFrom) >= 0) {
            Date current = currentDate.toDate();
            Date previousDate = currentDate.plusDays(1).toDate();

            List<PalletBalanceRowDto> previousInitialState = initialState.get(previousDate);
            if (previousInitialState != null) {
                finalState.put(current, previousInitialState);
            }
            initialState.put(
                    current,
                    calculateInitialState(typesOfPallet, current, finalState.get(current), inbounds.get(current),
                            outbounds.get(current)));

            currentDate = currentDate.minusDays(1);
        }
    }

    private List<PalletBalanceRowDto> calculateInitialState(List<String> typesOfPallet, Date date,
            List<PalletBalanceRowDto> finalStateRow, List<PalletBalanceRowDto> inboundsRow, List<PalletBalanceRowDto> outboundsRow) {

        List<PalletBalanceRowDto> initialStateRow = Lists.newArrayList();
        typesOfPallet.forEach(type -> {
            PalletBalanceRowDto finalPalletState = finalStateRow.stream().filter(dto -> type.equals(dto.getTypeOfPallet()))
                    .findAny().orElse(new PalletBalanceRowDto(type, date, 0));
            PalletBalanceRowDto initialPalletState = new PalletBalanceRowDto();
            String typeOfPallet = finalPalletState.getTypeOfPallet();
            initialPalletState.setDay(finalPalletState.getDay());
            initialPalletState.setTypeOfPallet(typeOfPallet);
            int inboundForPallet = getPalletsCountForType(inboundsRow, typeOfPallet);
            int outboundForPallet = getPalletsCountForType(outboundsRow, typeOfPallet);
            int finalForPallet = finalPalletState.getPalletsCount();
            initialPalletState.setPalletsCount(finalForPallet - inboundForPallet + outboundForPallet);
            initialStateRow.add(initialPalletState);
        });
        return initialStateRow;
    }

    private int getPalletsCountForType(List<PalletBalanceRowDto> source, String type) {
        if (source == null || source.isEmpty()) {
            return 0;
        }
        PalletBalanceRowDto stateForDay = source.stream().filter(dto -> type.equals(dto.getTypeOfPallet())).findAny()
                .orElse(new PalletBalanceRowDto());
        return stateForDay.getPalletsCount();
    }

    public Map<Date, Integer> getMoves(final Date dateFrom) {
        StringBuilder query = new StringBuilder();

        query.append("select date_trunc('day',d.time) as day, count(distinct sl.number) as palletsCount");
        query.append("  from materialflowresources_position p ");
        query.append("  join materialflowresources_storagelocation sl on p.storagelocation_id = sl.id ");
        query.append("  join materialflowresources_document d on p.document_id = d.id ");
        query.append("  join basic_palletnumber pn on pn.id = p.palletnumber_id ");
        query.append("where d.type in ('03internalOutbound','04release', '05transfer') and sl.highstoragelocation = true ");
        query.append("      and (pn.issuedatetime != d.time OR pn.issuedatetime is null) ");
        query.append("      and date_trunc('day', d.time) >= :dateFrom ");
        query.append("group by date_trunc('day',d.time);");

        Map<String, Object> params = Maps.newHashMap();
        params.put("dateFrom", dateFrom);
        Map<Date, Integer> result = jdbcTemplate.query(query.toString(), params, new ResultSetExtractor<Map<Date, Integer>>() {

            @Override
            public Map<Date, Integer> extractData(ResultSet rs) throws SQLException, DataAccessException {
                Map<Date, Integer> result = Maps.newHashMap();
                while (rs.next()) {
                    result.put(rs.getDate("day"), rs.getInt("palletsCount"));
                }
                return result;
            }
        });
        return result;
    }

    public Map<Date, List<PalletBalanceRowDto>> getInbounds(final Date dateFrom) {
        StringBuilder query = new StringBuilder();
        query.append("select p.typeofpallet as typeOfPallet, date_trunc('day', d.time) as day, count(distinct pn.number) as palletsCount ");
        query.append("  from materialflowresources_position p ");
        query.append("  join basic_palletnumber pn on p.palletnumber_id = pn.id ");
        query.append("  join materialflowresources_document d on p.document_id = d.id ");
        query.append("      where d.type in ('01receipt','02internalInbound') ");
        query.append("      and d.state ='02accepted' ");
        query.append("      and date_trunc('day', d.time) >= :dateFrom ");
        query.append("group by p.typeofpallet, date_trunc('day',d.time)");

        Map<String, Object> params = Maps.newHashMap();
        params.put("dateFrom", dateFrom);

        List<PalletBalanceRowDto> results = jdbcTemplate.query(query.toString(), params, new BeanPropertyRowMapper<>(
                PalletBalanceRowDto.class));

        return mapQueryResults(results);
    }

    public Map<Date, List<PalletBalanceRowDto>> getOutbounds(final Date dateFrom) {
        StringBuilder query = new StringBuilder();
        query.append("select date_trunc('day',pn.issuedatetime) as day, p.typeofpallet  as typeOfPallet, count(distinct pn.number) as palletsCount ");
        query.append("  from basic_palletnumber pn ");
        query.append("  join materialflowresources_position p on p.palletnumber_id = pn.id ");
        query.append("  join materialflowresources_document d on p.document_id = d.id ");
        query.append("      where date_trunc('day',pn.issuedatetime) >= :dateFrom ");
        query.append("      and d.type in ('03internalOutbound','04release')");
        query.append("group by date_trunc('day',pn.issuedatetime), p.typeofpallet");

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
