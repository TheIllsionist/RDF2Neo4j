package RDFLoader.impl;

import RDFLoader.ResourceLoader;
import RDFLoader.Words;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;

public class ApiResourceLoader implements ResourceLoader{

    /** 数据库实例是多线程安全的,但是一个库同时只能创建一个实例 **/
    private static GraphDatabaseService graphDb = null;

    private static enum RelTypes implements RelationshipType{
        RDF_TYPE,  //rdf:type,用来定义类或者声明实例的类
        RDFS_LABEL, //rdfs:label,用来声明资源的可读名称,一个资源可以有很多可读名称
        RDFS_COMMENT,  //rdfs:comment,资源的定义的描述
        RDFS_SUBCLASSOF,  //rdfs:subClassOf,用来描述类之间的上下位关系
        RDFS_SUBPROPERTYOF //rdfs:subPropertyOf,用来描述属性之间的上下位关系
    }

    public ApiResourceLoader(){
        //TODO:原生API不可以链接远程Neo4j数据库吗?
        this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File("/opt/Neo4j/neo4j-community-3.3.3/data/database/rdfDb"));
        registerShutdownHook(this.graphDb);
    }

    /**
     * 构造函数,传入Neo4j数据库路径,如果路径下没有该指定数据库,新库会被创建
     * @param dbPath
     */
    public ApiResourceLoader(String dbPath){
        this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(dbPath));
        registerShutdownHook(this.graphDb);
    }

    public ApiResourceLoader(GraphDatabaseService graphDb){
        this.graphDb = graphDb;
        registerShutdownHook(this.graphDb);
    }


    private Node loadWordAsNode(Words word) {
        return null;
    }

    /**
     * 图的初始化
     */
    @Override
    public void initGraph() {
        try(Transaction tx = graphDb.beginTx()){

        }
    }

    @Override
    public Node loadClassAsNode(OntClass ontClass) {
        return null;
    }

    @Override
    public Node loadPropertyAsNode(OntProperty ontProperty) {
        return null;
    }

    @Override
    public Node loadIndividualAsNode(Individual individual) {
        return null;
    }


    /**
     * 注册Neo4j关闭钩子以使得Neo4j可以更好的关闭掉(即使是在使用'Ctrl + C'时依然可以安全关闭)
     * @param graphDb
     */
    private static void registerShutdownHook(final GraphDatabaseService graphDb){
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                graphDb.shutdown();
            }
        });
    }

}
