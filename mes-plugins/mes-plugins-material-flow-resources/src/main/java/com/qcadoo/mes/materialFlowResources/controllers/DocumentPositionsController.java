package com.qcadoo.mes.materialFlowResources.controllers;

import com.qcadoo.mes.materialFlowResources.DocumentPositionDTO;
import com.qcadoo.mes.materialFlowResources.DocumentPositionService;
import com.qcadoo.mes.materialFlowResources.StorageLocationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/integration/rest/documentPositions")
public class DocumentPositionsController {
    
    @Autowired
    private DocumentPositionService documentPositionRepository;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "{id}")
    public List<DocumentPositionDTO> findAll(@PathVariable Long id, @RequestParam String sidx, @RequestParam String sord) {
        return documentPositionRepository.findAll(id, sidx, sord);
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
    public Map<String, Object> gridConfig(@PathVariable Long id){
        return documentPositionRepository.getGridConfig(id);
    }
}
