package rdfImporter;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.neo4j.graphdb.Node;

public interface ResourceImporter {

    void initGraph() throws Exception;

    /**
     * 将某个类作为Node存入Neo4j
     * @param ontClass
     * @return
     */
    Node loadClassAsNode(OntClass ontClass) throws Exception;

    /**
     * 将某个属性作为Node存入Neo4j
     * @param ontProperty
     * @return
     */
    Node loadPropertyAsNode(OntProperty ontProperty) throws Exception;

    /**
     * 将某个实例作为Node存入Neo4j
     * @param individual
     * @return
     */
    Node loadIndividualAsNode(Individual individual) throws Exception;




}
