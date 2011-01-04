package com.qcadoo.mes.products;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.products.print.pdf.util.PdfUtil;

@Controller
public class ProductController {

    public static final String XLS_EXTENSION = ".xls";

    public static final String XLS_CONTENT_TYPE = "application/vnd.ms-excel";

    public static final String PDF_CONTENT_TYPE = "application/pdf";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private void sentFileAsAttachement(final String path, final String contentType, final HttpServletResponse response) {
        try {
            File file = new File(path);
            InputStream input = new FileInputStream(file);

            response.setContentType(contentType);
            response.setHeader("Content-disposition", "attachment; filename=" + file.getName());

            OutputStream output = response.getOutputStream();
            int bytes = IOUtils.copy(input, output);

            response.setContentLength(bytes);

            output.flush();

            IOUtils.closeQuietly(input);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @RequestMapping(value = "products/materialRequirement.pdf", method = RequestMethod.GET)
    public void materialRequirementPdf(@RequestParam("id") final String id, final HttpServletResponse response) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "materialRequirement");
        Entity materialRequirement = dataDefinition.get(Long.parseLong(id));
        sentFileAsAttachement(materialRequirement.getStringField("fileName") + PdfUtil.PDF_EXTENSION, PDF_CONTENT_TYPE, response);
    }

    @RequestMapping(value = "products/materialRequirement.xls", method = RequestMethod.GET)
    public void materialRequirementXls(@RequestParam("id") final String id, final HttpServletResponse response) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "materialRequirement");
        Entity materialRequirement = dataDefinition.get(Long.parseLong(id));
        sentFileAsAttachement(materialRequirement.getStringField("fileName") + XLS_EXTENSION, XLS_CONTENT_TYPE, response);
    }

    @RequestMapping(value = "products/order.pdf", method = RequestMethod.GET)
    public ModelAndView orderPdf(@RequestParam("id") final String id) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "order");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("orderPdfView");
        mav.addObject("entity", dataDefinition.get(Long.parseLong(id)));
        return mav;
    }

    @RequestMapping(value = "products/workPlanForWorker.pdf", method = RequestMethod.GET)
    public void workPlanForWorkerPdf(@RequestParam("id") final String id, final HttpServletResponse response) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "workPlan");
        Entity workPlan = dataDefinition.get(Long.parseLong(id));
        sentFileAsAttachement(workPlan.getStringField("fileName") + "for_worker" + PdfUtil.PDF_EXTENSION, PDF_CONTENT_TYPE,
                response);
    }

    @RequestMapping(value = "products/workPlanForProduct.pdf", method = RequestMethod.GET)
    public void workPlanForProductPdf(@RequestParam("id") final String id, final HttpServletResponse response) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "workPlan");
        Entity workPlan = dataDefinition.get(Long.parseLong(id));
        sentFileAsAttachement(workPlan.getStringField("fileName") + "for_product" + PdfUtil.PDF_EXTENSION, PDF_CONTENT_TYPE,
                response);
    }

    @RequestMapping(value = "products/workPlanForMachine.pdf", method = RequestMethod.GET)
    public void workPlanForMachinePdf(@RequestParam("id") final String id, final HttpServletResponse response) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "workPlan");
        Entity workPlan = dataDefinition.get(Long.parseLong(id));
        sentFileAsAttachement(workPlan.getStringField("fileName") + "for_machine" + PdfUtil.PDF_EXTENSION, PDF_CONTENT_TYPE,
                response);
    }

    @RequestMapping(value = "products/workPlanForWorker.xls", method = RequestMethod.GET)
    public void workPlanForWorkerXls(@RequestParam("id") final String id, final HttpServletResponse response) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "workPlan");
        Entity workPlan = dataDefinition.get(Long.parseLong(id));
        sentFileAsAttachement(workPlan.getStringField("fileName") + "for_worker" + XLS_EXTENSION, XLS_CONTENT_TYPE, response);
    }

    @RequestMapping(value = "products/workPlanForProduct.xls", method = RequestMethod.GET)
    public void workPlanForProductXls(@RequestParam("id") final String id, final HttpServletResponse response) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "workPlan");
        Entity workPlan = dataDefinition.get(Long.parseLong(id));
        sentFileAsAttachement(workPlan.getStringField("fileName") + "for_product" + XLS_EXTENSION, XLS_CONTENT_TYPE, response);
    }

    @RequestMapping(value = "products/workPlanForMachine.xls", method = RequestMethod.GET)
    public void workPlanForMachineXls(@RequestParam("id") final String id, final HttpServletResponse response) {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "workPlan");
        Entity workPlan = dataDefinition.get(Long.parseLong(id));
        sentFileAsAttachement(workPlan.getStringField("fileName") + "for_machine" + XLS_EXTENSION, XLS_CONTENT_TYPE, response);
    }

}
