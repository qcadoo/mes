package com.qcadoo.mes.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ResourcesController {

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "js/{pluginIdentifier}/**", method = RequestMethod.GET)
    public void getMainView(@PathVariable("pluginIdentifier") final String pluginIdentifier, HttpServletRequest request,
            HttpServletResponse response) {

        System.out.println("-------");
        System.out.println(pluginIdentifier + " - " + request.getRequestURI());

        Resource r = applicationContext.getResource("classpath:META-INF" + request.getRequestURI());

        if (r != null) {
            System.out.println("FOUND");

            response.setContentType("text/javascript");

            try {
                copy(r.getInputStream(), response.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private static final int IO_BUFFER_SIZE = 4 * 1024;

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }
}
