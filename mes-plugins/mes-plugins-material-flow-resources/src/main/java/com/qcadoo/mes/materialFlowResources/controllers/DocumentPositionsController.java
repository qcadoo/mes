package com.qcadoo.mes.materialFlowResources.controllers;

import com.qcadoo.mes.basic.ProductRepository;
import com.qcadoo.mes.basic.ProductVO;
import com.qcadoo.mes.materialFlowResources.DocumentPositionRepository;
import com.qcadoo.mes.materialFlowResources.DocumentPositionVO;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/integration/rest/documentPositions")
public class DocumentPositionsController {

    @Autowired
    private DocumentPositionRepository documentPositionRepository;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> findAll(@RequestParam String sidx, @RequestParam String sord) {
        return documentPositionRepository.findAll(sidx, sord);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void create(@RequestBody DocumentPositionVO documentPositionVO) {
        documentPositionRepository.create(documentPositionVO);
    }

    @ResponseBody
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable Long id) {
        documentPositionRepository.delete(id);
    }

    @ResponseBody
    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public void update(@PathVariable Long id, @RequestBody DocumentPositionVO documentPositionVO) {
        documentPositionRepository.update(id, documentPositionVO);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/types")
    public List<Map<String, String>> getTypes() {
        return documentPositionRepository.getTypes();
    }
}
