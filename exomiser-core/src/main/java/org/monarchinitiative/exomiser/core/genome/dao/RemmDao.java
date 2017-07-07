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
package org.monarchinitiative.exomiser.core.genome.dao;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import htsjdk.tribble.readers.TabixReader;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.RemmScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class RemmDao {

    private final Logger logger = LoggerFactory.getLogger(RemmDao.class);

    private final TabixReader remmTabixReader;

    @Autowired
    public RemmDao(TabixReader remmTabixReader) {
        this.remmTabixReader = remmTabixReader;
    }

    @Cacheable(value = "remm")
    public PathogenicityData getPathogenicityData(Variant variant) {
        // MNCDS has not been trained on missense variants so skip these
        if (variant.getVariantEffect() == VariantEffect.MISSENSE_VARIANT) {
            return PathogenicityData.empty();
        }
        return processResults(variant);
    }

    private PathogenicityData processResults(Variant variant) {
        String chromosome = variant.getChromosomeName();
        int start = variant.getPosition();
        int end = calculateEndPosition(variant);
        return getRemmData(chromosome, start, end);
    }

    private int calculateEndPosition(Variant variant) {
        int pos = variant.getPosition();

        //we're doing this here in order not to have to count all this each time we need the value
        int refLength = variant.getRef().length();
        int altLength = variant.getAlt().length();
        //What about MNV?
        if (refLength == altLength) {
            return pos;
        }
        //these end positions are calculated according to recommendation by Max and Peter who produced the REMM score
        //don't change this unless they say.
        if (isDeletion(refLength, altLength)) {
            // test all deleted bases (being 1-based we need to correct the length)
            return pos + refLength - 1;
        } else if (isInsertion(refLength, altLength)) {
            // test bases either side of insertion
            return pos + 1;
        }
        return pos;
    }

    private static boolean isDeletion(int refLength, int altLength) {
        return refLength > altLength;
    }

    private static boolean isInsertion(int refLength, int altLength) {
        return refLength < altLength;
    }
    
    private PathogenicityData getRemmData(String chromosome, int start, int end) throws NumberFormatException {
        try {
            float ncds = Float.NaN;
            String line;
//            logger.info("Running tabix with " + chromosome + ":" + start + "-" + end);
            TabixReader.Iterator results = remmTabixReader.query(chromosome + ":" + start + "-" + end);
            while ((line = results.next()) != null) {
                String[] elements = line.split("\t");
                if (Float.isNaN(ncds)) {
                    ncds = Float.parseFloat(elements[2]);
                } else {
                    ncds = Math.max(ncds, Float.parseFloat(elements[2]));
                }
            }
            //logger.info("Final score " + ncds);
            if (!Float.isNaN(ncds)) {
                return PathogenicityData.of(RemmScore.valueOf(ncds));
            }
        } catch (IOException e) {
            logger.error("Unable to read from REMM tabix file {}", remmTabixReader.getSource(), e);
        }
        return PathogenicityData.empty();
    }

}
