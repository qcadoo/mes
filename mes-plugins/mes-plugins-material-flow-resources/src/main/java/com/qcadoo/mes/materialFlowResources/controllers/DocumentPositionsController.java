package com.qcadoo.mes.materialFlowResources.controllers;

import com.qcadoo.mes.basic.GridResponse;
import com.qcadoo.mes.materialFlowResources.DocumentPositionDTO;
import com.qcadoo.mes.materialFlowResources.DocumentPositionService;
import com.qcadoo.mes.materialFlowResources.StorageLocationDTO;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;

@Controller
@RequestMapping("/integration/rest/documentPositions")
public class DocumentPositionsController {

    @Autowired
    private DocumentPositionService documentPositionRepository;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "{id}")
    public GridResponse<DocumentPositionDTO> findAll(@PathVariable Long id, @RequestParam String sidx, @RequestParam String sord,
            @RequestParam(defaultValue = "1", required = false, value = "page") Integer page,
            @RequestParam(value = "rows") int perPage,
            DocumentPositionDTO positionDTO) {

        return documentPositionRepository.findAll(id, sidx, sord, page, perPage, positionDTO);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "units/{number}")
    public Map<String, Object> getUnitsForProduct(@PathVariable String number) {
        return documentPositionRepository.unitsOfProduct(number);
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
    public List<StorageLocationDTO> getStorageLocations(@RequestParam("query") String query) {
        return documentPositionRepository.getStorageLocations(query);
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
