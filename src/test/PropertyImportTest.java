package test;
import Appender.*;
import Appender.impl.CpElementAppender;
import Model.Relation;
import TaskThread.ImportPropRelThread;
import TaskThread.ImportPropThread;
import datasource.RdfProvider;
import datasource.impl.FileRdfProvider;
import org.apache.jena.ontology.OntProperty;
import rdfImporter.PropImporter;
import rdfImporter.impl.cypherImpl.CypherPropImporter;
import util.Words;
import java.util.Queue;

/**
 * Created by The Illsionist on 2018/8/15.
 */
public class PropertyImportTest {
    public static void main(String args[]) throws Exception {
        RdfProvider rdfProvider = new FileRdfProvider("G:\\");
        Appender appender = new CpElementAppender();
        PropImporter importer = new CypherPropImporter(appender);
        Queue<OntProperty> props = rdfProvider.allOntProperties();
        Queue<Relation<OntProperty,Words>> propRels = rdfProvider.allPropertyRels();
        ImportPropThread propIn = new ImportPropThread(props,importer);
        ImportPropRelThread propRelIn = new ImportPropRelThread(propRels,importer);
        new Thread(propIn).start();
        new Thread(propRelIn).start();
    }
}
