/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Repository
public class DefaultDiseaseDao implements DiseaseDao {

    private final Logger logger = LoggerFactory.getLogger(DefaultDiseaseDao.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public Set<String> getHpoIdsForDiseaseId(String diseaseId) {
        String hpoListString = "";
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement hpoIdsStatement = connection.prepareStatement("SELECT hp_id FROM disease_hp WHERE disease_id = ?");
            hpoIdsStatement.setString(1, diseaseId);
            ResultSet rs = hpoIdsStatement.executeQuery();
            rs.next();
            hpoListString = rs.getString(1);
        } catch (SQLException e) {
            logger.error("Unable to retrieve HPO terms for disease {}", diseaseId, e);
        }
        List<String> diseaseHpoIds = parseHpoIdListFromString(hpoListString);
        logger.info("{} HPO ids retrieved for disease {} - {}", diseaseHpoIds.size(), diseaseId, diseaseHpoIds);
        return new TreeSet<>(diseaseHpoIds);
    }

    private List<String> parseHpoIdListFromString(String hpoIdsString) {
        String[] hpoArray = hpoIdsString.split(",");
        List<String> hpoIdList = new ArrayList<>();
        for (String string : hpoArray) {
            hpoIdList.add(string.trim());
        }
        return hpoIdList;
    }

    @Cacheable(value="diseases")
    @Override
    public Map<String, String> getDiseaseIdToTerms() {
        Map<String, String> termsCache = new HashMap();
        String diseaseNameQuery = "SELECT disease_id, diseasename FROM disease";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement ontologyTermsStatement = connection.prepareStatement(diseaseNameQuery);
                ResultSet rs = ontologyTermsStatement.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString(1);
                String term = rs.getString(2);
                id = id.trim();
                termsCache.put(id, term);
            }
        } catch (SQLException e) {
            logger.error("Unable to execute query '{}' for disease terms cache", diseaseNameQuery, e);
        }
        logger.info("Created {} disease id : term mappings", termsCache.size());
        return termsCache;
    }
}
