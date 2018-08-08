package util;
import cypherelement.basic.*;
import cypherelement.clause.Cypher;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import rdfImporter.Words;
import java.security.InvalidParameterException;
import java.util.*;

/**
 * Created by The Illsionist on 2018/8/8.
 */
public class CypherUtil {

    private static final HashMap<String,String> nsMap;   //记录命名空间全称与前缀的对应
    private static final CypherProperty propUri;  //所有资源都有的uri属性
    private static final CypherProperty propPreLabel;  //所有资源都有的preLabel属性
    private static final CypherNode clsWord;  //导入类时用到的类定义词汇
    private static final CypherNode dpWord;   //导入数据类型属性时用到的数据类型属性定义词汇
    private static final CypherNode opWord;   //导入对象属性时用到的对象属性定义词汇
    private static final CypherNode insWord;  //导入实例时用到的实例定义词汇
    private static final CypherRelationship isA;
    private static final CypherRelationship rdfsLabel;
    private static final CypherRelationship rdfsComment;
    private static final CypherRelationship subClassOf;
    private static final CypherRelationship subPropertyOf;
    private static final CypherRelationship rdfsRange;
    private static final CypherRelationship rdfsDomain;
    static {  //静态初始化函数和final关键字都保证了初始化时状态和引用的可见性
        nsMap = new HashMap<>();
        fillNsMap();
        propUri = new CypherProperty("uri");
        propPreLabel = new CypherProperty("preLabel");
        clsWord = getWordCypherNode(Words.OWL_CLASS);
        dpWord = getWordCypherNode(Words.OWL_DATATYPEPROPERTY);
        opWord = getWordCypherNode(Words.OWL_OBJECTPROPERTY);
        insWord = getWordCypherNode(Words.OWL_NAMEDINDIVIDUAL);
        isA = getWordCypherRelation("ISA");
        rdfsLabel = getWordCypherRelation("RDFS_LABEL");
        rdfsComment = getWordCypherRelation("RDFS_COMMENT");
        subClassOf = getWordCypherRelation(CLASS_REL.SUBCLASS_OF);
        subPropertyOf = getWordCypherRelation(PROPERTY_REL.SUBPROPERTY_OF);
        rdfsRange = getWordCypherRelation("RANGE");
        rdfsDomain = getWordCypherRelation("DOMAIN");
    }
    /**
     * 将命名空间全称和前缀以及前缀和全称的对应加入nsMap
     */
    private static void fillNsMap(){
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
     */
    public static String getPreLabel(String uri){
        if(!uri.contains("#")){
            throw new InvalidParameterException("非合法的uri!");
        }
        return nsMap.get(uri.substring(0,uri.indexOf("#") + 1)) + ":" + uri.substring(uri.indexOf("#") + 1);
    }
    private static CypherNode getWordCypherNode(Words word){
        CypherNode wordNode = null;
        switch (word){
            case OWL_CLASS:{
                wordNode = new CypherNode("clsWord","OWL_WORD",new HashSet<>());
                wordNode.addCondition(new PropValPair(propUri,new CypherValue(OWL.Class.getURI())));
                wordNode.addCondition(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.Class.getURI()))));
            }break;
            case OWL_DATATYPEPROPERTY:{
                wordNode = new CypherNode("dpWord","OWL_WORD",new HashSet<>());
                wordNode.addCondition(new PropValPair(propUri,new CypherValue(OWL.DatatypeProperty.getURI())));
                wordNode.addCondition(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.DatatypeProperty.getURI()))));
            }break;
            case OWL_OBJECTPROPERTY:{
                wordNode = new CypherNode("opWord","OWL_WORD",new HashSet<>());
                wordNode.addCondition(new PropValPair(propUri,new CypherValue(OWL.ObjectProperty.getURI())));
                wordNode.addCondition(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.ObjectProperty.getURI()))));
            }break;
            case OWL_NAMEDINDIVIDUAL:{
                wordNode = new CypherNode("insWord","OWL_WORD",new HashSet<>());
                wordNode.addCondition(new PropValPair(propUri,new CypherValue(OWL2.NamedIndividual.getURI())));
                wordNode.addCondition(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL2.NamedIndividual.getURI()))));
            }break;
        }
        return wordNode;
    }
    private static CypherRelationship getWordCypherRelation(String relation){
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
    private static CypherRelationship getWordCypherRelation(CLASS_REL clsRel){
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
    private static CypherRelationship getWordCypherRelation(PROPERTY_REL proRel){
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
    private static CypherRelationship getWordCypherRelation(INSTANCE_REL insRel){
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

    public static String initGraphCypher(){
        Cypher cypher = new Cypher();
        List<CypherPath> words = new LinkedList<>();
        words.add(new CypherPath(clsWord));
        words.add(new CypherPath(dpWord));
        words.add(new CypherPath(opWord));
        words.add(new CypherPath(insWord));
        cypher.merge(words);  //注意这里使用merge
        return cypher.getCypher();
    }

    public static String intoClsCypher(OntClass ontClass) throws Exception{
        Cypher cypher = new Cypher().match(clsWord);  //拼接查找定义类结点的词汇结点的Cypher
        CypherNode cls = new CypherNode("cls");
        List<CypherPath> pathes = new LinkedList<>();
        pathes.add(new CypherPath(cls).connectThrough(isA).with(new CypherNode("clsWord")));  //声明结点是OWL类
        CypherNode node = new CypherNode(null,new HashSet<>());
        Iterator<RDFNode> labelNodes = ontClass.listLabels(null);
        while(labelNodes.hasNext()){
            node.addCondition(new PropValPair(new CypherProperty("value"),new CypherValue(labelNodes.next().toString())));
            pathes.add(new CypherPath(cls).connectThrough(rdfsLabel).with(node));  //给结点加一个rdfs:label
            node.getProperties().clear();
        }
        Iterator<RDFNode> commentNodes = ontClass.listComments(null);
        while(commentNodes.hasNext()){
            node.addCondition(new PropValPair(new CypherProperty("value"),new CypherValue(commentNodes.next().toString())));
            pathes.add(new CypherPath(cls).connectThrough(rdfsComment).with(node)); //给结点加一个rdfs:comment
            node.getProperties().clear();
        }
        Set<CypherCondition> clsProps = new HashSet<>();
        clsProps.add(new LabelCondition(cls,"OWL_CLASS"));
        clsProps.add(new PropValPair(new CypherProperty("uri",cls),Operator.EQ_TO,new CypherValue(ontClass.getURI())));
        clsProps.add(new PropValPair(new CypherProperty("preLabel",cls),Operator.EQ_TO,new CypherValue(getPreLabel(ontClass.getURI()))));
        cypher.create(pathes).set(clsProps).returnIdOf(cls);
        return cypher.getCypher();
    }

    public static String intoPropCypher(OntProperty ontProperty) throws Exception{
        Cypher cypher = new Cypher();
        CypherNode prop = new CypherNode("prop");
        List<CypherPath> paths = new LinkedList<>();
        if(ontProperty.hasProperty(RDF.type,OWL.DatatypeProperty)){
            cypher.match(dpWord);
            paths.add(new CypherPath(prop).connectThrough(isA).with(new CypherNode("dpWord")));
        }else{
            cypher.match(opWord);
            paths.add(new CypherPath(prop).connectThrough(isA).with(new CypherNode("opWord")));
        }
        CypherNode node = new CypherNode(null,new HashSet<>());

    }

    public static String intoInsCypher(OntProperty ontProperty){

    }


}
