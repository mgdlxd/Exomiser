/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.filters;

import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FilterTypeTest {
    
    public FilterTypeTest() {
    }

    /**
     * Test of valueOf method, of class FilterType.
     */
    @Test
    public void testValueOf() {
        String name = "FREQUENCY_FILTER";
        FilterType expResult = FilterType.FREQUENCY_FILTER;
        FilterType result = FilterType.valueOf(name);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGenePriorityScoreFilter() {
        FilterType filterType = FilterType.PRIORITY_SCORE_FILTER;
        String name = "PRIORITY_SCORE_FILTER";
        assertThat(FilterType.valueOf(name), equalTo(filterType));
        assertThat(filterType.toString(), equalTo("Gene priority score"));
    }
}
