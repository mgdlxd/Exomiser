/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.analysis.Settings;
import org.apache.commons.cli.Option;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhiveOptionMarshallerTest {
    
    private HiPhiveOptionMarshaller instance;
    
    @Before
    public void setUp() {
        instance = new HiPhiveOptionMarshaller();
    }
    
    @Test
    public void testCommandLineValue() {
        assertThat(instance.getCommandLineParameter(), equalTo("hiphive-params"));
    }
    
    @Test
    public void testThatOptionAcceptsMultipleValues() {
        Option option = instance.option;
        assertThat(option.hasArgs(), is(true));
        assertThat(option.getValueSeparator(), equalTo(','));
    }
    
    @Test
    public void testApplyValuesToSettingsBuilder() {
        String[] values = {"human","mouse","ppi"};
        
        Settings.SettingsBuilder settingsBuilder = new Settings.SettingsBuilder();
        instance.applyValuesToSettingsBuilder(values, settingsBuilder);
        Settings settings = settingsBuilder.build();
        
        assertThat(settings.getExomiser2Params(), equalTo("human,mouse,ppi"));    
    }
    
    @Test
    public void testApplyValuesToSettingsBuilderWithNoInput() {
        String[] values = {};
        
        Settings.SettingsBuilder settingsBuilder = new Settings.SettingsBuilder();
        instance.applyValuesToSettingsBuilder(values, settingsBuilder);
        Settings settings = settingsBuilder.build();
        
        assertThat(settings.getExomiser2Params(), equalTo(""));    
    }
    
}
