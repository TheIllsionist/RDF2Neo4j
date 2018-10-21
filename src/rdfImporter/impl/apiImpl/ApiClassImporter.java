package rdfImporter.impl.apiImpl;

import Appender.Appender;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.RDFNode;
import org.neo4j.graphdb.*;
import rdfImporter.ClassImporter;
import rdfImporter.cache.apiCache.ApiClassCache;
import util.Words;
import java.util.Iterator;

/**
 * Created by The Illsionist on 2018/10/18.
 */
public class ApiClassImporter implements ClassImporter {

    public static enum RelTypes implements RelationshipType
    {
        RDF_TYPE,
        RDFS_LABEL,
        RDFS_COMMENT,
        RDFS_SUBCLASSOF,
        OWL_EQCLASS,
        OWL_DJCLASS
    }


    private GraphDatabaseService graphDb = null;

    public ApiClassImporter(GraphDatabaseService graphDb){
        this.graphDb = graphDb;
    }


    @Override
    public boolean loadClassIn(OntClass ontClass) throws Exception {
        String preLabel = Appender.getPreLabel(ontClass.getURI());
        if(!ApiClassCache.classContained(preLabel)){  //该类别还未导入数据库
            try(Transaction tx = graphDb.beginTx())
            {
                Node clsWord = graphDb.findNode(Label.label("OWL_WORD"),"preLabel","owl:Class");  //查询词汇定义Node
                Node clsNode = graphDb.createNode();  //创建类Node
                clsNode.addLabel(Label.label("OWL_CLASS"));
                clsNode.setProperty("uri",ontClass.getURI());
                clsNode.setProperty("preLabel",preLabel);
                Relationship rel = clsNode.createRelationshipTo(clsWord,RelTypes.RDF_TYPE);
                rel.setProperty("uri","");
                rel.setProperty("preLabel","");
                Iterator<RDFNode> labelNodes = ontClass.listLabels(null);
                while (labelNodes.hasNext()){

                }
                tx.success();
            }
        }
    }

    @Override
    public boolean loadClassRelIn(OntClass class1, OntClass class2, Words rel) throws Exception {
        return false;
    }

}
