package rdfImporter.impl;

import concurrentannotation.ThreadSafe;
import connection.Neo4jConnection;
import cypherelement.basic.*;
import cypherelement.clause.Cypher;
import datasource.RdfProvider;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.graphdb.Node;
import rdfImporter.ResourceImporter;
import rdfImporter.Words;
import util.CLASS_REL;
import util.INSTANCE_REL;
import util.PROPERTY_REL;
import util.cacheUtil.CacheClass;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * Created by The Illsionist on 2018/7/18.
 */
@ThreadSafe
public class CypherResourceImporter implements ResourceImporter{

    private final RdfProvider provider;  //要导入的知识源
    private final HashMap<String,String> nsMap = new HashMap<>(); //记录命名空间全称与前缀的对应
    private final CypherProperty propUri = new CypherProperty("uri");
    private final CypherProperty propPreLabel = new CypherProperty("preLabel");

    private final CypherNode clsWord;
    private final CypherNode dpWord;
    private final CypherNode opWord;
    private final CypherNode insWord;

    private final CypherRelationship isA;
    private final CypherRelationship rdfsLabel;
    private final CypherRelationship rdfsComment;
    private final CypherRelationship subClassOf;
    private final CypherRelationship subPropertyOf;

    public CypherResourceImporter(RdfProvider provider){
        this.provider = provider;
        fillNsMap();
        clsWord = getWordCypherNode(Words.OWL_CLASS);
        dpWord = getWordCypherNode(Words.OWL_DATATYPEPROPERTY);
        opWord = getWordCypherNode(Words.OWL_OBJECTPROPERTY);
        insWord = getWordCypherNode(Words.OWL_NAMEDINDIVIDUAL);

        isA = getWordCypherRelation("ISA");
        rdfsLabel = getWordCypherRelation("RDFS_LABEL");
        rdfsComment = getWordCypherRelation("RDFS_COMMENT");
        subClassOf = getWordCypherRelation(CLASS_REL.SUBCLASS_OF);
        subPropertyOf = getWordCypherRelation(PROPERTY_REL.SUBPROPERTY_OF);
    }

    /**
     * 将命名空间全称和前缀以及前缀和全称的对应加入nsMap
     */
    private void fillNsMap(){
        nsMap.put("http://kse.seu.edu.cn/rdb#","rdb");
        nsMap.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf");
        nsMap.put("http://www.w3.org/2000/01/rdf-schema#","rdfs");
        nsMap.put("http://www.w3.org/2002/07/owl#","owl");
        nsMap.put("http://www.w3.org/2001/XMLSchema#","xsd");
        nsMap.put("http://kse.seu.edu.cn/meta#","meta");
        nsMap.put("http://kse.seu.edu.cn/wgbq#","wgbq");
        nsMap.put("http://kse.seu.edu.cn/xgbg#","xgbg");
        nsMap.put("rdb","http://kse.seu.edu.cn/rdb#");
        nsMap.put("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        nsMap.put("rdfs","http://www.w3.org/2000/01/rdf-schema#");
        nsMap.put("owl","http://www.w3.org/2002/07/owl#");
        nsMap.put("xsd","http://www.w3.org/2001/XMLSchema#");
        nsMap.put("meta","http://kse.seu.edu.cn/meta#");
        nsMap.put("wgbq","http://kse.seu.edu.cn/wgbq#");
        nsMap.put("xgbg","http://kse.seu.edu.cn/xgbg#");
    }

    /**
     * 得到一个Uri的前缀,比如'http://www.w3.org/2002/07/owl#DatatypeProperty'的简称是'owl:DatatypeProperty'
     * @param uri
     * @return
     */
    private String getPreLabel(String uri){
        if(!uri.contains("#")){
            throw new InvalidParameterException("非合法的uri!");
        }
        return nsMap.get(uri.substring(0,uri.indexOf("#") + 1)) + ":" + uri.substring(uri.indexOf("#") + 1);
    }

    private CypherNode getWordCypherNode(Words word){
        CypherNode wordNode = new CypherNode("word","OWL_WORD",new HashSet<>());
        switch (word){
            case OWL_CLASS:{
                wordNode.addCondition(new PropValPair(propUri,new CypherValue(OWL.Class.getURI())));
                wordNode.addCondition(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.Class.getURI()))));
            }break;
            case OWL_DATATYPEPROPERTY:{
                wordNode.addCondition(new PropValPair(propUri,new CypherValue(OWL.DatatypeProperty.getURI())));
                wordNode.addCondition(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.DatatypeProperty.getURI()))));
            }break;
            case OWL_OBJECTPROPERTY:{
                wordNode.addCondition(new PropValPair(propUri,new CypherValue(OWL.ObjectProperty.getURI())));
                wordNode.addCondition(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.ObjectProperty.getURI()))));
            }break;
            case OWL_NAMEDINDIVIDUAL:{
                wordNode.addCondition(new PropValPair(propUri,new CypherValue(OWL2.NamedIndividual.getURI())));
                wordNode.addCondition(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL2.NamedIndividual.getURI()))));
            }break;
        }
        return wordNode;
    }
    private CypherRelationship getWordCypherRelation(String relation){
        CypherRelationship rel = new CypherRightRelationship();
        Set<PropValPair> props = new HashSet<>();
        switch(relation){
            case "ISA":{
                rel.setType("RDF_TYPE");
                props.add(new PropValPair(propUri,new CypherValue(RDF.type.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(RDF.type.getURI()))));
            }break;
            case "RANGE":{
                rel.setType("RDFS_RANGE");
                props.add(new PropValPair(propUri,new CypherValue(RDFS.range.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(RDFS.range.getURI()))));
            }break;
            case "DOMAIN":{
                rel.setType("RDFS_DOMAIN");
                props.add(new PropValPair(propUri,new CypherValue(RDFS.domain.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(RDFS.domain.getURI()))));
            }break;
            case "RDFS_LABEL":{
                rel.setType("RDFS_LABEL");
                props.add(new PropValPair(propUri,new CypherValue(RDFS.range.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(RDFS.range.getURI()))));
            }break;
            case "RDFS_COMMENT":{
                rel.setType("RDFS_COMMENT");
                props.add(new PropValPair(propUri,new CypherValue(RDFS.comment.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(RDFS.comment.getURI()))));
            }break;
        }
        rel.setProperties(props);
        return rel;
    }
    private CypherRelationship getWordCypherRelation(CLASS_REL clsRel){
        CypherRelationship rel = new CypherRightRelationship();
        Set<PropValPair> props = new HashSet<>();
        switch (clsRel){
            case SUBCLASS_OF:{
                rel.setType("RDFS_SUBCLASSOF");
                props.add(new PropValPair(propUri,new CypherValue(RDFS.subClassOf.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(RDFS.subClassOf.getURI()))));
            }break;
            case EQUIVALENT_CLASS:{
                rel.setType("OWL_EQUIVALENTCLASS");
                props.add(new PropValPair(propUri,new CypherValue(OWL.equivalentClass.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.equivalentClass.getURI()))));
            }break;
            case DISJOINT_CLASS:{
                rel.setType("OWL_DISJOINTCLASS");
                props.add(new PropValPair(propUri,new CypherValue(OWL.disjointWith.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.disjointWith.getURI()))));
            }break;
        }
        rel.setProperties(props);
        return rel;
    }
    private CypherRelationship getWordCypherRelation(PROPERTY_REL proRel){
        CypherRelationship rel = new CypherRightRelationship();
        Set<PropValPair> props = new HashSet<>();
        switch (proRel){
            case SUBPROPERTY_OF:{
                rel.setType("RDFS_SUBPROPERTYOF");
                props.add(new PropValPair(propUri,new CypherValue(RDFS.subPropertyOf.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(RDFS.subPropertyOf.getURI()))));
            }break;
            case EQUIVALENT_PROPERTY:{
                rel.setType("OWL_EQUIVALENTPROPERTY");
                props.add(new PropValPair(propUri,new CypherValue(OWL.equivalentProperty.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.equivalentProperty.getURI()))));
            }break;
            case INVERSE_OF:{
                rel.setType("OWL_INVERSEOF");
                props.add(new PropValPair(propUri,new CypherValue(OWL.inverseOf.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.inverseOf.getURI()))));
            }break;
            case DISJOINT_PROPERTY:{
                rel.setType("OWL_DISJOINTPROPERTY");
                props.add(new PropValPair(propUri,new CypherValue(OWL2.propertyDisjointWith.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL2.propertyDisjointWith.getURI()))));
            }break;
        }
        rel.setProperties(props);
        return rel;
    }
    private CypherRelationship getWordCypherRelation(INSTANCE_REL insRel){
        CypherRelationship rel = new CypherRightRelationship();
        Set<PropValPair> props = new HashSet<>();
        switch (insRel){
            case SAME_AS:{
                rel.setType("OWL_SAMEAS");
                props.add(new PropValPair(propUri,new CypherValue(OWL.sameAs.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.sameAs.getURI()))));
            }break;
            case DIFFERENT_FROM:{
                rel.setType("OWL_DIFFERENTFROM");
                props.add(new PropValPair(propUri,new CypherValue(OWL.differentFrom.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.differentFrom.getURI()))));
            }break;
        }
        rel.setProperties(props);
        return rel;
    }


    @Override
    public void initGraph() {

    }

    /**
     * 检查该本体类是否已存在于知识库中,如果没有则将该本体类写入知识库
     * TODO:当前只有一个线程往知识库中写入类和写类缓存,所以目前写知识库和写类缓存没有作为一个原子操作
     * @param ontClass &nbsp 当前可能会被写入知识库和类缓存的类
     * @return &nbsp 结果保证该类要存在于知识库中
     */
    @Override
    public Node loadClassAsNode(OntClass ontClass) throws Exception{
        if(!CacheClass.isContained(getPreLabel(ontClass.getURI()))){ //可能之前导入的类会与当前知识源中的类有重合
            Cypher cypher = new Cypher().match(clsWord);  //拼接查找定义类结点的词汇结点的Cypher

            CypherNode cls = new CypherNode("cls");
            List<CypherPath> pathes = new LinkedList<>();
            pathes.add(new CypherPath(cls).connectThrough(isA).with(new CypherNode("word")));  //声明结点是OWL类
            CypherNode node = new CypherNode(null,new HashSet<>());
            Iterator<String> labels = provider.allLabelsOf(ontClass).iterator();
            while(labels.hasNext()){
                node.addCondition(new PropValPair(new CypherProperty("value"),new CypherValue(labels.next())));
                pathes.add(new CypherPath(cls).connectThrough(rdfsLabel).with(node));  //给结点加一个rdfs:label
                node.getProperties().clear();
            }
            Iterator<String> comments = provider.allCommentsOf(ontClass).iterator();
            while(comments.hasNext()){
                node.addCondition(new PropValPair(new CypherProperty("value"),new CypherValue(comments.next())));
                pathes.add(new CypherPath(cls).connectThrough(rdfsComment).with(node)); //给结点加一个rdfs:comment
                node.getProperties().clear();
            }
            Set<CypherCondition> clsProps = new HashSet<>();
            clsProps.add(new PropValPair(null,Operator.COLON,new CypherValue("OWL_CLASS")));
            clsProps.add(new PropValPair(new CypherProperty("uri",cls),Operator.EQ_TO,new CypherValue(ontClass.getURI())));
            clsProps.add(new PropValPair(new CypherProperty("preLabel",cls),Operator.EQ_TO,new CypherValue(getPreLabel(ontClass.getURI()))));
            cypher.create(pathes).set(clsProps).returnIdOf(cls);
            String resCypher = cypher.getCypher();
            Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction transaction) {
                    StatementResult mRst = transaction.run(resCypher);
                    return mRst.single().get(0).asInt();
                }
            });
            CacheClass.addClass(getPreLabel(ontClass.getURI()));  //写缓存
        }
        return null;  //TODO:目前先实现返回空
    }

    @Override
    public Node loadPropertyAsNode(OntProperty ontProperty) {
        return null;
    }

    @Override
    public Node loadIndividualAsNode(Individual individual) {
        return null;
    }
}
