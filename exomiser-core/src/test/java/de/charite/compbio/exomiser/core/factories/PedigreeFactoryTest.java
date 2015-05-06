/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.factories.PedigreeFactory.PedigreeCreationException;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PedigreeFactoryTest {
    
    private PedigreeFactory instance;
    
    private static final Path nullPath = null;
    private static final Path validPedFilePath = Paths.get("src/test/resources/validPedTestFile.ped");
    private static final Path inValidPedFilePath = Paths.get("src/test/resources/invalidPedTestFile.ped");

    private SampleData singleSampleData;
    private SampleData multiSampleData;
    
    @Before
    public void setUp() {
        instance = new PedigreeFactory();
        singleSampleData = createUnNamedSingleSampleData();
        multiSampleData = new SampleData();
        multiSampleData.setNumberOfSamples(2);
    }

    @Test(expected = PedigreeCreationException.class)
    public void throwsErrorWhenSampleDataIsEmpty() {
        SampleData emptySampleData = new SampleData();
        instance.createPedigreeForSampleData(nullPath, emptySampleData);        
    }
    
    @Test
    public void createsSingleSamplePedigreeWithDefaultNameWhenSampleHasNoNameOrPedFile() {      
        Pedigree result = instance.createPedigreeForSampleData(nullPath, singleSampleData);
        assertThat(result.members.get(0).name, equalTo(PedigreeFactory.DEFAULT_SAMPLE_NAME));
    }
    
    @Test
    public void createsSingleSamplePedigreeWhenSampleHasOnlyOneNamedMemberAndNoPedFile() {

        String joeBloggs = "Joe Bloggs";
        singleSampleData.setSampleNames(Arrays.asList(joeBloggs));
                        
        Pedigree result = instance.createPedigreeForSampleData(nullPath, singleSampleData);        
        assertThat(result.members.get(0).name, equalTo(joeBloggs));
    }
    
    @Test(expected = PedigreeCreationException.class)
    public void throwsErrorWhenMultiSampleDataHasNoPedFile() {

        instance.createPedigreeForSampleData(nullPath, multiSampleData);        
    }
    
    @Test(expected = PedigreeCreationException.class)
    public void throwsErrorForNamedMultiSampleWithInvalidPedFile() {

        List<String> trioNames = Arrays.asList("Adam", "Eva", "Seth");
        multiSampleData.setSampleNames(trioNames);
                        
        Pedigree pedigree = instance.createPedigreeForSampleData(inValidPedFilePath, multiSampleData);        
        System.out.println(pedigree);
        assertThat(pedigree.members.size(), equalTo(trioNames.size()));
    }
    
    @Test
    public void createsPedigreeForNamedMultiSampleWithPedFile() {

        List<String> trioNames = Arrays.asList("Adam", "Eva", "Seth");
        multiSampleData.setSampleNames(trioNames);
                        
        Pedigree pedigree = instance.createPedigreeForSampleData(validPedFilePath, multiSampleData);        
        System.out.println(pedigree);
        assertThat(pedigree.members.size(), equalTo(trioNames.size()));
    }
    
    @Test
    public void createsPedigreeForNamedMultiSampleWithPedFileWithDisorderedNames() {

        List<String> trioNames = Arrays.asList("Adam", "Seth", "Eva");
        multiSampleData.setSampleNames(trioNames);
                        
        Pedigree pedigree = instance.createPedigreeForSampleData(validPedFilePath, multiSampleData);        
        System.out.println(pedigree);
        assertThat(pedigree.members.size(), equalTo(trioNames.size()));

        assertThat(pedigree.hasPerson("Adam"), is(true));
        assertThat(pedigree.nameToMember.get("Adam").person.isUnaffected(), is(true));
        assertThat(pedigree.nameToMember.get("Adam").person.isMale(), is(true));
        
        assertThat(pedigree.hasPerson("Eva"), is(true));
        assertThat(pedigree.nameToMember.get("Eva").person.isUnaffected(), is(true));
        assertThat(pedigree.nameToMember.get("Eva").person.isFemale(), is(true));

        assertThat(pedigree.hasPerson("Seth"), is(true));
        assertThat(pedigree.nameToMember.get("Seth").person.isUnaffected(), is(false));
        assertThat(pedigree.nameToMember.get("Seth").person.isMale(), is(true));
        
    }

    private SampleData createUnNamedSingleSampleData() {
        SampleData singleSampleData = new SampleData();
        singleSampleData.setNumberOfSamples(1);
        return singleSampleData;
    }
    
}