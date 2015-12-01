package com.qcadoo.mes.materialFlowResources.mappers;

import com.qcadoo.mes.materialFlowResources.DocumentPositionDTO;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.jdbc.support.JdbcUtils;

public class DocumentPositionMapperTest {

    private static DocumentPositionMapper documentPositionMapper;
    private static DocumentPositionDTO documentPositionVO;
    private static MockedResultSet resultSet;
    

    @BeforeClass
    public static void setUpClass() {

        documentPositionMapper = new DocumentPositionMapper();
        documentPositionVO = getDocumentPositionVO();
resultSet = getResultSet();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    private static DocumentPositionDTO getDocumentPositionVO() {
//        try {
            DocumentPositionDTO vo = new DocumentPositionDTO();
//            vo.setAdditional_code_id(101L);
//            vo.setConversion(BigDecimal.valueOf(102L));
//            vo.setExpirationdate(new SimpleDateFormat().parse("2001-11-12"));
//            vo.setGivenquantity(BigDecimal.valueOf(103L));
//            vo.setGivenunit("givenUnit");
//            vo.setId(104L);
//            vo.setPallet_id(105L);
//            vo.setProduct(106L);
//            vo.setQuantity(BigDecimal.valueOf(107L));
//            vo.setResource_id(108L);
//            vo.setStorage_location_id(109L);
//            vo.setType_of_pallet("type_of_pallet");

            return vo;

//        } catch (ParseException ex) {
//            throw new RuntimeException(ex);
//        }
    }

    private static MockedResultSet getResultSet() {
        MockedResultSet resultSet = new MockedResultSet();
//        
//        resultSet.getData().put(null, resultSet);
//            vo.setAdditional_code_id(101L);
//            vo.setConversion(BigDecimal.valueOf(102L));
//            vo.setExpirationdate(new SimpleDateFormat().parse("2001-11-12"));
//            vo.setGivenquantity(BigDecimal.valueOf(103L));
//            vo.setGivenunit("givenUnit");
//            vo.setId(104L);
//            vo.setPallet_id(105L);
//            vo.setProduct_id(106L);
//            vo.setQuantity(BigDecimal.valueOf(107L));
//            vo.setResource_id(108L);
//            vo.setStorage_location_id(109L);
//            vo.setType_of_pallet("type_of_pallet");
        
        return resultSet;
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

//    @Test
    public void testMapRow() throws Exception {
//        ResultSet resultSet = null;
//
//        DocumentPositionDTO expResult = null;
//        DocumentPositionDTO result = documentPositionMapper.mapRow(resultSet, i);
//
//        assertEquals(expResult, result);
    }

//    @Test
    public void testMapVoToParams() {
//        DocumentPositionDTO vo = null;
//        Map<String, Object> expResult = null;
//        Map<String, Object> result = instance.mapVoToParams(vo);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

}
