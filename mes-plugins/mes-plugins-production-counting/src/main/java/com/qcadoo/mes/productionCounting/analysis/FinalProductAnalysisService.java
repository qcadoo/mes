package com.qcadoo.mes.productionCounting.analysis;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.productionCounting.analysis.dto.FinalProductAnalysisDto;
import com.qcadoo.mes.productionCounting.constants.FinalProductAnalysisEntryFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class FinalProductAnalysisService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public List<Entity> generateAnalysisForFinalProducts(final Entity generator) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT pl.number AS productionLineNumber, ord.number AS orderNumber, c.number AS companyNumber, assortment.name AS assortmentName, ");
        query.append("  product.number AS productNumber,product.name AS productName, product.unit AS productUnit, product.size AS size, ");
        query.append("  SUM(COALESCE(topoc.usedquantity, 0)) AS quantity, SUM(COALESCE(topoc.wastesquantity,0)) AS wastes, ");
        query.append("  SUM(COALESCE(topoc.usedquantity, 0)) + SUM(COALESCE(topoc.wastesquantity,0)) AS doneQuantity, ");
        query.append("  date_trunc('day',pt.timerangefrom) AS timeRangeFrom, date_trunc('day',pt.timerangeto) AS timeRangeTo, ");
        query.append("  shift.name AS shiftName, tcontext.number AS technologyGeneratorNumber ");
        query.append("FROM productioncounting_trackingoperationproductoutcomponent topoc ");
        query.append("  JOIN productioncounting_productiontracking pt ON pt.id = topoc.productiontracking_id ");
        query.append("  JOIN orders_order ord ON ord.id = pt.order_id ");
        query.append("  JOIN basic_product product ON topoc.product_id = product.id ");
        query.append("  JOIN technologies_technology technology ON technology.id = ord.technologyprototype_id ");
        query.append("  LEFT JOIN basic_shift shift ON pt.shift_id = shift.id ");
        query.append("  LEFT JOIN basic_assortment assortment ON product.assortment_id = assortment.id ");
        query.append("  LEFT JOIN productionlines_productionline pl ON ord.productionline_id = pl.id ");
        query.append("  LEFT JOIN basic_company c ON c.id = ord.company_id ");
        query.append("  LEFT JOIN technologiesgenerator_generatortechnologiesforproduct tgenn ON technology.id = tgenn.technology_id ");
        query.append("  LEFT JOIN technologiesgenerator_generatorcontext tcontext ON tcontext.id = tgenn.generatorcontext_id ");
        query.append("  WHERE pt.state = '02accepted' AND ord.parent_id IS NULL ");
        query.append("GROUP BY ord.number, shift.name, date_trunc('day',pt.timerangefrom), date_trunc('day',pt.timerangeto), productionLineNumber, ");
        query.append("  companyNumber, assortmentName, productNumber, productName, productUnit, size, technologyGeneratorNumber");
        Map<String, Object> params = Maps.newHashMap();

        List<FinalProductAnalysisDto> queryResult = jdbcTemplate.query(query.toString(), params, new BeanPropertyRowMapper<>(
                FinalProductAnalysisDto.class));
        DataDefinition dataDefinition = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_FINAL_PRODUCT_ANALYSIS_ENTRY);
        return queryResult.stream().map(dto -> createAnalysisEntryFromDto(dto, dataDefinition, generator))
                .collect(Collectors.toList());

    }

    private Entity createAnalysisEntryFromDto(final FinalProductAnalysisDto dto, final DataDefinition dataDefinition,
            final Entity generator) {
        Entity entry = dataDefinition.create();

        entry.setField(FinalProductAnalysisEntryFields.PRODUCION_LINE_NUMBER, dto.getProductionLineNumber());
        entry.setField(FinalProductAnalysisEntryFields.COMPANY_NUMBER, dto.getCompanyNumber());
        entry.setField(FinalProductAnalysisEntryFields.ASSORTMENT_NAME, dto.getAssortmentName());
        entry.setField(FinalProductAnalysisEntryFields.PRODUCT_NUMBER, dto.getProductNumber());
        entry.setField(FinalProductAnalysisEntryFields.PRODUCT_NAME, dto.getProductName());
        entry.setField(FinalProductAnalysisEntryFields.PRODUCT_UNIT, dto.getProductUnit());
        entry.setField(FinalProductAnalysisEntryFields.SHIFT_NAME, dto.getShiftName());
        entry.setField(FinalProductAnalysisEntryFields.DONE_QUANTITY, dto.getDoneQuantity());
        entry.setField(FinalProductAnalysisEntryFields.ORDER_NUMBER, dto.getOrderNumber());
        entry.setField(FinalProductAnalysisEntryFields.QUANTITY, dto.getQuantity());
        entry.setField(FinalProductAnalysisEntryFields.WASTES, dto.getWastes());
        entry.setField(FinalProductAnalysisEntryFields.TECHNOLOGY_GENERATOR_NUMBER, dto.getProductName());
        entry.setField(FinalProductAnalysisEntryFields.SIZE, dto.getSize());
        entry.setField(FinalProductAnalysisEntryFields.TIME_RANGE_FROM, dto.getTimeRangeFrom());
        entry.setField(FinalProductAnalysisEntryFields.TIME_RANGE_TO, dto.getTimeRangeTo());
        entry.setField(FinalProductAnalysisEntryFields.FINAL_PRODUCT_ANALYSIS_GENERATOR, generator);
        return entry;
    }
}
