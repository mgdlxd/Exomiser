/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.model.Gene;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Deprecated
public class PrioritiserRunner {

    private static final Logger logger = LoggerFactory.getLogger(PrioritiserRunner.class);

    /**
     * Applies the prioritisation criteria from the prioritisers to only those
     * genes which have passed the filtering steps. If no filtering has been
     * applied this will be equivalent to simply prioritising all genes.
     *
     * @param prioritisers
     * @param genes
     * @return
     */
    public List<Gene> prioritiseFilteredGenes(List<Prioritiser> prioritisers, List<Gene> genes) {
        List<Gene> filteredGenes = genes.stream().filter(Gene::passedFilters).collect(Collectors.toList());
        logger.info("{} of {} genes passed all filters", filteredGenes.size(), genes.size());        
        return prioritiseGenes(prioritisers, filteredGenes);
    }

    /**
     * Applies the prioritisation criteria from the prioritisers to all
     * genes irrespective of whether they have been filtered or not.
     *
     * @param prioritisers
     * @param genes
     * @return
     */
    public List<Gene> prioritiseGenes(List<Prioritiser> prioritisers, List<Gene> genes) {
        logger.info("Running prioritisers over {} genes", genes.size());
        prioritisers.forEach(prioritiser -> run(prioritiser, genes));
        logger.info("Done prioritising genes");
        return genes;
    }

    public void run(Prioritiser prioritiser, List<Gene> genes) {
        prioritiser.prioritizeGenes(genes);
    }
   
}
