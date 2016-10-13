/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import de.charite.compbio.exomiser.core.prioritisers.util.PriorityService;
import de.charite.compbio.exomiser.core.prioritisers.util.TestPriorityServiceFactory;
import org.jblas.DoubleMatrix;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhivePriorityTest {
    
    private HiPhivePriority instance;
    
    private List<String> hpoIds;

    private PriorityService priorityService = TestPriorityServiceFactory.TEST_SERVICE;
    private DataMatrix testMatrix;

    @Before
    public void setUp() {
        hpoIds = getHpoIds();

        double[][] ppiMatrix = {
                {0.707653999329, 0.000000009625, 0.000000008875, 0.000000372898, 0.000000268611, 0.000000023074, 0.000000040680, 0.000000133227, 0.000000064774, 0.000000113817},
                {0.000000005477, 0.713751792908, 0.000008168789, 0.000000000210, 0.000000001862, 0.000000013144, 0.000000000679, 0.000000001696, 0.000000001134, 0.000000002901},
                {0.000000032869, 0.000053159707, 0.703132450581, 0.000000005321, 0.000001411398, 0.000000034059, 0.000000138069, 0.000000080616, 0.000000016616, 0.000001185472},
                {0.000001284835, 0.000000001273, 0.000000004950, 0.701313674450, 0.000000023567, 0.000000434552, 0.000001564053, 0.000000178326, 0.000000213718, 0.000000099803},
                {0.000000758085, 0.000000009233, 0.000001075537, 0.000000019304, 0.702176809311, 0.000000022661, 0.000000009667, 0.000000021993, 0.000000019951, 0.000000053246},
                {0.000000047331, 0.000000047378, 0.000000018865, 0.000000258712, 0.000000016471, 0.707689404488, 0.000006197341, 0.000000126649, 0.000000020872, 0.000000080030},
                {0.000000023518, 0.000000000690, 0.000000021553, 0.000000262441, 0.000000001980, 0.000001746652, 0.702001333237, 0.000000298203, 0.000000004386, 0.000000007073},
                {0.000000442888, 0.000000009907, 0.000000072361, 0.000000172052, 0.000000025906, 0.000000205243, 0.000001714670, 0.700654745102, 0.000000116321, 0.000000084246},
                {0.000000010099, 0.000000000311, 0.000000000700, 0.000000009671, 0.000000001102, 0.000000001586, 0.000000001183, 0.000000005456, 0.701215505600, 0.000000269338},
                {0.000000034608, 0.000000001550, 0.000000097330, 0.000000008808, 0.000000005737, 0.000000011863, 0.000000003720, 0.000000007706, 0.000000525256, 0.705851793289}
        };
        DoubleMatrix doubleMatrix = new DoubleMatrix(ppiMatrix);

        testMatrix = new DataMatrix(doubleMatrix.toFloat(), new HashMap<>());

        instance = new HiPhivePriority(hpoIds, new HiPhiveOptions(), testMatrix, priorityService);

    }

    private List<String> getHpoIds() {
        return Lists.newArrayList(
                "HP:0010055",
                "HP:0001363",
                "HP:0001156",
                "HP:0011304");
    }

    private List<Gene> getGenes() {
        return Lists.newArrayList(
                new Gene("FGFR2", 2263),
                new Gene("ROR2", 4920),
                new Gene("FREM2", 341640),
                new Gene("ZNF738", 148203)
        );
    }

    //TODO: this should be the output of a Prioritiser: Genes + HPO -> PrioritiserResults
    private List<HiPhivePriorityResult> getPriorityResultsOrderedByScore(List<Gene> genes) {
        return genes.stream()
                .flatMap(gene -> gene.getPriorityResults().values()
                        .stream())
                .map(result -> (HiPhivePriorityResult) result)
                .sorted(Comparator.comparingDouble(PriorityResult::getScore).reversed())
                .collect(toList());
    }

    private Consumer<HiPhivePriorityResult> checkScores(Map<String, List<Double>> geneScores) {
        return result -> {
            System.out.println(result);
            List<Double> scores = geneScores.get(result.getGeneSymbol());
            checkResultScores(result, scores);
        };
    }

    private void checkResultScores(HiPhivePriorityResult result, List<Double> scores) {
        assertThat(result.getScore(), equalTo(scores.subList(0, 3).stream().sorted(Comparator.reverseOrder()).findFirst().get()));
        assertThat(result.getHumanScore(), equalTo(scores.get(0)));
        assertThat(result.getMouseScore(), equalTo(scores.get(1)));
        assertThat(result.getFishScore(), equalTo(scores.get(2)));
        assertThat(result.getWalkerScore(), equalTo(scores.get(3)));
        boolean isCandidateGeneMatch = (scores.get(4) == 1.0)? true : false;
        assertThat(result.isCandidateGeneMatch(), equalTo(isCandidateGeneMatch));
    }

    @Test
    public void testGetPriorityType() {
        assertThat(instance.getPriorityType(), equalTo(PriorityType.HIPHIVE_PRIORITY));
    }

    @Test
    public void testPrioritizeGenes() {
        List<Gene> genes = getGenes();

        instance = new HiPhivePriority(hpoIds, new HiPhiveOptions("", "", "human,mouse,fish"), null, priorityService);
        instance.prioritizeGenes(genes);
//        List<PriorityResult> results = instance.prioritizeGenes(genes);

        List<HiPhivePriorityResult> results = getPriorityResultsOrderedByScore(genes);
        assertThat(results.size(), equalTo(genes.size()));

        //human, mouse, fish, walker, candidateGene (this is really a boolean)
        Map<String, List<Double>> geneScores = new LinkedHashMap<>();
        geneScores.put("FGFR2", Lists.newArrayList(0.8762904736638727, 0.8039423769154914, 0.0, 0.0, 0.0));
        geneScores.put("ROR2", Lists.newArrayList(0.8400025551155774, 0.6796978490932033, 0.0, 0.0, 0.0));
        geneScores.put("FREM2", Lists.newArrayList(0.5929438966299952, 0.6033446654591643, 0.0, 0.0, 0.0));
        geneScores.put("ZNF738", Lists.newArrayList(0.0, 0.0, 0.0, 0.0, 0.0));

        results.forEach(checkScores(geneScores));
        //TODO: need to also check the
    }

    @Test
    public void testPrioritizeGenesRestrictedGeneList() {
        List<Gene> genes = getGenes().stream().filter(gene -> gene.getGeneSymbol().equals("FGFR2")).collect(toList());

        instance = new HiPhivePriority(hpoIds, new HiPhiveOptions("", "", "human,mouse,fish"), null, priorityService);
        instance.prioritizeGenes(genes);
//        List<PriorityResult> results = instance.prioritizeGenes(genes);

        List<HiPhivePriorityResult> results = getPriorityResultsOrderedByScore(genes);
        assertThat(results.size(), equalTo(genes.size()));

        //human, mouse, fish, walker, candidateGene (this is really a boolean)
        Map<String, List<Double>> geneScores = new LinkedHashMap<>();
        geneScores.put("FGFR2", Lists.newArrayList(0.8762904736638727, 0.8039423769154914, 0.0, 0.0, 0.0));

        results.forEach(checkScores(geneScores));
    }

    @Test
    public void testPrioritizeMouseOnlyGenes() {
        List<Gene> genes = getGenes();

        instance = new HiPhivePriority(hpoIds, new HiPhiveOptions("", "", "mouse"), null, priorityService);
        instance.prioritizeGenes(genes);
//        List<PriorityResult> results = instance.prioritizeGenes(genes);

        List<HiPhivePriorityResult> results = getPriorityResultsOrderedByScore(genes);
        assertThat(results.size(), equalTo(genes.size()));

        //human, mouse, fish, walker, candidateGene (this is really a boolean)
        Map<String, List<Double>> geneScores = new LinkedHashMap<>();
        geneScores.put("FGFR2", Lists.newArrayList(0.0, 0.8039423769154914, 0.0, 0.0, 0.0));
        geneScores.put("ROR2", Lists.newArrayList(0.0, 0.6796978490932033, 0.0, 0.0, 0.0));
        geneScores.put("FREM2", Lists.newArrayList(0.0, 0.6033446654591643, 0.0, 0.0, 0.0));
        geneScores.put("ZNF738", Lists.newArrayList(0.0, 0.0, 0.0, 0.0, 0.0));

        results.forEach(checkScores(geneScores));
    }

    @Test
    public void testPrioritizeGenesInBenchmarkingMode() {
        List<Gene> genes = getGenes();

        String candidateGeneSymbol = "FGFR2";
        String candidateDiseaseId = "OMIM:101600";

        instance = new HiPhivePriority(hpoIds, new HiPhiveOptions(candidateDiseaseId, candidateGeneSymbol, "human,mouse,fish"), null, priorityService);
        instance.prioritizeGenes(genes);

        List<HiPhivePriorityResult> results = getPriorityResultsOrderedByScore(genes);
        assertThat(results.size(), equalTo(genes.size()));

        HiPhivePriorityResult topResult = results.get(0);
        assertThat(topResult.getGeneSymbol(), not(equalTo(candidateGeneSymbol)));

        //human, mouse, fish, walker, candidateGene (this is really a boolean)
        Map<String, List<Double>> geneScores = new LinkedHashMap<>();
        geneScores.put("FGFR2", Lists.newArrayList(0.0, 0.8039423769154914, 0.0, 0.0, 1.0));
        geneScores.put("ROR2", Lists.newArrayList(0.8400025551155774, 0.6796978490932033, 0.0, 0.0, 0.0));
        geneScores.put("FREM2", Lists.newArrayList(0.5929438966299952, 0.6033446654591643, 0.0, 0.0, 0.0));
        geneScores.put("ZNF738", Lists.newArrayList(0.0, 0.0, 0.0, 0.0, 0.0));

        results.forEach(checkScores(geneScores));

    }

    @Ignore
    @Test
    public void testSetPriorityService() {
        ArrayListMultimap<Integer, Double>  multimapInsertOrderTest = ArrayListMultimap.create();
        multimapInsertOrderTest.put(1, 1.0);
        multimapInsertOrderTest.put(1, 2.0);
        multimapInsertOrderTest.put(1, 3.0);

        multimapInsertOrderTest.put(3, 1.0);

        multimapInsertOrderTest.put(2, 1.0);
        multimapInsertOrderTest.put(2, 1.0);

        System.out.println(multimapInsertOrderTest);
        List<Integer> expectedOrder = Lists.newArrayList(1, 3, 2);
        assertThat(multimapInsertOrderTest.keySet(), equalTo(expectedOrder));
    }

    @Test
    public void testToString() {
        System.out.println(instance);
    }
    
}
