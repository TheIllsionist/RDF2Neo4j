package util;
import cypherelement.basic.*;
import cypherelement.clause.Cypher;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

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

    /**
     * 拼接将输入的本体类导入Neo4j数据库的Cypher语句
     * @param ontClass &nbsp 输入的类
     * @return 将该类按照知识表示规范导入数据库的合法Cypher语句
     * @throws Exception
     * TODO:目前没有确定使用对象方式拼接更利于GC回收还是直接使用语句拼接更利于GC回收,后面需要进行实验
     */
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
        cypher.create(pathes).set(clsProps).returnIdOf(cls);  //返回导入类的id
        return cypher.getCypher();
    }

    /**
     * 拼接将输入的本体属性导入Neo4j数据库的Cypher语句
     * @param ontProperty &nbsp 输入的属性
     * @return 将该属性按照知识表示规范导入数据库的合法Cypher语句
     * @throws Exception
     * TODO:目前没有确定使用对象方式拼接更利于GC回收还是直接使用语句拼接更利于GC回收,后面需要进行实验
     */
    public static String intoPropCypher(OntProperty ontProperty) throws Exception{
        Cypher cypher = new Cypher();
        CypherNode prop = new CypherNode("prop");  //代表属性的结点
        Set<CypherCondition> propProps = new HashSet<>();
        List<CypherPath> pathes = new LinkedList<>();
        if(ontProperty.hasProperty(RDF.type,OWL.DatatypeProperty)){  //当前属性是数据类型属性
            propProps.add(new LabelCondition(prop,"OWL_DATATYPEPROPERTY"));
            cypher.match(dpWord);
            pathes.add(new CypherPath(prop).connectThrough(isA).with(new CypherNode("dpWord")));
        }else{                     //当前属性是对象属性
            propProps.add(new LabelCondition(prop,"OWL_OBJECTPROPERTY"));
            cypher.match(opWord);
            pathes.add(new CypherPath(prop).connectThrough(isA).with(new CypherNode("opWord")));
        }
        CypherNode node = new CypherNode(null,new HashSet<>());  //用到的中间结点
        Iterator<RDFNode> labelNodes = ontProperty.listLabels(null);  //列出该属性的所有rdfsLabel
        while(labelNodes.hasNext()){
            node.addCondition(new PropValPair(new CypherProperty("value"),new CypherValue(labelNodes.next().toString())));
            pathes.add(new CypherPath(prop).connectThrough(rdfsLabel).with(node));   //给结点加一个rdfs:label
            node.getProperties().clear();
        }
        Iterator<RDFNode> commentNodes = ontProperty.listComments(null);  //列出该属性的所有rdfsComment
        while(commentNodes.hasNext()){
            node.addCondition(new PropValPair(new CypherProperty("value"),new CypherValue(commentNodes.next().toString())));
            pathes.add(new CypherPath(prop).connectThrough(rdfsComment).with(node)); //给结点加一个rdfs:comment
            node.getProperties().clear();
        }
        propProps.add(new PropValPair(new CypherProperty("uri",prop),Operator.EQ_TO,new CypherValue(ontProperty.getURI())));
        propProps.add(new PropValPair(new CypherProperty("preLabel",prop),Operator.EQ_TO,new CypherValue(getPreLabel(ontProperty.getURI()))));
        cypher.create(pathes).set(propProps).returnIdOf(prop);  //返回导入属性的结点的id
        return cypher.getCypher();
    }

    /**
     * 拼接将输入的本体实例导入Neo4j数据库的Cypher语句
     * @param individual &nbsp 输入的实例
     * @return 将该实例按照知识表示规范导入数据库的合法Cypher语句
     * @throws Exception
     * TODO:目前没有确定使用对象方式拼接更利于GC回收还是直接使用语句拼接更利于GC回收,后面需要进行实验
     */
    public static String intoInsCypher(Individual individual) throws Exception{
        Cypher cypher = new Cypher();  //cypher拼接对象
        CypherNode ins = new CypherNode("ins");
        Set<CypherCondition> insProps = new HashSet<>(); //该实例所拥有的属性:rdfsLabel,rdfsComment
        List<CypherPath> pathes = new LinkedList<>();
        insProps.add(new LabelCondition(ins,"OWL_NAMEDINDIVIDUAL"));
        cypher.match(insWord);  //拼接-查询实例定义词汇
        pathes.add(new CypherPath(ins).connectThrough(isA).with(new CypherNode("insWord"))); //加一条词汇定义path
        CypherNode node = new CypherNode(null,new HashSet<>());   //临时使用node
        //处理实例的rdfsLabels
        Iterator<RDFNode> labelNodes = individual.listLabels(null);
        while(labelNodes.hasNext()){
            node.addCondition(new PropValPair(new CypherProperty("value"),new CypherValue(labelNodes.next().toString())));
            pathes.add(new CypherPath(ins).connectThrough(rdfsLabel).with(node));  //给结点加一个rdfs:label
            node.getProperties().clear();
        }
        //处理实例的rdfsComments
        Iterator<RDFNode> commentNodes = individual.listComments(null);
        while(commentNodes.hasNext()){
            node.addCondition(new PropValPair(new CypherProperty("value"),new CypherValue(commentNodes.next().toString())));
            pathes.add(new CypherPath(ins).connectThrough(rdfsComment).with(node)); //给结点加一个rdfs:comment
            node.getProperties().clear();
        }
        //处理实例的dps
        node.setLabel("DP_VALUE");           //node也是临时结点
        CypherRelationship tDpRel = new CypherRightRelationship(); //临时使用Cypher关系代表dp
        Set<PropValPair> dpProps = new HashSet<>();  //临时使用Cypher关系的属性集代表该dp的属性集
        List<String> dpLabels = new ArrayList<>();   //临时记录该dp的rdfslabels
        List<String> dpVals = new ArrayList<>();     //临时记录该dp的值集合
        StmtIterator stmtIter = individual.listProperties();  //列出该实例的所有属性
        while(stmtIter.hasNext()){
            Statement statement = stmtIter.nextStatement();
            Property prop = statement.getPredicate();
            if(prop.hasProperty(RDF.type,OWL.DatatypeProperty)){  //当前属性是数据类型属性
                OntProperty oDp = individual.getOntModel().getDatatypeProperty(prop.getURI());  //根据uri获取对应的DP
                //提取并设置第三元
                StmtIterator dpValIter = individual.listProperties(oDp);  //取该DP的值(注意:原程序中做了区分,现在都用String)
                while(dpValIter.hasNext()){
                    dpVals.add(dpValIter.nextStatement().getLiteral().getString());
                }
                if(dpVals.size() == 0)  //如果这个数据类型属性没有值,跳过该属性
                    continue;
                node.addCondition(new PropValPair(new CypherProperty("value"),new CypherValue(dpVals,DataType.STR)));
                //提取并记录第二元的属性labels,uri,preLabel
                Iterator<RDFNode> labelIter = oDp.listLabels(null);
                while(labelIter.hasNext()){
                    dpLabels.add(labelIter.next().toString());
                }
                if(dpLabels.size() > 0){    //如果该DP有preLabel,就加上
                    dpProps.add(new PropValPair(new CypherProperty("`rdfs:label`"),new CypherValue(dpLabels,DataType.STR)));
                }
                dpProps.add(new PropValPair(propUri,new CypherValue(oDp.getURI())));
                dpProps.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(oDp.getURI()))));
                tDpRel.setType("`" + getPreLabel(oDp.getURI()) + "`");  //设置第二元的Type
                tDpRel.setProperties(dpProps);                         //设置第二元的属性集
                pathes.add(new CypherPath(ins).connectThrough(tDpRel).with(node));  //拼接一条数据类型属性path
                //做一些清空工作
                dpProps.clear();
                dpLabels.clear();
                dpVals.clear();
                node.getProperties().clear();
            }
        }
        insProps.add(new PropValPair(new CypherProperty("uri",ins),Operator.EQ_TO,new CypherValue(individual.getURI())));
        insProps.add(new PropValPair(new CypherProperty("preLabel",ins),Operator.EQ_TO,new CypherValue(getPreLabel(individual.getURI()))));
        cypher.create(pathes).set(insProps).returnIdOf(ins);  //返回导入实例的结点的id
        return cypher.getCypher();
    }

    /**
     * 拼接将两个类之间的某种关系写入Neo4j数据库的Cypher语句
     * @param class1 &nbsp 先序关系中的第1个类
     * @param class2 &nbsp 先序关系中的第2个类
     * @param rel &nbsp 要写入的关系
     * @return
     * @throws Exception
     * TODO:目前没有确定使用对象方式拼接更利于GC回收还是直接使用语句拼接更利于GC回收,后面需要进行实验
     */
    public static String intoRelCypher(OntClass class1,OntClass class2,CLASS_REL rel) throws Exception{
        Cypher cypher = new Cypher();
        Set<PropValPair> props = new HashSet<>();
        props.add(new PropValPair(propPreLabel,new CypherValue(CypherUtil.getPreLabel(class1.getURI()))));
        CypherNode cls1 = new CypherNode("cls1","OWL_CLASS",props);
        props.clear();
        props.add(new PropValPair(propPreLabel,new CypherValue(CypherUtil.getPreLabel(class2.getURI()))));
        CypherNode cls2 = new CypherNode("cls2","OWL_CLASS",props);
        List<CypherNode> nodes = new ArrayList<>();
        nodes.add(cls1);
        nodes.add(cls2);
        cypher.match(nodes);  //拼接查询两个类的cypher语句
        cls1.setLabel(null);
        cls1.setProperties(null);
        cls2.setLabel(null);
        cls2.setProperties(null);
        props.clear();
        CypherRelationship relation = new CypherRightRelationship();
        switch (rel){
            case SUBCLASS_OF:{
                relation.setType("RDFS_SUBCLASSOF");
                props.add(new PropValPair(propUri,new CypherValue(RDFS.subClassOf.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(CypherUtil.getPreLabel(RDFS.subClassOf.getURI()))));
            }break;
            case EQUIVALENT_CLASS:{
                relation.setType("EQUIVALENT_CLASS");
                props.add(new PropValPair(propUri,new CypherValue(OWL2.equivalentClass.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(CypherUtil.getPreLabel(OWL2.equivalentClass.getURI()))));
            }break;
            case DISJOINT_CLASS:{
                relation.setType("DISJOINT_CLASS");
                props.add(new PropValPair(propUri,new CypherValue(OWL2.disjointWith.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(CypherUtil.getPreLabel(OWL2.disjointWith.getURI()))));
            }break;
        }
        relation.setName("r");
        relation.setProperties(props);
        CypherPath path = new CypherPath(cls1).connectThrough(relation).with(cls2);
        cypher.create(path).returnIdOf(relation);
        return cypher.getCypher();
    }

    /**
     * 拼接将两个属性之间的某种关系写入Neo4j数据库的Cypher语句
     * @param ontProp1 &nbsp 先序关系中的第1个属性
     * @param ontProp2 &nbsp 先序关系中的第2个属性
     * @param rel &nbsp 要写入的关系
     * @return
     * @throws Exception
     * TODO:目前没有确定使用对象方式拼接更利于GC回收还是直接使用语句拼接更利于GC回收,后面需要进行实验
     */
    public static String intoRelCypher(OntProperty ontProp1,OntProperty ontProp2,PROPERTY_REL rel) throws Exception{
        boolean isObj = false;
        if(ontProp1.hasProperty(RDF.type,OWL.ObjectProperty)){ //判断属性是对象属性还是数据类型属性
            isObj = true;
        }
        Cypher cypher = new Cypher();
        Set<PropValPair> props = new HashSet<>();
        props.add(new PropValPair(propPreLabel,new CypherValue(CypherUtil.getPreLabel(ontProp1.getURI()))));
        CypherNode prop1 = new CypherNode("prop1",isObj ? "OWL_OBJECTPROPERTY" : "OWL_DATATYPEPROPERTY",props);
        props.clear();
        props.add(new PropValPair(propPreLabel,new CypherValue(CypherUtil.getPreLabel(ontProp2.getURI()))));
        CypherNode prop2 = new CypherNode("prop2",isObj ? "OWL_OBJECTPROPERTY" : "OWL_DATATYPEPROPERTY",props);
        List<CypherNode> nodes = new ArrayList<>();
        nodes.add(prop1);
        nodes.add(prop2);
        cypher.match(nodes);  //拼接查询两个属性的cypher语句
        prop1.setLabel(null);
        prop1.setProperties(null);
        prop2.setLabel(null);
        prop2.setProperties(null);
        props.clear();
        CypherRelationship relation = new CypherRightRelationship();
        switch (rel){
            case SUBPROPERTY_OF:{
                relation.setType("RDFS_SUBPROPERTYOF");
                props.add(new PropValPair(propUri,new CypherValue(RDFS.subPropertyOf.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(CypherUtil.getPreLabel(RDFS.subPropertyOf.getURI()))));
            }break;
            case EQUIVALENT_PROPERTY:{
                relation.setType("EQUIVALENT_PROPERTY");
                props.add(new PropValPair(propUri,new CypherValue(OWL2.equivalentProperty.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(CypherUtil.getPreLabel(OWL2.equivalentProperty.getURI()))));
            }break;
            case DISJOINT_PROPERTY:{
                relation.setType("DISJOINT_PROPERTY");
                props.add(new PropValPair(propUri,new CypherValue(OWL2.propertyDisjointWith.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(CypherUtil.getPreLabel(OWL2.propertyDisjointWith.getURI()))));
            }break;
            case INVERSE_OF:{
                relation.setType("INVERSE_PROPERTY");
                props.add(new PropValPair(propUri,new CypherValue(OWL2.inverseOf.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(CypherUtil.getPreLabel(OWL2.inverseOf.getURI()))));
            }break;
        }
        relation.setName("r");
        relation.setProperties(props);
        CypherPath path = new CypherPath(prop1).connectThrough(relation).with(prop2);
        cypher.create(path).returnIdOf(relation);
        return cypher.getCypher();
    }

    public static String intoRelCypher(Individual ins1,Individual ins2,INSTANCE_REL rel){
        return null;
    }

    public static String intoRelCypher(Individual ins1, Individual ins2, ObjectProperty prop){
        return null;
    }

}
