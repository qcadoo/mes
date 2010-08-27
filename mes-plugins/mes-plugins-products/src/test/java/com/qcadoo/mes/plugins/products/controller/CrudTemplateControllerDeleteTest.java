package com.qcadoo.mes.plugins.products.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.qcadoo.mes.core.data.api.DataAccessService;

public class CrudTemplateControllerDeleteTest {

    private CRUD controller;

    private DataAccessService dasMock;

    @Before
    public void setUp() {
        dasMock = mock(DataAccessService.class);
        controller = new CRUD(dasMock);
    }

    @Test
    public void shouldDeleteAndReturnOkWhenListOfOneData() {
        // given

        // when
        String r = controller.deleteEntity(Arrays.asList(new Integer[] { 123 }), "products.product");

        // then
        assertEquals("ok", r);
        verify(dasMock).delete("products.product", (long) 123);
        verifyNoMoreInteractions(dasMock);
    }

    @Test
    public void shouldDeleteAndReturnOkWhenListOfThreeData() {
        // given

        // when
        String r = controller.deleteEntity(Arrays.asList(new Integer[] { 1, 2, 3 }), "products.product");

        // then
        assertEquals("ok", r);
        verify(dasMock).delete("products.product", (long) 1);
        verify(dasMock).delete("products.product", (long) 3);
        verify(dasMock).delete("products.product", (long) 2);
        verifyNoMoreInteractions(dasMock);
    }

    @Test
    public void shouldDoNothingAndReturnOkWhenEmptyList() {
        // given

        // when
        String r = controller.deleteEntity(Arrays.asList(new Integer[] {}), "products.product");

        // then
        assertEquals("ok", r);
        verifyNoMoreInteractions(dasMock);
    }

    private class CRUD extends CrudTemplate {

        public CRUD(DataAccessService das) {
            super(null, das, LoggerFactory.getLogger(CrudTemplateControllerDeleteTest.class), null);
        }
    }
}
