package test;

import datasource.RdfProvider;
import datasource.impl.FileRdfProvider;
import org.apache.jena.ontology.OntClass;
import rdfImporter.impl.cypherImpl.CypherClassImporter;
import rdfImporter.impl.cypherImpl.CypherResourceImporter;

import java.util.Iterator;
import java.util.Set;


/**
 * Created by The Illsionist on 2018/7/1.
 */
public class FileRdfProviderTest {
    public static void main(String args[]) throws Exception {
        RdfProvider rdfProvider = new FileRdfProvider("F:\\");
        CypherResourceImporter importer = new CypherResourceImporter();
        Set<OntClass> classes = rdfProvider.allOntClasses();
        Iterator<OntClass> classIterator = classes.iterator();
        while(classIterator.hasNext()){
            importer.loadClassIn(classIterator.next());
        }
    }
}
