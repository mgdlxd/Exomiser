/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import java.util.EnumSet;
import java.util.Set;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PathogenicityDataProvider extends AbstractFilterDataProvider {

    private final Set<PathogenicitySource> pathogenicitySources;
    
    public PathogenicityDataProvider(VariantDataService variantDataService, Set<PathogenicitySource> pathogenicitySources, VariantFilter variantFilter) {
        super(variantDataService, variantFilter);

        if (pathogenicitySources.isEmpty()) {
            this.pathogenicitySources = EnumSet.noneOf(PathogenicitySource.class);
        } else {
            this.pathogenicitySources = EnumSet.copyOf(pathogenicitySources);
        }
    }

    @Override
    public void provideVariantData(VariantEvaluation variantEvaluation) {
        if (variantEvaluation.getPathogenicityData().getPredictedPathogenicityScores().isEmpty()) {
            PathogenicityData pathData = variantDataService.getVariantPathogenicityData(variantEvaluation, pathogenicitySources);
            variantEvaluation.setPathogenicityData(pathData);
        }
    }

}
