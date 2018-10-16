package Appender.impl;

import Appender.Appender;
import cypherelement.basic.*;
import cypherelement.clause.Cypher;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import util.Words;
import java.util.*;


/**
 * Created by The Illsionist on 2018/8/8.
 * TODO:目前没有确定使用对象方式拼接更利于GC回收还是直接使用语句拼接更利于GC回收,后面需要进行实验
 */
public class CpElementAppender extends Appender {

    private static final HashMap<String,String> nsMap;
    private static final CypherProperty propUri;
    private static final CypherProperty propPreLabel;
    private static final CypherNode clsWord;
    private static final CypherNode dpWord;
    private static final CypherNode opWord;
    private static final CypherNode insWord;
    private static final CypherRelationship isA;
    private static final CypherRelationship rdfsLabel;
    private static final CypherRelationship rdfsComment;
    private static final CypherRelationship subClassOf;
    private static final CypherRelationship equalClass;
    private static final CypherRelationship disjointClass;
    private static final CypherRelationship subPropertyOf;
    private static final CypherRelationship equalProperty;
    private static final CypherRelationship disjointProperty;
    private static final CypherRelationship inverseProperty;
    private static final CypherRelationship rdfsRange;
    private static final CypherRelationship rdfsDomain;
    private static final CypherRelationship sameIns;
    private static final CypherRelationship differentIns;

    static {  //静态初始化函数和final关键字都保证了初始化时状态和引用的可见性
        nsMap = new HashMap<>();  //记录命名空间全称和简写的相互对应
        fillNsMap();         //将相互对应关系填充进去,后面可以修改为从配置文件中读取
        propUri = new CypherProperty("uri");
        propPreLabel = new CypherProperty("preLabel");
        clsWord = getWordCypherNode(Words.OWL_CLASS);    //声明类所用关键字
        dpWord = getWordCypherNode(Words.OWL_DATATYPEPROPERTY);  //声明数据类型属性所用关键字
        opWord = getWordCypherNode(Words.OWL_OBJECTPROPERTY);   //声明对象属性所用关键字
        insWord = getWordCypherNode(Words.OWL_NAMEDINDIVIDUAL); //声明实例所用关键字
        isA = getWordCypherRelation2(Words.RDF_TYPE);    //用ISA关系来声明元素
        rdfsLabel = getWordCypherRelation2(Words.RDFS_LABEL);   //用RDFS_LABEL关系来声明元素的可读名
        rdfsComment = getWordCypherRelation2(Words.RDFS_COMMENT); //用RDFS_COMMENT关系来解释概念的含义
        rdfsRange = getWordCypherRelation2(Words.RDFS_RANGE);    //用RDFS_RANGE关系声明属性的值域
        rdfsDomain = getWordCypherRelation2(Words.RDFS_DOMAIN); //用RDFS_DOMAIN关系声明属性的定义域
        subClassOf = getWordCypherRelation(Words.RDFS_SUBCLASSOF);   //用SUBCLASS_OF关系声明两个类之间的父子关系
        equalClass = getWordCypherRelation(Words.OWL_EQCLASS);  //用EQUIVALENT_CLASS关系表示两个类是等价的
        disjointClass = getWordCypherRelation(Words.OWL_DJCLASS);  //用DISJOINT_CLASS关系表示两个类的实例集合不相交
        subPropertyOf = getWordCypherRelation(Words.RDFS_SUBPROPERTYOF);  //用SUBPROPERTY_OF关系声明两个属性之间的父子关系
        equalProperty = getWordCypherRelation(Words.OWL_EQPROPERTY);//用EQUIVALENT_PROPERTY关系表示两个属性是等价的
        disjointProperty = getWordCypherRelation(Words.OWL_DJPROPERTY);//用DISJOINT_PROPERTY关系表示对同一主语这两个属性的值集合不相交
        inverseProperty = getWordCypherRelation(Words.OWL_IVPROPERTY);//用INVERSE_OF关系表示两个属性刚好表达了相反的语义(主语宾语刚好互换位置)
        sameIns = getWordCypherRelation(Words.OWL_SAME_AS);  //用SAME_AS关系表示两个实例就是同一个实例
        differentIns = getWordCypherRelation(Words.OWL_DFINS); //用DIFFERENT_FROM关系表示两个实例并不是同一实例
    }
    /**
     * 得到定义Node的词汇Node
     * @param word
     * @return
     */
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

    private static CypherRelationship getWordCypherRelation2(Words relWord){
        CypherRelationship rel = new CypherRightRelationship();
        Set<PropValPair> props = new HashSet<>();
        switch(relWord){
            case RDF_TYPE:{
                rel.setType("RDF_TYPE");
                props.add(new PropValPair(propUri,new CypherValue(RDF.type.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(RDF.type.getURI()))));
            }break;
            case RDFS_RANGE:{
                rel.setType("RDFS_RANGE");
                props.add(new PropValPair(propUri,new CypherValue(RDFS.range.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(RDFS.range.getURI()))));
            }break;
            case RDFS_DOMAIN:{
                rel.setType("RDFS_DOMAIN");
                props.add(new PropValPair(propUri,new CypherValue(RDFS.domain.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(RDFS.domain.getURI()))));
            }break;
            case RDFS_LABEL:{
                rel.setType("RDFS_LABEL");
                props.add(new PropValPair(propUri,new CypherValue(RDFS.range.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(RDFS.range.getURI()))));
            }break;
            case RDFS_COMMENT:{
                rel.setType("RDFS_COMMENT");
                props.add(new PropValPair(propUri,new CypherValue(RDFS.comment.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(RDFS.comment.getURI()))));
            }break;
        }
        rel.setProperties(props);
        return rel;
    }

    private static CypherRelationship getWordCypherRelation(Words relWord){
        CypherRelationship rel = new CypherRightRelationship();
        Set<PropValPair> props = new HashSet<>();
        switch (relWord){
            case RDFS_SUBCLASSOF:{
                rel.setType("RDFS_SUBCLASSOF");
                props.add(new PropValPair(propUri,new CypherValue(RDFS.subClassOf.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(RDFS.subClassOf.getURI()))));
            }break;
            case OWL_EQCLASS:{
                rel.setType("EQUIVALENT_CLASS");
                props.add(new PropValPair(propUri,new CypherValue(OWL.equivalentClass.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.equivalentClass.getURI()))));
            }break;
            case OWL_DJCLASS:{
                rel.setType("DISJOINT_CLASS");
                props.add(new PropValPair(propUri,new CypherValue(OWL.disjointWith.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.disjointWith.getURI()))));
            }break;
            case RDFS_SUBPROPERTYOF:{
                rel.setType("RDFS_SUBPROPERTYOF");
                props.add(new PropValPair(propUri,new CypherValue(RDFS.subPropertyOf.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(RDFS.subPropertyOf.getURI()))));
            }break;
            case OWL_EQPROPERTY:{
                rel.setType("EQUIVALENT_PROPERTY");
                props.add(new PropValPair(propUri,new CypherValue(OWL.equivalentProperty.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.equivalentProperty.getURI()))));
            }break;
            case OWL_DJPROPERTY:{
                rel.setType("DISJOINT_PROPERTY");
                props.add(new PropValPair(propUri,new CypherValue(OWL2.propertyDisjointWith.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL2.propertyDisjointWith.getURI()))));
            }break;
            case OWL_IVPROPERTY:{
                rel.setType("INVERSE_PROPERTY");
                props.add(new PropValPair(propUri,new CypherValue(OWL.inverseOf.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.inverseOf.getURI()))));
            }break;
            case OWL_SAME_AS:{
                rel.setType("SAME_INDIVIDUAL");
                props.add(new PropValPair(propUri,new CypherValue(OWL.sameAs.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.sameAs.getURI()))));
            }break;
            case OWL_DFINS:{
                rel.setType("DIFFERENT_INDIVIDUAL");
                props.add(new PropValPair(propUri,new CypherValue(OWL.differentFrom.getURI())));
                props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(OWL.differentFrom.getURI()))));
            }break;
        }
        rel.setProperties(props);
        rel.setName("r");  //TODO:内部实现,先加上元素名称吧
        return rel;
    }

    /**
     * 拼接初始化图数据库的Cypher语句
     * 语句作用是将定义类、属性、实例这三种基本元素的OWL词汇写入
     * @return
     */
    public String initBase() throws Exception{
        Cypher cypher = new Cypher();
        List<CypherPath> words = new LinkedList<>();
        words.add(new CypherPath(clsWord));
        words.add(new CypherPath(dpWord));
        words.add(new CypherPath(opWord));
        words.add(new CypherPath(insWord));
        cypher.merge(words);  //使用merge,不重复写词汇
        return cypher.getCypher();
    }

    /**
     * 拼接将输入的本体类导入Neo4j数据库的Cypher语句
     * @param ontClass &nbsp 输入的类
     * @return 将该类按照知识表示规范导入数据库的合法Cypher语句
     * @throws Exception
     */
    public String intoCls(OntClass ontClass) throws Exception{
        Cypher cypher = new Cypher().match(clsWord);  //拼接查找定义类结点的词汇结点的Cypher
        CypherNode cls = new CypherNode("cls");
        List<CypherPath> pathes = new LinkedList<>();
        pathes.add(new CypherPath(cls).connectThrough(isA).with(new CypherNode("clsWord")));  //声明结点是OWL类
        CypherNode node = new CypherNode(null,new HashSet<>());
        Iterator<RDFNode> labelNodes = ontClass.listLabels(null);  //列出该类结点的rdfs:label
        while(labelNodes.hasNext()){
            node.addCondition(new PropValPair(new CypherProperty("value"),new CypherValue(labelNodes.next().toString())));
            pathes.add(new CypherPath(cls).connectThrough(rdfsLabel).with(node));  //给结点加一个rdfs:label
            node.getProperties().clear();
        }
        Iterator<RDFNode> commentNodes = ontClass.listComments(null);  //列出该类结点的rdfs:comment
        while(commentNodes.hasNext()){
            node.addCondition(new PropValPair(new CypherProperty("value"),new CypherValue(commentNodes.next().toString())));
            pathes.add(new CypherPath(cls).connectThrough(rdfsComment).with(node)); //给结点加一个rdfs:comment
            node.getProperties().clear();
        }
        Set<CypherCondition> clsProps = new HashSet<>();
        clsProps.add(new LabelCondition(cls,"OWL_CLASS"));
        clsProps.add(new PropValPair(new CypherProperty("uri",cls),Operator.EQ_TO,new CypherValue(ontClass.getURI())));
        clsProps.add(new PropValPair(new CypherProperty("preLabel",cls),Operator.EQ_TO,new CypherValue(getPreLabel(ontClass.getURI()))));
        cypher.create(pathes).set(clsProps).returnIdOf(cls);  //返回导入的类结点的id
        return cypher.getCypher();
    }

    /**
     * 拼接将输入的本体属性导入Neo4j数据库的Cypher语句
     * @param ontProperty &nbsp 输入的属性
     * @return 将该属性按照知识表示规范导入数据库的合法Cypher语句
     * @throws Exception
     */
    public String intoProp(OntProperty ontProperty) throws Exception{
        boolean isObj = false;    //是否为对象属性
        if(ontProperty.hasProperty(RDF.type,OWL.ObjectProperty)){
            isObj = true;
        }
        Cypher cypher = new Cypher();
        CypherNode prop = new CypherNode("prop");  //代表属性的结点
        Set<CypherCondition> propProps = new HashSet<>();
        List<CypherPath> pathes = new LinkedList<>();
        if(!isObj){         //数据类型属性
            propProps.add(new LabelCondition(prop,"OWL_DATATYPEPROPERTY"));
            cypher.match(dpWord);
            pathes.add(new CypherPath(prop).connectThrough(isA).with(new CypherNode("dpWord")));
        }else{             //对象属性
            propProps.add(new LabelCondition(prop,"OWL_OBJECTPROPERTY"));
            cypher.match(opWord);
            pathes.add(new CypherPath(prop).connectThrough(isA).with(new CypherNode("opWord")));
        }
        CypherNode node = new CypherNode(null,new HashSet<>());  //用到的临时结点
        Iterator<RDFNode> labelNodes = ontProperty.listLabels(null);  //列出该属性的所有rdfs:label
        while(labelNodes.hasNext()){
            node.addCondition(new PropValPair(new CypherProperty("value"),new CypherValue(labelNodes.next().toString())));
            pathes.add(new CypherPath(prop).connectThrough(rdfsLabel).with(node));   //给结点加一个rdfs:label
            node.getProperties().clear();
        }
        Iterator<RDFNode> commentNodes = ontProperty.listComments(null);  //列出该属性的所有rdfs:comment
        while(commentNodes.hasNext()){
            node.addCondition(new PropValPair(new CypherProperty("value"),new CypherValue(commentNodes.next().toString())));
            pathes.add(new CypherPath(prop).connectThrough(rdfsComment).with(node)); //给结点加一个rdfs:comment
            node.getProperties().clear();
        }
        int dC = 0;  //记录该属性domain的数目
        ExtendedIterator<OntClass> domains = (ExtendedIterator<OntClass>) ontProperty.listDomain();  //列出该属性的Domain
        node.setLabel("OWL_CLASS");
        while(domains.hasNext()){       //逐个domain迭代处理
            node.setName("d" + dC++);
            node.addCondition(new PropValPair(propPreLabel,new CypherValue(getPreLabel(domains.next().getURI()))));
            cypher.match(node);            //拼接查询该domain的语句
            node.getProperties().clear();  //把临时node的preLabel条件去掉
        }
        node.setLabel(null);
        while(dC > 0){
            node.setName("d" + --dC);
            pathes.add(new CypherPath(prop).connectThrough(rdfsDomain).with(node));  //给结点加一个domain
        }
        if(isObj){  //对象属性,还需将它的Range添加进来
            int rC = 0;  //记录该属性range的数目
            ExtendedIterator<OntClass> ranges = (ExtendedIterator<OntClass>) ontProperty.listRange(); //列出该属性的Range
            node.setLabel("OWL_CLASS");
            while(ranges.hasNext()){   //查询各个range
                node.setName("r" + rC++);
                node.addCondition(new PropValPair(propPreLabel,new CypherValue(getPreLabel(ranges.next().getURI()))));
                cypher.match(node);   //拼接查询该range的语句
                node.getProperties().clear(); //把临时node的preLabel条件去掉
            }
            node.setLabel(null);
            while(rC > 0){
                node.setName("r" + --rC);
                pathes.add(new CypherPath(prop).connectThrough(rdfsRange).with(node));   //给结点加一个range
            }
        }
        //TODO:数据类型属性的Range还未处理
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
     */
    public String intoIns(Individual individual) throws Exception{
        Cypher cypher = new Cypher();  //cypher拼接对象
        CypherNode ins = new CypherNode("ins");
        Set<CypherCondition> insProps = new HashSet<>(); //该实例所拥有的属性
        List<CypherPath> pathes = new LinkedList<>();
        insProps.add(new LabelCondition(ins,"OWL_NAMEDINDIVIDUAL"));
        cypher.match(insWord);  //拼接查询实例定义词汇
        pathes.add(new CypherPath(ins).connectThrough(isA).with(new CypherNode("insWord"))); //加一条词汇定义path
        CypherNode node = new CypherNode(null,new HashSet<>());   //临时使用node
        //处理实例的rdfs:labels
        Iterator<RDFNode> labelNodes = individual.listLabels(null);
        while(labelNodes.hasNext()){
            node.addCondition(new PropValPair(new CypherProperty("value"),new CypherValue(labelNodes.next().toString())));
            pathes.add(new CypherPath(ins).connectThrough(rdfsLabel).with(node));  //给结点加一个rdfs:label
            node.getProperties().clear();
        }
        //处理实例的rdfs:comments
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
        List<String> dpLabels = new ArrayList<>();   //临时记录该dp的rdfs:labels
        List<String> dpVals = new ArrayList<>();     //临时记录该dp的值集合
        StmtIterator stmtIter = individual.listProperties();  //列出该实例的所有属性
        while(stmtIter.hasNext()){
            Statement statement = stmtIter.nextStatement();
            Property prop = statement.getPredicate();
            if(prop.hasProperty(RDF.type,OWL.DatatypeProperty)){  //数据类型属性
                DatatypeProperty oDp = individual.getOntModel().getDatatypeProperty(prop.getURI());  //根据uri获取对应的DP
                //提取并设置第三元
                StmtIterator dpValIter = individual.listProperties(oDp);  //取该DP的值(TODO:原程序中做了区分,现在都用String)
                while(dpValIter.hasNext()){
                    dpVals.add(dpValIter.nextStatement().getLiteral().getString());
                }
                if(dpVals.size() == 0)  //此数据类型属性没有值,跳过
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
        //处理实例所属的类
        int cC = 0;
        ExtendedIterator<OntClass> clses = individual.listOntClasses(true);  //这里列出了实例直接所属的类
        node.setLabel("OWL_CLASS");
        while(clses.hasNext()){
            node.setName("c" + cC++);
            node.addCondition(new PropValPair(propPreLabel,new CypherValue(getPreLabel(clses.next().getURI()))));
            cypher.match(node);  //查询出该类
            node.getProperties().clear();
        }
        node.setLabel(null);
        while(cC > 0){
            node.setName("c" + --cC);
            pathes.add(new CypherPath(ins).connectThrough(isA).with(node));  //给结点加一个rdf:type关系指向它所属的一个类
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
     */
    public String intoRel(OntClass class1,OntClass class2,Words rel) throws Exception{
        Cypher cypher = new Cypher();
        Set<PropValPair> props = new HashSet<>();
        props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(class1.getURI()))));
        CypherNode cls1 = new CypherNode("cls1","OWL_CLASS",props);
        props.clear();
        props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(class2.getURI()))));
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
        CypherRelationship relation = null;
        switch (rel){
            case RDFS_SUBCLASSOF:{
                relation = subClassOf;
            }break;
            case OWL_EQCLASS:{
                relation = equalClass;
            }break;
            case OWL_DJCLASS:{
                relation = disjointClass;
            }break;
        }
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
     */
    public String intoRel(OntProperty ontProp1,OntProperty ontProp2,Words rel) throws Exception{
        boolean isObj = false;  //对象属性标志
        if(ontProp1.hasProperty(RDF.type,OWL.ObjectProperty)){
            isObj = true;
        }
        Cypher cypher = new Cypher();
        Set<PropValPair> props = new HashSet<>();
        props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(ontProp1.getURI()))));
        CypherNode prop1 = new CypherNode("prop1",isObj ? "OWL_OBJECTPROPERTY" : "OWL_DATATYPEPROPERTY",props);
        props.clear();
        props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(ontProp2.getURI()))));
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
        CypherRelationship relation = null;
        switch (rel){
            case RDFS_SUBPROPERTYOF:{
                relation = subPropertyOf;
            }break;
            case OWL_EQPROPERTY:{
                relation = equalProperty;
            }break;
            case OWL_DJPROPERTY:{
                relation = disjointProperty;
            }break;
            case OWL_IVPROPERTY:{
                relation = inverseProperty;
            }break;
        }
        CypherPath path = new CypherPath(prop1).connectThrough(relation).with(prop2);
        cypher.create(path).returnIdOf(relation);
        return cypher.getCypher();
    }

    /**
     * 拼接将两个实例之间的某种语义关系写入Neo4j数据库的Cypher语句
     * @param ins1 &nbsp 先序关系中的第1个实例
     * @param ins2 &nbsp 先序关系中的第2个实例
     * @param rel &nbsp 要写入的关系
     * @return
     * @throws Exception
     */
    public String intoRel(Individual ins1,Individual ins2,Words rel) throws Exception{
        Cypher cypher = new Cypher();
        Set<PropValPair> props = new HashSet<>();
        props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(ins1.getURI()))));
        CypherNode i1 = new CypherNode("ins1","OWL_NAMEDINDIVIDUAL",props);
        props.clear();
        props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(ins2.getURI()))));
        CypherNode i2 = new CypherNode("ins2","OWL_NAMEDINDIVIDUAL",props);
        List<CypherNode> nodes = new ArrayList<>();
        nodes.add(i1);
        nodes.add(i2);
        cypher.match(nodes);  //拼接查询两个实例的cypher语句
        i1.setLabel(null);
        i1.setProperties(null);
        i2.setLabel(null);
        i2.setProperties(null);
        props.clear();
        CypherRelationship relation = null;
        switch (rel){
            case OWL_SAME_AS:{
                relation = sameIns;
            }break;
            case OWL_DFINS:{
                relation = differentIns;
            }break;
        }
        CypherPath path = new CypherPath(i1).connectThrough(relation).with(i2);
        cypher.create(path).returnIdOf(relation);
        return cypher.getCypher();
    }

    /**
     * 拼接将两个实例之间的某种属性关系写入Neo4j数据库的Cypher语句
     * @param ins1 &nbsp 先序关系中的第1个实例
     * @param ins2 &nbsp 先序关系中的第2个实例
     * @param prop &nbsp 要写入的关系
     * @return
     * @throws Exception
     */
    public String intoRel(Individual ins1, Individual ins2, ObjectProperty prop) throws Exception{
        Cypher cypher = new Cypher();
        Set<PropValPair> props = new HashSet<>();
        props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(ins1.getURI()))));
        CypherNode i1 = new CypherNode("ins1","OWL_NAMEDINDIVIDUAL",props);
        props.clear();
        props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(ins2.getURI()))));
        CypherNode i2 = new CypherNode("ins2","OWL_NAMEDINDIVIDUAL",props);
        List<CypherNode> nodes = new ArrayList<>();
        nodes.add(i1);
        nodes.add(i2);
        cypher.match(nodes);  //拼接查询两个实例的cypher语句
        i1.setLabel(null);
        i1.setProperties(null);
        i2.setLabel(null);
        i2.setProperties(null);
        props.clear();
        CypherRelationship relation = new CypherRightRelationship();
        String relPre = getPreLabel(prop.getURI());
        relation.setName("r");
        relation.setType("`" + relPre + "`");
        props.add(new PropValPair(propUri,new CypherValue(prop.getURI())));
        props.add(new PropValPair(propPreLabel,new CypherValue(getPreLabel(prop.getURI()))));
        ExtendedIterator<RDFNode> labels = prop.listLabels(null);
        List<String> labelVals = new ArrayList<>();
        while(labels.hasNext()){
            labelVals.add(labels.next().asLiteral().getString());
        }
        if(labelVals.size() != 0){
            props.add(new PropValPair(new CypherProperty("`rdfs:label`"),new CypherValue(labelVals,DataType.STR)));
        }
        relation.setProperties(props);
        CypherPath path = new CypherPath(i1).connectThrough(relation).with(i2);
        cypher.create(path).returnIdOf(relation);
        return cypher.getCypher();
    }

}
