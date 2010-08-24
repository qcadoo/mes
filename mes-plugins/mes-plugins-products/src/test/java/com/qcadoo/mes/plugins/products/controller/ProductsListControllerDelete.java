package com.qcadoo.mes.plugins.products.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.core.data.api.DataAccessService;

public class ProductsListControllerDelete {

    private ProductsListController controller;

    private DataAccessService dasMock;

    @Before
    public void setUp() {
        dasMock = mock(DataAccessService.class);
        controller = new ProductsListController(null, dasMock);
        controller.setPrintException(false);
    }

    @Test
    public void shouldDeleteAndReturnOkWhenListOfOneData() {
        // given

        // when
        String r = controller.deleteData(Arrays.asList(new String[] { "123" }));

        // then
        assertEquals("ok", r);
        verify(dasMock).delete("product", (long) 123);
        verifyNoMoreInteractions(dasMock);
    }

    @Test
    public void shouldDeleteAndReturnOkWhenListOfThreeData() {
        // given

        // when
        String r = controller.deleteData(Arrays.asList(new String[] { "1", "2", "3" }));

        // then
        assertEquals("ok", r);
        verify(dasMock).delete("product", (long) 1);
        verify(dasMock).delete("product", (long) 3);
        verify(dasMock).delete("product", (long) 2);
        verifyNoMoreInteractions(dasMock);
    }

    @Test
    public void shouldDoNothingAndReturnOkWhenEmptyList() {
        // given

        // when
        String r = controller.deleteData(Arrays.asList(new String[] {}));

        // then
        assertEquals("ok", r);
        verifyNoMoreInteractions(dasMock);
    }

    @Test
    public void shouldDoNothingAndReturnErrorWhenListWithOneString() {
        // given

        // when
        String r = controller.deleteData(Arrays.asList(new String[] { "aa" }));

        // then
        assertEquals("error", r);
        verifyNoMoreInteractions(dasMock);
    }

    @Test
    public void shouldDoNothingAndReturnErrorWhenListWithTwoNumbersAndOneString() {
        // given

        // when
        String r = controller.deleteData(Arrays.asList(new String[] { "1", "2", "a" }));

        // then
        assertEquals("error", r);
        verifyNoMoreInteractions(dasMock);
    }

}
