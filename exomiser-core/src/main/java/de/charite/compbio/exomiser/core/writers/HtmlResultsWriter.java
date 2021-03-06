/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk7.Jdk7Module;
import de.charite.compbio.exomiser.core.analysis.Analysis;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.filters.FilterReport;
import de.charite.compbio.exomiser.core.model.Gene;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HtmlResultsWriter implements ResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(HtmlResultsWriter.class);

    private final TemplateEngine templateEngine;
    
    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.HTML;
    
    public HtmlResultsWriter(TemplateEngine templateEngine) {
        Locale.setDefault(Locale.UK);
        this.templateEngine = templateEngine;
    }

    @Override
    public void writeFile(Analysis analysis, OutputSettings settings) {

        String outFileName = ResultsWriterUtils.makeOutputFilename(analysis.getVcfPath(), settings.getOutputPrefix(), OUTPUT_FORMAT);
        Path outFile = Paths.get(outFileName);

        try (BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {

            writer.write(writeString(analysis, settings));

        } catch (IOException ex) {
            logger.error("Unable to write results to file {}.", outFileName, ex);
        }
        logger.info("{} results written to file {}.", OUTPUT_FORMAT, outFileName);

    }

    @Override
    public String writeString(Analysis analysis, OutputSettings settings) {
        Context context = new Context();
        //write the settings
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        //required for correct output of Path types
        mapper.registerModule(new Jdk7Module());
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
//        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        String jsonSettings = "";
        try {
            jsonSettings = mapper.writeValueAsString(analysis);
            jsonSettings += mapper.writeValueAsString(settings);
        } catch (JsonProcessingException ex) {
            logger.error("Unable to process JSON settings", ex);
        }
        context.setVariable("settings", jsonSettings);
        
        SampleData sampleData = analysis.getSampleData();
        //make the user aware of any unanalysed variants
        List<VariantEvaluation> unAnalysedVarEvals = sampleData.getUnAnnotatedVariantEvaluations();
        context.setVariable("unAnalysedVarEvals", unAnalysedVarEvals);
        
        //write out the analysis reports section
        List<FilterReport> analysisStepReports = makeAnalysisStepReports(analysis);
        context.setVariable("filterReports", analysisStepReports);
        //write out the variant type counters
        List<VariantEffectCount> variantTypeCounters = makeVariantEffectCounters(sampleData.getVariantEvaluations());
        List<String> sampleNames= sampleData.getSampleNames();
        String sampleName = "Anonymous";
        if(!sampleNames.isEmpty()) {
            sampleName = sampleNames.get(0);
        }
        context.setVariable("sampleName", sampleName);
        context.setVariable("sampleNames", sampleNames);
        context.setVariable("variantTypeCounters", variantTypeCounters);
                 
        List<Gene> passedGenes = ResultsWriterUtils.getMaxPassedGenes(sampleData.getGenes(), settings.getNumberOfGenesToShow());       
        context.setVariable("genes", passedGenes);
        
        return templateEngine.process("results", context);
    }

    protected List<VariantEffectCount> makeVariantEffectCounters(List<VariantEvaluation> variantEvaluations) {
        return ResultsWriterUtils.makeVariantEffectCounters(variantEvaluations);
    }
    
    protected List<FilterReport> makeAnalysisStepReports(Analysis analysis) {  
        return ResultsWriterUtils.makeFilterReports(analysis);   
    }

    //TODO:
//    /**
//     * This function writes out a table representing the PED file of the family
//     * being analysed (if a multisample VCF file is being analysed) or the name
//     * of the sample (for a single-sample VCF file).
//     * <P>
//     * For multisample VCF files, a color code is used to mark the following
//     * kinds of samples (individuals):
//     * <ul>
//     * <li>Unaffected parent: white</li>
//     * <li>Affected (whether parent or not): dark grey</li>
//     * <li>Unaffected sibling: light blue</li>
//     * </ul>
//     * The same color code will be used for showing the genotypes of the
//     * individual variants, which hopefully will help in their interpretation.
//     *
//     * @param out An open file handle (can come from the command line or server
//     * versions of Exomiser).
//     */
//    public void writePedigreeTable(Writer out) throws IOException {
//        int n = this.pedigree.getNumberOfIndividualsInPedigree();
//        if (n == 1) {
//            String sampleName = this.pedigree.getSingleSampleName();
//            out.write("<table class=\"pedigree\">\n");
//            out.write(String.format("<tr><td>Sample name: %s</td></tr>\n", sampleName));
//            out.write("</table>\n");
//        } else { /* multiple samples */
//
//            out.write("<h2>Analyzed samples</h2>\n");
//            out.write("<p>affected: red, parent of affected: light blue, unaffected: white</p>\n");
//            out.write("<table class=\"pedigree\">\n");
//            for (int i = 0; i < n; ++i) {
//                List<String> lst = this.pedigree.getPEDFileDatForNthPerson(i);
//                String fam = lst.get(0);
//                String id = lst.get(1);
//                String fathID = lst.get(2);
//                String mothID = lst.get(3);
//                String sex = lst.get(4);
//                String disease = lst.get(5);
//                out.write("<tr><td>" + fam + "</td>");
//                if (this.pedigree.isNthPersonAffected(i)) {
//                    out.write("<td id=\"g\">" + id + "</td>");
//                } else if (this.pedigree.isNthPersonParentOfAffected(i)) {
//                    out.write("<td id=\"b\">" + id + "</td>");
//                } else {
//                    out.write("<td id=\"w\">" + id + "</td>");
//                }
//                out.write("<td>" + fathID + "</td><td>" + mothID + "</td><td>"
//                        + sex + "</td><td>" + disease + "</td></tr>\n");
//            }
//            out.write("</table>\n");
//            out.write("<br/>\n");
//        }
//    }
    
}
