package com.qcadoo.mes.materialFlowResources.palletBalance;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.TypeOfLoadUnitFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PalletBalanceReportHelper {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<String> getTypesOfLoadUnit() {
        return getTypeOfLoadUnitDD().find().add(SearchRestrictions.eq("active", true)).list().getEntities().stream().map(e -> e.getStringField(TypeOfLoadUnitFields.NAME)).collect(Collectors.toList());
    }

    public Map<Date, List<PalletBalanceRowDto>> getCurrentState(Date dateTo) {
        StringBuilder query = new StringBuilder();
        query.append("select typeofloadunit.name as typeOfLoadUnit, count(distinct p.number) as palletsCount, current_date AS day ");
        query.append("  from materialflowresources_resource r ");
        query.append("  join basic_palletnumber p on r.palletnumber_id = p.id ");
        query.append(" left join basic_typeofloadunit typeofloadunit on typeofloadunit.id = r.typeofloadunit_id ");
        query.append("group by typeofloadunit.name");

        List<PalletBalanceRowDto> results = jdbcTemplate.query(query.toString(), new BeanPropertyRowMapper<>(
                PalletBalanceRowDto.class));

        Map<Date, List<PalletBalanceRowDto>> map = Maps.newHashMap();
        map.put(dateTo, results);
        return map;
    }

    public void fillFinalAndInitialState(List<String> typesOfLoadUnit, Map<Date, List<PalletBalanceRowDto>> finalState,
                                         Map<Date, List<PalletBalanceRowDto>> initialState,
                                         Map<Date, List<PalletBalanceRowDto>> inbounds,
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
                    calculateInitialState(typesOfLoadUnit, current, finalState.get(current), inbounds.get(current),
                            outbounds.get(current)));

            currentDate = currentDate.minusDays(1);
        }
    }

    private List<PalletBalanceRowDto> calculateInitialState(List<String> typesOfLoadUnit, Date date,
                                                            List<PalletBalanceRowDto> finalStateRow,
                                                            List<PalletBalanceRowDto> inboundsRow,
                                                            List<PalletBalanceRowDto> outboundsRow) {

        List<PalletBalanceRowDto> initialStateRow = Lists.newArrayList();
        typesOfLoadUnit.forEach(type -> {
            PalletBalanceRowDto finalPalletState = finalStateRow.stream().filter(dto -> type.equals(dto.getTypeOfLoadUnit()))
                    .findAny().orElse(new PalletBalanceRowDto(type, date, 0));
            PalletBalanceRowDto initialPalletState = new PalletBalanceRowDto();
            String typeOfLoadUnit = finalPalletState.getTypeOfLoadUnit();
            initialPalletState.setDay(finalPalletState.getDay());
            initialPalletState.setTypeOfLoadUnit(typeOfLoadUnit);
            int inboundForPallet = getPalletsCountForType(inboundsRow, typeOfLoadUnit);
            int outboundForPallet = getPalletsCountForType(outboundsRow, typeOfLoadUnit);
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
        PalletBalanceRowDto stateForDay = source.stream().filter(dto -> type.equals(dto.getTypeOfLoadUnit())).findAny()
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
        query.append("select typeofloadunit.name as typeOfLoadUnit, date_trunc('day', d.time) as day, count(distinct pn.number) as palletsCount ");
        query.append("  from materialflowresources_position p ");
        query.append("  join basic_palletnumber pn on p.palletnumber_id = pn.id ");
        query.append("  join materialflowresources_document d on p.document_id = d.id ");
        query.append("LEFT JOIN basic_typeofloadunit typeofloadunit ");
        query.append("ON typeofloadunit.id = p.typeofloadunit_id ");
        query.append("      where d.type in ('01receipt','02internalInbound') ");
        query.append("      and d.state ='02accepted' ");
        query.append("      and date_trunc('day', d.time) >= :dateFrom ");
        query.append("group by typeofloadunit.name, date_trunc('day',d.time)");

        Map<String, Object> params = Maps.newHashMap();
        params.put("dateFrom", dateFrom);

        List<PalletBalanceRowDto> results = jdbcTemplate.query(query.toString(), params, new BeanPropertyRowMapper<>(
                PalletBalanceRowDto.class));

        return mapQueryResults(results);
    }

    public Map<Date, List<PalletBalanceRowDto>> getOutbounds(final Date dateFrom) {
        StringBuilder query = new StringBuilder();
        query.append("select date_trunc('day',pn.issuedatetime) as day, typeofloadunit.name as typeOfLoadUnit, count(distinct pn.number) as palletsCount ");
        query.append("  from basic_palletnumber pn ");
        query.append("  join materialflowresources_position p on p.palletnumber_id = pn.id ");
        query.append("  join materialflowresources_document d on p.document_id = d.id ");
        query.append("LEFT JOIN basic_typeofloadunit typeofloadunit ");
        query.append("ON typeofloadunit.id = p.typeofloadunit_id ");
        query.append("      where date_trunc('day',pn.issuedatetime) >= :dateFrom ");
        query.append("      and d.type in ('03internalOutbound','04release')");
        query.append("group by date_trunc('day',pn.issuedatetime), typeofloadunit.name");

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


    private DataDefinition getTypeOfLoadUnitDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_TYPE_OF_LOAD_UNIT);
    }

}
