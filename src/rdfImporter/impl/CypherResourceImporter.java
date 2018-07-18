package rdfImporter.impl;

import concurrentannotation.ThreadSafe;
import cypherelement.basic.*;
import cypherelement.clause.Cypher;
import datasource.RdfProvider;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.neo4j.graphdb.Node;
import rdfImporter.ResourceImporter;
import rdfImporter.Words;
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
    private final CypherRightRelationship rdfType = null;
    private final CypherRightRelationship rdfsLabel = null;
    private final CypherRightRelationship rdfsComment = null;
    private final CypherNode clsWord;
    private final CypherNode dpWord;
    private final CypherNode opWord;
    private final CypherNode insWord;

    public CypherResourceImporter(RdfProvider provider){
        this.provider = provider;
        fillNsMap();

        clsWord = getWordCypherNode(Words.OWL_CLASS);
        dpWord = getWordCypherNode(Words.OWL_DATATYPEPROPERTY);
        opWord = getWordCypherNode(Words.OWL_OBJECTPROPERTY);
        insWord = getWordCypherNode(Words.OWL_NAMEDINDIVIDUAL);
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
        CypherNode wordNode = new CypherNode("word","OWL_WORD",null);
        Set<PropValPair> props = new HashSet<>();
        switch (word){
            case OWL_CLASS:{
//                props.add(new PropValPair(propUri,new CypherValue(OWL.Class.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.Class.getURI()))));
            }break;
            case OWL_DATATYPEPROPERTY:{
//                props.add(new PropValPair(propUri,new CypherValue(OWL.DatatypeProperty.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.DatatypeProperty.getURI()))));
            }break;
            case OWL_OBJECTPROPERTY:{
//                props.add(new PropValPair(propUri,new CypherValue(OWL.ObjectProperty.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.ObjectProperty.getURI()))));
            }break;
            case OWL_NAMEDINDIVIDUAL:{
//                props.add(new PropValPair(propUri,new CypherValue(OWL2.NamedIndividual.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL2.NamedIndividual.getURI()))));
            }break;
        }
        wordNode.setProperties(props);
        return wordNode;
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
    public Node loadClassAsNode(OntClass ontClass) {
        if(!CacheClass.isContained(getPreLabel(ontClass.getURI()))){ //可能之前导入的类会与当前知识源中的类有重合
            Cypher cypher = new Cypher().match(clsWord);
            Set<PropValPair> props = clsWord.getProperties();
            clsWord.setLabel(null);
            CypherNode clsNode = new CypherNode("cls");
            List<CypherPath> pathes = new LinkedList<>();
            CypherPath path = new CypherPath(clsNode);

        }
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
}
