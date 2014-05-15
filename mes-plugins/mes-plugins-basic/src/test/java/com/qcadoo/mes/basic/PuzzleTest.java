package com.qcadoo.mes.basic;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Created by marcinkubala on 14.05.2014.
 */
public class PuzzleTest {

    @Test
    public final void should() {
        // given
        Map<String, List<Long>> origMap = ImmutableMap.<String, List<Long>> of("a", ImmutableList.of(1L, 2L, 3L));

        // when
        Map<String, Iterator<Long>> map1 = Maps.newHashMap();
        for (Map.Entry<String, List<Long>> mapEntry : origMap.entrySet()) {
            map1.put(mapEntry.getKey(), mapEntry.getValue().iterator());
        }

        Map<String, Iterator<Long>> map2 = Maps.transformValues(origMap, new Function<List<Long>, Iterator<Long>>() {

            @Override
            public Iterator<Long> apply(final List<Long> input) {
                return input.iterator();
            }
        });

        // then
        assertTrue(ObjectUtils.equals(1L, map1.get("a").next()));
        assertTrue(ObjectUtils.equals(2L, map1.get("a").next()));

        assertTrue(ObjectUtils.equals(1L, map2.get("a").next()));
        assertTrue(ObjectUtils.equals(2L, map2.get("a").next()));

        fail("Terrific failure!");
    }
}
