package com.qcadoo.mes.basic.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qcadoo.mes.basic.controllers.dataProvider.DataProvider;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.DataResponse;

@Controller
public final class BasicApiController {

    @Autowired
    private DataProvider dataProvider;

    @ResponseBody
    @RequestMapping(value = "/products", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataResponse getProductsByQuery(@RequestParam("query") String query) {
        return dataProvider.getProductsResponseByQuery(query);
    }

    @ResponseBody
    @RequestMapping(value = "/additionalcodes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataResponse getAdditionalCodesByQuery(@RequestParam("query") String query, @RequestParam(required = false, value = "productnumber") String productnumber) {
        return dataProvider.getAdditionalCodesResponseByQuery(query, productnumber);
    }

    @ResponseBody
    @RequestMapping(value = "/palletnumbers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataResponse getPalletNumbersByQuery(@RequestParam("query") String query) {
        return dataProvider.getPalletNumbersResponseByQuery(query);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/units")
    public List<Map<String, String>> getUnits() {
        return dataProvider.getUnits();
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/typeOfPallets")
    public List<Map<String, String>> getTypeOfPallets() {
        return dataProvider.getTypeOfPallets();
    }
}
