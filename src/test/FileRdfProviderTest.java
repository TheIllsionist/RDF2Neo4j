package test;

import datasource.RdfProvider;
import datasource.impl.FileRdfProvider;
import org.apache.jena.ontology.OntClass;
import rdfImporter.impl.CypherResourceImporter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


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
            importer.loadClassAsNode(classIterator.next());
        }
    }
}
