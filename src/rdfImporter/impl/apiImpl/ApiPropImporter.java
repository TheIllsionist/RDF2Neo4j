package rdfImporter.impl.apiImpl;

import org.apache.jena.ontology.OntProperty;
import rdfImporter.PropImporter;
import util.Words;

/**
 * Created by The Illsionist on 2018/10/18.
 */
public class ApiPropImporter implements PropImporter {

    @Override
    public boolean loadPropertyIn(OntProperty property) throws Exception {
        return false;
    }

    @Override
    public boolean loadPropertyRelIn(OntProperty prop1, OntProperty prop2, Words rel) throws Exception {
        return false;
    }

}
