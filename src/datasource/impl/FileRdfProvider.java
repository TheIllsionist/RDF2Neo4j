package datasource.impl;

import datasource.RdfProvider;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import util.CLASS_REL;
import util.INSTANCE_REL;
import util.PROPERTY_REL;
import util.Pair;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;


public class FileRdfProvider implements RdfProvider {

    private final OntModel ontModel;    //导入/存储程序不应修改知识源

    /**
     * 构造函数,根据指定的文件夹路径读取该文件夹下的所有owl文件的内容,如果该文件夹下没有owl文件,则返回一个空模型
     * @param sourcePath &nbsp 指定的文件夹路径
     */
    public FileRdfProvider(String sourcePath){
        File folder = new File(sourcePath);
        File[] files = folder.listFiles(new FileFilter() {  //将当前文件夹下的所有.owl文件列出(不支持多重文件夹,因为没有必要)
            @Override
            public boolean accept(File pathname) {
                if(pathname.getName().endsWith(".owl")) { return true; }
                return false;
            }
        });
        ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); //创建Model,OWL Full,In Memory,No Reasoner
        if(files.length == 0){
            return;
        }
        for (File file : files) {  //将文件夹下的所有本体文件的内容读入
            InputStream in = FileManager.get().open(file.getAbsolutePath());
            this.ontModel.read(in,null);  //TODO:还未测试过这种读入方式的有效性
        }
    }

    /**
     * 返回该RDF提供者拥有的根类集
     * @return &nbsp 根类集合
     */
    public Set<OntClass> rootClasses(){
        Set<OntClass> roots = new HashSet<>();
        ExtendedIterator<OntClass> rootIter = ontModel.listHierarchyRootClasses();  //列出根类集
        while(rootIter.hasNext()){
            roots.add(rootIter.next());
        }
        return roots;
    }

    /**
     * 返回该RDF提供者拥有的所有类
     * @return &nbsp 类别集合
     */
    @Override
    public Set<OntClass> allOntClasses() {
        HashSet<OntClass> classes = new HashSet<>();
        ExtendedIterator<OntClass> classIter = ontModel.listClasses();
        while (classIter.hasNext()){
            classes.add(classIter.next());
        }
        return classes;
    }

    /**
     * 返回该RDF提供者拥有的所有属性(包括DP,OP,AP,FP,TP等等)
     * @return &nbsp 属性集合
     */
    @Override
    public Set<OntProperty> allOntProperties(){
        HashSet<OntProperty> ontProperties = new HashSet<>();
        ExtendedIterator<OntProperty> ontPropertyIter = ontModel.listAllOntProperties();
        while(ontPropertyIter.hasNext()){
            ontProperties.add(ontPropertyIter.next());
        }
        return ontProperties;
    }

    /**
     * 返回该RDF提供者拥有的所有数据类型属性
     * @return &nbsp 所有的数据类型属性的集合
     */
    @Override
    public Set<DatatypeProperty> allDatatypeProperties() {
        HashSet<DatatypeProperty> dps = new HashSet<>();
        ExtendedIterator<DatatypeProperty> dpIter = ontModel.listDatatypeProperties();
        while(dpIter.hasNext()){
            dps.add(dpIter.next());
        }
        return dps;
    }

    /**
     * 返回该RDF提供者拥有的所有对象属性
     * @return &nbsp 所有的对象属性的集合
     */
    @Override
    public Set<ObjectProperty> allObjectProperties() {
        HashSet<ObjectProperty> objectProperties = new HashSet<>();
        ExtendedIterator<ObjectProperty> opIter = ontModel.listObjectProperties();
        while(opIter.hasNext()){
            objectProperties.add(opIter.next());
        }
        return objectProperties;
    }

    /**
     * 返回该RDF提供者拥有的所有实例
     * @return &nbsp 所有的实例的集合
     */
    @Override
    public Set<Individual> allIndividuals() {
        HashSet<Individual> individuals = new HashSet<>();
        ExtendedIterator<Individual> insIter = ontModel.listIndividuals();
        while(insIter.hasNext()){
            individuals.add(insIter.next());
        }
        return individuals;
    }

    /**
     * 返回此RDF提供者中直接属于某指定类别的实例的集合
     * @param ontClass &nbsp 指定类别
     * @return &nbsp 属于指定类别ontClass的实例集合
     */
    @Override
    public Set<Individual> allIndividualsOfClass(OntClass ontClass) {
        Set<Individual> individuals = new HashSet<>();
        ExtendedIterator<? extends OntResource> insIter = ontClass.listInstances(true);  //这里列出了直接属于该类的实例
        while (insIter.hasNext()){
            individuals.add((Individual)insIter.next());
        }
        return individuals;
    }

    /**
     * 返回某个指定本体资源所拥有的所有Label(rdfs:label属性值)
     * @param ontResource 指定的资源
     * @return &nbsp 指定资源ontResource所拥有的所有的Label集合
     */
    @Override
    public Set<String> allLabelsOf(OntResource ontResource) {
        HashSet<String> labels = new HashSet<>();
        NodeIterator labelIter = ontResource.listPropertyValues(RDFS.label);
        while(labelIter.hasNext()){
            labels.add(labelIter.next().asLiteral().getString());
        }
        return labels;
    }

    /**
     * 返回某个指定本体资源所拥有的所有Comments(rdfs:comment属性值)
     * @param ontResource 指定的资源
     * @return &nbsp 指定资源ontResource所拥有的所有Label集合
     */
    @Override
    public Set<String> allCommentsOf(OntResource ontResource) {
        HashSet<String> comments = new HashSet<>();
        NodeIterator commentIter = ontResource.listPropertyValues(RDFS.comment);
        while(commentIter.hasNext()){
            comments.add(commentIter.next().asLiteral().getString());
        }
        return comments;
    }

    /**
     * 返回某个指定实例所拥有的所有数据类型属性及其对应的取值
     * @param individual &nbsp 某指定实例
     * @return &nbsp 指定实例individual所拥有的DP以及它这些DP的取值的字典
     */
    @Override
    public Map<DatatypeProperty, List<Literal>> allDpValuesOf(Individual individual) {
        Map<DatatypeProperty,List<Literal>> dpValsOfIns = new HashMap<>();
        StmtIterator iterator = individual.listProperties();  //列出该实例声明所有属性的三元组集
        while(iterator.hasNext()) {  //迭代该三元组集
            Statement propStmt = iterator.nextStatement();
            Property property = propStmt.getPredicate();  //取当前此条三元组的谓词
            if (property.hasProperty(RDF.type, OWL.DatatypeProperty)) {  //当前谓词是数据类型属性
                DatatypeProperty dp = ontModel.getDatatypeProperty(property.getURI());
                if (!dpValsOfIns.containsKey(dp)) {
                    dpValsOfIns.put(dp, new LinkedList<>());
                }
                //数据类型属性的取值可以是链表,且在同一个DP上可能有多个取值
                StmtIterator valIter = individual.listProperties(dp);
                while (valIter.hasNext()) {
                    dpValsOfIns.get(dp).add(valIter.nextStatement().getLiteral());
                }
            }
        }
        return dpValsOfIns;
    }

    /**
     * 返回某个指定实例所拥有的所有对象属性,该对象属性的实例取值
     * 如果该指定实例非blankNode实例,则它的对象属性可能有多个,每个对象属性可能有多个取值（比如航母配置了舰载机,火箭等等）
     * 如果该指定实例是blankNode实例,则它只有一个对象属性(meta:实例),且该对象属性只有一个值
     * @param individual &nbsp 某指定实例
     * @return 该指定实例individual的所有对象属性及它这些对象属性对应的取值
     */
    @Override
    public Map<ObjectProperty, List<Individual>> allOpValuesOf(Individual individual) {
        Map<ObjectProperty,List<Individual>> opValsOfIns = new HashMap<>();
        StmtIterator iterator = individual.listProperties();
        while(iterator.hasNext()){
            Statement propStmt = iterator.nextStatement();
            Property property = propStmt.getPredicate();
            if(property.hasProperty(RDF.type,OWL.ObjectProperty)){
                ObjectProperty op = ontModel.getObjectProperty(property.getURI());
                if(!opValsOfIns.containsKey(op)){
                    opValsOfIns.put(op,new LinkedList<>());
                }
                StmtIterator insIter = individual.listProperties(op);
                //有可能出现同一个对象属性的取值有多个,比如说一个航母配置了舰载机,火箭等等
                while(insIter.hasNext()){
                    opValsOfIns.get(op).add(ontModel.getIndividual(insIter.next().getObject().asResource().getURI()));
                }
            }
        }
        return opValsOfIns;
    }

    /**
     * 根据某个指定的类别间关系从当前RDFModel中取出具有该关系的主宾对
     * @param rel &nbsp 指定的类间关系类型
     * @return
     */
    @Override
    public Queue<Pair<OntClass, OntClass>> relsBetweenClasses(CLASS_REL rel) {
        Queue<Pair<OntClass,OntClass>> subObjPair = new LinkedList<>();
        Selector selector = null;
        switch (rel){
            case SUBCLASS_OF:selector = new SimpleSelector(null,RDFS.subClassOf,(OntClass)null);break; //是父子关系时,子类在前,父类在后
            case DISJOINT_CLASS:selector = new SimpleSelector(null,OWL2.disjointWith,(OntClass)null);break;
            case EQUIVALENT_CLASS:selector = new SimpleSelector(null,OWL.equivalentClass,(OntClass)null);break;
        }
        StmtIterator iterator = ontModel.listStatements(selector);
        while (iterator.hasNext()){
            Statement statement = iterator.nextStatement();
            OntClass fClass = ontModel.getOntClass(statement.getSubject().getURI());  //是父子关系时,fClass是子类
            OntClass sClass = ontModel.getOntClass(statement.getObject().asResource().getURI());  //是父子关系时,sClass是父类
            if(fClass.hasProperty(RDF.type,OWL.Class) && sClass.hasProperty(RDF.type,OWL.Class)){
                subObjPair.add(new Pair<>(fClass,sClass));
            }
        }
        return subObjPair;
    }

    /**
     * 根据某个指定的属性关系从当前RDFModel中取出具有该关系的主宾对
     * @param rel &nbsp 指定的属性间关系类型
     * @return
     */
    @Override
    public Queue<Pair<OntProperty, OntProperty>> relsBetweenProperties(PROPERTY_REL rel) {
        Queue<Pair<OntProperty,OntProperty>> subObjPair = new LinkedList<>();
        Selector selector = null;
        switch (rel){
            case SUBPROPERTY_OF:selector = new SimpleSelector(null,RDFS.subPropertyOf,(OntProperty)null);break;
            case DISJOINT_PROPERTY:selector = new SimpleSelector(null, OWL2.propertyDisjointWith,(OntProperty)null);break;
            case EQUIVALENT_PROPERTY:selector = new SimpleSelector(null,OWL.equivalentProperty,(OntProperty)null);break;
            case INVERSE_OF:selector = new SimpleSelector(null,OWL2.inverseOf,(OntProperty)null);break;
        }
        StmtIterator iterator = ontModel.listStatements(selector);
        while (iterator.hasNext()){
            Statement statement = iterator.nextStatement();
            OntProperty fProperty = ontModel.getOntProperty(statement.getSubject().getURI());
            OntProperty sProperty = ontModel.getOntProperty(statement.getObject().asResource().getURI());
            if(fProperty == null || sProperty == null){
                continue;
            }
            subObjPair.add(new Pair<>(fProperty,sProperty));
        }
        return subObjPair;
    }

    /**
     * 根据某个指定的实例关系从当前RDFModel中取出具有该关系的主宾对
     * @param rel &nbsp 指定的实例间关系类型
     * @return
     */
    @Override
    public Queue<Pair<Individual, Individual>> relsBetweenIndividuals(INSTANCE_REL rel) {
        Queue<Pair<Individual,Individual>> subObjPair = new LinkedList<>();
        Selector selector = null;
        switch (rel){
            case SAME_AS:selector = new SimpleSelector(null,OWL.sameAs,(Individual) null);break;
            case DIFFERENT_FROM:selector = new SimpleSelector(null,OWL.differentFrom,(Individual)null);break;
        }
        StmtIterator iterator = ontModel.listStatements(selector);
        while(iterator.hasNext()){
            Statement statement = iterator.nextStatement();
            Individual fIns = ontModel.getIndividual(statement.getSubject().getURI());
            Individual sIns = ontModel.getIndividual(statement.getObject().asResource().getURI());
            if(fIns.isIndividual() && sIns.isIndividual()){
                subObjPair.add(new Pair<>(fIns,sIns));
            }
        }
        return subObjPair;
    }

    /**
     * 返回此RDFModel中所有具有父子关系的父子类对(子类在前,父类在后)
     * @return &nbsp 包含所有父子类对的队列
     */
    @Override
    public Queue<Pair<OntClass, OntClass>> allSubClassOfRels() {
        return relsBetweenClasses(CLASS_REL.SUBCLASS_OF);
    }

    /**
     * 返回此RDFModel中所有具有等价关系的类对
     * @return &nbsp 包含所有类对的队列
     */
    @Override
    public Queue<Pair<OntClass,OntClass>> allEqualClassRels(){
        return relsBetweenClasses(CLASS_REL.EQUIVALENT_CLASS);
    }

    /**
     * 返回此RDFModel中所有具有不相交关系的类对
     * @return &nbsp 包含所有类对的队列
     */
    @Override
    public Queue<Pair<OntClass,OntClass>> allDisJointClassRels(){
        return relsBetweenClasses(CLASS_REL.DISJOINT_CLASS);
    }

    /**
     * 返回类之间的所有关系
     * TODO:当前实现方式比较慢,后面换成直接从OntModel中迭代出所有的类间关系
     * @return
     */
    public Queue<Pair<OntClass,OntClass>> allClassRels(){
        Queue<Pair<OntClass,OntClass>> rels = new LinkedList<>(allSubClassOfRels());
        rels.addAll(allEqualClassRels());
        rels.addAll(allDisJointClassRels());
        return rels;
    }

    /**
     * 返回RDFModel中所有具有父子关系的父子属性对(子属性在前,父属性在后)
     * @return &nbsp 包含所有父子属性对的队列
     */
    @Override
    public Queue<Pair<OntProperty, OntProperty>> allSubPropertyOfRels() {
        return relsBetweenProperties(PROPERTY_REL.SUBPROPERTY_OF);
    }

    /**
     * 返回RDFModel中所有具有等价关系的属性对
     * @return
     */
    @Override
    public Queue<Pair<OntProperty,OntProperty>> allEqualPropertyRels(){
        return relsBetweenProperties(PROPERTY_REL.EQUIVALENT_PROPERTY);
    }

    /**
     * 返回RDFModel中所有具有不相交关系的属性对
     * @return
     */
    @Override
    public Queue<Pair<OntProperty,OntProperty>> allDisjointPropRels(){
        return relsBetweenProperties(PROPERTY_REL.DISJOINT_PROPERTY);
    }

    /**
     * 返回RDFModel中所有具有语义相反关系的属性对
     * @return
     */
    @Override
    public Queue<Pair<OntProperty,OntProperty>> allInversePropRels(){
        return relsBetweenProperties(PROPERTY_REL.INVERSE_OF);
    }

    /**
     * 返回所有的rdfs:domain关系的主宾对
     * 该方法能起到实际作用的前提是推理机设置了某个属性的定义域是几个类别的并集
     * @return
     */
    @Override
    public Queue<Pair<OntProperty, OntClass>> allRdfsDomainRels() {
        Queue<Pair<OntProperty,OntClass>> subObjPair = new LinkedList<>();
        Selector selector = new SimpleSelector(null,RDFS.domain,(OntClass)null);
        StmtIterator iterator = ontModel.listStatements(selector);
        while (iterator.hasNext()){
            Statement statement = iterator.nextStatement();
            OntProperty sub = ontModel.getOntProperty(statement.getSubject().getURI());
            OntClass obj = ontModel.getOntClass(statement.getObject().asResource().getURI());
            if((sub.hasProperty(RDF.type,OWL.DatatypeProperty) || sub.hasProperty(RDF.type,OWL.ObjectProperty))
                    && obj.hasProperty(RDF.type,OWL.Class)){
                subObjPair.add(new Pair<>(sub,obj));
            }
        }
        return subObjPair;
    }

    /**
     * 返回所有的rdfs:range关系的主宾对
     * 该方法能起到实际作用的前提是推理机设置了某个属性的值域是几个类别的并集
     * @return
     */
    @Override
    public Queue<Pair<OntProperty, OntClass>> allRdfsRangeRels() {
        Queue<Pair<OntProperty,OntClass>> subObjPair = new LinkedList<>();
        Selector selector = new SimpleSelector(null,RDFS.range,(OntResource)null);
        StmtIterator iterator = ontModel.listStatements(selector);
        while (iterator.hasNext()){
            Statement statement = iterator.nextStatement();
            OntProperty sub = ontModel.getOntProperty(statement.getSubject().getURI());
            OntClass obj = ontModel.getOntClass(statement.getObject().asResource().getURI());
            if((sub.hasProperty(RDF.type,OWL.DatatypeProperty) || sub.hasProperty(RDF.type,OWL.ObjectProperty))
                    && obj.hasProperty(RDF.type,OWL.Class)){
                subObjPair.add(new Pair<>(sub,obj));
            }
        }
        return subObjPair;
    }

}
