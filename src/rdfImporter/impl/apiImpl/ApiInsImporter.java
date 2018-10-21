package rdfImporter.impl.apiImpl;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import rdfImporter.InsImporter;
import util.Words;

/**
 * Created by The Illsionist on 2018/10/18.
 */
public class ApiInsImporter implements InsImporter {

    @Override
    public boolean loadInsIn(Individual individual) throws Exception {
        return false;
    }

    @Override
    public boolean loadInsRelIn(Individual ins1, Individual ins2, ObjectProperty property) throws Exception {
        return false;
    }

    @Override
    public boolean loadInsRelIn(Individual ins1, Individual ins2, Words rel) throws Exception {
        return false;
    }

}
