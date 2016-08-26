package com.qcadoo.mes.materialFlowResources.controllers;

import com.qcadoo.mes.basic.GridResponse;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ProductDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.DataResponse;
import com.qcadoo.mes.materialFlowResources.DocumentPositionDTO;
import com.qcadoo.mes.materialFlowResources.DocumentPositionService;
import com.qcadoo.mes.materialFlowResources.ResourceDTO;
import com.qcadoo.mes.materialFlowResources.StorageLocationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

@Controller
@RequestMapping("/rest/documentPositions")
public class DocumentPositionsController {

    @Autowired
    private DocumentPositionService documentPositionRepository;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "{id}")
    public GridResponse<DocumentPositionDTO> findAll(@PathVariable Long id, @RequestParam String sidx, @RequestParam String sord,
            @RequestParam(defaultValue = "1", required = false, value = "page") Integer page,
            @RequestParam(value = "rows") int perPage, DocumentPositionDTO positionDTO) {

        return documentPositionRepository.findAll(id, sidx, sord, page, perPage, positionDTO);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "units/{number}")
    public Map<String, Object> getUnitsForProduct(@PathVariable String number) {
        return documentPositionRepository.unitsOfProduct(number);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "product/{number}")
    public ProductDTO getProductForProductNumber(@PathVariable String number) {
        return documentPositionRepository.getProductForProductNumber(number);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.PUT)
    public void create(@RequestBody DocumentPositionDTO documentPositionVO) {
        documentPositionRepository.create(documentPositionVO);
        documentPositionRepository.updateDocumentPositionsNumbers(documentPositionVO.getDocument());
    }

    @ResponseBody
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable Long id) {
        Long documentId = documentPositionRepository.findDocumentByPosition(id);
        documentPositionRepository.delete(id);
        documentPositionRepository.updateDocumentPositionsNumbers(documentId);
    }

    @ResponseBody
    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public void update(@PathVariable Long id, @RequestBody DocumentPositionDTO documentPositionVO) {
        documentPositionRepository.update(id, documentPositionVO);
        documentPositionRepository.updateDocumentPositionsNumbers(documentPositionVO.getDocument());
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "storagelocations")
    public DataResponse getStorageLocations(@RequestParam("query") String query, @RequestParam("product") String product,
            @RequestParam("location") String location) {
        return documentPositionRepository.getStorageLocationsResponse(query, product, location);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "storageLocation//{document}")
    public StorageLocationDTO getStorageLocationForEmptyProduct(@PathVariable String document) {
        return null;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "storageLocation/{product}/{document}")
    public StorageLocationDTO getStorageLocationForProductAndWarehouse(@PathVariable String product, @PathVariable String document) {
        return documentPositionRepository.getStorageLocation(product, document);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "resource")
    public ResourceDTO getResourceForProduct(@RequestParam("context") Long document, @RequestParam("product") String product,
            @RequestParam("conversion") BigDecimal conversion, @RequestParam("ac") String additionalCode) {
        return documentPositionRepository.getResource(document, product, conversion, additionalCode);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "resources")
    public DataResponse getResources(@RequestParam("query") String query, @RequestParam("product") String product,
            @RequestParam("conversion") BigDecimal conversion, @RequestParam("context") Long document,
            @RequestParam("ac") String additionalCode) {
        return documentPositionRepository.getResourcesResponse(document, query, product, conversion, additionalCode);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "resourceByNumber/{document}/{resource}")
    public ResourceDTO getBatchForResource(@PathVariable Long document, @PathVariable String resource)
            throws UnsupportedEncodingException {
        String decodedResource = URLDecoder.decode(resource, "UTF-8");
        return documentPositionRepository.getResourceByNumber(decodedResource);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "productFromLocation/{location}")
    public ProductDTO getProductFromLocation(@PathVariable String location) {
        return documentPositionRepository.getProductFromLocation(location);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "gridConfig/{id}")
    public Map<String, Object> gridConfig(@PathVariable Long id) {
        return documentPositionRepository.getGridConfig(id);
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder, Locale locale, HttpServletRequest request) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
        dataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }
}
